package jp.ac.tokushima_u.is.ll.ui;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.R.drawable;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Items;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.BitmapUtil;
import jp.ac.tokushima_u.is.ll.util.Lists;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler;
import jp.ac.tokushima_u.is.ll.util.UIUtils;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler.AsyncQueryListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

public class MapTestActivity extends MapActivity implements AsyncQueryListener{

	private final String tag = "MapTestActivity";

	private MyLocationListener locationlistener = new MyLocationListener();
	private LocationManager locationmanager;
	private static final int gps_circle_time = 5 * 60 * 1000;
	private static final int gps_circle_distance = 10;
	private CustomOverlay markers;

	private MapView mapView;
	private CursorAdapter mAdapter;


	private NotifyingAsyncQueryHandler mHandler;
	private Handler mMessageQueueHandler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = getIntent();
		Uri itemsUri;

		this.locationmanager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
		locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER,gps_circle_time, gps_circle_distance, locationlistener);

		double lat = 0.0;
		double lng = 0.0;
		double speed = 0.0;

		Location location = this.locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (location != null) {
				lat = location.getLatitude();
				lng = location.getLongitude();
				speed = location.getSpeed();
		}

		//setContentView(R.layout.activity_maptest);

		mapView = new MapView(this, getResources().getString(R.string.map_key));

		mapView.setEnabled(true);
		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);

		final MyLocationOverlay overlay = new MyLocationOverlay(getApplicationContext(),mapView);
		overlay.onProviderEnabled(LocationManager.GPS_PROVIDER); // GPS を使用する
		overlay.enableMyLocation();

		Drawable marker = getResources().getDrawable(R.drawable.ic_title_map);
		markers = new CustomOverlay(marker,this);

		mapView.getOverlays().add(overlay);
		mapView.getOverlays().add(markers);

		mapView.invalidate();
        setContentView(mapView);

		itemsUri = Items.buildItemSearchUri("geo:" + lat + ","+ lng);
		
		String[] projection;
        projection = ItemsQuery.PROJECTION;

        mHandler = new NotifyingAsyncQueryHandler(getContentResolver(), this);
//      mHandler.startQuery(sessionsUri, projection, Items.DEFAULT_SORT);
        mHandler.startQuery(itemsUri, projection, Items.DISABLED+"!=?", new String[]{"1"}, Items.DEFAULT_SORT);
	}

	public void showDialog(Drawable d,String t){
		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		LayoutInflater factory = LayoutInflater.from(this);
		final View customDialogView = factory.inflate(R.layout.custom_dialog, null);
		alertDialogBuilder.setView(customDialogView);
		alertDialogBuilder.setTitle(t);
		ImageView image = (ImageView) customDialogView.findViewById(R.id.custom_daialog_image);
		if(d!=null)
			image.setImageDrawable(d);
		else
			image.setImageResource(R.drawable.noimage);
		alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				  dialog.cancel();
				}
			});
		alertDialogBuilder.create().show();
	}


	@Override
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		// TODO 自動生成されたメソッド・スタブ

		int i,j;
		cursor.moveToFirst();
		if(cursor.getCount() < 30)
			j = cursor.getCount();
		else
			j = 30;

		for(i = 0;i < j;i++){
			if(cursor.getString(3) != null)
				markers.setPhotoUrl(cursor.getString(3));
			else
				markers.setPhotoUrl();
			markers.setTitle(cursor.getString(5));
			GeoPoint point = new GeoPoint((int)(cursor.getDouble(7) * 1000000),(int)(cursor.getDouble(8) * 1000000));
			addPointMarkerUpdateMap(point,cursor.getString(5));
			cursor.moveToNext();
		}
		cursor.close();
	}



	@Override
	protected boolean isRouteDisplayed() {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	private void addPointMarkerUpdateMap(GeoPoint point, String addressName) {
		  markers.addNewItem(point, addressName, "");
		  //mapView.invalidate();   //必須
	}

	// TODO Overlay
	private class CustomOverlay extends ItemizedOverlay<OverlayItem>{
		Context context;
		// マーカーの表示位置とメッセージを保持するオーバレイクラスのリスト
		private ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
		private ArrayList<String> photoUrl = new ArrayList<String>();
		private ArrayList<String> titles = new ArrayList<String>();

		public CustomOverlay(Drawable defaultMarker,Context context) {
			super(boundCenterBottom(defaultMarker));
			this.context = context;
			populate();
		}

		public void setPhotoUrl(String purl){
			photoUrl.add("http://ll.is.tokushima-u.ac.jp/static/learninglog/"+purl+"_320x240.png");
		}

		public void setPhotoUrl(){
			photoUrl.add(null);
		}

		public String getPhotoUrl(int index){
			return photoUrl.get(index);
		}

		public void setTitle(String title){
			titles.add(title);
		}

		public String getTitle(int index){
			return titles.get(index);
		}


		@Override
		protected OverlayItem createItem(int i) {
			return items.get(i);
		}

		@Override
		public int size() {
			return items.size();
		}

		// ユーザがマーカーをタップした時に親クラスから呼び出される
		// 今回は簡単なサンプルなのでテキストをトースト表示ようにした
		@Override
		protected boolean onTap(int index) {
			String markerText = items.get(index).getTitle();

				Drawable d = null;
				try{
					if(getPhotoUrl(index) != null){
						URL url = new URL(getPhotoUrl(index));
						HttpURLConnection http = (HttpURLConnection)url.openConnection();
						http.setRequestMethod("GET");
						http.connect();
						InputStream in = http.getInputStream();
						d = Drawable.createFromStream(in, "a");
						in.close();
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				String t = getTitle(index);
				showDialog(d,t);
		    return true;
		}

		public void addNewItem(GeoPoint point, String markerText, String snippet) {
			items.add(new OverlayItem(point, markerText, snippet));
			populate();
		}

	}

    /** {@link Sessions} query parameters. */
    private interface ItemsQuery {
        String[] PROJECTION = {
                BaseColumns._ID,
                Items.ITEM_ID,
                Items.NICK_NAME,
                Items.PHOTO_URL,
                Items.NOTE,
                Items.TITLES,
                Items.UPDATE_TIME,
                Items.LATITUTE,
                Items.LNGITUTE
        };

        int _ID = 0;
        int ITEM_ID = 1;
        int NICK_NAME = 2;
        int PHOTO_URL = 3;
        int NOTE = 4;
        int TITLES = 5;
        int UPDATE_TIME = 6;
        int LATITUTE = 7;
        int LNGITUTE = 8;
    }

}
