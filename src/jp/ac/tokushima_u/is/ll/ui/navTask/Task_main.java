/**
 * 
 * @author 徳島大学　Kousuke Mouri
 * 
 */
package jp.ac.tokushima_u.is.ll.ui.navTask;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jp.ac.tokushima_u.is.ll.R;

import jp.ac.tokushima_u.is.ll.service.ContextAwareService;
import jp.ac.tokushima_u.is.ll.service.ContextAwareService.MyLocationListener;
import jp.ac.tokushima_u.is.ll.ui.nav.SubMyLocationOverlay;
import jp.ac.tokushima_u.is.ll.ui.nav.nav;
import jp.ac.tokushima_u.is.ll.ui.navTaskselect.ChangeTaskscript_select2;
import jp.ac.tokushima_u.is.ll.ui.navTaskselect.TaskClearScreen;

import jp.ac.tokushima_u.is.ll.util.ContextUtil;
import jp.co.yahoo.android.maps.GeoPoint;
import jp.co.yahoo.android.maps.MapActivity;
import jp.co.yahoo.android.maps.MapController;
import jp.co.yahoo.android.maps.MapView;
import jp.co.yahoo.android.maps.MyLocationOverlay;
import jp.co.yahoo.android.maps.OverlayItem;
import jp.co.yahoo.android.maps.PinOverlay;
import jp.co.yahoo.android.maps.PolylineOverlay;
import jp.co.yahoo.android.maps.PopupOverlay;
import jp.co.yahoo.android.maps.ar.ARController;
import jp.co.yahoo.android.maps.ar.ARControllerListener;
import jp.co.yahoo.android.maps.navi.NaviController;
import jp.co.yahoo.android.maps.navi.NaviController.NaviControllerListener;
import jp.co.yahoo.android.maps.routing.RouteOverlay;
import jp.co.yahoo.android.maps.routing.RouteOverlay.RouteOverlayListener;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Task_main extends MapActivity implements RouteOverlayListener,
		NaviControllerListener, View.OnClickListener, ARControllerListener {
	MapView mapView;
	private double lat, lng;
	private String[] taskscript = new String[8];
	int local_lat, local_lng;
	int[] Target_lat = new int[8];
	int[] Target_lng = new int[8];
//	private String[] lat_result = new String[8];
//	private String[] lng_result = new String[8];
	private String[] latarray = new String[8];
	private String[] lngarray = new String[8];
	private String[] image = new String[8];
	private boolean flag1 = false;
	private boolean leaveflag1, leaveflag2;
	private Button button;
	public Context context;
	ARController arController;
	Location location;
	int Tasklat, Tasklng;
	String task = null;
	int Target_Tasklat, Target_Tasklng;
	private LocationManager locationmanager;
	public static final int CircleMessage = 1;
	public static final int LowBatteryMessage = 2;
	public static final int NotifiedMessage = 3;
	NaviController naviController;
	private MyLocationListener locationlistener = new MyLocationListener();
	// private final Runnable listener;
	//
	RouteOverlay routeOverlay;
	GeoPoint centerPoint = null;
	PinOverlay pinOverlay = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get_intent_information
		get_Tasklatlng();
		// GPS sensor
		GPS();
		// lat and lng setting
		lat_lng_setting();
		// YOLP setting
		YOLP();

	}
	@Override
	public void onPause() {
		super.onPause();
//		mapView=null;
//		taskscript=null;
//		Target_lat = null;
//		Target_lng = null;
//		latarray =null;
//		lngarray = null;
//		image =null;
//		locationlistener.setLocation(null);
//		naviController.setNaviControlListener(null);
//		naviController.setARController(null);
//		arController=null;
//		routeOverlay=null;
//		routeOverlay.setRouteOverlayListener(null);
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		mapView=null;
//		taskscript=null;
//		Target_lat = null;
//		Target_lng = null;
//		latarray =null;
//		lngarray = null;
//		image =null;
//		locationlistener.setLocation(null);
//		naviController.setNaviControlListener(null);
//		naviController.setARController(null);
//		arController=null;
//		routeOverlay=null;
////		routeOverlay.setRouteOverlayListener(null);
//		
////		taskscript=null;
////		latarray = null;
////		lngarray = null;
//		finish();
	}
	private void YOLP() {
		// TODO Auto-generated method stub
		// YOLPのMAP設定
		mapView = new MapView(this,
				"n5a596uxg65hFrFlsReD20KjfDpg2qhCcpCmnvOiM7s0aZoAHU0TSiicjBHMbmzzpmMrWyhxFFE-");
		MapController c = mapView.getMapController();

		// GPS取得が成功した場合自身の位置を返す
		if (flag1 == true) {
			centerPoint = new GeoPoint(local_lat, local_lng);
			c.setCenter(centerPoint); // 初期表示の地図を指定
			// GeoPoint mid = new GeoPoint(local_lat, local_lng);
			pinOverlay = new PinOverlay(PinOverlay.PIN_RED);
			pinOverlay.addPoint(centerPoint, "Your Current Position");
			mapView.getOverlays().add(pinOverlay);
			PopupOverlay popupOverlay = new PopupOverlay() {
				@Override
				public void onTap(OverlayItem item) {
					if (leaveflag1 == true) {

					}
				}
			};

			mapView.getOverlays().add(popupOverlay);
			pinOverlay.setOnFocusChangeListener(popupOverlay);

			flag1 = true;

		} else {
			centerPoint = new GeoPoint(34078736, 134568919);
			c.setCenter(centerPoint); // 初期表示の地図を指定
		}

		mapView.setBuiltInZoomControls(true);
		c.setZoom(6);

		// RouteOverlay作成
		routeOverlay = new RouteOverlay(this,
				"n5a596uxg65hFrFlsReD20KjfDpg2qhCcpCmnvOiM7s0aZoAHU0TSiicjBHMbmzzpmMrWyhxFFE-");

		// 出発地ピンの吹き出し設定
		routeOverlay.setStartTitle("Your Current Position");

		// routeOverlay
		// .setRoutePos(new GeoPoint(local_lat, local_lng), new GeoPoint(
		// Target_Tasklat, Target_Tasklng), RouteOverlay.TRAFFIC_WALK);
		if (location != null) {
			routeOverlay.setRoutePos(new GeoPoint(local_lat, local_lng),
					new GeoPoint(Tasklat, Tasklng), RouteOverlay.TRAFFIC_WALK);
		} else {
			Toast.makeText(Task_main.this, "GPSが取得できていないので固定されたデータを設定します",
					Toast.LENGTH_SHORT).show();
			// routeOverlay.setRoutePos(new GeoPoint(34078878, 134561877),
			// new GeoPoint(34086964, 134549689),
			routeOverlay.setRoutePos(new GeoPoint(34078878, 134561877),
					new GeoPoint(34078878, 134561877),
					RouteOverlay.TRAFFIC_WALK);
		}
		// RouteOverlayListenerの設定
		routeOverlay.setRouteOverlayListener(this);

		// MapViewにRouteOverlayを追加
		mapView.getOverlays().add(routeOverlay);
		routeOverlay.setRoutePinVisible(false);
		// 検索を開始
		routeOverlay.search();

	}

	private void lat_lng_setting() {
		// TODO Auto-generated method stub
		// YOLPでの緯度経度取得
		if (lat != 0 && lng != 0) {
			local_lat = (int) (lat * 1000000);
			local_lng = (int) (lng * 1000000);
			int count = latarray.length;
			for (int i = 0; i < count; i++) {
				Target_lat[i] = (int) (Double.parseDouble(latarray[i]) * 1000000);
				Target_lng[i] = (int) (Double.parseDouble(lngarray[i]) * 1000000);
			}

			// Target_Tasklat = (int) (Tasklat * 1000000);
			// Target_Tasklng = (int) (Tasklng * 1000000);
			flag1 = true;
		} else {
			flag1 = false;
			TestThread thread = new TestThread();
			thread.start();
		}
		// -------GPS Thread end-------------
	}

	private void GPS() {
		// TODO Auto-generated method stub
		// ------GPS detail------------
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

	private void get_Tasklatlng() {
		// TODO Auto-generated method stub
		Bundle extras = getIntent().getExtras();

		Tasklat = extras.getInt("Tasklat");
		Tasklng = extras.getInt("Tasklng");
		task = extras.getString("task");
		taskscript = extras.getStringArray("taskscript");
		latarray = extras.getStringArray("latarray");
		lngarray = extras.getStringArray("lngarray");

		image = extras.getStringArray("image");
	}

	@Override
	protected void onResume() {
		super.onResume();

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

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	// ルート検索が正常に終了した場合
	public boolean finishRouteSearch(RouteOverlay routeOverlay) {
		// NaviControllerを作成しRouteOverlayインスタンスを設定
		naviController = new NaviController(this, routeOverlay);

		// MapViewインスタンスを設定
		naviController.setMapView(mapView);

		// NaviControllerListenerを設定
		naviController.setNaviControlListener(this);

		// ARControllerインスタンス作成
		arController = new ARController(this, this);

		// ARControllerをNaviControllerに設定
		naviController.setARController(arController);
		if (lat != 0 && lng != 0) {
			arController.setCurrentPos(lat, lng, 0, 20);
		} else {
			arController.setCurrentPos(34.078884, 134.562172, 0, 20);
		}

		// 案内処理を開始
		naviController.start();
		TextView text = new TextView(this);
		TextView texttitle = new TextView(this);
		TextView texttask = new TextView(this);
		TextView texttasktitle = new TextView(this);
		if (task != null) {
			texttask.setText("Task : " + task);
			texttask.setTextColor(Color.BLACK);
			texttask.setTypeface(Typeface.create(Typeface.SANS_SERIF,
					Typeface.BOLD_ITALIC));
			texttask.setTextSize(18);
		}

		if (taskscript != null) {
			text.setText("  " + taskscript[0]);
			text.setTextColor(Color.BLACK);
			text.setBackgroundColor(Color.WHITE);
			texttitle.setText("Script:Step1");
			texttitle.setTextColor(Color.BLACK);
			texttitle.setTypeface(Typeface.create(Typeface.SANS_SERIF,
					Typeface.BOLD_ITALIC));
			texttitle.setTextSize(18);
		} else {
			text.setText("This is a TextView sample.");
		}
		//
		// LinearLayout layout = new LinearLayout(this);
		// layout.addView(text, new LinearLayout.LayoutParams(
		// LinearLayout.LayoutParams.WRAP_CONTENT,
		// LinearLayout.LayoutParams.WRAP_CONTENT));
		// addContentView(layout,new LinearLayout.LayoutParams(
		// LinearLayout.LayoutParams.WRAP_CONTENT,
		// LinearLayout.LayoutParams.WRAP_CONTENT));
		AbsoluteLayout layout = new AbsoluteLayout(this);
		layout.addView(texttask, new AbsoluteLayout.LayoutParams(600, 130, 10,
				330));
		layout.addView(text, new AbsoluteLayout.LayoutParams(290, 130, 10, 410));
		layout.addView(texttitle, new AbsoluteLayout.LayoutParams(200, 200, 10,
				370));
		addContentView(layout, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		return false;
	}

	// ルート検索が正常に終了しなかった場合
	public boolean errorRouteSearch(RouteOverlay arg0, int arg1) {
		return false;
	}

	// 現在位置が更新された場合
	public boolean onLocationChanged(NaviController naviController) {
		// 目的地までの残りの距離
		double rema_dist = 0;
		rema_dist = naviController.getTotalDistance();

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
		rema_dist = 0;
		// ゴールに到着（Taskscriptに移動）
		if (rema_dist <= 5) {
//			int a = taskscript.length;
			if (taskscript.length != 1) {
				Intent logintent = new Intent(Task_main.this,
						ChangeTaskscript_select2.class);
				List<String> list = new ArrayList<String>(
						Arrays.asList(taskscript));
				if (list.size() != 0) {
					list.remove(0);
				}
				String[] convert_taskscript = (String[]) list
						.toArray(new String[list.size()]);

				List<String> list2 = new ArrayList<String>(
						Arrays.asList(latarray));
				if (list2.size() != 0) {
					list2.remove(0);
				}
				String[] convert_lat = (String[]) list2
						.toArray(new String[list2.size()]);

				List<String> list3 = new ArrayList<String>(
						Arrays.asList(lngarray));
				if (list3.size() != 0) {
					list3.remove(0);
				}
				String[] convert_lng = (String[]) list3
						.toArray(new String[list3.size()]);

				String nullcompare = "null";
				System.gc();
				// if (nullcompare.equals(image[0])) {
				//
				// }
				// else if (nullcompare.equalsIgnoreCase(image[0])) {
				//
				// }
				// else {
				// List<String> list4 = new ArrayList<String>(
				// Arrays.asList(image));
				// if (list.size() != 0) {
				// list4.remove(0);
				// }
				// String[] convert_image = (String[]) list4
				// .toArray(new String[list4.size()]);
				// logintent.putExtra("image", convert_image);
				// }
				logintent.putExtra("task2", task);
				logintent.putExtra("taskscript2", convert_taskscript);
				logintent.putExtra("Tasklat2", convert_lat);
				logintent.putExtra("Tasklng2", convert_lng);
//				Log.i("Taskscript", convert_taskscript[0]+convert_taskscript[1]);
				Task_main.this.startActivity(logintent);
				naviController.stop();
				arController.onPause();

				naviController.setARController(null);
				finish();
			}
		 else {
			Log.i("ループに入りました","test");
			Intent logintent = new Intent(Task_main.this,
					TaskClearScreen.class);
			Task_main.this.startActivity(logintent);
			this.finish();
		}}
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
		//ARの停止処理
		arController.onPause();
		//ARControllerをNaviControllerから削除
		naviController.setARController(null);



		// Intent logintent = new Intent(Task_main.this,
		// Taskscript_select.class);
		// Task_main.this.startActivity(logintent);
		finish();
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

}
