package jp.ac.tokushima_u.is.ll.ui.nav;
import java.util.ArrayList;
import java.util.List;

import jp.ac.tokushima_u.is.ll.R;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapController;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.ItemizedOverlay;
import android.location.Location;
import android.location.LocationManager;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.*;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import android.os.Bundle;
public class Map extends MapActivity implements LocationListener{
LocationManager mLocationManager;
MapController mMapController;
private double lat,lng;
PendingIntent pIntent;
private TouchedItemizedOverlay touchedItemizedOverlay;
TextView tv;	
@Override
public void onCreate(Bundle savedInstanceState){
	super.onCreate(savedInstanceState);
//	MapView mapView = new MapView(this,"0DtPSTNmx51DPCUJCb8G7h_UYiwMG_kJYypUkKw");
//	mapView.setEnabled(true);
//	mapView.setClickable(true);
	setContentView(R.layout.main2);

	final MapView mapview =(MapView) findViewById(R.id.mapview);
	//MapControllerの取得
	mMapController=mapview.getController();
	//コントローラの初期値
	mapview.getController().setZoom(6);
	//ズームを配置
	mapview.setBuiltInZoomControls(true);
	mapview.invalidate();
	//ロケーションマネージャの取得
	mLocationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
//	tv=(TextView)findViewById(R.id.text);
//	tv.setText("位置情報が未取得です");
	
	
	LocationManager locationmanager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
	List<String> providers = locationmanager.getProviders(false);
		long latest = 0;
		for(String provider:providers){

			Location location = locationmanager.getLastKnownLocation(provider);

			if(location!=null){
				float accuracy = location.getAccuracy();
				if(accuracy<=30){
					lat = location.getLatitude();
					lng = location.getLongitude();
				}
				if(latest!=0){
					if(location.getTime()>latest)
						latest = location.getTime();
				}else
					latest = location.getTime();
			}
		}
	
//****************************************オーバレイゾーン******************************************************************************//	
	 Drawable pin = getResources().getDrawable( R.drawable.number5_0);
	 Drawable pin1 = getResources().getDrawable( R.drawable.number5_1);
	 Drawable pin2 = getResources().getDrawable( R.drawable.number5_2);
	 Drawable pin3 = getResources().getDrawable( R.drawable.number5_3);
	 Drawable pin4 = getResources().getDrawable( R.drawable.number5_4);
	 Drawable pin5 = getResources().getDrawable( R.drawable.number5_5);
	 Drawable pin6 = getResources().getDrawable( R.drawable.number5_6);
	 Drawable pin7 = getResources().getDrawable( R.drawable.number5_7);
	 Drawable pin8 = getResources().getDrawable( R.drawable.number5_8);
	 Drawable pin9 = getResources().getDrawable( R.drawable.number5_9);
     PinItemizedOverlay pinOverlay = new PinItemizedOverlay(pin);
     PinItemizedOverlay pinOverlay1 = new PinItemizedOverlay(pin1);
     PinItemizedOverlay pinOverlay2 = new PinItemizedOverlay(pin2);
     PinItemizedOverlay pinOverlay3 = new PinItemizedOverlay(pin3);
     PinItemizedOverlay pinOverlay4 = new PinItemizedOverlay(pin4);
     PinItemizedOverlay pinOverlay5 = new PinItemizedOverlay(pin5);
     PinItemizedOverlay pinOverlay6 = new PinItemizedOverlay(pin6);
     PinItemizedOverlay pinOverlay7 = new PinItemizedOverlay(pin7);
     PinItemizedOverlay pinOverlay8 = new PinItemizedOverlay(pin8);
     PinItemizedOverlay pinOverlay9 = new PinItemizedOverlay(pin9);
     mapview.getOverlays().add(pinOverlay);
     mapview.getOverlays().add(pinOverlay1);
     mapview.getOverlays().add(pinOverlay2);
     mapview.getOverlays().add(pinOverlay3);
     mapview.getOverlays().add(pinOverlay4);
     mapview.getOverlays().add(pinOverlay5);
     mapview.getOverlays().add(pinOverlay6);
     mapview.getOverlays().add(pinOverlay7);
     mapview.getOverlays().add(pinOverlay8);
     touchedItemizedOverlay = new TouchedItemizedOverlay(pin, this);
     
     SharedPreferences DATA = getSharedPreferences("DATALATANDLNG",Context.MODE_PRIVATE);  	
		int count = DATA.getInt("COUNT",0);
		 float[] itemlat=new float[count];
	     float[] itemlng=new float[count];
	     float[] itemdata1=new float[count];
	     float[] itemdata2=new float[count];
	     float[] gpsdistance=new float[count];
	     float[] results={0,0,0};
		 //float[] houkou=new float[count];
		 int i=0;
		 int j=0;
	     for(i=0;i<count;i++){
		itemlat[i] = DATA.getFloat("lat"+i, 0);
		itemlng[i] = DATA.getFloat("lng"+i, 0);
		 Location.distanceBetween(lat,lng,itemlat[i],itemlng[i],results);
		 gpsdistance[i] =results[0];
		 if(gpsdistance[i]<300){
			 
			 itemdata1[j]=itemlat[i];
			 itemdata2[j]=itemlng[i];
			 j++;
		 }
		//gpsdistance[i] = DATA.getFloat("gpsdistance"+i,0);
	     }for(int a=0;a<count;a++){
	     itemdata1[a]=itemdata1[a]*1000000;     
	     itemdata2[a]=itemdata2[a]*1000000;
	     GeoPoint data1 = new GeoPoint((int)itemdata1[a],(int)itemdata2[a]);
	     if(a==0){
	     pinOverlay.addPoint(data1);
	     }
	     else if(a==1){ pinOverlay1.addPoint(data1); }
         else if(a==2){ pinOverlay2.addPoint(data1); }
         else if(a==3){ pinOverlay3.addPoint(data1); }
         else if(a==4){ pinOverlay4.addPoint(data1); }
         else if(a==5){ pinOverlay5.addPoint(data1); }
         else if(a==6){ pinOverlay6.addPoint(data1); }
         else if(a==7){ pinOverlay7.addPoint(data1); }
         else if(a==8){ pinOverlay8.addPoint(data1); }
         else{	pinOverlay9.addPoint(data1); }
	     
	     
	     
	     }
	 
	    
	    	
	    	 
	     
	     Intent intent=new Intent(this,Request.class);
	     pIntent=PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
	   //  for(int a=0;a<10;a++){
	   mLocationManager.addProximityAlert(itemdata1[3],itemdata2[3],1000000,-1,pIntent);
	    // }
	     
	     
	     ((MapView) findViewById(R.id.mapview)).getOverlays().add(new Overlay() {
	         @Override
	         public boolean onTap(GeoPoint data1, MapView mapView) {
	             Log.v(this.getClass().getName(), "Overlay.onTap start.");
	             super.onTap(data1, mapView);
	             String locationPoint = new StringBuilder()
	                     .append("latitude: ")
	                     .append(data1.getLatitudeE6())
	                     .append(", longitude:")
	                     .append(data1.getLongitudeE6())
	                     .toString();
	             
	           
	          //   OverlayItem overlayItem = new OverlayItem(p, locationPoint, "");
	          //   touchedItemizedOverlay.clearOverlay();
	          //   touchedItemizedOverlay.addOverlay(overlayItem);
	         //    mapView.getController().animateTo(p);
	          //   mapView.getOverlays().add(touchedItemizedOverlay);
	         //    mapView.invalidate();
	             return true;
	         }
	     });
	     
	     
	     
}
@Override
public void onPause() {
	super.onPause();
	mLocationManager.removeProximityAlert(pIntent);
}
@Override
public void onResume(){
	super.onResume();
	//位置情報更新の設定
	mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 6000,10,this);
}

@Override
 protected boolean isRouteDisplayed(){
	return false;
}
@Override
public void onLocationChanged(Location location){
	GeoPoint gp=new GeoPoint((int)(location.getLatitude()*1E6),(int)(location.getLongitude()*1E6));
	double lat=gp.getLatitudeE6()/1E6;
	double lng=gp.getLongitudeE6()/1E6;
	//地図の中心に移動
	mMapController.animateTo(gp);

	//Bitmap icon=BitmapFactory.decodeResource(getResources(), R.drawable.favicon);
	//AddOverlay overlay=new AddOverlay(icon,gp);
	//MapView mapview=(MapView)findViewById(R.id.mapview);
	//List<Overlay> overlays=mapview.getOverlays();
	//overlays.add(overlay);
	//mapview.invalidate();
}

public class PinItemizedOverlay extends ItemizedOverlay<PinOverlayItem> {

    private List<GeoPoint> points = new ArrayList<GeoPoint>();
    Context context;
    public PinItemizedOverlay(Drawable defaultMarker) {
        super( boundCenterBottom(defaultMarker) );
        this.context = context;
        populate();
    }
    @Override
    protected boolean onTap(int index) {
        super.onTap(index);
        String title = getItem(index).getTitle();
        Toast.makeText(context, title, 3).show();
        return true;
    }
    
    
    @Override
    protected PinOverlayItem createItem(int i) {
        GeoPoint point = points.get(i);
        return new PinOverlayItem(point);
    }

    @Override
    public int size() {
        return points.size();
    }

    public void addPoint(GeoPoint point) {
        this.points.add(point);
        populate();
    }
	
    public void clearPoint() {
        this.points.clear();
        populate();
    }
}
public class PinOverlayItem extends OverlayItem {

    public PinOverlayItem(GeoPoint point){
        super(point, "", "");
    }
}


class TouchedItemizedOverlay extends ItemizedOverlay {
	
	// マーカーの表示位置とメッセージを保持するオーバレイクラスのリスト
    private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
    Context context;
    public TouchedItemizedOverlay(Drawable defaultMarker, Context context) {
        super(boundCenterBottom(defaultMarker));   //必須

        this.context = context;
    }
    
    // ユーザがマーカーをタップした時に親クラスから呼び出される

    @Override
    protected boolean onTap(int index) {
        super.onTap(index);
        String title = getItem(index).getTitle();
        Toast.makeText(context, title, 3).show();
        return true;
    }
    @Override
    protected OverlayItem createItem(int i) {
        return overlays.get(i);
    }
    @Override
    public int size() {
        return overlays.size();
    }
    void addOverlay(OverlayItem overlay) {
        overlays.add(overlay);
        populate();
    }
    void clearOverlay() {
        overlays.clear();
        populate();
    }
}

@Override
public void onProviderDisabled(String provider){
	
}
@Override
public void onProviderEnabled(String provider){
	
}

@Override
public void onStatusChanged(String provider,int status,Bundle extras){
	
}

public void onGetCenter(View view){
	Intent ObjectNav = new Intent(this, ObjectNavdata.class);
	this.startActivity(ObjectNav);
	
	
}

/** {@link Sessions} query parameters. */

//protected void CreateMenuLayer()
//{	
//	
//	// Create Overlay Menu
//	final LinearLayout menuLayout;
//	menuLayout = new LinearLayout(this);
//	menuLayout.setOrientation(LinearLayout.VERTICAL);
//	
//	// Create menu buttons
//	Button ExitButton = new Button(this); 
//    ExitButton.setText("   Exit  " ); 
//    
//    //レイアウトの追加
//   	menuLayout.addView(ExitButton);
//    addContentView(menuLayout, new LayoutParams (LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//    menuLayout.setVisibility(LinearLayout.INVISIBLE); // Hide it
//    
//
//    // Menu buttons Lister
//    ExitButton.setOnClickListener(new OnClickListener() 
//    {  @Override
//        public void onClick(View v)     	
//        {   
//            	finish();     	
//        };   
//    });
//
//}

}


