package jp.ac.tokushima_u.is.ll.ui.navTask;

/**
 * 
 * @author Kousuke Mouri University of Tokushima,Japan
 * 
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.provider.TaskDatabase;
import jp.ac.tokushima_u.is.ll.service.TaskSyncService;
import jp.ac.tokushima_u.is.ll.service.TaskscriptSyncService;
import jp.ac.tokushima_u.is.ll.ui.nav.nav;
import jp.ac.tokushima_u.is.ll.ui.MyLocationListener;

import jp.ac.tokushima_u.is.ll.ui.navTaskselect.RelatedItemHttp;
import jp.ac.tokushima_u.is.ll.ui.navTaskselect.TaskscriptselectHttp;
import jp.co.yahoo.android.maps.GeoPoint;

/**
 * Test activity to display the list view
 */
public class TestActivity extends Activity {

	/** Id for the toggle rotation menu item */
	private static final int TOGGLE_ROTATION_MENU_ITEM = 0;

	/** Id for the toggle lighting menu item */
	private static final int TOGGLE_LIGHTING_MENU_ITEM = 1;

	public static SQLiteDatabase db;
	/** The list view */
	private MyListView mListView;

	// datacount
	int count;
	String[] level_code = new String[20];
	String[] title_code = new String[20];
	String[] change_level_code = new String[20];
	String[] change_title_code = new String[20];
	String[] change_taskid_code = new String[20];
	String[] change_place = new String[20];
	String[] lat_code = new String[20];
	String[] lng_code = new String[20];
	String[] taskid_code = new String[20];
	String[] place=new String[20];
	Cursor cursor;
	double[] Tasklatw = new double[20];
	double[] Tasklngw = new double[20];
	String[] test1 = new String[10];
	String japaneselevel;
	private LocationManager locationmanager;
	Location location;
	private double lat, lng;
    MyAdapter adapter;
	private MyLocationListener locationlistener = new MyLocationListener();

	private static class Contact {

		String mName;

		String mNumber;

		public Contact(final String name, final String number) {
			mName = name;
			mNumber = number;
		}

	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_list);
		Bundle extras = getIntent().getExtras();
		// japaneselevel = extras.getString("japaneselevel");

		// Taskdatabase select
		Task_database_setting();
		// GPS setting
		GPS_SETTING();
		// Distance setting
		TASK_distance_setting();

	}

	@Override
	public void onPause() {
		super.onPause();
//		mListView = null;
////		mListView.setAdapter(null);
//		adapter=null;
////		locationlistener=null;
//		finish();
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mListView = null;
////	mListView.setAdapter(null);
	adapter=null;
	locationlistener=null;
		finish();
	}

	private void TASK_distance_setting() {
		// TODO Auto-generated method stub

		final ArrayList<Contact> contacts = createContactList(20);
		adapter = new MyAdapter(this, contacts);

		mListView = (MyListView) findViewById(R.id.my_list);
		mListView.setAdapter(adapter);

		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(final AdapterView<?> parent,
					final View view, final int position, final long id) {
				final String message = "Taskを開始します:回線は3Gですので少し時間がかかりますので待ってください ";
				Toast.makeText(TestActivity.this, message, Toast.LENGTH_SHORT)
						.show();
			
				
				
				Intent Item = new Intent(TestActivity.this,
						RelatedItemHttp.class);
				Item.putExtra("taskid", change_taskid_code[position]);
				Item.putExtra("title", change_title_code[position]);
				Item.putExtra("level", change_level_code[position]);
				Item.putExtra("place", change_place[position]);
				// Item.putExtra("taskid", taskid_code[position]);
				// Item.putExtra("title", title_code[position]);
				// Item.putExtra("level", level_code[position]);
				startActivity(Item);

//				finish();

			}
		});

		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(final AdapterView<?> parent,
					final View view, final int position, final long id) {
				final String message = "OnLongClick: "
						+ contacts.get(position).mName;
				Toast.makeText(TestActivity.this, message, Toast.LENGTH_SHORT)
						.show();
				return true;
			}
		});
	}

	private void GPS_SETTING() {
		// TODO Auto-generated method stub
		location = null;
		locationmanager = (LocationManager) this
				.getSystemService(LOCATION_SERVICE);
		List<String> providers = locationmanager.getProviders(false);
		long latest = 0;
		if (!locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getText(R.string.info_require_gps))
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int id) {
									launchGPSOptions();
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int id) {
									dialog.cancel();
								}
							});
			final AlertDialog alert = builder.create();
			alert.show();
		}

		if (locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			locationmanager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1000, 0, locationlistener);
			location = locationmanager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null) {
				lat = location.getLatitude();
				lng = location.getLongitude();
			}
		} else {
			for (String provider : providers) {
				location = locationmanager.getLastKnownLocation(provider);

				if (location != null) {
					float accuracy = location.getAccuracy();
					if (accuracy <= 70) {
						if ((Calendar.getInstance().getTimeInMillis() - location
								.getTime()) < 10 * 60 * 1000) {
							lat = location.getLatitude();
							lng = location.getLongitude();
							break;
						} else
							continue;
					}
				}
			}
		}
	}

	private void launchGPSOptions() {
		Intent intent = new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(intent);
	}

	private void Task_database_setting() {
		// TODO Auto-generated method stub
		try {
			TaskDatabase helper = new TaskDatabase(getApplicationContext());
			db = helper.getReadableDatabase();
			cursor = db.rawQuery("select * from TASK_TABLE;", null);
			count = cursor.getCount();
			int i = 0;
			while (cursor.moveToNext()) {
				taskid_code[i] = cursor.getString(1);
				title_code[i] = cursor.getString(6);
				level_code[i] = cursor.getString(11);
				lat_code[i] = cursor.getString(3);
				lng_code[i] = cursor.getString(4);
				place[i]=cursor.getString(5);
				i++;
			}
		} finally {
			cursor.close();
			db.close();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		menu.add(Menu.NONE, TOGGLE_ROTATION_MENU_ITEM, 0, "Toggle Rotation");
		menu.add(Menu.NONE, TOGGLE_LIGHTING_MENU_ITEM, 1, "Toggle Lighting");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case TOGGLE_ROTATION_MENU_ITEM:
			mListView.enableRotation(!mListView.isRotationEnabled());
			return true;

		case TOGGLE_LIGHTING_MENU_ITEM:
			mListView.enableLight(!mListView.isLightEnabled());
			return true;

		default:
			return false;
		}
	}

	private ArrayList<Contact> createContactList(final int size) {
		final ArrayList<Contact> contacts = new ArrayList<Contact>();
//		 int j=0;
//		 for(int l=0;l<title_code.length;l++){
//		
//		 contacts.add(new Contact("Task_name", "Level: " + title_code[l]));
//		 change_level_code[j] = level_code[l];
//		 change_title_code[j] = title_code[l];
//		 change_taskid_code[j] = taskid_code[l];
//		 j++;
//		 }
		 // int j=0;
//		 for(int l=0;l<title_code.length;l++){
//		 if(japaneselevel.equals(level_code[l])){
//		 contacts.add(new Contact("Task_name", "Level: " + level_code[l]+ " "
//		 + title_code[l]));
//		 change_level_code[j] = level_code[l];
//		 change_title_code[j] = title_code[l];
//		 change_taskid_code[j] = taskid_code[l];
//		 }
//		 }
		if(lat==0.0 || lng==0){
			 int j=0;
			 for(int l=0;l<title_code.length;l++){
		
			 contacts.add(new Contact("Task_name", "Level: " + title_code[l]));
			 change_level_code[j] = level_code[l];
			 change_title_code[j] = title_code[l];
			 change_taskid_code[j] = taskid_code[l];
			 j++;
			
			 }
			  
//			 for(int l=0;l<title_code.length;l++){
//			 if(japaneselevel.equals(level_code[l])){
//			 contacts.add(new Contact("Task_name", "Level: " + level_code[l]+ " "
//			 + title_code[l]));
//			 change_level_code[j] = level_code[l];
//			 change_title_code[j] = title_code[l];
//			 change_taskid_code[j] = taskid_code[l];
//			 }
//			 }
			
			
			
		}
		
		double startLat, startLng, endLat, endLng;
		startLat = lat;
		startLng = lng;
		int j = 0;
		for (int i = 0; i < lat_code.length; i++) {
			float[] results = { 0, 0, 0 };
			if (lat_code[i] != null) {

				endLat = Double.parseDouble(lat_code[i]);
				endLng = Double.parseDouble(lng_code[i]);

				if (endLat != 0 && endLng != 0) {

					location.distanceBetween(startLat, startLng, endLat,
							endLng, results);

				}
			}
			if (results[0] <= 5000 && results[0] != 0) {
				contacts.add(new Contact("Task_name", "Level: " + level_code[i]
						+ " " + title_code[i]+"　　距離："+results[0]));

				change_level_code[j] = level_code[i];
				change_title_code[j] = title_code[i];
				change_taskid_code[j] = taskid_code[i];
				change_place[j] = place[i];
				j++;
			}
		}
		//
		// if (contacts.size() >= 10) {
		// int js = 0;
		// contacts.clear();
		// for (int i = 0; i < lat_code.length; i++) {
		// float[] results = { 0, 0, 0 };
		// if (lat_code[i] != null) {
		//
		// endLat = Double.parseDouble(lat_code[i]);
		// endLng = Double.parseDouble(lng_code[i]);
		//
		// if (endLat != 0 && endLng != 0) {
		//
		// location.distanceBetween(startLat, startLng, endLat,
		// endLng, results);
		//
		// }
		// }
		// if (results[0] <= 1000 && results[0] != 0) {
		// contacts.add(new Contact("Task_name", "Level: "
		// + level_code[i] + " " + title_code[i]));
		//
		// change_level_code[js] = level_code[i];
		// change_title_code[js] = title_code[i];
		// change_taskid_code[js] = taskid_code[i];
		// js++;
		// }
		// }
		//
		// }

		return contacts;
	}

	/**
	 * Adapter class to use for the list
	 */
	private static class MyAdapter extends ArrayAdapter<Contact> {

		/** Re-usable contact image drawable */
		private final Drawable contactImage;

		/**
		 * Constructor
		 * 
		 * @param context
		 *            The context
		 * @param contacts
		 *            The list of contacts
		 */
		public MyAdapter(final Context context,
				final ArrayList<Contact> contacts) {
			super(context, 0, contacts);
			contactImage = context.getResources().getDrawable(
					R.drawable.taskicon);
		}

		@Override
		public View getView(final int position, final View convertView,
				final ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				view = LayoutInflater.from(getContext()).inflate(
						R.layout.list_item, null);
			}

			final TextView name = (TextView) view
					.findViewById(R.id.contact_name);
			if (position == 14) {
				name.setText("This is a long text that will make this box big. "
						+ "Really big. Bigger than all the other boxes. Biggest of them all.");
			} else {
				name.setText(getItem(position).mName);
			}

			final TextView number = (TextView) view
					.findViewById(R.id.contact_number);
			number.setText(getItem(position).mNumber);

			final ImageView photo = (ImageView) view
					.findViewById(R.id.contact_photo);
			photo.setImageDrawable(contactImage);

			return view;
		}
	}
}
