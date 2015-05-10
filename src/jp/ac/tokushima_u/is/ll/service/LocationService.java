package jp.ac.tokushima_u.is.ll.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

public class LocationService extends Service {
	private LocationManager locationmanager;
	
	private static final int gps_circle_time = 5 * 60 * 1000;
	private static final int gps_circle_distance = 10;
	
	private MyLocationListener locationlistener = new MyLocationListener();
	@Override
	public void onCreate() {
		super.onCreate();
		this.locationmanager = (LocationManager) this
				.getSystemService(LOCATION_SERVICE);
		locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				gps_circle_time, gps_circle_distance, locationlistener);
//		this.startService(service);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
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
			
//			if (loc != null) 
//	        {
//	     	      	
//	        	//if (true)
//	        	if ((loc.hasAccuracy())&&(loc.getAccuracy()<=30))     	
//	        	{	// Update the current location in the SharedPreferences
//	        		int mode = Activity.MODE_PRIVATE;
//	        		SharedPreferences.Editor editor;
//	        		SharedPreferences CurrentLocation = context.getSharedPreferences("CurrentLocation",mode);  		
//	    			editor = CurrentLocation.edit();
//	    			editor.putFloat("lat",(float)loc.getLatitude());
//	    			editor.putFloat("lng", (float)loc.getLongitude());
//	    			Log.v("LocationChanged","LocationChanged");
//	    			editor.commit();
//	    		}
//	        }
			
		}

		@Override
		public void onProviderDisabled(String provider) {
//			Criteria criteria = new Criteria();
//			criteria.setAccuracy(Criteria.ACCURACY_FINE);
//			criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
//			criteria.setAltitudeRequired(true);
//			criteria.setBearingRequired(false);
//			criteria.setSpeedRequired(true);
//			criteria.setCostAllowed(true);
//			
//			if(locationmanager==null)
//				locationmanager = (LocationManager)getSystemService(LOCATION_SERVICE);
//			locationmanager.
		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {

		}
	}

}
