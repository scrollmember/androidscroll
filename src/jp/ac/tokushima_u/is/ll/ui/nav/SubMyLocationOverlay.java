package jp.ac.tokushima_u.is.ll.ui.nav;

import jp.co.yahoo.android.maps.GeoPoint;
import jp.co.yahoo.android.maps.MapView;
import jp.co.yahoo.android.maps.MyLocationOverlay;
import android.app.Activity;
import android.content.Context;

public class SubMyLocationOverlay extends MyLocationOverlay {
	 
    MapView _mapView = null;
    Activity _activity = null;
 
    public SubMyLocationOverlay(Context context, MapView mapView, Activity activity) {
        super(context, mapView);
 
        _mapView = mapView;
        _activity = activity;
        
        
    }
    
 
    //現在地更新のリスナーイベント
    @Override
    public void onLocationChanged(android.location.Location location) {
        super.onLocationChanged(location);
 
        if (_mapView.getMapController() != null) {
            //位置が更新されると地図の位置も変える。
        	if(location.getLatitude()!=0){
            GeoPoint p = new GeoPoint((int) (location.getLatitude() * 1E6),(int) (location.getLongitude() * 1E6));
            _mapView.getMapController().animateTo(p);
            _mapView.invalidate();
        }
        	else{
        		
        		//34078884, 134562172
        		
        		GeoPoint p = new GeoPoint((int) (34.078884 * 1E6),(int) (134.562172 * 1E6));
        		
        		 _mapView.getMapController().animateTo(p);
                 _mapView.invalidate();
        	}
    }
}}
