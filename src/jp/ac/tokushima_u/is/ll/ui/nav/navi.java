package jp.ac.tokushima_u.is.ll.ui.nav;

/**
 * 
 * @author Kousuke Mouri University of Tokushima,Japan
 * 
 */
import java.util.Calendar;
import java.util.List;

import jp.ac.tokushima_u.is.ll.R;

import jp.ac.tokushima_u.is.ll.service.ContextAwareService;
import jp.ac.tokushima_u.is.ll.service.ContextAwareService.MyLocationListener;
import jp.ac.tokushima_u.is.ll.ui.nav.SubMyLocationOverlay;
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
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class navi extends MapActivity implements RouteOverlayListener,
		NaviControllerListener, View.OnClickListener {
	MapView mapView;
	private double lat, lng;
	int local_lat, local_lng;
	boolean flag1 = false;
	boolean leaveflag1, leaveflag2;
	private Button button;
	public Context context;
	private MyLocationOverlay _overlay;
	Location location;
	private LocationManager locationmanager;
	public static final int CircleMessage = 1;
	public static final int LowBatteryMessage = 2;
	public static final int NotifiedMessage = 3;
//	private MyLocationListener locationlistener = new MyLocationListener();
	// private final Runnable listener;
	//

	int[] itemlat = new int[50];
	int[] itemlng = new int[50];
	String[] name = new String[50];
	String[] title = new String[50];

	float[] lattest = new float[50];
	float[] lngtest = new float[50];
	NaviController naviController;
	GeoPoint centerPoint = null;
	PinOverlay pinOverlay = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		Intent intent = this.getIntent();
//
//		if (intent != null) {
//			lattest = intent.getFloatArrayExtra("latnavi");
//			lngtest = intent.getFloatArrayExtra("lngnavi");
//
//		} else {
//			finish();
//		}
//
//		location = null;
//		// locationmanagerを取得するためにはContext.getSystemService(LoCATION_SERVICE)を呼び出す
//		locationmanager = (LocationManager) this
//				.getSystemService(LOCATION_SERVICE);
//		// Android端末で確実に位置情報を取得するためには三つのプロバイダーを呼びます
//		// GPSは[gps],3GおよびWi-Fiは[Network]という文字列が取得できる
//		// Android2.2からプロバイダにpassiveが追加される。　このプロバイダーは使い方は不明
//		List<String> providers = locationmanager.getProviders(false);
//		long latest = 0;
//
//		if (!locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//			builder.setMessage(getText(R.string.info_require_gps))
//					.setCancelable(false)
//					.setPositiveButton("Yes",
//							new DialogInterface.OnClickListener() {
//								@Override
//								public void onClick(
//										final DialogInterface dialog,
//										final int id) {
//									launchGPSOptions();
//								}
//							})
//					.setNegativeButton("No",
//							new DialogInterface.OnClickListener() {
//								@Override
//								public void onClick(
//										final DialogInterface dialog,
//										final int id) {
//									dialog.cancel();
//								}
//							});
//			final AlertDialog alert = builder.create();
//			alert.show();
//		}
//
//		if (locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//			locationmanager.requestLocationUpdates(
//					LocationManager.GPS_PROVIDER, 1000, 0, locationlistener);
//			location = locationmanager
//					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//			if (location != null) {
//				lat = location.getLatitude();
//				lng = location.getLongitude();
//				// 経度取得
//			}
//		} else {
//			for (String provider : providers) {
//				location = locationmanager.getLastKnownLocation(provider);
//
//				if (location != null) {
//					// getAccuracy()メッソドで精度を取得する
//					float accuracy = location.getAccuracy();
//					if (accuracy <= 70) {
//						if ((Calendar.getInstance().getTimeInMillis() - location
//								.getTime()) < 10 * 60 * 1000) {
//							// 緯度取得
//							lat = location.getLatitude();
//							// 経度取得
//							lng = location.getLongitude();
//							break;
//						} else
//							continue;
//					}
//
//				}
//			}
//		}

		SharedPreferences DATA = getSharedPreferences("DATALATANDLNG",
				Activity.MODE_PRIVATE);
		int count = DATA.getInt("COUNT", 0);
		int i;
		for (i = 0; i <= count; i++) {

			itemlat[i] = (int) (DATA.getFloat("lat" + i, 0) * 1000000);
			itemlng[i] = (int) (DATA.getFloat("lng" + i, 0) * 1000000);
			name[i] = DATA.getString("name" + i, "");
			title[i] = DATA.getString("title" + i, "");

		}

//		// YOLPでの緯度経度取得
//		if (lat != 0 && lng != 0) {
//			local_lat = (int) (lat * 1000000);
//			local_lng = (int) (lng * 1000000);
//			flag1 = true;
//		} else {
//			flag1 = false;
//			TestThread thread = new TestThread();
//			thread.start();
//		}
		// YOLPのMAP設定
		local_lat=35665721;
		local_lng=139731006;
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
		RouteOverlay routeOverlay = new RouteOverlay(this,
				"n5a596uxg65hFrFlsReD20KjfDpg2qhCcpCmnvOiM7s0aZoAHU0TSiicjBHMbmzzpmMrWyhxFFE-");

		// 出発地ピンの吹き出し設定
		routeOverlay.setStartTitle("Your Current Position");

		// RouteOverlayListenerの設定
		routeOverlay.setRouteOverlayListener(this);

		// MapViewにRouteOverlayを追加
		mapView.getOverlays().add(routeOverlay);
		routeOverlay.setRoutePinVisible(false);

		for (i = 0; i <= count; i++) {
			switch (i) {
			case 0:

				GeoPoint mid = new GeoPoint(itemlat[0], itemlng[0]);
				PinOverlay pinOverlay = new PinOverlay(PinOverlay.PIN_VIOLET);
				pinOverlay.addPoint(mid, "User:" + name[0] + "\r\n" + "Log:"
						+ title[0], "Learninglog1");
				mapView.getOverlays().add(pinOverlay);
				leaveflag1 = true;

				PopupOverlay popupOverlay = new PopupOverlay() {
					@Override
					public void onTap(OverlayItem item) {
						if (leaveflag1 == true) {
							int mode = Context.MODE_PRIVATE;
							SharedPreferences.Editor editor;
							SharedPreferences CurrentLocation = getSharedPreferences(
									"Changenavi", mode);
							editor = CurrentLocation.edit();
							editor.putInt("lat", itemlat[0]);
							editor.putInt("lng", itemlng[0]);
							editor.putString("name", name[0]);
							editor.putString("title", title[0]);
							editor.commit();
							finish();
							Intent ARModeIntent = new Intent(navi.this,
									Changenavi.class);
							navi.this.startActivity(ARModeIntent);
						}

					}
				};
				mapView.getOverlays().add(popupOverlay);
				pinOverlay.setOnFocusChangeListener(popupOverlay);

				break;
			case 1:
				GeoPoint mid2 = new GeoPoint(itemlat[1], itemlng[1]);
				PinOverlay pinOverlay2 = new PinOverlay(PinOverlay.PIN_VIOLET);
				pinOverlay2.addPoint(mid2, "User:" + name[1] + "\r\n" + "Log:"
						+ title[1], "Learninglog1");
				mapView.getOverlays().add(pinOverlay2);
				leaveflag1 = true;

				PopupOverlay popupOverlay2 = new PopupOverlay() {
					@Override
					public void onTap(OverlayItem item) {
						if (leaveflag1 == true) {
							int mode = Context.MODE_PRIVATE;
							SharedPreferences.Editor editor;
							SharedPreferences CurrentLocation = getSharedPreferences(
									"Changenavi", mode);
							editor = CurrentLocation.edit();
							editor.putInt("lat", itemlat[1]);
							editor.putInt("lng", itemlng[1]);
							editor.putString("name", name[1]);
							editor.putString("title", title[1]);
							editor.commit();
							finish();
							Intent ARModeIntent = new Intent(navi.this,
									Changenavi.class);
							navi.this.startActivity(ARModeIntent);
						}

					}
				};
				mapView.getOverlays().add(popupOverlay2);
				pinOverlay2.setOnFocusChangeListener(popupOverlay2);

				break;
			case 2:
				GeoPoint mid3 = new GeoPoint(itemlat[2], itemlng[2]);
				PinOverlay pinOverlay3 = new PinOverlay(PinOverlay.PIN_VIOLET);
				pinOverlay3.addPoint(mid3, "User:" + name[2] + "\r\n" + "Log:"
						+ title[2], "Learninglog1");
				mapView.getOverlays().add(pinOverlay3);
				leaveflag1 = true;

				PopupOverlay popupOverlay3 = new PopupOverlay() {
					@Override
					public void onTap(OverlayItem item) {
						if (leaveflag1 == true) {
							int mode = Context.MODE_PRIVATE;
							SharedPreferences.Editor editor;
							SharedPreferences CurrentLocation = getSharedPreferences(
									"Changenavi", mode);
							editor = CurrentLocation.edit();
							editor.putInt("lat", itemlat[2]);
							editor.putInt("lng", itemlng[2]);
							editor.putString("name", name[2]);
							editor.putString("title", title[2]);
							editor.commit();
							finish();
							Intent ARModeIntent = new Intent(navi.this,
									Changenavi.class);
							navi.this.startActivity(ARModeIntent);
						}

					}
				};
				mapView.getOverlays().add(popupOverlay3);
				pinOverlay3.setOnFocusChangeListener(popupOverlay3);
				break;
			case 3:
				GeoPoint mid4 = new GeoPoint(itemlat[3], itemlng[3]);
				PinOverlay pinOverlay4 = new PinOverlay(PinOverlay.PIN_VIOLET);
				pinOverlay4.addPoint(mid4, "User:" + name[3] + "\r\n" + "Log:"
						+ title[3], "Learninglog1");
				mapView.getOverlays().add(pinOverlay4);
				leaveflag1 = true;

				PopupOverlay popupOverlay4 = new PopupOverlay() {
					@Override
					public void onTap(OverlayItem item) {
						if (leaveflag1 == true) {
							int mode = Context.MODE_PRIVATE;
							SharedPreferences.Editor editor;
							SharedPreferences CurrentLocation = getSharedPreferences(
									"Changenavi", mode);
							editor = CurrentLocation.edit();
							editor.putInt("lat", itemlat[3]);
							editor.putInt("lng", itemlng[3]);
							editor.putString("name", name[3]);
							editor.putString("title", title[3]);
							editor.commit();
							finish();
							Intent ARModeIntent = new Intent(navi.this,
									Changenavi.class);
							navi.this.startActivity(ARModeIntent);
						}

					}
				};
				mapView.getOverlays().add(popupOverlay4);
				pinOverlay4.setOnFocusChangeListener(popupOverlay4);
				break;
			case 4:
				GeoPoint mid5 = new GeoPoint(itemlat[4], itemlng[4]);
				PinOverlay pinOverlay5 = new PinOverlay(PinOverlay.PIN_VIOLET);
				pinOverlay5.addPoint(mid5, "User:" + name[4] + "\r\n" + "Log:"
						+ title[4], "Learninglog1");
				mapView.getOverlays().add(pinOverlay5);
				leaveflag1 = true;

				PopupOverlay popupOverlay5 = new PopupOverlay() {
					@Override
					public void onTap(OverlayItem item) {
						if (leaveflag1 == true) {
							int mode = Context.MODE_PRIVATE;
							SharedPreferences.Editor editor;
							SharedPreferences CurrentLocation = getSharedPreferences(
									"Changenavi", mode);
							editor = CurrentLocation.edit();
							editor.putInt("lat", itemlat[4]);
							editor.putInt("lng", itemlng[4]);
							editor.putString("name", name[4]);
							editor.putString("title", title[4]);
							editor.commit();
							finish();
							Intent ARModeIntent = new Intent(navi.this,
									Changenavi.class);
							navi.this.startActivity(ARModeIntent);
						}

					}
				};
				mapView.getOverlays().add(popupOverlay5);
				pinOverlay5.setOnFocusChangeListener(popupOverlay5);
				break;
			case 5:
				GeoPoint mid6 = new GeoPoint(itemlat[5], itemlng[5]);
				PinOverlay pinOverlay6 = new PinOverlay(PinOverlay.PIN_VIOLET);
				pinOverlay6.addPoint(mid6, "User:" + name[5] + "\r\n" + "Log:"
						+ title[5], "Learninglog1");
				mapView.getOverlays().add(pinOverlay6);
				leaveflag1 = true;

				PopupOverlay popupOverlay6 = new PopupOverlay() {
					@Override
					public void onTap(OverlayItem item) {
						if (leaveflag1 == true) {
							int mode = Context.MODE_PRIVATE;
							SharedPreferences.Editor editor;
							SharedPreferences CurrentLocation = getSharedPreferences(
									"Changenavi", mode);
							editor = CurrentLocation.edit();
							editor.putInt("lat", itemlat[5]);
							editor.putInt("lng", itemlng[5]);
							editor.putString("name", name[5]);
							editor.putString("title", title[5]);
							editor.commit();
							finish();
							Intent ARModeIntent = new Intent(navi.this,
									Changenavi.class);
							navi.this.startActivity(ARModeIntent);
						}

					}
				};
				mapView.getOverlays().add(popupOverlay6);
				pinOverlay6.setOnFocusChangeListener(popupOverlay6);
				break;

			case 6:
				GeoPoint mid7 = new GeoPoint(itemlat[6], itemlng[6]);
				PinOverlay pinOverlay7 = new PinOverlay(PinOverlay.PIN_VIOLET);
				pinOverlay7.addPoint(mid7, "User:" + name[6] + "\r\n" + "Log:"
						+ title[6], "Learninglog1");
				mapView.getOverlays().add(pinOverlay7);
				leaveflag1 = true;

				PopupOverlay popupOverlay7 = new PopupOverlay() {
					@Override
					public void onTap(OverlayItem item) {
						if (leaveflag1 == true) {
							int mode = Context.MODE_PRIVATE;
							SharedPreferences.Editor editor;
							SharedPreferences CurrentLocation = getSharedPreferences(
									"Changenavi", mode);
							editor = CurrentLocation.edit();
							editor.putInt("lat", itemlat[6]);
							editor.putInt("lng", itemlng[6]);
							editor.putString("name", name[6]);
							editor.putString("title", title[6]);
							editor.commit();
							finish();
							Intent ARModeIntent = new Intent(navi.this,
									Changenavi.class);
							navi.this.startActivity(ARModeIntent);
						}

					}
				};
				mapView.getOverlays().add(popupOverlay7);
				pinOverlay7.setOnFocusChangeListener(popupOverlay7);
				break;

			case 7:
				GeoPoint mid8 = new GeoPoint(itemlat[7], itemlng[7]);
				PinOverlay pinOverlay8 = new PinOverlay(PinOverlay.PIN_VIOLET);
				pinOverlay8.addPoint(mid8, "User:" + name[7] + "\r\n" + "Log:"
						+ title[7], "Learninglog1");
				mapView.getOverlays().add(pinOverlay8);
				leaveflag1 = true;

				PopupOverlay popupOverlay8 = new PopupOverlay() {
					@Override
					public void onTap(OverlayItem item) {
						if (leaveflag1 == true) {
							int mode = Context.MODE_PRIVATE;
							SharedPreferences.Editor editor;
							SharedPreferences CurrentLocation = getSharedPreferences(
									"Changenavi", mode);
							editor = CurrentLocation.edit();
							editor.putInt("lat", itemlat[7]);
							editor.putInt("lng", itemlng[7]);
							editor.putString("name", name[7]);
							editor.putString("title", title[7]);
							editor.commit();
							finish();
							Intent ARModeIntent = new Intent(navi.this,
									Changenavi.class);
							navi.this.startActivity(ARModeIntent);
						}

					}
				};
				mapView.getOverlays().add(popupOverlay8);
				pinOverlay8.setOnFocusChangeListener(popupOverlay8);
				break;
			case 8:
				GeoPoint mid9 = new GeoPoint(itemlat[8], itemlng[8]);
				PinOverlay pinOverlay9 = new PinOverlay(PinOverlay.PIN_VIOLET);
				pinOverlay9.addPoint(mid9, "User:" + name[8] + "\r\n" + "Log:"
						+ title[8], "Learninglog1");
				mapView.getOverlays().add(pinOverlay9);
				leaveflag1 = true;

				PopupOverlay popupOverlay9 = new PopupOverlay() {
					@Override
					public void onTap(OverlayItem item) {
						if (leaveflag1 == true) {
							int mode = Context.MODE_PRIVATE;
							SharedPreferences.Editor editor;
							SharedPreferences CurrentLocation = getSharedPreferences(
									"Changenavi", mode);
							editor = CurrentLocation.edit();
							editor.putInt("lat", itemlat[8]);
							editor.putInt("lng", itemlng[8]);
							editor.putString("name", name[8]);
							editor.putString("title", title[8]);
							editor.commit();
							finish();
							Intent ARModeIntent = new Intent(navi.this,
									Changenavi.class);
							navi.this.startActivity(ARModeIntent);
						}

					}
				};
				mapView.getOverlays().add(popupOverlay9);
				pinOverlay9.setOnFocusChangeListener(popupOverlay9);
				break;
			case 9:
				GeoPoint mid10 = new GeoPoint(itemlat[9], itemlng[9]);
				PinOverlay pinOverlay10 = new PinOverlay(PinOverlay.PIN_VIOLET);
				pinOverlay10.addPoint(mid10, "User:" + name[9] + "\r\n"
						+ "Log:" + title[9], "Learninglog1");
				mapView.getOverlays().add(pinOverlay10);
				leaveflag1 = true;

				PopupOverlay popupOverlay10 = new PopupOverlay() {
					@Override
					public void onTap(OverlayItem item) {
						if (leaveflag1 == true) {
							int mode = Context.MODE_PRIVATE;
							SharedPreferences.Editor editor;
							SharedPreferences CurrentLocation = getSharedPreferences(
									"Changenavi", mode);
							editor = CurrentLocation.edit();
							editor.putInt("lat", itemlat[9]);
							editor.putInt("lng", itemlng[9]);
							editor.putString("name", name[9]);
							editor.putString("title", title[9]);
							editor.commit();
							finish();
							Intent ARModeIntent = new Intent(navi.this,
									Changenavi.class);
							navi.this.startActivity(ARModeIntent);
						}

					}
				};
				mapView.getOverlays().add(popupOverlay10);
				pinOverlay10.setOnFocusChangeListener(popupOverlay10);
				break;

			}
		}
		// ///////////////////////////////END//////////////////////////////////////////////////////////////////////////////////////////////////////////

		setContentView(mapView);

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// if (msg.what == NotifiedMessage)
			// setlatlng();
			// else if (msg.what == LowBatteryMessage)
			// setlatlng();
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

//	class TestThread extends Thread {
//		@Override
//		public void run() {
//			// Location location = locationlistener.getLocation();
//			Location loc = locationmanager
//					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//
//			int j = 0;
//			while ((loc == null || !loc.hasAccuracy() || loc.getAccuracy() < 30)
//					&& j <= 6) {
//				j++;
//				try {
//					sleep(10);
//					// sleep(20000);
//					loc = locationmanager
//							.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//
//				} catch (Exception e) {
//
//				}
//			}
//			// if(loc.getAccuracy()<=50){
//			// handler.sendEmptyMessage(NotifiedMessage);
//			// }
//			if (loc != null) {
//				lat = loc.getLatitude();
//				lng = loc.getLongitude();
//				// speed = location.getSpeed();
//			}
//
//			handler.sendEmptyMessage(LowBatteryMessage);
//		}
//	}
//
//	public class MyLocationListener implements LocationListener {
//		private Location location;
//
//		public Location getLocation() {
//			return location;
//		}
//
//		public void setLocation(Location location) {
//			this.location = location;
//		}
//
//		@Override
//		public void onLocationChanged(Location loc) {
//			this.location = loc;
//		}
//
//		@Override
//		public void onProviderDisabled(String provider) {
//			Criteria criteria = new Criteria();
//			criteria.setAccuracy(Criteria.ACCURACY_FINE);
//			criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
//			criteria.setAltitudeRequired(true);
//			criteria.setBearingRequired(false);
//			criteria.setSpeedRequired(true);
//			criteria.setCostAllowed(false);
//
//			if (locationmanager == null)
//				locationmanager = (LocationManager) getSystemService(LOCATION_SERVICE);
//			// locationProvider = locationmanager.getBestProvider(criteria,
//			// true);
//			List<String> providers = locationmanager.getProviders(false);
//			long latest = 0;
//
//			// locationmanager.requestLocationUpdates(locationProvider,
//			// gps_circle_time, gps_circle_distance, locationlistener);
//
//		}
//
//		@Override
//		public void onProviderEnabled(String provider) {
//			Criteria criteria = new Criteria();
//			criteria.setAccuracy(Criteria.ACCURACY_FINE);
//			criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
//			criteria.setAltitudeRequired(true);
//			criteria.setBearingRequired(false);
//			criteria.setSpeedRequired(true);
//			criteria.setCostAllowed(false);
//			if (locationmanager == null)
//				locationmanager = (LocationManager) getSystemService(LOCATION_SERVICE);
//			// locationProvider = locationmanager.getBestProvider(criteria,
//			// true);
//			// locationmanager.requestLocationUpdates(locationProvider,
//			// gps_circle_time, gps_circle_distance, locationlistener);
//		}
//
//		@Override
//		public void onStatusChanged(String provider, int status, Bundle extras) {
//
//		}
//	}

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

		// 案内処理を開始
		naviController.start();
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

	private void launchGPSOptions() {
		Intent intent = new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(intent);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		finish();
	}
}
