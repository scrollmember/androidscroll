package jp.ac.tokushima_u.is.ll.ui.nav;

/**
 * 
 * @author Kousuke Mouri University of Tokushima,Japan
 * 
 */
import java.util.Calendar;
import java.util.List;

import jp.ac.tokushima_u.is.ll.R;


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
import android.app.AlertDialog;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class Changenavi extends MapActivity implements RouteOverlayListener,
		NaviControllerListener, View.OnClickListener {
	MapView mapView;
	private double lat, lng;
	int local_lat, local_lng;
	boolean flag1;
	boolean leaveflag1, leaveflag2;
	private Button button;
	public Context context;
	private LocationManager locationmanager;
	private MyLocationListener locationlistener = new MyLocationListener();
	Location location;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LocationManager locationmanager = (LocationManager) this
				.getSystemService(LOCATION_SERVICE);
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

		int i;

		SharedPreferences DATA = getSharedPreferences("DATALATANDLNG",
				Activity.MODE_PRIVATE);
		int count = DATA.getInt("COUNT", 0);
		int[] itemlat = new int[count];
		int[] itemlng = new int[count];
		String[] name = new String[count];
		String[] title = new String[count];
		for (i = 0; i < count; i++) {

			itemlat[i] = (int) (DATA.getFloat("lat" + i, 0) * 1000000);
			itemlng[i] = (int) (DATA.getFloat("lng" + i, 0) * 1000000);
			name[i] = DATA.getString("name" + i, "");
			title[i] = DATA.getString("title" + i, "");

		}

		// YOLPでの緯度経度取得
		if (lat != 0 && lng != 0) {
			local_lat = (int) (lat * 1000000);
			local_lng = (int) (lng * 1000000);
			flag1 = true;
		}
		// YOLPのMAP設定
		mapView = new MapView(this,
				"n5a596uxg65hFrFlsReD20KjfDpg2qhCcpCmnvOiM7s0aZoAHU0TSiicjBHMbmzzpmMrWyhxFFE-");
		MapController c = mapView.getMapController();

		// GPS取得が成功した場合自身の位置を返す
		if (flag1 == true) {
			c.setCenter(new GeoPoint(local_lat, local_lng)); // 初期表示の地図を指定
			flag1 = true;
		} else {
			c.setCenter(new GeoPoint(34078736, 134568919)); // 初期表示の地図を指定
		}

		mapView.setBuiltInZoomControls(true);
		c.setZoom(5);
		// RouteOverlay作成
		RouteOverlay routeOverlay = new RouteOverlay(this,
				"n5a596uxg65hFrFlsReD20KjfDpg2qhCcpCmnvOiM7s0aZoAHU0TSiicjBHMbmzzpmMrWyhxFFE-");

		// 出発地ピンの吹き出し設定
		routeOverlay.setStartTitle("Your current position");
		SharedPreferences Change = getSharedPreferences("Changenavi",
				Activity.MODE_PRIVATE);
		int lat = Change.getInt("lat", 0);
		int lng = Change.getInt("lng", 0);
		String change_name = Change.getString("name", "");
		String change_title = Change.getString("title", "");
		// 目的地ピンの吹き出し設定
		routeOverlay.setGoalTitle("User:" + change_name + "\r\n" + "Log:"
				+ change_title);

		// 出発地、目的地、移動手段を設定
		// routeOverlay.setRoutePos(new GeoPoint(34078736,134568919), new
		// GeoPoint(34078860,134562199), RouteOverlay.TRAFFIC_WALK);
		routeOverlay.setRoutePos(new GeoPoint(local_lat, local_lng),
				new GeoPoint(lat, lng), RouteOverlay.TRAFFIC_WALK);
		// RouteOverlayListenerの設定
		routeOverlay.setRouteOverlayListener(this);

		// 検索を開始
		routeOverlay.search();

		// MapViewにRouteOverlayを追加
		mapView.getOverlays().add(routeOverlay);
		routeOverlay.setRoutePinVisible(false);

		GeoPoint mid = new GeoPoint(itemlat[0], itemlng[0]);

		PopupOverlay popupOverlay = new PopupOverlay() {
			@Override
			public void onTap(OverlayItem item) {
				if (leaveflag1 == true) {
					// Intent ARModeIntent = new Intent(Changenavi.this,
					// Changenavi.class);
					// Changenavi.this.startActivity(ARModeIntent);
				}

			}
		};
		button = new Button(this);
		button.setText("AR Navigator");
		button.setOnClickListener(this);
		setLLParams(button);
		mapView.addView(button);
		setContentView(mapView);
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

	public void onClick(View view) {
		if (view == button) {
			Intent ARModeIntent = new Intent(Changenavi.this,
					MainActivity.class);
			Changenavi.this.startActivity(ARModeIntent);

		}

	}

	private static void setLLParams(View view) {
		view.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	// ルート検索が正常に終了した場合
	public boolean finishRouteSearch(RouteOverlay routeOverlay) {
		// NaviControllerを作成しRouteOverlayインスタンスを設定
		NaviController naviController = new NaviController(this, routeOverlay);

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
}