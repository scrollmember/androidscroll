package jp.ac.tokushima_u.is.ll.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Answers;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemcomments;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Items;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemtags;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemtitles;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Languages;
import jp.ac.tokushima_u.is.ll.ui.media.AudioPlayer;
import jp.ac.tokushima_u.is.ll.ui.media.ShowPhoto;
import jp.ac.tokushima_u.is.ll.ui.media.VideoPlayer;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.AudioUtil;
import jp.ac.tokushima_u.is.ll.util.BitmapUtil;
import jp.ac.tokushima_u.is.ll.util.Constants;
import jp.ac.tokushima_u.is.ll.util.ContextUtil;
import jp.ac.tokushima_u.is.ll.util.HttpClientFactory;
import jp.ac.tokushima_u.is.ll.util.JsonItemUtil.AnswerQuery;
import jp.ac.tokushima_u.is.ll.util.JsonItemUtil.QuestionQuery;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler.AsyncQueryListener;
import jp.ac.tokushima_u.is.ll.util.UIUtils;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.MapActivity;

public class ViewLogActivity extends MapActivity implements AsyncQueryListener, Runnable {
	private LocationManager locationmanager;
	private static final int DIALOG_Sever_Error = 1;
	private static final int DIALOG_Delete_Info = R.string.add_item_question;
	private ProgressDialog pd;
	private NotifyingAsyncQueryHandler asynhandler;
	private Double lat;
	private Double lng;

	private boolean mItemCursor = false;
	private boolean mItemtitleCursor = false;
	private boolean mItemtagCursor = false;
	private boolean mItemcommentCursor = false;
	private boolean mQuestionCursor = false;
	private boolean mAnswerCursor = false;

	private static final int CodeKey = R.color.white;
	private static final int ContentKey = R.color.black;


	private static final SimpleDateFormat sTimeFormat = new SimpleDateFormat(
			"yyyy/MM/dd hh:mm:ss", Locale.JAPAN);

	private String itemId = null;

	private long start;
	private long stop;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		start = Calendar.getInstance().getTimeInMillis();
		pd = ProgressDialog.show(this, "Wait..", "Loading...", true, false);
		this.setContentView(R.layout.activity_log_detail);
		Intent intent = this.getIntent();
		Uri uri = intent.getData();
		itemId = Items.getItemId(uri);
		Uri titleuri = Items.buildItemtitlesUri(itemId);
		Uri taguri = Items.buildItemtagsUri(itemId);
		Uri commenturi = Items.buildItemcommentsUri(itemId);
		Uri questionUri = Items.buildItemQuestionUri(itemId);
		Uri answerUri = Items.buildItemAnswerUri(itemId);

		locationmanager = (LocationManager) getSystemService(LOCATION_SERVICE);
		asynhandler = new NotifyingAsyncQueryHandler(this.getContentResolver(),this);
		asynhandler.startQuery(ItemsQuery._Token, uri, ItemsQuery.PROJECTION);
		asynhandler.startQuery(ItemtitlesQuery._Token, titleuri, ItemtitlesQuery.PROJECTION);
		asynhandler.startQuery(ItemtagsQuery._Token, taguri, ItemtagsQuery.PROJECTION);
		asynhandler.startQuery(ItemcommentsQuery._Token, commenturi, ItemcommentsQuery.PROJECTION);
		asynhandler.startQuery(QuestionQuery._Token, questionUri, QuestionQuery.PROJECTION);
		asynhandler.startQuery(AnswerQuery._Token, answerUri, AnswerQuery.PROJECTION, Answers.DEFAULT_SORT);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_Sever_Error:
			return this
					.buildDialogOK(this, R.string.error_unable_access_server);
		case DIALOG_Delete_Info:
			return new AlertDialog.Builder(this)
					.setMessage(R.string.dialog_log_delete)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									Uri uri = Items.buildItemUri(itemId);
									asynhandler.startDelete(uri);
									new DeleteThread().start();
//									UIUtils.goHome(ViewLogActivity.this);
								}
							})
					.setNegativeButton(R.string.alert_dialog_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).create();
		}

		return null;
	}

	public Dialog buildDialogOK(Context context, int title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(R.drawable.alert_dialog_icon);
		builder.setTitle(title);
		builder.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		return builder.create();
	}


	public void onHomeClick(View view) {
		UIUtils.goHome(this);
	}

	public void onMapClick(View view){
		UIUtils.goMap(this, lat, lng);
	}

	public void onEditClick(View view){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_EDIT);
		intent.setData(LearningLogContract.Items.buildItemUri(itemId));
		this.startActivity(intent);
	}

	public void onDeleteClick(View view){
		this.showDialog(DIALOG_Delete_Info);
	}


	@Override
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		if(token == ItemsQuery._Token){
			this.onItemQueryComplete(cursor);
		}else if(token == ItemtitlesQuery._Token){
			this.onItemtitleQueryComplete(cursor);
		}else if(token == ItemtagsQuery._Token){
			this.onItemtagQueryComplete(cursor);
		}else if(token == ItemcommentsQuery._Token){
			this.onItemcommentQueryComplete(cursor);
		}else if(token == QuestionQuery._Token){
			this.onQuestionQueryComplete(cursor);
		}else if(token == AnswerQuery._Token){
			this.onAnswerQueryComplete(cursor);
		}else if(cursor!=null){
			cursor.close();
		}

		if(this.mItemCursor&&this.mItemtitleCursor&&this.pd!=null&&this.pd.isShowing()){
			this.pd.dismiss();
			stop = Calendar.getInstance().getTimeInMillis();
			Log.e("learninglog", "LogListActivity loaded costed "+(stop-start)/1000+" seconds");
		}
	}

	public void onItemQueryComplete(Cursor cursor){
		try{
			this.mItemCursor = true;
			if(!cursor.moveToFirst()) return;

			new Thread(this).start();

			String authorInfo = "";
			String nickname = cursor.getString(ItemsQuery.NICK_NAME);
			if(nickname!=null){
				authorInfo = authorInfo +"created by "+ nickname;
			}

			Long updateTime = cursor.getLong(ItemsQuery.UPDATE_TIME);
			if(updateTime!=null){
				authorInfo = authorInfo + " at " + sTimeFormat.format(new Date(updateTime));
			}

			String photoUrl = cursor.getString(ItemsQuery.PHOTO_URL);
			String filetype = cursor.getString(ItemsQuery.FILE_TYPE);
			Integer syncType = cursor.getInt(ItemsQuery.SYNC_TYPE);

			if(Items.SYNC_TYPE_CLIENT_INSERT.equals(syncType)){
				try{
					String attached = cursor.getString(ItemsQuery.ATTACHED);
					Bitmap bp = null;
					if(attached!=null)
						bp = BitmapUtil.zoomImage(BitmapFactory.decodeFile(attached), 400, 300);
					if(bp!=null){
						LinearLayout fileBlock = (LinearLayout)this.findViewById(R.id.log_file_block);
						fileBlock.setVisibility(View.VISIBLE);
						ImageView imageView = new ImageView(this);
						imageView.setImageBitmap(bp);
						fileBlock.addView(imageView);
					}
				}catch(Exception e){

				}
			}else if(photoUrl!=null&&filetype!=null){
//				Bitmap bitmap = BitmapUtil.getURLBitmap(ApiConstants.Image_Server_Url+photoUrl+ApiConstants.MiddleSizePostfix);
				Bitmap bitmap = BitmapUtil
				.getBitmap(ViewLogActivity.this, photoUrl, ApiConstants.MiddleSizePostfix);
	    		if (bitmap != null){
	    			LinearLayout fileBlock = (LinearLayout)this.findViewById(R.id.log_file_block);
	    			fileBlock.setVisibility(View.VISIBLE);
	    			String filepath = ApiConstants.Image_Server_Url+photoUrl;
	    			if (Constants.FileTypeVideo.equals(filetype)) {
	    				filepath = filepath + "_320x240.mp4";
	    			} else if (Constants.FileTypeAudio.equals(filetype)) {
	    				filepath = filepath + ".mp3";
	    			} else if (Constants.FileTypeImage.equals(filetype)) {
	    				filepath = filepath + ApiConstants.MiddleSizePostfix;
	    			}

	    			Bundle args = new Bundle();
					args.putString(Constants.FileTypeKey,
							filetype);
					args.putString(Constants.FilePathKey,
							filepath);
	    			ImageView imageView = new ImageView(this);
	    			imageView.setTag(args);
	    			imageView.setImageBitmap(bitmap);
	    			imageView.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							Bundle args = (Bundle) v.getTag();
							if (args == null)
								return;
							String path = args
									.getString(Constants.FilePathKey);
							String type = args
									.getString(Constants.FileTypeKey);
							if (Constants.FileTypeAudio
									.equals(type)) {
								Intent intent = new Intent(
										ViewLogActivity.this,
										AudioPlayer.class);
								intent.setData(Uri.parse(path));
								startActivity(intent);
							} else if (Constants.FileTypeImage
									.equals(type)) {
								Intent intent = new Intent(
										ViewLogActivity.this,
										ShowPhoto.class);
								intent.setData(Uri.parse(path));
								startActivity(intent);
							} else if (Constants.FileTypeVideo
									.equals(type)) {
								Intent intent = new Intent(
										ViewLogActivity.this,
										VideoPlayer.class);
								intent.setData(Uri.parse(path));
								startActivity(intent);
							}
						}});
	    			fileBlock.addView(imageView);
	    		}
			}

			this.lat = cursor.getDouble(ItemsQuery.LATITUTE);
			this.lng = cursor.getDouble(ItemsQuery.LNGITUTE);
			if(this.lat!=null&&this.lng!=null){
//				this.findViewById(R.id.btn_title_map).setVisibility(View.VISIBLE);
//
//				this.findViewById(R.id.log_map_block).setVisibility(View.VISIBLE);
//				MapView mapView = (MapView)this.findViewById(R.id.log_mapview);
//				mapView.setKeepScreenOn(false);
//				mapView.setFadingEdgeLength(10);
//				mapView.setVerticalFadingEdgeEnabled(true);
//				mapView.setVerticalScrollBarEnabled(true);
//				mapView.setHorizontalFadingEdgeEnabled(true);
//				mapView.setHorizontalScrollBarEnabled(true);
//				mapView.setFocusableInTouchMode(true);
//				mapView.setBuiltInZoomControls(true);
//				List<Overlay> mapOverlays = mapView.getOverlays();
//				GeoPoint point = new GeoPoint((int)(lat * 1000000),(int)(lng * 1000000));
////				mapView.setLayoutParams(new MapView.LayoutParams(10, 20, point,  MapView.LayoutParams.CENTER));
////				OverlayItem overlayitem = new OverlayItem(point, "Hola, Mundo!", "I'm in Mexico City!");
////				itemizedoverlay.addOverlay(overlayitem);
//				final MyLocationOverlay overlay = new MyLocationOverlay(getApplicationContext(),mapView);
//				overlay.onProviderEnabled(LocationManager.GPS_PROVIDER); // GPS を使用する
//				overlay.enableMyLocation();
//
//				OverlayItem overlayitem = new OverlayItem(point, "Hola, Mundo!", "I'm in Mexico City!");
//				itemizedoverlay.addOverlay(overlayitem);
//
//				mapView.getOverlays().add(overlay);
//				mapView.getOverlays().add(itemizedoverlay);
//
//				mapView.getOverlays().add(overlay);
//				mapOverlays.add(itemizedoverlay);
			}

			//Edit
			String authorId = ContextUtil.getUserId(this);
			String i_author = cursor.getString(ItemsQuery.AUTHOR_ID);
			if(authorId.equals(i_author)){
				this.findViewById(R.id.btn_title_delete).setVisibility(View.VISIBLE);
				this.findViewById(R.id.btn_title_edit).setVisibility(View.VISIBLE);
			}


			TextView txtAuthor = (TextView)this.findViewById(R.id.log_author);
			txtAuthor.setText(authorInfo);

			String note = cursor.getString(ItemsQuery.NOTE);
			if(note!=null){
				LinearLayout noteBlock = (LinearLayout)this.findViewById(R.id.log_note_block);
				noteBlock.setVisibility(View.VISIBLE);
				TextView txtNote = (TextView)this.findViewById(R.id.log_note);
				txtNote.setText(note);
			}

			String place = cursor.getString(ItemsQuery.PLACE);
//			String place1 = new String();
			if(place != null && place.length() > 0){
				LinearLayout PlaceBlock = (LinearLayout)this.findViewById(R.id.log_place_block);
				PlaceBlock.setVisibility(View.VISIBLE);
				TextView txtPlace = (TextView)this.findViewById(R.id.log_place);
//				Scanner scan = new Scanner(place);
//				scan.useDelimiter(",");
//				while(scan.hasNext()){
//					String vol = scan.next();
//					place1 += vol + "\n";
//				}scan.close();
				txtPlace.setText(place);
			}
			String relate = cursor.getString(ItemsQuery.RELATE);
			if(relate != null && relate.length() > 0){
				LinearLayout RelateBlock = (LinearLayout)this.findViewById(R.id.log_related_block);
				RelateBlock.setVisibility(View.VISIBLE);
				TextView txtRelate = (TextView)this.findViewById(R.id.log_relate);
				txtRelate.setText(relate);
			}
		}finally{
			cursor.close();
		}
	}

	public void onItemtitleQueryComplete(Cursor cursor){
		try{
			this.mItemtitleCursor = true;
			if(!cursor.moveToFirst()) return;
			LinearLayout title_part = (LinearLayout)this.findViewById(R.id.log_title_block);
			do{
				String name = cursor.getString(ItemtitlesQuery.NAME);
				String content = cursor.getString(ItemtitlesQuery.CONTENT);
				String code = cursor.getString(ItemtitlesQuery.CODE);

				View titleView = this.getLayoutInflater().inflate(R.layout.list_item_title, null);
				TextView nameText = (TextView)titleView.findViewById(R.id.txt_title_name);
				nameText.setText(name+":");
				TextView contentText = (TextView)titleView.findViewById(R.id.txt_title_content);
				contentText.setText(content);
				ImageView tts_button = (ImageView)titleView.findViewById(R.id.btn_pronounce);
				tts_button.setTag(CodeKey, code);
				tts_button.setTag(ContentKey, content);
				tts_button.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
//						 if (isServiceRunning(
//			                "net.npaka.serviceex.PlayerService")) return;
						String code = (String)v.getTag(CodeKey);
						String content = (String)v.getTag(ContentKey);
						Intent intent = new Intent(ViewLogActivity.this, jp.ac.tokushima_u.is.ll.service.TTSService.class);
//						String url = ApiConstants.Pronounce_URI+"?ie=UTF-8&lang="+code+"&text=";
//						try{
//							url = url+URLEncoder.encode(content,"UTF-8");
//						}catch(UnsupportedEncodingException e){
//
//						}

						File file = AudioUtil.getPronounceAudio(ViewLogActivity.this, content, code);
						if(file!=null&&file.exists()){
							Uri uri = Uri.fromFile(file);
							intent.setData(uri);
							startService(intent);
						}
						//Log.d("test", "clicked");
					}

					//サービスが起動中かどうか
//					private boolean isServiceRunning(String className) {
//				        ActivityManager am=(ActivityManager)getSystemService(ACTIVITY_SERVICE);
//				        List<ActivityManager.RunningServiceInfo> serviceInfos=
//				            am.getRunningServices(Integer.MAX_VALUE);
//				        for (int i=0;i<serviceInfos.size();i++) {
//				            if (serviceInfos.get(i).service.getClassName().equals(className)) {
//				                return true;
//				            }
//				        }
//				        return false;
//				    }

				});
				title_part.addView(titleView);
			}while(cursor.moveToNext());
		}finally{
			cursor.close();
		}
	}

	public void onItemtagQueryComplete(Cursor cursor){
		try{
			this.mItemtagCursor = true;
			if(!cursor.moveToFirst()) return;
			String tags = "";
			do{
				String tag = cursor.getString(ItemtagsQuery.TAG);
				if(tag!=null&&tag.length()>0){
					if(tags.length()>0)
						tags = tags +", ";
					tags = tags + tag;
				}
			}while(cursor.moveToNext());

			if(tags.length()>0){
				LinearLayout tagBlock = (LinearLayout)this.findViewById(R.id.log_tag_block);
				tagBlock.setVisibility(View.VISIBLE);
				TextView txtTag = (TextView)this.findViewById(R.id.log_tag);
				txtTag.setText(tags);
			}

		}finally{
			cursor.close();
		}
	}

	public void onItemcommentQueryComplete(Cursor cursor){
		try{
			this.mItemcommentCursor = true;
			if(!cursor.moveToFirst()) return;
			LinearLayout commentBlock = (LinearLayout)this.findViewById(R.id.log_comments_block);
			commentBlock.setVisibility(View.VISIBLE);
			do{
				String comment = cursor.getString(ItemcommentsQuery.COMMENT);
				String nickname = cursor.getString(ItemcommentsQuery.NICKNAME);

				View commentView = this.getLayoutInflater().inflate(R.layout.list_log_comment, null);
				TextView nameText = (TextView)commentView.findViewById(R.id.txt_title_name);
				nameText.setText(comment);
				TextView contentText = (TextView)commentView.findViewById(R.id.txt_title_content);
				contentText.setText(nickname);
				commentBlock.addView(commentView);
			}while(cursor.moveToNext());
		}finally{
			cursor.close();
		}
	}

	public void onQuestionQueryComplete(Cursor cursor){
		try{
			this.mQuestionCursor = true;
			if(!cursor.moveToFirst())return;
			String content = cursor.getString(QuestionQuery.CONTENT);
			if(content!=null&&content.length()>0){
				LinearLayout questionBlock = (LinearLayout)this.findViewById(R.id.log_question_block);
				questionBlock.setVisibility(View.VISIBLE);

				View contentView = this.getLayoutInflater().inflate(R.layout.list_log_question, null);
				TextView contentText = (TextView)contentView.findViewById(R.id.txt_title_content);
				if(content!=null)
					contentText.setText(content);

//			TextView questionText = new TextView(this);
//			questionText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
//			questionText.setText(content);
				questionBlock.addView(contentView);
			}
		}finally{
			cursor.close();
		}
	}

	public void onAnswerQueryComplete(Cursor cursor){
		try{
			this.mAnswerCursor = true;
			if(!cursor.moveToFirst())return;
			LinearLayout questionBlock = (LinearLayout)this.findViewById(R.id.log_question_block);
//			questionBlock.setVisibility(View.VISIBLE);
			do{
				String content = cursor.getString(AnswerQuery.CONTENT);
				String nickname = cursor.getString(AnswerQuery.NICKNAME);
				View contentView = this.getLayoutInflater().inflate(R.layout.list_log_comment, null);
				TextView contentText = (TextView)contentView.findViewById(R.id.txt_title_name);
				if(content!=null)
					contentText.setText(content);
				TextView nicknameText = (TextView)contentView.findViewById(R.id.txt_title_content);
				if(nickname!=null)
					nicknameText.setText(nickname);
				questionBlock.addView(contentView);
			}while(cursor.moveToNext());
//			String content = cursor.getString(QuestionQuery.CONTENT);
//			TextView questionText = new TextView(this);
//			questionText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
//			questionText.setText(content);
//			questionBlock.addView(questionText);
		}finally{
			cursor.close();
		}
	}


    private interface ItemsQuery {
    	int _Token = 1;
        String[] PROJECTION = {
                BaseColumns._ID,
                Items.ITEM_ID,
                Items.NICK_NAME,
                Items.PHOTO_URL,
                Items.FILE_TYPE,
                Items.TAG,
                Items.NOTE,
                Items.PLACE,
                Items.RELATE,
                Items.LNGITUTE,
                Items.LATITUTE,
                Items.UPDATE_TIME,
                Items.AUTHOR_ID,
                Items.SYNC_TYPE,
                Items.ATTACHED
        };

        int _ID = 0;
        int ITEM_ID = 1;
        int NICK_NAME = 2;
        int PHOTO_URL = 3;
        int FILE_TYPE = 4;
        int TAG = 5;
        int NOTE = 6;
        int PLACE = 7;
        int LNGITUTE = 8;
        int LATITUTE = 9;
        int UPDATE_TIME = 10;
        int AUTHOR_ID = 11;
        int SYNC_TYPE = 12;
        int ATTACHED = 13;
        int RELATE = 14;
    }

    private interface ItemtitlesQuery {
    	int _Token = 2;
        String[] PROJECTION = {
                Itemtitles._ID,
                Itemtitles.ITEMTITLE_ID,
                Itemtitles.ITEM_ID,
                Itemtitles.LANGUAGE_ID,
                Itemtitles.CONTENT,
                Languages.NAME,
                Languages.CODE
        };

        int _ID = 0;
        int ITEMTITLE_ID = 1;
        int ITEM_ID = 2;
        int LANGUAGE_ID = 3;
        int CONTENT = 4;
        int NAME = 5;
        int CODE = 6;
    }

    private interface ItemtagsQuery {
    	int _Token = 3;
        String[] PROJECTION = {
                Itemtags._ID,
                Itemtags.ITEMTAG_ID,
                Itemtags.ITEM_ID,
                Itemtags.TAG
        };

        int _ID = 0;
        int ITEMTAG_ID = 1;
        int ITEM_ID = 2;
        int TAG = 3;
    }

    private interface ItemcommentsQuery {
    	int _Token = 4;
        String[] PROJECTION = {
                Itemcomments._ID,
                Itemcomments.ITEMCOMMENT_ID,
                Itemcomments.ITEM_ID,
                Itemcomments.COMMENT,
                Itemcomments.NICKNAME,
        };

        int _ID = 0;
        int ITEMCOMMENT_ID = 1;
        int ITEM_ID = 2;
        int COMMENT = 3;
        int NICKNAME = 4;
    }


	@Override
	public void run() {
			DefaultHttpClient client = HttpClientFactory.getInstance(this);
			String url = ApiConstants.Item_View_URL;

			try {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				Location loc = null;
				try{
					if(this.locationmanager==null)
						this.locationmanager = (LocationManager) getSystemService(LOCATION_SERVICE);
					loc = this.locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				}catch(Exception e){

				}
				nvps.add(new BasicNameValuePair("id", this.itemId));
				if (loc != null) {
					nvps.add(new BasicNameValuePair("latitude", loc.getLatitude()
							+ ""));
					nvps.add(new BasicNameValuePair("longitude", loc.getLongitude()
							+ ""));
					nvps.add(new BasicNameValuePair("speed", loc.getSpeed() + ""));
				}
				String param = URLEncodedUtils.format(nvps, HTTP.UTF_8);
				url = url+"?" + param;
				HttpGet httpGet = new HttpGet(url);
				client.execute(httpGet);
			} catch (Exception e) {
					Log.e(Constants.LOG_TAG, "record view info error" ,e);
			}
	}

	class DeleteThread extends Thread{

		@Override
		public void run() {
			DefaultHttpClient client = HttpClientFactory.getInstance(ViewLogActivity.this);
			String url = ApiConstants.ITEM_Add_URI;

			try {
//				url = url+"/" +itemId + "/delete";
				url = url+"/" +itemId ;
				HttpDelete httpDelete = new HttpDelete(url);
				client.execute(httpDelete);
				resultHandler.sendEmptyMessage(1);
			} catch (Exception e) {
				Log.e(Constants.LOG_TAG, "record view info error" ,e);
				resultHandler.sendEmptyMessage(2);
			}

		}
	}

	private Handler resultHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 1:
				Toast.makeText(ViewLogActivity.this, R.string.info_delete_success, Toast.LENGTH_SHORT).show();
				UIUtils.goHome(ViewLogActivity.this);break;
			case 2:
				Toast.makeText(ViewLogActivity.this, R.string.info_delete_failure, Toast.LENGTH_SHORT).show();break;
			};
		}
	};

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}
