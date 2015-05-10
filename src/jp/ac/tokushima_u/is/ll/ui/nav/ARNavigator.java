package jp.ac.tokushima_u.is.ll.ui.nav;
/**
 * 
 * @author Kousuke Mouri University of Tokushima,Japan
 * 
 */
import java.util.List;
import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Items;

import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler.AsyncQueryListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.util.Log;

public class ARNavigator extends Activity implements AsyncQueryListener{
	String userEmail = null;
	String userPassword = null;
	private double lat, lng;
	private NotifyingAsyncQueryHandler mHandler;
	
	
	ProgressDialog progressDialog;
	int DIALOG_DISPLAY_LENGHT = 20000;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// ProgressDialogインスタンスを生成
		progressDialog = new ProgressDialog(this);
		
		// プログレススタイルを設定
		progressDialog.setProgressStyle(
				ProgressDialog.STYLE_SPINNER
		);
		
		// キャンセル可能に設定
		progressDialog.setCancelable(true);
		
		// タイトルを設定
		progressDialog.setTitle( "LLO取得中" );
		
		// メッセージを設定
		progressDialog.setMessage( "しばらくお待ちください" );
		
		// ダイアログを表示
		progressDialog.show();
		
		// 3秒後にダイアログを消す
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				progressDialog.dismiss();
			}
		}, DIALOG_DISPLAY_LENGHT);

		LocationManager locationmanager = (LocationManager) this
		.getSystemService(LOCATION_SERVICE);
List<String> providers = locationmanager.getProviders(false);
long latest = 0;
for (String provider : providers) {
	Location location = locationmanager.getLastKnownLocation(provider);
	if (location != null) {
	
		float accuracy = location.getAccuracy();
		if (accuracy <= 100) {
		
			lat = location.getLatitude();
			lng = location.getLongitude();
		}
		if (latest != 0) {
			if (location.getTime() > latest)
				latest = location.getTime();
		} else
			latest = location.getTime();
	}

	// compare the location by time and accuracy
}

Uri itemsUri;
String[] projection;
projection = ItemsQuery.PROJECTION;
itemsUri = Items.buildItemSearchUri("geo:" + lat + "," + lng);
mHandler = new NotifyingAsyncQueryHandler(getContentResolver(), this);	
mHandler.startQuery(itemsUri, projection, Items.DISABLED + "!=?",
		new String[] { "1" }, Items.DEFAULT_SORT);


	}
	
	@Override
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		// TODO 自動生成されたメソッド・スタブ
		int i1 = 0;
		int i;
		Log.d("count", "" + cursor.getCount());
		double[] lat = new double[cursor.getCount()];
		double[] lng = new double[cursor.getCount()];
		String[] name = new String[cursor.getCount()];
		String[] title = new String[cursor.getCount()];

		try {
			cursor.moveToFirst();
			while (cursor.moveToNext()) {
				if(i1<cursor.getCount()){
				lat[i1] = cursor.getDouble(7);
				lng[i1] = cursor.getDouble(8);
				name[i1] = cursor.getString(2);
				title[i1] = cursor.getString(5);
				int mode = Context.MODE_PRIVATE;
				SharedPreferences.Editor editor;
				SharedPreferences CurrentLocation = getSharedPreferences(
						"ARDATA", mode);
				editor = CurrentLocation.edit();
				editor.putFloat("lat" + i1, (float) lat[i1]);
				editor.putFloat("lng" + i1, (float) lng[i1]);
				editor.putString("name" + i1, name[i1]);
				editor.putString("title" + i1, title[i1]);
				editor.putInt("COUNT", i1);
				editor.commit();
				i1++;
				}
				else{
					break;
				}
			}

		} finally {
			cursor.close();
			Intent QRModeIntent = new Intent(ARNavigator.this,
					ObjectNavdata.class);
			ARNavigator.this.startActivity(QRModeIntent);
		}

	}

	// ItemQuery
	private interface ItemsQuery {
		String[] PROJECTION = { BaseColumns._ID, Items.ITEM_ID,
				Items.NICK_NAME, Items.PHOTO_URL, Items.NOTE, Items.TITLES,
				Items.UPDATE_TIME, Items.LATITUTE, Items.LNGITUTE };

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