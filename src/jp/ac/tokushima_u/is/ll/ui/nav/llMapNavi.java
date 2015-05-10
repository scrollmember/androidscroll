package jp.ac.tokushima_u.is.ll.ui.nav;

//////////////////////////////////////////////////////////////////////////////////////////////
///////////// Developed By Dr.Moushir M. El-Bishouty, The University of Tokushima, 2010 //////
//////////////////////////////////////////////////////////////////////////////////////////////

import jp.ac.tokushima_u.is.ll.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class llMapNavi extends Activity 
{
	 private ProgressDialog myMapProgress;
	 private WebView webView;
	 private Location Curlocation;
	 boolean pageLoaded;
	 private LocationManager locationManager;
	 	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        
        // GPS service
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        //locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 2, locationListener);
        Curlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);   
               
        
        
        
		Intent intent = this.getIntent();
	
		
		if(intent!=null)
		{
			double [] itemsLat= intent.getDoubleArrayExtra("itemsLat");
			double [] itemsLng= intent.getDoubleArrayExtra("itemsLng");
			String [] itemsTitle=intent.getStringArrayExtra("itemsTitle");
			int selItem=intent.getIntExtra("selItem", 0);
			
			// Load map and data
			pageLoaded=false;
			setWebViewLayout(itemsLat,itemsLng,itemsTitle,selItem);
			loadMap();
			
			
			
		}
        
 
        
	}//onCreate
	
    //----------------------------------------------------------------------   
    @Override
    protected void onPause() {
    	locationManager.removeUpdates(locationListener);
    	
       super.onPause();
    }
    
    //----------------------------------------------------------------------   
    @Override
    protected void onResume() {
    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 2, locationListener);
    	
    	super.onResume();
    }
    
    //----------------------------------------------------------------------   
    @Override
    protected void onDestroy() {
    	locationManager.removeUpdates(locationListener);   
    	webView.destroy();
    	
    	
       super.onDestroy();
    }
 
	
	private void loadMap() 
	{
        myMapProgress = ProgressDialog.show(this, "Loading Learning-Log Map","Please wait...");
        myMapProgress.setCancelable(true);
        myMapProgress.setOnCancelListener(new OnCancelListener() 
        {
            @Override
			public void onCancel(final DialogInterface dialog) 
            {
                webView.stopLoading();
            }
        });
        webView.loadUrl("file:///android_asset/map.html");
        
     }
	
	   private void setWebViewLayout(final double []itemsLat,final double []itemsLng, final String [] itemsTitle,final int selItem) 
	   {
	        //create webview
	        webView = (WebView) findViewById(R.id.webview);
	        webView.setClickable(true);
	        webView.getSettings().setJavaScriptEnabled(true);
	        webView.addJavascriptInterface(new JavaScriptInterface(), "android");
	        webView.setKeepScreenOn(true);
	        //webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
	        //listener
	        webView.setWebViewClient(new WebViewClient() 
	        {
	        	@Override
	            public void onPageFinished(final WebView view, final String url) 
	            {	
	            	
	                overlayMap(itemsLat,itemsLng,itemsTitle,selItem);
	                String centerURL = "javascript:centerAt(" + Curlocation.getLatitude() + "," + Curlocation.getLongitude()+ ")"; 
	                webView.loadUrl(centerURL);
	                pageLoaded=true;
	                myMapProgress.dismiss();
	            }
	        });
	        
	    }
	   
       private void overlayMap(double []itemsLat,double []itemsLng,String [] itemsTitle, int selItem)
       {
           AddCurrPosMarker(Curlocation.getLatitude(),Curlocation.getLongitude());
           
           for (int i=0;i<itemsLat.length;i++)
           {
        	   AddMarker(itemsLat[i],itemsLng[i],itemsTitle[i]);
           }
 
           if (itemsLat.length>0) ShowRoute(itemsLat,itemsLng);
           
           ShowDirection(Curlocation.getLatitude(),Curlocation.getLongitude(),itemsLat[selItem],itemsLng[selItem]);
                     
       }
 
	   private void AddCurrPosMarker(double lat, double lng)
	   {
		   final String js = "javascript:AddCurrMarker(" + lat + "," + lng + ")"; 
	        webView.loadUrl(js); 
	   }
	   
	   private void AddMarker(double lat, double lng, String title)
	   {
		   final String js = "javascript:AddMarker(" + lat + "," + lng + ", '" + title + "' )"; 
	        webView.loadUrl(js); 
	   }
	   
	   private void ShowDirection(double lat1, double lng1,double lat2, double lng2)
	   {
		   final String js = "javascript:calcRoute(" + lat1 + "," + lng1 + "," + lat2 +"," + lng2 +")"; 
	        webView.loadUrl(js); 
	   }
	   
	   private void ShowRoute(double [] lat, double [] lng)
	   {	// Show the route of a set of items
		   String wayPtsLat=null;
		   String wayPtsLng=null;
		   if ((lat!=null)&&(lng!=null))
		   {   wayPtsLat="[";
			   wayPtsLng="[";
			   for (int i=0;i<lat.length;i++)
			   {
				   wayPtsLat+=lat[i];
				   wayPtsLng+=lng[i];
				   if (i<lat.length-1) 
			       {	
					   	wayPtsLat+=",";
			       		wayPtsLng+=",";
			       }
			   }
			   wayPtsLat+="]";
			   wayPtsLng+="]";
		   }
			   
		   final String js = "javascript:calcRouteWayPts(" + wayPtsLat +"," + wayPtsLng +")"; 
	       webView.loadUrl(js); 
	   }
	   
	 
	   
	   private void updateMyCurrentLocation(Location location) 
		{	
		   Curlocation=location;
		   
           if (pageLoaded) 
           {	String js = "javascript:centerAt(" + Curlocation.getLatitude() + "," + Curlocation.getLongitude()+ ")"; 
        	   	webView.loadUrl(js);
           		js = "javascript:MoveCurrMarker(" + Curlocation.getLatitude() + "," + Curlocation.getLongitude()+ ")";
           		webView.loadUrl(js);
           }
		}
	   
	   private final LocationListener locationListener = new LocationListener() {
	 	      @Override
			public void onLocationChanged(Location location) {
	 	    	  
	 	    	 if (location != null) 
	 	    	 {
	 	    		 if ((location.hasAccuracy())&&(location.getAccuracy()<20))
	 	    		 {	
	 	    			 updateMyCurrentLocation(location);
	 	    		 }
	 	    	 }	 
	 	      }
	 	     
	 	      @Override
			public void onProviderDisabled(String provider){
	 	    	  updateMyCurrentLocation(null);
	 	      }

	 	      @Override
			public void onProviderEnabled(String provider) {}

	 	      @Override
			public void onStatusChanged(String provider, int status, Bundle extras) {}
	 	    };
	 	    
	 	   
////////////////////jaba interface ///////////////////////////////////////
	 	    
	 	    private class JavaScriptInterface { 
	 		  public double getLatitude(){ 
	 		    return Curlocation.getLatitude(); 
	 		  } 
	 		  public double getLongitude(){ 
	 		    return Curlocation.getLongitude(); 
	 		  } 
	 		}
	 	 
		
} // Class llMapNavi

