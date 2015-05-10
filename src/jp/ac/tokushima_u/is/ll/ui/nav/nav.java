package jp.ac.tokushima_u.is.ll.ui.nav;

/**
 * 
 * @author Kousuke Mouri University of Tokushima,Japan
 * 
 */
import java.util.Calendar;
import java.util.List;

import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Items;
import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.service.TaskSyncService;
import jp.ac.tokushima_u.is.ll.service.TaskscriptSyncService;
import jp.ac.tokushima_u.is.ll.ui.HomeActivity;
//import jp.ac.tokushima_u.is.ll.service.ContextAwareService.GPSThread;
import jp.ac.tokushima_u.is.ll.ui.LogListActivity;
import jp.ac.tokushima_u.is.ll.ui.MyLocationListener;
import jp.ac.tokushima_u.is.ll.ui.nav.examination.ExaminationScreen;
import jp.ac.tokushima_u.is.ll.ui.nav.qr.qr_codeMain;
import jp.ac.tokushima_u.is.ll.ui.navTask.CameraTask;
import jp.ac.tokushima_u.is.ll.ui.navTask.Task_main;
import jp.ac.tokushima_u.is.ll.ui.navTask.TestActivity;
import jp.ac.tokushima_u.is.ll.ui.navTask.navTask_view;
import jp.ac.tokushima_u.is.ll.ui.navTaskselect.TaskClearScreen;
//import jp.ac.tokushima_u.is.ll.ui.MapTestActivity.ItemsQuery;
import jp.ac.tokushima_u.is.ll.util.Constants;
import jp.ac.tokushima_u.is.ll.util.ContextUtil;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler;
import jp.ac.tokushima_u.is.ll.util.UIUtils;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler.AsyncQueryListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.database.Cursor;
import android.app.NotificationManager;

public class nav extends Activity implements AsyncQueryListener {

	String userEmail = null;
	String userPassword = null;
	private double lat, lng;
	private NotifyingAsyncQueryHandler mHandler;
	private Handler mMessageQueueHandler = new Handler();
	private LocationManager locationmanager;
	private static final int gps_circle_time = 5 * 60 * 1000;
	private static final int gps_circle_distance = 10;
	private MyLocationListener locationlistener = new MyLocationListener();
	private Handler primaryThread;
	private Runnable Task;
	Location location;
	double[] latw = new double[10];
	double[] lngw = new double[10];
	String[] namew = new String[10];
	String[] titlew = new String[10];

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.navmain);
		Intent taskintent = new Intent(nav.this, TaskSyncService.class);
		startService(taskintent);

		location = null;
		// locationmanagerを取得するためにはContext.getSystemService(LoCATION_SERVICE)を呼び出す
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
				// 経度取得
			}
		} else {
			for (String provider : providers) {
				location = locationmanager.getLastKnownLocation(provider);

				if (location != null) {
					// getAccuracy()メッソドで精度を取得する
					float accuracy = location.getAccuracy();
					if (accuracy <= 70) {
						if ((Calendar.getInstance().getTimeInMillis() - location
								.getTime()) < 10 * 60 * 1000) {
							// 緯度取得
							lat = location.getLatitude();
							// 経度取得
							lng = location.getLongitude();
							break;
						} else
							continue;
					}
				}
			}
		}

		// new GPSThread().start();
		// ///////////////////////////////////////////////////////////////////////////////
		Uri itemsUri;
		String[] projection;
		projection = ItemsQuery.PROJECTION;
		itemsUri = Items.buildItemSearchUri("geo:" + lat + "," + lng);
		mHandler = new NotifyingAsyncQueryHandler(getContentResolver(), this);
		// //////////////////////////////////////////////////////////////////////////////

		// mHandler.startQuery(sessionsUri, projection, Items.DEFAULT_SORT);
		// mHandler.startQuery(itemsUri, ItemsQuery.PROJECTION,
		// Items.DISABLED+"!=?", new String[]{"1"}, Items.DEFAULT_SORT);
		// mHandler.startQuery(itemsUri, ItemsQuery.PROJECTION,
		// Items.DISABLED+"!=?", new String[]{"1"}, Items.DEFAULT_SORT);
//		primaryThread = new Handler();
//		Task = new Runnable() {
//
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//
//				primaryThread.postDelayed(this, 100);
//
//			}

//		};
//		primaryThread.postDelayed(Task, 100);
		// /////////////////////////////////////////////////////////////////////////////////////////////
		mHandler.startQuery(itemsUri, projection, Items.DISABLED + "!=?",
				new String[] { "1" }, Items.DEFAULT_SORT);

		NotificationManager nm = (NotificationManager) this
				.getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(Constants.NavNotificationID);
		String itemnotifyid = null;
		Intent intent = this.getIntent();
		// QRModeButton.setEnabled(false);
		if (intent != null) {
			userEmail = intent.getStringExtra("userEmail");
			userPassword = intent.getStringExtra("userPassword");
			lat = intent.getDoubleExtra("lat", 0);
			lng = intent.getDoubleExtra("lng", 0);
			itemnotifyid = intent.getStringExtra("itemnotifyid");
		}

		if ((userEmail == "") && (userPassword == "")) {

			Toast.makeText(this,
					"Please run it first from Learning Log For You App menu",
					Toast.LENGTH_LONG).show();
			finish();
		} else {
			// Toast.makeText(this,".........Please run it from the main Menu.........",Toast.LENGTH_LONG).show();
			// finish();
		}

	} // end onCreate

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	public void onResume() {
		super.onResume();

	}

	
	
	
	public void onCameraModeClick(View v) {
		Intent CameraModeIntent = new Intent(nav.this, CameraNav.class);
		CameraModeIntent.putExtra("userEmail", userEmail);
		CameraModeIntent.putExtra("userPassword", userPassword);
		nav.this.startActivity(CameraModeIntent);
	}

	public void onARNavigatorClick(View v) {
		Intent ARModeIntent = new Intent(nav.this, navi.class);
		ARModeIntent.putExtra("userEmail", userEmail);
		ARModeIntent.putExtra("userPassword", userPassword);
		nav.this.startActivity(ARModeIntent);
	}

	public void onQRModeClick(View v) {
		Intent QRModeIntent = new Intent(nav.this, qr_codeMain.class);
		QRModeIntent.putExtra("userEmail", userEmail);
		QRModeIntent.putExtra("userPassword", userPassword);
		nav.this.startActivity(QRModeIntent);
	}

	public void onTaskModeClick(View v) {
		Intent TaskIntent = new Intent(nav.this, TestActivity.class);
		// //// Intent TaskIntent = new Intent(nav.this, CameraTask.class);
		// Intent TaskIntent = new Intent(nav.this, TaskSyncService.class);
		// // TaskIntent.putExtra("userEmail", userEmail);
		// // TaskIntent.putExtra("userPassword", userPassword);
		nav.this.startActivity(TaskIntent);
	}

	public void onEXITClick(View v) {
		Intent TaskIntent = new Intent(nav.this, ExaminationScreen.class);
		
		nav.this.startActivity(TaskIntent);
		finish();
	}

	/** Handle "home" title-bar action. */
	public void onHomeClick(View v) {
		UIUtils.goHome(this);
	}

	public void onSearchClick(View v) {
		UIUtils.goSearch(this);
	}

	@Override
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		// TODO 自動生成されたメソッド・スタブ
		int i1 = 0;
		int i;
		Log.d("count", "" + cursor.getCount());
		double[] lat = new double[cursor.getCount()];
		double[] lng = new double[cursor.getCount()];
		String[] name = new String[cursor.getCount()];
		String[] title = new String[cursor.getCount()];

		try {
			cursor.moveToFirst();
			while (cursor.moveToNext()) {
				if (i1 < 10) {
					lat[i1] = cursor.getDouble(7);
					lng[i1] = cursor.getDouble(8);
					name[i1] = cursor.getString(2);
					title[i1] = cursor.getString(5);
					int mode = Context.MODE_PRIVATE;
					SharedPreferences.Editor editor;
					SharedPreferences CurrentLocation = getSharedPreferences(
							"DATALATANDLNG", mode);
					editor = CurrentLocation.edit();
					editor.putFloat("lat" + i1, (float) lat[i1]);
					editor.putFloat("lng" + i1, (float) lng[i1]);
					editor.putString("name" + i1, name[i1]);
					editor.putString("title" + i1, title[i1]);
					editor.putInt("COUNT", i1);
					editor.commit();
					i1++;
					Intent store = new Intent(nav.this, navi.class);
					store.putExtra("latnavi", lat);
					store.putExtra("lngnavi", lng);
					store.putExtra("namenavi", name);
					store.putExtra("titlenavi", title);
					store.putExtra("count", i1);

				} else {
					break;
				}
			}

		} finally {
			cursor.close();
		}

	}

	// ItemQuery
	private interface ItemsQuery {
		String[] PROJECTION = { BaseColumns._ID, Items.ITEM_ID,
				Items.NICK_NAME, Items.PHOTO_URL, Items.NOTE, Items.TITLES,
				Items.UPDATE_TIME, Items.LATITUTE, Items.LNGITUTE };

		int _ID = 0;
		int ITEM_ID = 1;
		int NICK_NAME = 2;
		int PHOTO_URL = 3;
		int NOTE = 4;
		int TITLES = 5;
		int UPDATE_TIME = 6;
		int LATITUTE = 7;
		int LNGITUTE = 8;
	}

	private void launchGPSOptions() {
		Intent intent = new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	
	}

}// navclass end

