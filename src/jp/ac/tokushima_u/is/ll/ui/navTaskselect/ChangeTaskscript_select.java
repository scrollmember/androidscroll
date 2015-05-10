package jp.ac.tokushima_u.is.ll.ui.navTaskselect;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

import jp.ac.tokushima_u.is.ll.R;

import jp.ac.tokushima_u.is.ll.ui.navTask.Task_main;
import jp.ac.tokushima_u.is.ll.util.LocalApiContants;
import jp.co.yahoo.android.maps.GeoPoint;
import jp.co.yahoo.android.maps.MapActivity;
import jp.co.yahoo.android.maps.MapController;
import jp.co.yahoo.android.maps.MapView;
import jp.co.yahoo.android.maps.PinOverlay;
import jp.co.yahoo.android.maps.navi.NaviController;
import jp.co.yahoo.android.maps.navi.NaviController.NaviControllerListener;
import jp.co.yahoo.android.maps.routing.RouteOverlay;
import jp.co.yahoo.android.maps.routing.RouteOverlay.RouteOverlayListener;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class ChangeTaskscript_select extends MapActivity implements
		OnClickListener, RouteOverlayListener, NaviControllerListener {

	int Id;
	String task = "";
	int taskcount;
	String[] taskscript = new String[10];

	int[] Target_lat = new int[8];
	int[] Target_lng = new int[8];
	String[] lat_result = new String[8];
	String[] lng_result = new String[8];
	static String[] image = new String[8];
	// GPS and Map
	private LocationManager locationmanager;
	Location location;
	private double lat, lng;
	private MyLocationListener locationlistener = new MyLocationListener();
	int local_lat, local_lng;
	// int Target_lat, Target_lng;
	double Target_Tasklat, Target_Tasklng;
	boolean flag1 = false;
	GeoPoint centerPoint = null;
	PinOverlay pinOverlay = null;
	boolean leaveflag1, leaveflag2;
	MapView mapView;
	public static final int CircleMessage = 1;
	public static final int LowBatteryMessage = 2;
	public static final int NotifiedMessage = 3;
	private final int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;
	private final int FULL_PARENT = ViewGroup.LayoutParams.FILL_PARENT;
	NaviController naviController;
	private ViewFlipper viewFlipper;
	RelativeLayout relativeLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.navtaskscript);

		// intent information
		info();
		// GPS_SENSOR
		GPS_setting();

		// 緯度経度設定
		lat_lng_setting();

		// task_step
		task_step();
		// task_language
		// task_luanguage();
		// time
		time();
		// place
		place();
		// task_script
		task_script();
		// knowledge
		// task_knowledge();

		Navigator_map();
		// next_task and navigator
		navigator();

		// image
		image_script();
	}
	@Override
	public void onPause() {
		super.onPause();
	
			finish();
		
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView=null;
		viewFlipper=null;
		relativeLayout=null;
		naviController.stop();
		mapView=null;
		taskscript=null;
		Target_lat = null;
		Target_lng = null;
		image =null;
		naviController.setNaviControlListener(null);
		
		naviController.setARController(null);
//
//		mapView.removeAllViews();
//		relativeLayout.removeAllViews();
		
		System.gc();

		finish();
	}

	private void image_script() {
		// TODO Auto-generated method stub
		// SampleView sample = new SampleView(this);

		// ImageView image = (ImageView) findViewById(R.id.taskimage);
		// image.setImageDrawable(sample.d);
		viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper1);
		RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.reativelayout2);

		relativeLayout.addView(mapView);
		viewFlipper.setDisplayedChild(2);
	}

	private void GPS_setting() {
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
	}

	private void lat_lng_setting() {
		// TODO Auto-generated method stub
		if (lat != 0 && lng != 0) {
			local_lat = (int) (lat * 1000000);
			local_lng = (int) (lng * 1000000);
			// Target_lat = (int) (34.07844586944945 * 1000000);
			// Target_lng = (int) (134.55750375134278 * 1000000);
			int count = lat_result.length;
			for (int i = 0; i < count; i++) {
				Target_lat[i] = (int) (Double.parseDouble(lat_result[i]) * 1000000);
				Target_lng[i] = (int) (Double.parseDouble(lng_result[i]) * 1000000);
			}

			// Target_lat = (int) (Target_Tasklat * 1000000);
			// Target_lng = (int) (Target_Tasklng * 1000000);
			flag1 = true;
		} else {
			flag1 = false;
			TestThread thread = new TestThread();
			thread.start();
		}
	}

	private void info() {
		// TODO Auto-generated method stub

		Bundle extras = getIntent().getExtras();

		// Id = extras.getInt("ID");
		task = extras.getString("task2");

		taskscript = extras.getStringArray("taskscript2");
		System.out.print(taskscript);
		lat_result = extras.getStringArray("Tasklat2");
		lng_result = extras.getStringArray("Tasklng2");

		image = extras.getStringArray("image");


	}

	private void task_step() {
		// TODO Auto-generated method stub
		TextView taskscript1 = (TextView) findViewById(R.id.step);
		taskscript1.setText(" Step 1 of ");
		taskscript1.setTextColor(Color.BLACK);
		taskscript1.setTypeface(Typeface.create(Typeface.SANS_SERIF,
				Typeface.BOLD_ITALIC));
		taskscript1.setTextSize(18);
		if (task != null) {
			TextView place2 = (TextView) findViewById(R.id.step2);
			place2.setText(task);
			place2.setTextColor(Color.BLACK);
			place2.setGravity(Gravity.LEFT | Gravity.TOP);
			place2.setTextSize(15);
		}
	}


	private void time() {
		// TODO Auto-generated method stub
		TextView time = (TextView) findViewById(R.id.time);
		time.setText(" Time : ");
		time.setTextColor(Color.BLACK);
		time.setTypeface(Typeface.create(Typeface.SANS_SERIF,
				Typeface.BOLD_ITALIC));
		time.setTextSize(18);

		TextView timename = (TextView) findViewById(R.id.timename);
		timename.setText("14:00 ~ 15:00");
		timename.setTextColor(Color.BLACK);
		timename.setGravity(Gravity.LEFT | Gravity.TOP);
		timename.setTextSize(15);

	}

	private void place() {
		// TODO Auto-generated method stub
		TextView place = (TextView) findViewById(R.id.place);
		place.setText(" Place : ");
		place.setTextColor(Color.BLACK);
		place.setTypeface(Typeface.create(Typeface.SANS_SERIF,
				Typeface.BOLD_ITALIC));
		place.setTextSize(18);

		TextView placename = (TextView) findViewById(R.id.placename);
		placename.setText("bank");
		placename.setTextColor(Color.BLACK);
		placename.setGravity(Gravity.LEFT | Gravity.TOP);
		placename.setTextSize(15);

	}

	private void task_script() {
		// TODO Auto-generated method stub
		TextView place3 = (TextView) findViewById(R.id.script);
		place3.setText("  Script : ");
		place3.setTextColor(Color.BLACK);
		place3.setTypeface(Typeface.create(Typeface.SANS_SERIF,
				Typeface.BOLD_ITALIC));
		place3.setTextSize(18);

		TextView place = (TextView) findViewById(R.id.step3);
		place.setText(" ");
		place.setTextColor(Color.BLACK);
		place.setTypeface(Typeface.create(Typeface.SANS_SERIF,
				Typeface.BOLD_ITALIC));
		place.setTextSize(18);
		// RelativeLayout relativeLayout = (RelativeLayout)
		// findViewById(R.id.reativelayout);
		if (taskscript[0] != null) {
			TextView place4 = (TextView) findViewById(R.id.step4);
			place4.setText(taskscript[0]);
			place4.setTextColor(Color.BLACK);
			place4.setGravity(Gravity.LEFT | Gravity.TOP);
			place4.setTextSize(15);
		}


	}


	private void Navigator_map() {
		// TODO Auto-generated method stub
		mapView = new MapView(this,
				"n5a596uxg65hFrFlsReD20KjfDpg2qhCcpCmnvOiM7s0aZoAHU0TSiicjBHMbmzzpmMrWyhxFFE-");
		MapController c = mapView.getMapController();

		if (flag1 == true) {
			centerPoint = new GeoPoint(local_lat, local_lng);
			c.setCenter(centerPoint); // 初期表示の地図を指定
			
			flag1 = true;

		} else {
			centerPoint = new GeoPoint(34078736, 134568919);
			c.setCenter(centerPoint); // 初期表示の地図を指定
		}

		mapView.setBuiltInZoomControls(true);
		c.setZoom(6);

		// RouteOverlay作成
		RouteOverlay routeOverlay = new RouteOverlay(this,
				"n5a596uxg65hFrFlsReD20KjfDpg2qhCcpCmnvOiM7s0aZoAHU0TSiicjBHMbmzzpmMrWyhxFFE-");

		// 出発地ピンの吹き出し設定
		routeOverlay.setStartTitle("Your Current Position");
		routeOverlay.setGoalTitle("Task_goal");
		if (location != null) {
			routeOverlay.setRoutePos(new GeoPoint(local_lat, local_lng),
					new GeoPoint(Target_lat[0], Target_lng[0]),
					RouteOverlay.TRAFFIC_WALK);
		} else {
			Toast.makeText(ChangeTaskscript_select.this,
					"GPSが取得できていないので固定されたデータを設定します", Toast.LENGTH_SHORT).show();
			routeOverlay.setRoutePos(new GeoPoint(34078878, 134561877),
					new GeoPoint(34086964, 134549689),
					RouteOverlay.TRAFFIC_WALK);
		}
		// RouteOverlayListenerの設定
		routeOverlay.setRouteOverlayListener(this);

		// MapViewにRouteOverlayを追加
		mapView.getOverlays().add(routeOverlay);
		routeOverlay.setRoutePinVisible(false);
		// 検索を開始
		routeOverlay.search();
		// RelativeLayout relativeLayout = (RelativeLayout)
		// findViewById(R.id.reativelayout2);
		//
		// relativeLayout.addView(mapView);

	}

	private void navigator() {
		// TODO Auto-generated method stub
		// TextView place3 = (TextView) findViewById(R.id.location);
		// place3.setText(" Location");
		// place3.setTextColor(Color.BLACK);
		// place3.setTypeface(Typeface.create(Typeface.SANS_SERIF,
		// Typeface.BOLD_ITALIC));
		// place3.setTextSize(20);
		Button btn = (Button) findViewById(R.id.nextbutton);
		Button btn2 = (Button) findViewById(R.id.navigatorbutton);
		Button btn3 = (Button) findViewById(R.id.commentbutton);
		Button btn4 = (Button) findViewById(R.id.imagebutton);
		Button btn5 = (Button) findViewById(R.id.mapbutton);
		// EditText hukku_note = (EditText) this.findViewById(R.id.comment);
		// btn.setText("delete");
		// btn.setTextSize(20);
		// btn.setHeight(40);
		// btn.setTypeface(Typeface.DEFAULT_BOLD);
		btn.setOnClickListener(this);
		btn2.setOnClickListener(this);
		btn3.setOnClickListener(this);
		btn4.setOnClickListener(this);
		btn5.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		// EditText edit = new EditText(this);
		// RelativeLayout relativeLayout3 = (RelativeLayout)
		// findViewById(R.id.reativelayout2);
		switch (v.getId()) {
		case R.id.nextbutton:
			finish();
			break;

		case R.id.navigatorbutton:
			System.gc();
			Intent logintent = new Intent(ChangeTaskscript_select.this,
					Task_main.class);
			logintent.putExtra("Tasklat", Target_lat[0]);
			logintent.putExtra("Tasklng", Target_lng[0]);
			logintent.putExtra("task", task);
			logintent.putExtra("taskscript", taskscript);
			logintent.putExtra("latarray", lat_result);
			logintent.putExtra("lngarray", lng_result);
			finish();
			naviController.stop();
			ChangeTaskscript_select.this.startActivity(logintent);

			
			break;

		case R.id.commentbutton:
			viewFlipper.setDisplayedChild(0);
			// relativeLayout3.addView(edit, new
			// LinearLayout.LayoutParams(FULL_PARENT, FULL_PARENT));
			EditText hukku_note = (EditText) this.findViewById(R.id.comment);
			viewFlipper.setDisplayedChild(1);
			break;

		case R.id.mapbutton:
			viewFlipper.setDisplayedChild(2);
			// relativeLayout3.removeView(edit);
			relativeLayout = (RelativeLayout) findViewById(R.id.reativelayout2);

			viewFlipper.setDisplayedChild(2);
			break;
		case R.id.imagebutton:
			viewFlipper.setDisplayedChild(0);
//			SampleView sample = new SampleView(this);

//			ImageView image = (ImageView) findViewById(R.id.taskimage);
//			image.setImageDrawable(sample.d);
			viewFlipper.setDisplayedChild(0);
			break;

		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			Location loc = locationmanager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (loc != null && pinOverlay != null) {
				mapView.getOverlays().remove(pinOverlay);
				centerPoint = new GeoPoint((int) (loc.getLatitude() * 1000000),
						(int) (loc.getLongitude() * 1000000));
				pinOverlay.addPoint(centerPoint, "Your Current Position");
				mapView.getOverlays().add(pinOverlay);
			}
			// pinOverlay
		}

	};

	class TestThread extends Thread {
		@Override
		public void run() {

			Location loc = locationmanager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);

			int j = 0;
			while ((loc == null || !loc.hasAccuracy() || loc.getAccuracy() < 30)
					&& j <= 6) {
				j++;
				try {
					sleep(10);

					loc = locationmanager
							.getLastKnownLocation(LocationManager.GPS_PROVIDER);

				} catch (Exception e) {

				}
			}

			if (loc != null) {
				lat = loc.getLatitude();
				lng = loc.getLongitude();
			}

			handler.sendEmptyMessage(LowBatteryMessage);
		}
	}

	public class MyLocationListener implements LocationListener {
		private Location location;

		public Location getLocation() {
			return location;
		}

		public void setLocation(Location location) {
			this.location = location;
		}

		@Override
		public void onLocationChanged(Location loc) {
			this.location = loc;
		}

		@Override
		public void onProviderDisabled(String provider) {
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
			criteria.setAltitudeRequired(true);
			criteria.setBearingRequired(false);
			criteria.setSpeedRequired(true);
			criteria.setCostAllowed(false);

			if (locationmanager == null)
				locationmanager = (LocationManager) getSystemService(LOCATION_SERVICE);
			// locationProvider = locationmanager.getBestProvider(criteria,
			// true);
			List<String> providers = locationmanager.getProviders(false);
			long latest = 0;

			// locationmanager.requestLocationUpdates(locationProvider,
			// gps_circle_time, gps_circle_distance, locationlistener);

		}

		@Override
		public void onProviderEnabled(String provider) {
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
			criteria.setAltitudeRequired(true);
			criteria.setBearingRequired(false);
			criteria.setSpeedRequired(true);
			criteria.setCostAllowed(false);
			if (locationmanager == null)
				locationmanager = (LocationManager) getSystemService(LOCATION_SERVICE);
			// locationProvider = locationmanager.getBestProvider(criteria,
			// true);
			// locationmanager.requestLocationUpdates(locationProvider,
			// gps_circle_time, gps_circle_distance, locationlistener);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}
	}

	// ルート検索が正常に終了した場合
	public boolean finishRouteSearch(RouteOverlay routeOverlay) {
		// NaviControllerを作成しRouteOverlayインスタンスを設定
		naviController = new NaviController(this, routeOverlay);

		// MapViewインスタンスを設定
		naviController.setMapView(mapView);

		// NaviControllerListenerを設定
		naviController.setNaviControlListener(this);

		// 案内処理を開始
		naviController.start();
		// RelativeLayout relativeLayout = (RelativeLayout)
		// findViewById(R.id.reativelayout2);
		//
		// relativeLayout.addView(mapView);
		return false;
	}

	// ルート検索が正常に終了しなかった場合
	public boolean errorRouteSearch(RouteOverlay arg0, int arg1) {
		return false;
	}

	// 現在位置が更新された場合
	public boolean onLocationChanged(NaviController naviController) {
		// 目的地までの残りの距離
		double rema_dist = naviController.getTotalDistance();

		// 目的地までの残りの時間
		double rema_time = naviController.getTotalTime();

		// 出発地から目的地までの距離
		double total_dist = naviController.getDistanceOfRemainder();

		// 出発地から目的地までの時間
		double total_time = naviController.getTimeOfRemainder();

		// 現在位置
		Location location = naviController.getLocation();
		lat = location.getLatitude();
		lng = location.getLongitude();

		if (location.getAccuracy() >= 100) {

		}

		// //ゴールに到着（Taskscriptに移動）
		// if(rema_dist<=5){
		// Intent logintent = new Intent(Task_main.this,
		// CameraTask.class);
		// Task_main.this.startActivity(logintent);
		// finish();
		// }
		return false;
	}

	// 現在位置取得エラーが発生した場合
	public boolean onLocationTimeOver(NaviController arg0) {
		return false;
	}

	// 現在位置の精度が悪い場合
	public boolean onLocationAccuracyBad(NaviController arg0) {
		return false;
	}

	// ルートから外れたと判断された場合
	public boolean onRouteOut(NaviController arg0) {
		return false;
	}

	// 目的地に到着した場合
	public boolean onGoal(NaviController naviController) {
		// 案内処理を継続しない場合は停止させる
		naviController.stop();
		return false;
	}

	public void onLocationClick(View view) {

	}

	public void ARControllerListenerOnPOIPick(int index) {

	}

	private void launchGPSOptions() {
		Intent intent = new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(intent);
	}

//	private static class SampleView extends View {
//		private Drawable d;
//		Context context;
//
//		public SampleView(Context context) {
//			super(context);
//			d = loadImage(LocalApiContants.task_image + image[0]
//					+ LocalApiContants.MiddleSizePostfix);
//		}
//
//		public Drawable loadImage(String str) {
//			Drawable d = null;
//			if (image[0] != null) {
//				try {
//					URL url = new URL(str);
//					HttpURLConnection http = (HttpURLConnection) url
//							.openConnection();
//					// http.setRequestMethod("GET");
//					http.connect();
//					InputStream in = http.getInputStream();
//					d = Drawable.createFromStream(in, "a");
//					in.close();
//				} catch (Exception e) {
//				}
//				d.setBounds(20, 20, 143, 59);
//				return d;
//			} else {
//
//				Drawable mIcon = context.getResources().getDrawable(
//						R.drawable.noimage);
//
//				return mIcon;
//			}
//		}
//
//		@Override
//		protected void onDraw(Canvas canvas) {
//			d.draw(canvas);
//		}
//
//	}

}
