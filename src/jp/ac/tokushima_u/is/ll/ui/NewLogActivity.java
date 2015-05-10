package jp.ac.tokushima_u.is.ll.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.ItemPlace;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.ItemRelate;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Items;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemtags;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemtitles;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Languages;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Questions;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Settings;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.SyncColumns;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Users;
import jp.ac.tokushima_u.is.ll.ui.media.AudioPlayer;
import jp.ac.tokushima_u.is.ll.ui.media.AudioRecorder;
import jp.ac.tokushima_u.is.ll.ui.media.ShowPhoto;
import jp.ac.tokushima_u.is.ll.ui.media.VideoPlayer;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.Constants;
import jp.ac.tokushima_u.is.ll.util.ContextUtil;
import jp.ac.tokushima_u.is.ll.util.HttpClientFactory;
import jp.ac.tokushima_u.is.ll.util.JsonItemUtil;
import jp.ac.tokushima_u.is.ll.util.Lists;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler.AsyncQueryListener;
import jp.ac.tokushima_u.is.ll.util.StringUtils;
import jp.ac.tokushima_u.is.ll.util.UIUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author:dameng <a> This class is used to add/edit a new learning log object.
 *                </a>
 */
public class NewLogActivity extends Activity implements AsyncQueryListener,
		Runnable, LocationListener {
	private static final String TAG = "NewLogActivity";

	private NotifyingAsyncQueryHandler mHandler;

	private static final int TRANSLATE_MESSAGE_SUCCESS = 0;
	private static final int TAG_CODE_KEY = R.string.alert_dialog_cancel;
	private static final int TAG_LANGUAGE_KEY = R.string.alert_dialog_ok;

	public MyLocationListener locationlistener = new MyLocationListener();
	public LocationManager locationmanager;
	public String locationProvider;
	String APIKey1 = "AIzaSyC-QtWx-R-7UITO7cYjLqGhyAbLOIs-f7M";
	String APIKey2 = "AIzaSyCmZKtSZny9Ong4e7SCP1ZybezOu6MoOBQ";
	private static final int gps_circle_time = 0;
	private static final int gps_circle_distance = 0;
	public static final int INDEX_NOT_FOUND = -1;
	private CheckBox[] checkBox;
	private CheckBox[] checkBox1;
	public static int count = 0;
	
	private Handler translateHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TRANSLATE_MESSAGE_SUCCESS:
				Bundle bundle = msg.getData();
				String code = bundle.getString("code");
				String result = bundle.getString("result");
				if (StringUtils.isBlank(code) || StringUtils.isBlank(result)) {
					return;
				}
				EditText text = getTitleEditText(code);
				text.setText(result);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	};

	private Handler relatedHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
				case 0:
					Bundle bundle = msg.getData();
					String related = bundle.getString("words");
					if(StringUtils.isBlank(related)){
						return;
					}
//					EditText text = getRelatedEditText(related);
//					text.setText(related);
					relatedcheck();
					break;
				default:
					super.handleMessage(msg);
			}
		}
	};
	
	private Handler placeHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
				case 0:
					Bundle bundle = msg.getData();
					String places = bundle.getString("place");
					if(StringUtils.isBlank(places)){
						return;
					}
					placecheck();
					break;
				default:
					super.handleMessage(msg);
			}
		}
	};
	// category values & tag values
	private String[] cate_names = new String[] {};
	private String[] cate_values = new String[] {};
//	private String[] lan_names = new String[] {};
//	private String[] lan_values = new String[] {};
	private LinkedList<String> lan_names = new LinkedList<String>();
	private LinkedList<String> lan_values = new LinkedList<String>();
	private List<String> tags = new ArrayList<String>();
	private String selectedCat;
	private String selectedLanCode;
	private String questionContent;
	private String itemId = null;
	private Integer syncType = Items.SYNC_TYPE_PUSH;
	private ImageView logAttach;
	private boolean isPublic = true;
	private boolean isLocationBased = true;
	private boolean isTextChoice = true;
	private boolean isFileChoice = true;
	private boolean isYesNoQuiz = true;

	// Dialog IDs
	private static final int DialogCategoryList = 1;
	private static final int DialogSettingList = 2;
	private static final int DialogTagList = 3;
	private static final int DialogAttachList = 4;
	private static final int DialogAttachAction = 5;
	private static final int DialogPlaceList = 6;
	private static final int DialogRelateList = 7;
	private static final int DialogQuestionAction = R.string.description_attach;

	// Activity return values
	private static final int VOICE_RECOGNITION_REQUEST_CODE = R.string.dialog_attach_option_btn_back;
	private static final int Gallery_REQUEST_CODE = R.string.dialog_attach_option_btn_delete;
	private static final int Camera_REQUEST_CODE = R.string.dialog_attach_option_btn_view;
	private static final int VIDEO_REQUEST_CODE = R.string.dialog_attach_option_title;
	private static final int AUDIO_REQUEST_CODE = R.string.description_home;

	private ProgressDialog pd;
	private ProgressDialog pd_upload;

	private boolean mItemCursor = false;
	private boolean mItemtitleCursor = false;
	private boolean mItemtagCursor = false;
	private boolean mItemPlaceCursor = false;
	private boolean mItemRelatedCursor = false;
	private boolean mSettingCursor = false;
	private boolean mSettingLanguageCursor = false;

	private EditText logNote;
	private LinearLayout titleLayout;
	private LinearLayout relatedLayout;
	private EditText PlaceNote;
	private EditText relatenote;
	private EditText relatednote;
	private LinearLayout logAttachblock;
//	private EditText PlaceComit;
	private LinearLayout relateblock;
	
	private int screenWidth;
	private int screenHeight;
	private ArrayList<String> place = new ArrayList<String>();
	private ArrayList<String> relate = new ArrayList<String>();
	private ArrayList<String> d_relate = new ArrayList<String>();
	
	public static double lat = 0.0;
	public static double lng = 0.0;
	int radius =5;
	// Image shrink rate
	private static final double attachImageWidthScale = 0.3;
	private static final double attachImageHeightScale = 0.4;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log_edit);
		logNote = (EditText) this.findViewById(R.id.log_title_note);
		titleLayout = (LinearLayout) this.findViewById(R.id.log_title_block);
		relatedLayout = (LinearLayout) this.findViewById(R.id.log_related_block);
		logAttach = (ImageView) this.findViewById(R.id.log_attach);
		PlaceNote = (EditText) this.findViewById(R.id.log_place_note);
		relatenote = (EditText) this.findViewById(R.id.txt_related_content);
		relatednote = (EditText) this.findViewById(R.id.txt_related_note);
//		PlaceComit = (EditText) this.findViewById(R.id.log_place_comit);
		relateblock = (LinearLayout) this.findViewById(R.id.log_relate_block);

		DisplayMetrics dm = new DisplayMetrics();
		dm = getApplicationContext().getResources().getDisplayMetrics();
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;

		mHandler = new NotifyingAsyncQueryHandler(this.getContentResolver(),
				this);
		String userId = ContextUtil.getUserId(this);
		selectedCat = ContextUtil.getDefaultCat(this);

		pd = ProgressDialog.show(this, "Wait..", "Loading...", true, false);

		Uri uri = Users.buildUsersSettingUri(userId);
		mHandler.startQuery(SettingQuery._TOKEN, null, uri,
				SettingQuery.PROJECTION, Settings.FIELD + "=?",
				new String[] { Settings.SETTING_CATEGORY_FIELD_ID.toString() },
				Settings.DEFAULT_SORT);

		Uri languageuri = Users.buildUsersSettingLanguageUri(userId);

		mHandler.startQuery(SettingLanguageQuery._TOKEN, languageuri,
				SettingLanguageQuery.PROJECTION, Settings.DEFAULT_SORT);

		Intent intent = this.getIntent();
		if (intent != null && Intent.ACTION_EDIT.equals(intent.getAction())) {
			Uri itemuri = intent.getData();
			this.itemId = Items.getItemId(itemuri);
			Uri taguri = Items.buildItemtagsUri(itemId);
			Uri titleuri = Items.buildItemtitlesUri(itemId);
			Uri placeuri = Items.buildItemPlaceUri(itemId);
			Uri relateuri = Items.buildItemRelateUri(itemId);
			mHandler.startQuery(ItemsQuery._TOKEN, itemuri,
					ItemsQuery.PROJECTION);
			mHandler.startQuery(ItemtitlesQuery._TOKEN, titleuri,
					ItemtitlesQuery.PROJECTION);
			mHandler.startQuery(ItemtagsQuery._TOKEN, taguri,
					ItemtagsQuery.PROJECTION);
			mHandler.startQuery(ItemPlaceQuery._TOKEN, placeuri,
					ItemPlaceQuery.PROJECTION);
			mHandler.startQuery(ItemRelatedQuery._TOKEN, relateuri,
					ItemRelatedQuery.PROJECTION);
		} else if (intent != null
				&& intent.getType() != null
				&& (intent.getType().contains("image/") || intent.getType()
						.equals("video/"))) {
			this.setAttachFile(intent);
		}

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAltitudeRequired(true);
		criteria.setBearingRequired(false);
		criteria.setSpeedRequired(true);
		criteria.setCostAllowed(false);
		this.locationmanager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
		locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER,gps_circle_time, gps_circle_distance, locationlistener);
		locationProvider = locationmanager.getBestProvider(criteria, true);
		Location location = this.locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Thread thread1 = new PlaceThread(location);
		thread1.start();

		//TODO  locationprovider is null
		if(locationProvider!=null)
			locationmanager.requestLocationUpdates(locationProvider, gps_circle_time, gps_circle_distance, locationlistener);
		// mHandler.startQuery(SettingLanguageQuery._TOKEN, null, languageuri,
		// SettingLanguageQuery.PROJECTION, Settings.FIELD + "=? or "
		// + Settings.FIELD + "=?", new String[] {
		// Settings.SETTING_MYLAN_FIELD_ID.toString(),
		// Settings.SETTING_STUDYLAN_FIELD_ID.toString() },
		// Settings.DEFAULT_SORT);
	}
	
	protected void relatedcheck(){

		checkBox1 = new CheckBox[relate.size()];
		int flg;
		TextView tv = (TextView) this.findViewById(R.id.relate_text_view);
		tv.setVisibility(View.VISIBLE);
		for(int i = 0; i < d_relate.size(); i++){
			flg = relate.indexOf(d_relate.get(i));
			if(flg != -1){
				relate.remove(flg);
			}
		}

			for(int i = 0; i < relate.size(); i++){
				checkBox1[i] = new CheckBox(this);	
					checkBox1[i].setText(relate.get(i));
					checkBox1[i].setTextColor(Color.rgb(0,0,0));
					checkBox1[i].setTextSize(16);
					setMyLayoutParams(checkBox1[i]);
			//relatedLayout -> relateblock
					relateblock.addView(checkBox1[i]);
			}
			boolean[] checkArray1 = new boolean[relate.size()];
			for(int i = 0; i < relate.size(); i++){
				checkArray1[i] = false;
				checkArray1[i] = checkBox1[i].isChecked();
			}
			d_relate = new ArrayList<String>(relate);
	}
	
	protected void placecheck() {
		// TODO Auto-generated method stub
		boolean[] checkArray = new boolean[place.size()];
		checkBox = new CheckBox[place.size()];       // チェックボックス配列
		LinearLayout placeLayout = (LinearLayout) this.findViewById(R.id.log_places_block);
		TextView tv = new TextView(this);
		tv.setText("Please select Place Tags");
		placeLayout.addView(tv,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

		for (int i=0; i<place.size(); i++){
			checkBox[i] = new CheckBox(this);
			checkBox[i].setText(place.get(i));
			checkBox[i].setTextColor(Color.rgb(0,0,0));
			checkBox[i].setTextSize(16);
			setMyLayoutParams(checkBox[i]);
			placeLayout.addView(this.checkBox[i]);
		}
		for(int i = 0; i < place.size(); i++){
			checkArray[i] = false;
			checkArray[i] = checkBox[i].isChecked();
		}
	}

    private static void setMyLayoutParams(View view) {
        view.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
    }

	public void onHomeClick(View view) {
		UIUtils.goHome(this);
	}

	public void onSaveClick(View view) {
		String places = "";
		String relates = "";
		Map<String, String> titles = this.getLanTitleValues();
		if (titles == null || titles.size() <= 0) {
			Toast.makeText(NewLogActivity.this, R.string.info_titles_empty,
					Toast.LENGTH_LONG).show();
			return;
		}
		this.pd_upload = ProgressDialog.show(this, "Wait..", "Uploading...",
				true, false);

		String wrap = new String();
		wrap = PlaceNote.getText().toString();
		places += wrap + ",";
		for(int i = 0;i < place.size();i++){
			if(checkBox[i].isChecked() == true){
				places += place.get(i) + ",";
			}
		}
		String tmp = new String();
		tmp = relatednote.getText().toString();
		relates += tmp + ",";
		for(int j = 0; j < relate.size(); j++){
			if(checkBox1[j].isChecked() == true){
				relates += relate.get(j) + ",";
			}
		}
		PlaceNote.setText(places);
		relatednote.setText(relates);
		new Thread(this).start();
	}
	
	public void onRelatedClick(View view) {
		final String relateword = relatenote.getText().toString();
		Toast.makeText(NewLogActivity.this, R.string.item_title_intranslate, 
				Toast.LENGTH_SHORT).show();

		new Thread(new Runnable(){
			String related = relateword;
			ArrayList<String> words = new ArrayList<String>();
			@Override
			public void run() {
				// TODO Auto-generated method stub
				HttpPost httpPost2 = new HttpPost("http://ll.is.tokushima-u.ac.jp/learninglog2/api/relatedwords.json?w="+related);
				DefaultHttpClient pClient = HttpClientFactory.createHttpClient();
				MultipartEntity pParams = new MultipartEntity();
				httpPost2.setEntity(pParams);
				try{
					HttpResponse response = pClient.execute(httpPost2);
					if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
						HttpEntity entity = response.getEntity();
						if (entity != null) {
							InputStream instream = entity.getContent();
							String result = convertStreamToString(instream);
							instream.close();						
//							String result1 = strip(result,"[]");
							result = "{" + "\"result\":" + result + "}";
							JSONObject pJson = new JSONObject(result);
							if(pJson!=null){
//								String relatedword = pJson.getString("relate");
//								relatednote.setText(relatedword);
								JSONArray array = pJson.getJSONArray("result");
								if(array!=null){
									for(int i=0;i<array.length();i++){
										JSONObject o = array.getJSONObject(i);
//										JSONArray typesArray = o.getJSONArray("relate");
										String relateword = o.getString("relate");
											Scanner scan = new Scanner(relateword);
											scan.useDelimiter("\\s*,\\s*");
											while(scan.hasNext()){
												String vol = scan.next();
												String vol1 = strip(vol,"\\s\n\"[]");
												int flg = relate.indexOf(vol1);
												if(flg != -1){
												}else{
													words.add(vol1);
													relate.add(vol1);
												}
											}scan.close();
										//}
									}
								}
								String str = new String();
								for(String obj : words)
									str += (String)obj + ",";
								if(relatednote.getText().toString().length() == 0){
									final Message msg = new Message();
									Bundle bundle = new Bundle();
									bundle.putString("words", strip(str,","));
									msg.setData(bundle);
									msg.what = TRANSLATE_MESSAGE_SUCCESS;
									relatedHandler.post(new Runnable(){

										@Override
										public void run() {
											// TODO Auto-generated method stub
											relatedHandler.sendMessage(msg);
										
										}
									});
//									relatedHandler.sendMessage(msg);
								}else{
								}
							}
						}
					}else{
						return;
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				}catch(Exception e){
					e.printStackTrace();
				} finally{
					pClient.getConnectionManager().shutdown();
				}
			}
		}).start();
	}

	public void onQuestionClick(View view){
		this.showDialog(DialogQuestionAction);
	}

	public void onDiscardClick(View view) {
		UIUtils.goHome(this);
	}

	public void onCategoryClick(View view) {
		this.showDialog(DialogCategoryList);
	}

	public void onCatTagClick(View view) {
		this.showDialog(DialogTagList);
	}

	public void onSettingClick(View view) {
		this.showDialog(DialogSettingList);
	}

	public void onAttachClick(View view) {
		this.showDialog(DialogAttachList);
	}

	private Handler resultHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (pd_upload != null && pd_upload.isShowing())
				pd_upload.dismiss();
			switch (msg.what) {
			case 1:
				Toast.makeText(NewLogActivity.this,
						R.string.info_upload_success, Toast.LENGTH_SHORT)
						.show();
				UIUtils.goHome(NewLogActivity.this);
				break;
			case 2:
				Toast.makeText(NewLogActivity.this,
						R.string.info_upload_failure, Toast.LENGTH_SHORT)
						.show();
				break;
			}
			;
		}
	};

	public void run() {
		final ArrayList<ContentProviderOperation> subbatch = Lists
		.newArrayList();

		if(this.itemId!=null){
			subbatch.addAll(JsonItemUtil.deleteItemBatch(itemId));
		}

		ContentProviderOperation.Builder builder = ContentProviderOperation
		.newInsert(Items.CONTENT_URI);

		long update_time = Calendar.getInstance().getTimeInMillis();
		String author_id = ContextUtil.getUserId(this);

//		ContentValues cv = new ContentValues();
		String randomId = null;
		if(this.itemId!=null)
			randomId = this.itemId;
		else
			randomId = StringUtils.randomUUID();
		builder.withValue(Items.ITEM_ID, randomId);
		builder.withValue(Items.CATEGORY, this.selectedCat);
		builder.withValue(Items.LOCATION_BASED, this.isLocationBased);

		if (this.isPublic)
			builder.withValue(Items.SHARE, this.getResources().getString(R.string.item_share_public));
		else
			builder.withValue(Items.SHARE, this.getResources().getString(R.string.item_share_private));

		List<String> types = new ArrayList<String>();
		if (this.isFileChoice)
			types.add(QuizActivity.QuizTypeImageMutiChoice.toString());
		if (this.isTextChoice)
			types.add(QuizActivity.QuizTypeTextMutiChoice.toString());
		if (this.isYesNoQuiz)
			types.add(QuizActivity.QuizTypeYesNoQuestion.toString());
		if(types.size()>0)
			builder.withValue(Items.QUESTION_TYPES, types.toString());

		if (this.logNote.getText() != null
				&& this.logNote.getText().length() > 0)
			builder.withValue(Items.NOTE, this.logNote.getText().toString());

		try {
			if (this.locationmanager == null)
				this.locationmanager = (LocationManager) getSystemService(LOCATION_SERVICE);
			Location loc = this.locationmanager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (loc != null) {
				builder.withValue(Items.LATITUTE, loc.getLatitude());
				builder.withValue(Items.LNGITUTE,loc.getLongitude());
				builder.withValue(Items.SPEED, loc.getSpeed());
			}

		} catch (Exception e) {

		}
		
		//change RelatedNote -> relatenote
		if(relatednote!=null && this.relatednote.getText() != null
				&& this.relatednote.getText().length() > 0)
			builder.withValue(Items.RELATE, this.relatednote.getText().toString());
		
		if(PlaceNote!=null && this.PlaceNote.getText() != null
				&& this.PlaceNote.getText().length() > 0)
			builder.withValue(Items.PLACE, this.PlaceNote.getText().toString());

		if (this.logAttach.getVisibility() == View.VISIBLE) {
			Bundle args = (Bundle) this.logAttach.getTag();
			String path = args
					.getString(MultiFileActionListener.FilePathKey);
			if(path!=null)
				builder.withValue(Items.ATTACHED, path);
		}

		Map<String, String> titles = this.getLanTitleValues();


		for (String key : titles.keySet()) {
			ContentProviderOperation.Builder titlebuilder = ContentProviderOperation
			.newInsert(Itemtitles.CONTENT_URI);

			titlebuilder.withValue(Itemtitles.ITEMTITLE_ID,
					StringUtils.randomUUID());
			titlebuilder.withValue(Itemtitles.ITEM_ID, randomId);
			titlebuilder.withValue(Itemtitles.LANGUAGE_ID, key);
			titlebuilder.withValue(Itemtitles.CONTENT, titles.get(key));
			subbatch.add(titlebuilder.build());
		}

		if(this.selectedLanCode!=null&&this.selectedLanCode.length()>0&&this.questionContent!=null&&this.questionContent.length()>0){
			ContentProviderOperation.Builder questionBuilder = ContentProviderOperation.newInsert(Questions.CONTENT_URI);
			questionBuilder.withValue(Questions.AUTHOR_ID, author_id);
			questionBuilder.withValue(Questions.ITEM_ID, randomId);
			questionBuilder.withValue(Questions.LANGUAGE_CODE, this.selectedLanCode);
			questionBuilder.withValue(Questions.CONTENT, this.questionContent);
			questionBuilder.withValue(Questions.QUESTION_ID, StringUtils.randomUUID());
			questionBuilder.withValue(Questions.STATE, Questions.NotAnsweredState);
			questionBuilder.withValue(Questions.UPDATE_TIME, update_time);
			subbatch.add(questionBuilder.build());
		}


//		if (this.tags != null) {
//			for (int i = 0; i < tags.size(); i++) {
//				ContentProviderOperation.Builder tagbuilder = ContentProviderOperation
//				.newInsert(Itemtags.CONTENT_URI);
//				tagbuilder.withValue(Itemtags.ITEM_ID, randomId);
//				tagbuilder.withValue(Itemtags.ITEMTAG_ID, StringUtils.randomUUID());
//				tagbuilder.withValue(Itemtags.TAG, tags.get(i));
//				subbatch.add(tagbuilder.build());
//			}
//		}

		if(this.tags!=null){
			String t = "";
			for(int i=0;i<tags.size();i++){
				t = t+tags.get(i);
				if(i!=(tags.size()-1))
					t = t+",";
			}
			if(t!="")
				builder.withValue(Items.TAG, t);
		}
		builder.withValue(Items.AUTHOR_ID, author_id);
		builder.withValue(Items.UPDATE_TIME, update_time);
		builder.withValue(Items.UPDATED, update_time);
		builder.withValue(Items.DISABLED, 0);
		if(this.itemId!=null&&!Items.SYNC_TYPE_CLIENT_INSERT.equals(this.syncType))
			builder.withValue(Items.SYNC_TYPE, SyncColumns.SYNC_TYPE_CLIENT_UPDATE);
		else
			builder.withValue(Items.SYNC_TYPE, SyncColumns.SYNC_TYPE_CLIENT_INSERT);

		subbatch.add(builder.build());
		try{
			this.getContentResolver().applyBatch(LearningLogContract.CONTENT_AUTHORITY, subbatch);
			resultHandler.sendEmptyMessage(1);
		}catch(Exception e){
			Log.e("error message",e.getMessage());
			resultHandler.sendEmptyMessage(2);
		}

//		Log.d(Constants.LOG_TAG, "" + response.getStatusLine().getStatusCode());
//		Integer result = response.getStatusLine().getStatusCode() == 200 ? 1
//				: 2;
//		if (Constants.Item_SUCCESS.equals(result)) {
//			resultHandler.sendEmptyMessage(1);
//			// UIUtils.goSync(this);
//		} else {
//			resultHandler.sendEmptyMessage(2);
//		}


//		this.mHandler.startUpdate(Quizs.buildQuizUri(quizId), cv);
	}

	// public void onCameraClick(View view) {
	// if (!Environment.getExternalStorageState().equals(
	// Environment.MEDIA_MOUNTED)) {
	// Toast.makeText(NewLogActivity.this, R.string.info_txt_no_sd_card,
	// Toast.LENGTH_LONG).show();
	// return;
	// }
	//
	// try {
	// Intent intent = new Intent();
	// intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
	// File tempfile = File.createTempFile("learninglog", ".jpg",
	// new File("/sdcard/DCIM/Camera/"));
	// intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempfile));
	// startActivityForResult(intent, Camera_REQUEST_CODE);
	// } catch (IOException e) {
	// Log.e(TAG, "can not open camera", e);
	// }
	// }
	//
	// public void onMicClick(View view){
	// if (!Environment.getExternalStorageState().equals(
	// Environment.MEDIA_MOUNTED)) {
	// Toast.makeText(NewLogActivity.this,
	// R.string.info_txt_no_sd_card, Toast.LENGTH_LONG).show();
	// return;
	// }
	// Intent intent = new Intent();
	// intent.setClass(NewLogActivity.this, AudioRecorder.class);
	// startActivityForResult(intent, AUDIO_REQUEST_CODE);
	// }

	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		switch (id) {
		case DialogAttachAction: {
			ImageView iv = (ImageView) dialog
					.findViewById(R.id.attach_option_image);
			iv.setTag(args);
			Map<String, Object> map = this.getMultiFileParams(args);
			Bitmap bp = (Bitmap) map.get("bitmap");
			String filename = (String) map.get("filename");
			Long filesize = (Long) map.get("filesize");
			if (bp != null && filename != null && filesize != null) {
				iv.setImageBitmap(bp);
			}
		}
			break;
		default:
			super.onPrepareDialog(id, dialog, args);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
		case DialogAttachAction: {
			LayoutInflater layoutInflater = LayoutInflater.from(this);
			View v = layoutInflater.inflate(
					R.layout.alert_dialog_attach_option, null);
			ImageView iv = (ImageView) v.findViewById(R.id.attach_option_image);
			iv.setTag(args);
			// ImageView iv = (ImageView)v.findViewById(R.id.attach_item_img);
			Map<String, Object> map = this.getMultiFileParams(args);
			Bitmap bp = (Bitmap) map.get("bitmap");
			String filename = (String) map.get("filename");
			Long filesize = (Long) map.get("filesize");
			if (bp != null && filename != null && filesize != null) {
				iv.setImageBitmap(bp);
				return new AlertDialog.Builder(this)
						.setTitle(R.string.dialog_attach_option_title)
						.setView(v)
						.setNeutralButton(
								R.string.dialog_attach_option_btn_view,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Dialog d = (Dialog) dialog;
										ImageView iv = (ImageView) d
												.findViewById(R.id.attach_option_image);
										Bundle args = (Bundle) iv.getTag();
										if (args == null)
											return;
										String path = args
												.getString(MultiFileActionListener.FilePathKey);
										String type = args
												.getString(MultiFileActionListener.FileTypeKey);
										if (Constants.FileTypeAudio
												.equals(type)) {
											Intent intent = new Intent(
													NewLogActivity.this,
													AudioPlayer.class);
											intent.setData(Uri
													.fromFile(new File(path)));
											startActivity(intent);
										} else if (Constants.FileTypeImage
												.equals(type)) {
											Intent intent = new Intent(
													NewLogActivity.this,
													ShowPhoto.class);
											intent.setData(Uri
													.fromFile(new File(path)));
											startActivity(intent);
										} else if (Constants.FileTypeVideo
												.equals(type)) {
											Intent intent = new Intent(
													NewLogActivity.this,
													VideoPlayer.class);
											intent.setData(Uri
													.fromFile(new File(path)));
											startActivity(intent);
										}
									}
								})
						.setPositiveButton(
								R.string.dialog_attach_option_btn_delete,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										logAttach.setVisibility(View.GONE);
									}
								})
						.setNegativeButton(
								R.string.dialog_attach_option_btn_back,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {

									}
								}).create();
			}
		}
		default:
			return super.onCreateDialog(id, args);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DialogCategoryList: {
			int sel = 0;
			for (int j = 0; j < this.cate_values.length; j++) {
				if (this.selectedCat.equals(this.cate_values[j])) {
					sel = j;
					break;
				}
			}
			return new AlertDialog.Builder(this)
					.setTitle(R.string.item_category_label)
					.setSingleChoiceItems(this.cate_names, sel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									selectedCat = cate_values[whichButton];
								}
							})
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).create();
		}
		case DialogSettingList: {
			return new AlertDialog.Builder(NewLogActivity.this)
					.setIcon(R.drawable.ic_title_setting)
					.setTitle(R.string.description_setting)
					.setMultiChoiceItems(
							R.array.item_options,
							new boolean[] { this.isPublic,
									this.isLocationBased, this.isTextChoice,
									this.isFileChoice, this.isYesNoQuiz },
							new DialogInterface.OnMultiChoiceClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton, boolean isChecked) {
									switch (whichButton) {
									case 0:
										isPublic = isChecked;
										break;
									case 1:
										isLocationBased = isChecked;
										break;
									case 2:
										isTextChoice = isChecked;
										break;
									case 3:
										isFileChoice = isChecked;
										break;
									case 4:
										isYesNoQuiz = isChecked;
										break;
									}
								}
							})
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).create();
		}
		case DialogTagList: {
			LayoutInflater factory = LayoutInflater.from(this);
			final View settingView = factory.inflate(
					R.layout.alert_dialog_log_setting, null);
			Spinner spanCategory = (Spinner) settingView
					.findViewById(R.id.spn_category);
			ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(
					this, android.R.layout.simple_spinner_item, this.cate_names);
			categoryAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spanCategory.setAdapter(categoryAdapter);
			EditText tagEdit = (EditText) settingView
					.findViewById(R.id.log_tag_edit);
			tagEdit.setText(Lists.arrayToString(this.tags));

			return new AlertDialog.Builder(NewLogActivity.this)
					.setIcon(R.drawable.ic_title_category_default)
					.setTitle(R.string.description_category_tag)
					.setView(settingView)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									Spinner spanCategory = (Spinner) settingView
											.findViewById(R.id.spn_category);
									EditText tagEdit = (EditText) settingView
											.findViewById(R.id.log_tag_edit);
									tags = Lists.stringToArray(tagEdit
											.getText().toString());
									selectedCat = cate_values[spanCategory
											.getSelectedItemPosition()];
								}
							}).create();
		}
		case DialogAttachList: {
			ListAdapter adapter = new SimpleAdapter(this, this.getAttachData(),
					R.layout.alert_dialog_log_attach_item, new String[] {
							"img", "txt" }, new int[] { R.id.attach_item_img,
							R.id.attach_item_text });
			DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					switch (which) {
					case 0: {
						try {
							File f = new File("/sdcard/DCIM/Camera/");
							if(!f.exists()){
								boolean existed = f.mkdirs();
								if(!existed){
									Toast.makeText(NewLogActivity.this, "Can not create director", Toast.LENGTH_SHORT).show();
									return;
								}
							}
							Intent intent = new Intent();
							intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
							File tempfile = File.createTempFile("learninglog",
									".jpg", new File("/sdcard/DCIM/Camera/"));
							intent.putExtra(MediaStore.EXTRA_OUTPUT,
									Uri.fromFile(tempfile));
							startActivityForResult(intent, Camera_REQUEST_CODE);
						} catch (IOException e) {
							Log.e(TAG, "can not open camera", e);
						}
					}
						break;
					case 1: {
						if (!Environment.getExternalStorageState().equals(
								Environment.MEDIA_MOUNTED)) {
							Toast.makeText(NewLogActivity.this,
									R.string.info_txt_no_sd_card,
									Toast.LENGTH_LONG).show();
							return;
						}
						Intent intent = new Intent();
						intent.setClass(NewLogActivity.this,
								AudioRecorder.class);
						startActivityForResult(intent, AUDIO_REQUEST_CODE);
					}
						break;
					case 2: {
						Intent intent = new Intent();
						intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
						intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
						startActivityForResult(intent, VIDEO_REQUEST_CODE);
					}
						break;
					case 3: {
						Intent intent = new Intent();
						intent.setType("*/*");
						intent.setAction(Intent.ACTION_GET_CONTENT);
						startActivityForResult(
								Intent.createChooser(intent, "Select Picture"),
								Gallery_REQUEST_CODE);
					}
						break;
					}
				};
			};
			return new AlertDialog.Builder(NewLogActivity.this).setAdapter(
					adapter, clickListener).create();
		}
		case DialogQuestionAction:{
			LayoutInflater factory = LayoutInflater.from(this);
			final View questionView = factory.inflate(
					R.layout.alert_dialog_question, null);
			Spinner spanLan = (Spinner) questionView
					.findViewById(R.id.spn_language);
			ArrayAdapter<String> lanAdapter = new ArrayAdapter<String>(
					this, android.R.layout.simple_spinner_item, this.lan_names);
			lanAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spanLan.setAdapter(lanAdapter);

			return new AlertDialog.Builder(NewLogActivity.this)
					.setIcon(R.drawable.ic_title_question)
					.setTitle(R.string.description_question)
					.setView(questionView)
//					.setNegativeButton(R.string.alert_dialog_reset, new DialogInterface.OnClickListener() {
//
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							EditText ques = (EditText)questionView.findViewById(R.id.txt_question_content);
//							ques.setText("");
//						}
//
//					})
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									Spinner spanLan = (Spinner) questionView.findViewById(R.id.spn_language);
									int index = spanLan.getSelectedItemPosition();
									selectedLanCode = lan_values.get(index);
									EditText ques = (EditText)questionView.findViewById(R.id.txt_question_content);
									if(ques.getText()!=null)
										questionContent = ques.getText().toString();
									Log.e("learninglog", index+"  "+selectedLanCode+"  "+questionContent);
								}
							})
					.create();
		}

		default:
			return null;
		}
	}

	private List<Map<String, Object>> getAttachData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("img", R.drawable.camera);
		map.put("txt", "Photoes");
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.mic);
		map.put("txt", "Audios");
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.videorecord);
		map.put("txt", "Videos");
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.folder);
		map.put("txt", "Files");
		list.add(map);

		return list;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {
			if (requestCode == Gallery_REQUEST_CODE) {
				try {
					final ContentResolver cr = this.getContentResolver();
					Uri uri = data.getData();
					Cursor c = cr.query(uri, new String[] {
							MediaStore.MediaColumns.DATA,
							MediaStore.MediaColumns.MIME_TYPE }, null, null,
							null);
					String filetype = "";
					String path = "";
					Bitmap bp = null;
					try {
						if (c.moveToFirst()) {
							path = c.getString(0);
							String mimetype = c.getString(1);
							if (mimetype.contains("video")) {
								filetype = Constants.FileTypeVideo;
								bp = ThumbnailUtils.createVideoThumbnail(path,
										MediaStore.Video.Thumbnails.MICRO_KIND);
							} else if (mimetype.contains("audio")) {
								filetype = Constants.FileTypeAudio;
								bp = BitmapFactory.decodeResource(
										this.getResources(),
										R.drawable.audio_file_middle);
							} else if (mimetype.contains("image")) {
								filetype = Constants.FileTypeImage;
								Bitmap bitmap = BitmapFactory.decodeFile(path);
								bp = ThumbnailUtils.extractThumbnail(bitmap,
										new Double(screenWidth
												* attachImageWidthScale)
												.intValue(),
										new Double(screenWidth
												* attachImageHeightScale)
												.intValue());
							}
						}
					} finally {
						c.close();
					}

					if (filetype != null && filetype.length() > 0
							&& path != null && path.length() > 0 && bp != null) {
						this.logAttach.setVisibility(View.VISIBLE);
						Bundle args = new Bundle();
						args.putString(MultiFileActionListener.FileTypeKey,
								filetype);
						args.putString(MultiFileActionListener.FilePathKey,
								path);
						logAttach.setTag(args);
						logAttach
								.setOnClickListener(new MultiFileActionListener());
						logAttach.setVisibility(View.VISIBLE);
						this.logAttach.setImageBitmap(bp);
					}

				} catch (Exception e) {
					Log.e(TAG, "Cannot obtain photo", e);
				}
			} else if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
				ArrayList<String> matches = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				String str = "";
				if (matches != null && matches.size() > 0) {
					for (String v : matches) {
						str = str + v;
					}
				}
			} else if (requestCode == Camera_REQUEST_CODE) {
				final ContentResolver cr = getContentResolver();
				final String[] imageproject = new String[] {
						MediaStore.Images.ImageColumns._ID,
						MediaStore.Images.ImageColumns.DATE_TAKEN,
						MediaStore.Images.ImageColumns.DATA };
				Cursor imagecursor = cr.query(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
						imageproject, null, null, imageproject[1] + " DESC");
				try {
					if (imagecursor.moveToFirst()) {
						String uristringpic = "content://media/external/images/media/"
								+ imagecursor.getInt(0);
						Uri newuri = Uri.parse(uristringpic);
						ImageView icon = (ImageView) findViewById(R.id.log_attach);
						Bitmap bitmap = BitmapFactory.decodeStream(cr
								.openInputStream(newuri));
						icon.setImageBitmap(ThumbnailUtils.extractThumbnail(
								bitmap,
								new Double(screenWidth * attachImageWidthScale)
										.intValue(),
								new Double(screenWidth * attachImageHeightScale)
										.intValue()));
						icon.setVisibility(View.VISIBLE);
						Bundle args = new Bundle();
						args.putString(MultiFileActionListener.FileTypeKey,
								Constants.FileTypeImage);
						args.putString(MultiFileActionListener.FilePathKey,
								imagecursor.getString(2));
						icon.setTag(args);

						icon.setOnClickListener(new MultiFileActionListener());
					}
				} catch (Exception e) {
					Log.e(TAG, "file not found", e);
				} finally {
					imagecursor.close();
				}
			} else if (requestCode == VIDEO_REQUEST_CODE) {
				final ContentResolver cr = getContentResolver();
				final String[] videoproject = new String[] {
						MediaStore.Video.VideoColumns._ID,
						MediaStore.Video.VideoColumns.DATE_TAKEN,
						MediaStore.Video.VideoColumns.DATA };
				Cursor vcursor = cr.query(
						MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
						videoproject, null, null, videoproject[1] + " DESC");
				try {
					if (vcursor.moveToFirst()) {
						String loc = vcursor.getString(2);
						Bitmap b = ThumbnailUtils.createVideoThumbnail(loc,
								MediaStore.Video.Thumbnails.MICRO_KIND);
						ImageView icon = (ImageView) findViewById(R.id.log_attach);
						icon.setImageBitmap(b);
						Bundle args = new Bundle();
						args.putString(MultiFileActionListener.FileTypeKey,
								Constants.FileTypeVideo);
						args.putString(MultiFileActionListener.FilePathKey,
								vcursor.getString(2));
						icon.setTag(args);

						icon.setOnClickListener(new MultiFileActionListener());
						icon.setVisibility(View.VISIBLE);
					}
				} catch (Exception e) {
					Log.e(TAG, "file not found", e);
				} finally {
					vcursor.close();
				}

			} else if (requestCode == AUDIO_REQUEST_CODE) {
				String filepath = data.getStringExtra("filepath");
				if (filepath != null) {
					this.logAttach.setVisibility(View.VISIBLE);
					Bundle args = new Bundle();
					args.putString(MultiFileActionListener.FileTypeKey,
							Constants.FileTypeAudio);
					args.putString(MultiFileActionListener.FilePathKey,
							filepath);
					logAttach.setTag(args);
					logAttach.setOnClickListener(new MultiFileActionListener());
					logAttach.setVisibility(View.VISIBLE);
					this.logAttach.setImageBitmap(BitmapFactory.decodeResource(
							this.getResources(), R.drawable.audio_file_middle));
				}
			}
		}
	}

	private void setAttachFile(Intent data) {
		try {
			final ContentResolver cr = this.getContentResolver();
			Uri uri = data.getData();
			if (uri == null && data.getExtras() != null) {
				String str_uri = data.getExtras().getString(
						"android.intent.extra.STREAM");
				if (str_uri != null)
					uri = Uri.parse(str_uri);
			}
			if (uri == null)
				return;
			Cursor c = cr.query(uri, new String[] {
					MediaStore.MediaColumns.DATA,
					MediaStore.MediaColumns.MIME_TYPE }, null, null, null);
			String filetype = "";
			String path = "";
			Bitmap bp = null;
			try {
				if (c.moveToFirst()) {
					path = c.getString(0);
					String mimetype = c.getString(1);
					if (mimetype.contains("video")) {
						filetype = Constants.FileTypeVideo;
						bp = ThumbnailUtils.createVideoThumbnail(path,
								MediaStore.Video.Thumbnails.MICRO_KIND);
					} else if (mimetype.contains("audio")) {
						filetype = Constants.FileTypeAudio;
						bp = BitmapFactory.decodeResource(this.getResources(),
								R.drawable.audio_file_middle);
					} else if (mimetype.contains("image")) {
						filetype = Constants.FileTypeImage;
						Bitmap bitmap = BitmapFactory.decodeFile(path);
						bp = ThumbnailUtils.extractThumbnail(bitmap,
								new Double(screenWidth * attachImageWidthScale)
										.intValue(), new Double(screenWidth
										* attachImageHeightScale).intValue());
					}
				}
			} finally {
				c.close();
			}

			if (filetype != null && filetype.length() > 0 && path != null
					&& path.length() > 0 && bp != null) {
				this.logAttach.setVisibility(View.VISIBLE);
				Bundle args = new Bundle();
				args.putString(MultiFileActionListener.FileTypeKey, filetype);
				args.putString(MultiFileActionListener.FilePathKey, path);
				logAttach.setTag(args);
				logAttach.setOnClickListener(new MultiFileActionListener());
				logAttach.setVisibility(View.VISIBLE);
				this.logAttach.setImageBitmap(bp);
			}

		} catch (Exception e) {
			Log.e(TAG, "Cannot obtain photo", e);
		}
	}

	@Override
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		if (token == SettingLanguageQuery._TOKEN) {
			this.onTitleQueryComplete(cursor);
		} else if (token == SettingQuery._TOKEN) {
			this.onCategoryQueryComplete(cursor);
		} else if (token == ItemsQuery._TOKEN) {
			this.onItemQueryComplete(cursor);
		} else if (token == ItemtitlesQuery._TOKEN) {
			this.onItemtitleQueryComplete(cursor);
		} else if (token == ItemtagsQuery._TOKEN) {
			this.onItemtagQueryComplete(cursor);
		} /**else if (token == ItemRelatedQuery._TOKEN) {
			this.onItemRelatedQueryComplete(cursor);
		}*/else if (cursor != null) {
			cursor.close();
		}

		if (this.pd != null && this.mSettingLanguageCursor
				&& this.mSettingCursor) {
			if (this.itemId != null) {
				if (this.mItemCursor && this.mItemtagCursor
						&& this.mItemtitleCursor)
					pd.dismiss();
			} else if (pd != null && pd.isShowing()) {
				pd.dismiss();
			}
		}
	}

	private void onTitleQueryComplete(Cursor cursor) {
		try {
			this.mSettingCursor = true;
			if (!cursor.moveToFirst())
				return;
//			this.lan_names = new String[cursor.getCount()];
//			this.lan_values = new String[cursor.getCount()];
//			int i = 0;
			do {
				// LinearLayout titleLayout = (LinearLayout) this
				// .findViewById(R.id.log_title_block);
				String content = cursor.getString(SettingLanguageQuery.NAME);
				String code = cursor.getString(SettingLanguageQuery.CODE);
				String language_id = cursor.getString(SettingLanguageQuery.LANGUAGE_ID);
				int field = cursor.getInt(SettingLanguageQuery.FIELD);

//				this.lan_names[i] = content;
//				this.lan_values[i] = code;
				if(field == Settings.SETTING_STUDYLAN_FIELD_ID){
					this.lan_names.add(content);
					this.lan_values.add(code);
				}
				this.addTitleField(code, language_id, content);
//				i++;
				// LinearLayout titleBlock = (LinearLayout) this
				// .getLayoutInflater().inflate(R.layout.log_edit_title,
				// null);
				// TextView name = (TextView) titleBlock
				// .findViewById(R.id.txt_title_name);
				// EditText title = (EditText) titleBlock
				// .findViewById(R.id.txt_title_content);
				// name.setText(content);
				// title.setTag(code);
				// titleLayout.addView(titleBlock);
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}
	}
	
	private IBinder ibinder = new IBinder() {

		@Override
		public String getInterfaceDescriptor() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean pingBinder() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isBinderAlive() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public IInterface queryLocalInterface(String descriptor) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void dump(FileDescriptor fd, String[] args)
				throws RemoteException {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean transact(int code, Parcel data, Parcel reply, int flags)
				throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void linkToDeath(DeathRecipient recipient, int flags)
				throws RemoteException {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
			// TODO Auto-generated method stub
			return false;
		}
/**
		@Override
		public void dumpAsync(FileDescriptor arg0, String[] arg1)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		*/

		@Override
		public void dumpAsync(FileDescriptor arg0, String[] arg1)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}
	};
	
	private EditText addTitleField(final String code, String language_id, String content) {
		LinearLayout titleLayout = (LinearLayout) this
				.findViewById(R.id.log_title_block);

		LinearLayout titleBlock = (LinearLayout) this.getLayoutInflater()
				.inflate(R.layout.log_edit_title, null);
		Button name = (Button) titleBlock.findViewById(R.id.txt_title_name);
		name.setTag(code);
		name.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Object t = v.getTag();
				if (t != null)
					onTranslateClick((String) t);
			}
		});

		EditText title = (EditText) titleBlock
				.findViewById(R.id.txt_title_content);

		name.setText(content);
		title.setTag(TAG_CODE_KEY, code);
		title.setTag(TAG_LANGUAGE_KEY, language_id);
		titleLayout.addView(titleBlock);
		return title;
	}

	protected void onTranslateClick(String code) {
		Map<String, String> titleValues = this.getCodeTitleValues();
		if(titleValues==null || titleValues.isEmpty())return;
		EditText text = this.getTitleEditText(code);
		if (text == null || !StringUtils.isBlank(text.getText().toString())) {
			return;
		}
		String testText = "";
		for (String t : titleValues.values()) {
			if (!StringUtils.isBlank(t)) {
				testText = t;
			}
		}
		if (StringUtils.isBlank(testText)) {
			return;
		}

		Toast.makeText(NewLogActivity.this, R.string.item_title_intranslate,
				Toast.LENGTH_SHORT).show();

		String uid = ContextUtil.getUserId(this);
		JSONObject jsonObject = new JSONObject(titleValues);
		String titles = jsonObject.toString();

		Thread thread = new TranslateThread(uid, code, titles);
		thread.start();
	}

	private void onCategoryQueryComplete(Cursor cursor) {
		try {
			this.mSettingLanguageCursor = true;
			if (!cursor.moveToFirst())
				return;
			this.cate_names = new String[cursor.getCount()];
			this.cate_values = new String[cursor.getCount()];
			int i = 0;
			do {
				this.cate_values[i] = cursor.getString(SettingQuery.CONTENT);
				this.cate_names[i] = cursor.getString(SettingQuery.NAME);
				i++;
			} while (cursor.moveToNext());

		} finally {
			cursor.close();
		}

	}

	public void onItemQueryComplete(Cursor cursor) {
		try {
			this.mItemCursor = true;
			if (!cursor.moveToFirst())
				return;

			// String photoUrl = cursor.getString(ItemsQuery.PHOTO_URL);
			// if(photoUrl!=null){
			// Bitmap bitmap =
			// BitmapUtil.getURLBitmap(ApiConstants.Image_Server_Url+photoUrl+ApiConstants.MiddleSizePostfix);
			// // Uri uri =
			// Uri.parse(ApiConstants.Image_Server_Url+photoUrl+ApiConstants.MiddleSizePostfix);
			// if (bitmap != null){
			// LinearLayout fileBlock =
			// (LinearLayout)this.findViewById(R.id.log_file_block);
			// fileBlock.setVisibility(View.VISIBLE);
			// ImageView imageView = new ImageView(this);
			// imageView.setImageBitmap(bitmap);
			// fileBlock.addView(imageView);
			// }
			// }

			String note = cursor.getString(ItemsQuery.NOTE);
			syncType = cursor.getInt(ItemsQuery.SYNC_TYPE);
			if (note != null) {
				this.logNote.setText(note);
			}
			String rnote = cursor.getString(ItemsQuery.RELATE);
			syncType = cursor.getInt(ItemsQuery.SYNC_TYPE);
			if (rnote != null && this.relatednote != null){
				this.relatednote.setText(rnote);
			}
			String pnote = cursor.getString(ItemsQuery.PLACE);
			syncType = cursor.getInt(ItemsQuery.SYNC_TYPE);
			if (pnote != null&&this.PlaceNote!=null){
				this.PlaceNote.setText(pnote);
			}
			String category = cursor.getString(ItemsQuery.Category);
			if (category != null)
				this.selectedCat = category;
		} finally {
			cursor.close();
		}
	}

	public void onItemtitleQueryComplete(Cursor cursor) {
		try {
			this.mItemtitleCursor = true;
			if (!cursor.moveToFirst())
				return;
			do {
				String code = cursor.getString(ItemtitlesQuery.CODE);
				EditText title = (EditText) this.getTitleEditText(code);
				String content = cursor.getString(ItemtitlesQuery.CONTENT);
				String language_id = cursor.getString(SettingLanguageQuery.LANGUAGE_ID);
				if (title == null)
					title = this.addTitleField(code, language_id,
							cursor.getString(ItemtitlesQuery.NAME));
				title.setText(content);
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}
	}

	public void onItemtagQueryComplete(Cursor cursor) {
		try {
			this.mItemtagCursor = true;
			if (!cursor.moveToFirst())
				return;
			do {
				String tag = cursor.getString(ItemtagsQuery.TAG);
				this.tags.add(tag);
			} while (cursor.moveToNext());

		} finally {
			cursor.close();
		}
	}

	public void onItemRelatedQueryComplete(Cursor cursor){
		try{
			this.mItemRelatedCursor = true;
			if(!cursor.moveToFirst())
				return;
			do{
				String relate = cursor.getString(ItemRelatedQuery.RELATE);
				this.logNote.setText(relate);
			}while(cursor.moveToNext());
		}finally{
				cursor.close();
		}
	}
	
	public void onItemPlaceQueryComplete(Cursor cursor){
		try {
			this.mItemPlaceCursor = true;
			if(!cursor.moveToFirst())
				return;
			do{
				String Place = cursor.getString(ItemPlaceQuery.PLACE);
				this.logNote.setText(Place);
			}while(cursor.moveToNext());
		}finally{
			cursor.close();
		}
	}

	public Map<String, String> getLanTitleValues() {
		int count = titleLayout.getChildCount();
		Map<String, String> titles = new HashMap<String, String>();
		for (int i = 0; i < count; i++) {
			View view = titleLayout.getChildAt(i);
			if (view instanceof LinearLayout) {
				LinearLayout child = (LinearLayout) view;
				int ac = child.getChildCount();
				for (int j = 0; j < ac; j++) {
					View cv = child.getChildAt(j);
					if (cv instanceof EditText) {
						EditText et = (EditText) cv;
						if (et.getText() != null && et.getText().length() > 0
								&& et.getTag(TAG_LANGUAGE_KEY) != null) {
							titles.put(et.getTag(TAG_LANGUAGE_KEY).toString(), et.getText()
									.toString());
						}
					}
				}
			}
		}
		return titles;
	}

	public Map<String, String> getCodeTitleValues() {
		int count = titleLayout.getChildCount();
		Map<String, String> titles = new HashMap<String, String>();
		for (int i = 0; i < count; i++) {
			View view = titleLayout.getChildAt(i);
			if (view instanceof LinearLayout) {
				LinearLayout child = (LinearLayout) view;
				int ac = child.getChildCount();
				for (int j = 0; j < ac; j++) {
					View cv = child.getChildAt(j);
					if (cv instanceof EditText) {
						EditText et = (EditText) cv;
						if (et.getText() != null && et.getText().length() > 0
								&& et.getTag(TAG_CODE_KEY) != null) {
							titles.put(et.getTag(TAG_CODE_KEY).toString(), et.getText()
									.toString());
						}
					}
				}
			}
		}
		return titles;
	}

	public EditText getTitleEditText(String code) {
		int count = titleLayout.getChildCount();
		for (int i = 0; i < count; i++) {
			View view = titleLayout.getChildAt(i);
			if (view instanceof LinearLayout) {
				LinearLayout child = (LinearLayout) view;
				int ac = child.getChildCount();
				for (int j = 0; j < ac; j++) {
					View cv = child.getChildAt(j);
					if (cv instanceof EditText) {
						EditText et = (EditText) cv;
						if (code.equals(et.getTag(TAG_CODE_KEY)))
							return et;
					}
				}
			}
		}
		return null;
	}

	public class MultiFileActionListener implements OnClickListener {
		public static final String FileTypeKey = "filetype";
		public static final String FilePathKey = "filepath";

		// public static final String BitmapKey = "bitmap";

		@Override
		public void onClick(View v) {
			showDialog(DialogAttachAction, (Bundle) v.getTag());
		}
	}

	public Map<String, Object> getMultiFileParams(Bundle args) {
		Map<String, Object> map = new HashMap<String, Object>();
		String filetype = args.getString("filetype");
		String filepath = args.getString("filepath");
		Bitmap bp = null;
		try {
			if (Constants.FileTypeVideo.equals(filetype)) {
				bp = ThumbnailUtils.createVideoThumbnail(filepath,
						MediaStore.Video.Thumbnails.MICRO_KIND);
			} else if (Constants.FileTypeAudio.equals(filetype)) {
				bp = BitmapFactory.decodeResource(this.getResources(),
						R.drawable.audio_file_large);
			} else if (Constants.FileTypeImage.equals(filetype)) {
				bp = ThumbnailUtils.extractThumbnail(
						BitmapFactory.decodeFile(filepath),
						(int) (this.screenWidth * attachImageWidthScale),
						(int) (this.screenWidth * attachImageHeightScale));
			}
			File file = new File(filepath);
			if (bp != null)
				map.put("bitmap", bp);
			map.put("filename", file.getCanonicalPath());
			map.put("filesize", file.length());
		} catch (Exception e) {

		}
		return map;
	}

	private interface SettingQuery {
		public static String[] PROJECTION = { Settings.AUTHOR_ID,
				Settings.CONTENT, Settings.NAME, Settings.FIELD, Settings.NUM };
		public static int _TOKEN = 1;

		int CONTENT = 1;
		int NAME = 2;
	}

	private interface SettingLanguageQuery {
		public static String[] PROJECTION = { Settings.AUTHOR_ID,
				Settings.CONTENT, Settings.NAME, Languages.CODE,Languages.LANGUAGE_ID,
				Settings.FIELD, Settings.NUM };
		public static int _TOKEN = 2;

		int NAME = 2;
		int CODE = 3;
		int LANGUAGE_ID = 4;
		int FIELD = 5;
	}

	private interface ItemsQuery {
		int _TOKEN = 3;
		String[] PROJECTION = { BaseColumns._ID, Items.ITEM_ID,
				Items.NICK_NAME, Items.PHOTO_URL, Items.FILE_TYPE, Items.TAG,
				Items.NOTE,
				Items.PLACE,
				Items.RELATE,
				Items.LNGITUTE, Items.LATITUTE, Items.UPDATE_TIME,
				Items.CATEGORY, Items.SYNC_TYPE };
		int NOTE = 6;
		int PLACE = 7;
		int RELATE = 8;
		int Category = 10;
		int SYNC_TYPE = 11;
	}

	private interface ItemtitlesQuery {
		int _TOKEN = 4;
		String[] PROJECTION = { Itemtitles._ID, Itemtitles.ITEMTITLE_ID,
				Itemtitles.ITEM_ID, Itemtitles.LANGUAGE_ID, Itemtitles.CONTENT,
				Languages.NAME, Languages.CODE };
		int CONTENT = 4;
		int NAME = 5;
		int CODE = 6;
	}

	private interface ItemtagsQuery {
		int _TOKEN = 5;
		String[] PROJECTION = { Itemtags._ID, Itemtags.ITEMTAG_ID,
				Itemtags.ITEM_ID, Itemtags.TAG };

		int TAG = 3;
	}
	
	private interface ItemPlaceQuery {
		int PLACE = 7;
		int _TOKEN = 6;
		String[] PROJECTION = { ItemPlace._ID, ItemPlace.ITEM_ID, ItemPlace.PLACE};
	}
	
	private interface ItemRelatedQuery {
		int RELATE = 8;
		int _TOKEN = 7;
		String[] PROJECTION = { ItemRelate._ID,
				ItemRelate.ITEM_ID, ItemRelate.LANGUAGE_ID,ItemRelate.RELATE,
				Languages.CODE };
	}
	
	
	class PlaceThread extends Thread{
		Location location;
		Double lat = null;
		Double lng = null;
		public PlaceThread() {
			// TODO Auto-generated constructor stub
			super();
		}
		public PlaceThread(Location loc) {
			// TODO Auto-generated constructor stub
			super();
			this.location = loc;
		}
		ArrayList<String>types = new ArrayList<String>();

		@Override
		public void run() {
			PlaceNote.setText("");
			if(location == null)
				return;
			lat = location.getLatitude();
			lng = location.getLongitude();
			HttpPost httpPost2 = new HttpPost("https://maps.googleapis.com/maps/api/place/search/json?location="+lat+","+lng+"&radius="+radius+"&sensor=true&key="+APIKey1);
			DefaultHttpClient pClient = HttpClientFactory.createHttpClient();
			MultipartEntity pParams = new MultipartEntity();
			httpPost2.setEntity(pParams);
			try{
				HttpResponse response = pClient.execute(httpPost2);
				if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						InputStream instream = entity.getContent();
						String result = convertStreamToString(instream);
						instream.close();
						JSONObject pJson = new JSONObject(result);
						if(pJson!=null){
							JSONArray array = pJson.getJSONArray("results");
							if(array!=null){
								for(int i=0;i<array.length();i++){
									JSONObject o = array.getJSONObject(i);
									JSONArray typesArray = o.getJSONArray("types");
									boolean f= true;
									for(int j1 = 0;j1<typesArray.length();j1++){
										String type = typesArray.getString(j1);
										if(type.equals("sublocality"))
											f = false;
									}
									if(f){
										String type = o.getString("types");
										Scanner scan = new Scanner(type);
										scan.useDelimiter("\\s*,\\s*");
										while(scan.hasNext()){
											String vol = scan.next();
											String vol1 = strip(vol,"\\s\n\"[]");
											int flg = types.indexOf(vol1);
											if(flg != -1){
											}else{
											
												types.add(vol1);
												place.add(vol1);
											}
										}scan.close();
									}
								}
							}
							String str = new String();
							for(String obj : types)
								if(str == null)
									str += (String)obj;
								else
									str += "," + (String)obj;
							if(PlaceNote.getText().toString().length() == 0){
								final Message msg = new Message();
								Bundle bundle = new Bundle();
								bundle.putString("place", strip(str,","));
								msg.setData(bundle);
								msg.what = TRANSLATE_MESSAGE_SUCCESS;
								placeHandler.post(new Runnable(){
									public void run(){
										placeHandler.sendMessage(msg);
									}
								});
//								placeHandler.sendMessage(msg);
							}else{
							}
						}
					}
				}else{
					return;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			} finally{
				pClient.getConnectionManager().shutdown();
			}
		}
	}
	public static String stripStart(String str, String stripChars) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        int start = 0;
        if (stripChars == null) {
            while (start != strLen && Character.isWhitespace(str.charAt(start))) {
                start++;
            }
        } else if (stripChars.length() == 0) {
            return str;
        } else {
            while (start != strLen && stripChars.indexOf(str.charAt(start)) != -1) {
                start++;
            }
        }
        return str.substring(start);
    }

	public static String stripEnd(String str, String stripChars) {
        int end;
        if (str == null || (end = str.length()) == 0) {
            return str;
        }

        if (stripChars == null) {
            while (end != 0 && Character.isWhitespace(str.charAt(end - 1))) {
                end--;
            }
        } else if (stripChars.length() == 0) {
            return str;
        } else {
            while (end != 0 && stripChars.indexOf(str.charAt(end - 1)) != -1) {
                end--;
            }
        }
        return str.substring(0, end);
    }

    public static String strip(String name, String string){
    	String arg = new String();
    	String arg1 = new String();
    	arg = stripStart(name, string);
    	arg1 = stripEnd(arg, string);
    	return arg1;
    }

	private static String convertStreamToString(InputStream is)throws UnsupportedEncodingException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	class TranslateThread extends Thread {
		private String uid;
		private String target;
		private String titles;

		public TranslateThread(String uid, String target, String titles) {
			super();
			this.uid = uid;
			this.target = target;
			this.titles = titles;
		}

		@Override
		public void run() {
			DefaultHttpClient client = HttpClientFactory
					.getInstance(NewLogActivity.this);

			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			qparams.add(new BasicNameValuePair("uid", uid));
			qparams.add(new BasicNameValuePair("target", target));
			qparams.add(new BasicNameValuePair("titles", titles));

			HttpGet httpGet = new HttpGet(ApiConstants.TRANSLATE_TITLE_URI
					+ "?" + URLEncodedUtils.format(qparams, "UTF-8"));
			try {
				HttpResponse response = client.execute(httpGet);
				if (HttpStatus.SC_OK == response.getStatusLine()
						.getStatusCode()) {
					String result = EntityUtils.toString(response.getEntity());
					if (!StringUtils.isBlank(result)) {
						Message msg = new Message();
						Bundle bundle = new Bundle();
						bundle.putString("code", target);
						bundle.putString("result", result);
						msg.setData(bundle);
						msg.what = TRANSLATE_MESSAGE_SUCCESS;
						translateHandler.sendMessage(msg);
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public class MyLocationListener implements LocationListener {
		public Location location;

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
			locationProvider = locationmanager.getBestProvider(criteria, true);
			locationmanager.requestLocationUpdates(locationProvider, gps_circle_time, gps_circle_distance, locationlistener);

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
			locationProvider = locationmanager.getBestProvider(criteria, true);
			locationmanager.requestLocationUpdates(locationProvider, gps_circle_time, gps_circle_distance, locationlistener);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {

		}
	}
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStatusChanged(String s, int i, Bundle bundle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String s) {
		// TODO Auto-generated method stub

	}
}
