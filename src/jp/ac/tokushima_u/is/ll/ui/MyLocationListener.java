package jp.ac.tokushima_u.is.ll.ui;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class MyLocationListener implements LocationListener {
	private Location location;
	
	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	@Override
	public void onLocationChanged(Location location) {
		this.location = location;
	}

	@Override
	public void onProviderDisabled(String provider) {
//		Criteria criteria = new Criteria();
//		criteria.setAccuracy(Criteria.ACCURACY_FINE);
//		criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
//		criteria.setAltitudeRequired(true);
//		criteria.setBearingRequired(false);
//		criteria.setSpeedRequired(true);
//		criteria.setCostAllowed(true);
//		
//		if(locationmanager==null)
//			locationmanager = (LocationManager)getSystemService(LOCATION_SERVICE);
//		locationmanager.
	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {

	}
}
