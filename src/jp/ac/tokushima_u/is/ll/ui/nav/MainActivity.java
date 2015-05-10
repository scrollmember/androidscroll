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
import jp.co.yahoo.android.maps.ar.ARController;
import jp.co.yahoo.android.maps.ar.ARControllerListener;
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
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

public class MainActivity extends MapActivity implements RouteOverlayListener,
		NaviControllerListener, ARControllerListener {
	MapView mapView;
	ARController arController;
	NaviController naviController;
	private double lat, lng;
	int local_lat,local_lng;
	boolean flag1;
	Location location;
	private LocationManager locationmanager;
	
	private MyLocationListener locationlistener = new MyLocationListener();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		location=null;
		// locationmanagerを取得するためにはContext.getSystemService(LoCATION_SERVICE)を呼び出す
		locationmanager = (LocationManager) this
				.getSystemService(LOCATION_SERVICE);
		// Android端末で確実に位置情報を取得するためには三つのプロバイダーを呼びます
		// GPSは[gps],3GおよびWi-Fiは[Network]という文字列が取得できる
		// Android2.2からプロバイダにpassiveが追加される。　このプロバイダーは使い方は不明
		List<String> providers = locationmanager.getProviders(false);
		long latest = 0;
		
		if (!locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getText(R.string.info_require_gps))
					.setCancelable(false).setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int id) {
									launchGPSOptions();
								}
							}).setNegativeButton("No",
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
		
		if (locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationlistener);
			location = locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if(location!=null){
				lat = location.getLatitude();
				lng = location.getLongitude();
				// 経度取得
			}
		}else{
			for (String provider : providers) {
				location = locationmanager.getLastKnownLocation(provider);
				
				if (location != null) {
					// getAccuracy()メッソドで精度を取得する
					float accuracy = location.getAccuracy();
					if (accuracy <= 70) {
						if((Calendar.getInstance().getTimeInMillis()-location.getTime())<10*60*1000){
							// 緯度取得
							lat = location.getLatitude();
							// 経度取得
							lng = location.getLongitude();
							break;
						}else
							continue;
					}
//				if (latest != 0) {
//					if (location.getTime() > latest)
//						latest = location.getTime();
//				} else
//					latest = location.getTime();
				}
			}
		}
		
		
		// YOLPでの緯度経度取得
		if (lat != 0 && lng != 0) {
			local_lat = (int) (lat * 1000000);
			local_lng = (int) (lng * 1000000);
			flag1 = true;
		}
		
		
		
		
		mapView = new MapView(this,
				"Wk0I8NSxg66.V5Z1yRV4U0K7nMNOjF2ohBpI0CSpUxzQUaC86I.LhQkj3KGOphgnRA4IwsUYoGM-");
		MapController c = mapView.getMapController();
		c.setCenter(new GeoPoint(local_lat,local_lng)); // 初期表示の地図を指定
		c.setZoom(8);
		// RouteOverlay作成
		RouteOverlay routeOverlay = new RouteOverlay(this,
				"Wk0I8NSxg66.V5Z1yRV4U0K7nMNOjF2ohBpI0CSpUxzQUaC86I.LhQkj3KGOphgnRA4IwsUYoGM-");

		// 出発地ピンの吹き出し設定
		routeOverlay.setStartTitle("Your Current Position");

		// 目的地ピンの吹き出し設定
		routeOverlay.setGoalTitle("");

		// 出発地、目的地、移動手段を設定
		// routeOverlay.setRoutePos(new GeoPoint(34078736,134568919), new
		// GeoPoint(34078860,134562199), RouteOverlay.TRAFFIC_WALK);
		// routeOverlay.setRoutePos(new GeoPoint(34078884,134562172), new
		// GeoPoint(33886306,134638996), RouteOverlay.TRAFFIC_WALK);
		SharedPreferences Change = getSharedPreferences("Changenavi",
				Activity.MODE_PRIVATE);
		 int Target_lat=Change.getInt("lat", 0);
		 int Target_lng= Change.getInt("lng", 0);
		
		routeOverlay.setRoutePos(new GeoPoint(local_lat,local_lng),
				new GeoPoint(Target_lat,Target_lng), RouteOverlay.TRAFFIC_WALK);
		// RouteOverlayListenerの設定
		routeOverlay.setRouteOverlayListener(this);

		// 検索を開始
		routeOverlay.search();

		// MapViewにRouteOverlayを追加
		mapView.getOverlays().add(routeOverlay);
		routeOverlay.setRoutePinVisible(false);

//		setContentView(mapView);

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

		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		this.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// ARControllerインスタンス作成
		arController = new ARController(this, this);

		// ARControllerをNaviControllerに設定
		naviController.setARController(arController);

		// Drawable pin = getResources().getDrawable(R.drawable.icon);

		// arController.addPOI(34.078884,134.562172,pin, 50, 100);
		if(lat != 0 && lng != 0){
			arController.setCurrentPos(lat,lng, 0, 20);
		}
		else{
			arController.setCurrentPos(34.078884, 134.562172, 0, 20);
		}
		// 案内処理を開始
		naviController.start();
		return false;
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
			
			if(locationmanager==null)
				locationmanager = (LocationManager)getSystemService(LOCATION_SERVICE);
			//locationProvider = locationmanager.getBestProvider(criteria, true);
			List<String> providers = locationmanager.getProviders(false);
			long latest = 0;

//			locationmanager.requestLocationUpdates(locationProvider, gps_circle_time, gps_circle_distance, locationlistener);
			
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
			if(locationmanager==null)
				locationmanager = (LocationManager)getSystemService(LOCATION_SERVICE);
//			locationProvider = locationmanager.getBestProvider(criteria, true);
//			locationmanager.requestLocationUpdates(locationProvider, gps_circle_time, gps_circle_distance, locationlistener);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

			
		}
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

		// arController.setCurrentPos(location.getLatitude(),location.getLongitude(),location.getAltitude(),20);
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
		arController.onPause();
		naviController.stop();
		naviController.setARController(null);
		return false;
	}

	public void ARControllerListenerOnPOIPick(int index) {

		// if(index==2){
		// Intent QRModeIntent = new Intent(MainActivity.this,
		// navi.class);
		// MainActivity.this.startActivity(QRModeIntent);
		// finish();
		// }

	}
	private void launchGPSOptions() {
		Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS); 
        startActivity(intent); 
	}
}