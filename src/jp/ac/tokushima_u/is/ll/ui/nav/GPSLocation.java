package jp.ac.tokushima_u.is.ll.ui.nav;

/**
 * 
 * @author Kousuke Mouri University of Tokushima,Japan
 * 
 */
import java.lang.Math;

import jp.ac.tokushima_u.is.ll.ui.navTask.CameraTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;


class GPSLocation extends CameraNav {

	LocationManager mLocationManager;
	LocationListener mLocationListener;
	SensorManager mSensorManager;
	float minLocationDiStance = 2f; // the change rate in meters
	// 追加
	double startLatitude;
	double startLongitude;
	double endLatitude;
	double endLongitude;
	double centerx = 0, centery = 0;
	public float deviceDegreeToNorth;
	public float predeviceDegreeToNorth = -1f;
	Compass compass;
	Button Button12, Button21, Button23, Button32;
	AlertDialog.Builder adB12, adB21, adB23, adB32;
	DrawOnTop mCompassView;
	Context mContext;
	String DeviceNorthItem, DeviceEastItem, DeviceSouthItem, DeviceWestItem;

	public float CenterX;
	public float CenterY;
	public float radius;
	public int gridWidth;
	public int gridHeight;

	// DrawOnTop drawtop;
	// カメラナビの場所を取得するためのメソッド

	GPSLocation(CameraNav Nav) {

		mLocationManager = (LocationManager) Nav
				.getSystemService(Context.LOCATION_SERVICE);

		mLocationListener = new MyLocationListener(Nav, minLocationDiStance);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				10, 0, mLocationListener);

	}

	GPSLocation(LightNav Nav) {

		// ---use the LocationManager class to obtain GPS locations---
		mLocationManager = (LocationManager) Nav
				.getSystemService(Context.LOCATION_SERVICE);
		mLocationListener = new MyLocationListener(Nav, minLocationDiStance);

	}

	void OpenGPSListener() {
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				0, minLocationDiStance, mLocationListener);
	}

	void CloseGPS() {
		mLocationManager.removeUpdates(mLocationListener);
	}
}; // GPSLocation

// ///////////////////////////////////////////////////////////////////////
// ------------------- Location Listener Class ---------------------------
// ///////////////////////////////////////////////////////////////////////
class MyLocationListener implements LocationListener {
	public CameraNav CNav;
	public LightNav LNav;
	public Context context;
	public DrawOnTop mDraw;
	public Compass compass;
	// public GetObjectsTask getobject;
	public float MinDis;
	public float e;
	double[] latitute;
	double[] lngitute;
	int count;
	float gpsdistance[];
	float houkou[];

	MyLocationListener(CameraNav Nav, float minDiStance) {
		this.CNav = Nav;
		context = CNav.getApplicationContext();
		mDraw = new DrawOnTop(Nav);
		Nav.addContentView(mDraw, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		mDraw.text("GPS:Weak", Color.RED);
		MinDis = minDiStance;
	}

	MyLocationListener(LightNav Nav, float minDiStance) {
		this.LNav = Nav;
		context = LNav.getApplicationContext();
		mDraw = new DrawOnTop(Nav);
		Nav.addContentView(mDraw, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		mDraw.text("GPS:Weak", Color.WHITE);
		MinDis = minDiStance;
	}

	@Override
	// 位置情報が更新された場合
	public void onLocationChanged(Location loc) {
		double startLatitude = loc.getLatitude();
		double startLongitude = loc.getLongitude();
		float[] results = { 0, 0, 0 };
		int i;

		SharedPreferences DATA = context.getSharedPreferences("DATALATANDLNG",
				Context.MODE_PRIVATE);
		int count = DATA.getInt("COUNT", 0);
		float[] itemlat = new float[count];
		float[] itemlng = new float[count];
		float[] gpsdistance = new float[count];
		float[] houkou = new float[count];
		for (i = 0; i < count; i++) {
			if (i < 10) {
				itemlat[i] = DATA.getFloat("lat" + i, 0);
				itemlng[i] = DATA.getFloat("lng" + i, 0);

				Location.distanceBetween(startLatitude, startLongitude,
						itemlat[i], itemlng[i], results);
				gpsdistance[i] = results[0];
				houkou[i] = results[1];
				SharedPreferences.Editor editor;
				SharedPreferences gpsdata = context.getSharedPreferences(
						"GPSDATA", Context.MODE_PRIVATE);
				editor = gpsdata.edit();
				editor.putFloat("lat" + i, itemlng[i]);
				editor.putFloat("lng" + i, itemlng[i]);
				editor.putFloat("gpsdistance" + i, gpsdistance[i]);
				editor.putFloat("cos" + i, (float) Math.cos(houkou[i]));
				editor.putFloat("sin" + i, (float) Math.sin(houkou[i]));
				editor.putInt("count", i);
				Log.v("LocationChanged", "LocationChanged");
				editor.commit();
			} else {
				break;
			}
		}

		if (loc != null) {
			if ((loc.hasAccuracy()) && (loc.getAccuracy() <= 30)) {
				mDraw.text("GPS:Fine ", Color.GREEN);
				int mode = Context.MODE_PRIVATE;
				for (i = 0; i <= count; i++) {
					if (i < 10) {
						SharedPreferences.Editor editor;
						SharedPreferences CurrentLocation = context
								.getSharedPreferences("CurrentLocation", mode);
						editor = CurrentLocation.edit();
						editor.putFloat("lat", (float) loc.getLatitude());
						editor.putFloat("lng", (float) loc.getLongitude());
						Log.v("LocationChanged", "LocationChanged");
						editor.commit();
					} else {
						break;
					}
				}
				if (CNav != null) {
					CNav.lat = loc.getLatitude();
					CNav.lng = loc.getLongitude();
					// Run GetObjectsThread after certain time
					// CNav.queueUpdate(2000); // in millisecond
				} else if (LNav != null) {
					LNav.lat = loc.getLatitude();
					LNav.lng = loc.getLongitude();
					// Run GetObjectsThread after certain time
					// LNav.queueUpdate(2000); // in millisecond
				}
			} else {
				// Show overlay GPS receiving notification
				mDraw.text("GPS:Weak", Color.WHITE);

			}
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		mDraw.text("", Color.WHITE);
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}// end MyLocationListener

// ///////////////////////////////////////////////////////////////////////
// ------------------- Display GPS data Layer ---------------------------
// ///////////////////////////////////////////////////////////////////////

class DrawOnTop extends View {
	String mtext = "";
	Paint paint = new Paint();
	RectF rect;
	// 追加
	float kyori;
	float houkou;
	float deg;
	public Compass compass;
	public GPSLocation gpslocation;

	int color;
	public int line = 0;

	public DrawOnTop(Context context) {
		super(context);
		SharedPreferences screen = context.getSharedPreferences("screen",
				Context.MODE_PRIVATE);
		if (screen.getInt("height", 0) != 0)
			line = screen.getInt("height", 0) - 80;

	}

	public void text(String _text, int c) {
		mtext = _text;
		color = c;
		invalidate();
	}

	public void latdate(float latd, float hougaku) {
		kyori = latd;
		houkou = hougaku;
		invalidate();
	}

	public void data1(float deg2) {
		deg = deg2;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		paint.setStyle(Paint.Style.FILL);
		paint.setTextSize(20);
		paint.setColor(color);
		// HTC Magic
		canvas.drawText(mtext, 10, line, paint);
		// 追加
		// kyori=10;
		int x = getWidth() / 2;
		int y = getHeight() / 2;

		super.onDraw(canvas);
	}
}
