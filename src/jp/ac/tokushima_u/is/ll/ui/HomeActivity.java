package jp.ac.tokushima_u.is.ll.ui;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.c2dm.C2DMReceiver;
import jp.ac.tokushima_u.is.ll.io.SettingSyncThread;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Items;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Settings;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.SyncColumns;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Users;
import jp.ac.tokushima_u.is.ll.service.SyncService;
import jp.ac.tokushima_u.is.ll.sphinx.MainActivity;
import jp.ac.tokushima_u.is.ll.ui.nav.nav;
import jp.ac.tokushima_u.is.ll.ui.quiz.FourSelectedImageQuiz;
import jp.ac.tokushima_u.is.ll.util.Constants;
import jp.ac.tokushima_u.is.ll.util.ContextUtil;
import jp.ac.tokushima_u.is.ll.util.DetachableResultReceiver;
import jp.ac.tokushima_u.is.ll.util.FileUtil;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler.AsyncQueryListener;
import jp.ac.tokushima_u.is.ll.util.UIUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class HomeActivity extends Activity implements AsyncQueryListener,
		DetachableResultReceiver.Receiver {
	private State mState;
	private NotifyingAsyncQueryHandler mQueryHandler;
	private String userId;
	private String username;
	private String password;
//	private String quizId;
//	private Integer quizType;
//	private String quizItemId;
	private final static String TAG = "HomeActivityTag";
	
	private LocationManager locationmanager;
	private MyLocationListener locationlistener = new MyLocationListener();
	
	private static final int groupId = 0;
	private static final int startMenuItem = 0;
//	private static final int stopMenuItem = 1;
	private static final int logoutMenuItem = 2;
	private static final int settingMenuItem = 3;
	private static final int CacheMenuItem = 4;
	private static final int SphinxMenuItem = 5;
	
	private static final int ActivityResultSetting = R.id.ans1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!UIUtils.checkUser(HomeActivity.this)) {
			UIUtils.goLogin(HomeActivity.this);
			return;
		}
		
		this.setContentView(R.layout.activity_home);
		this.userId = this.getUserId();

		C2DMReceiver.refreshAppC2DMRegistrationState(this);

		mQueryHandler = new NotifyingAsyncQueryHandler(
				this.getContentResolver(), this);
		String settingorder = SyncColumns.UPDATED + " desc ";
		mQueryHandler.startQuery(SettingQuery._TOKEN, null,
				Users.buildUsersSettingUri(userId), SettingQuery.PROJECTION,
				null, null, settingorder);

		this.initLocation();
		
		UIUtils.goContextAware(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// 获取最后一次配置，有可能为空
		mState = (State) getLastNonConfigurationInstance();
		final boolean previousState = mState != null;

		if (previousState) {
			// Start listening for SyncService updates again
			mState.mReceiver.setReceiver(this);
			updateRefreshStatus();
		} else {
			mState = new State();
			mState.mReceiver.setReceiver(this);
//			onRefreshClick(null);
			UIUtils.goSync(HomeActivity.this, mState.mReceiver);
		}
		
//		String loc_setting = this.getResources().getString(
//				R.string.setting_location_prefer_key);
//		SharedPreferences settings = PreferenceManager
//				.getDefaultSharedPreferences(this);
//		
//		boolean loc_setting_flg = settings.getBoolean(loc_setting, true);
//		Log.e("learninglog", loc_setting_flg + " ===");
	}
	
	
	private void initLocation(){
		this.locationmanager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
		if (!locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getText(R.string.info_require_gps))
					.setCancelable(false).setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(
										final DialogInterface dialog,
										final int id) {
									launchGPSOptions();
								}
							}).setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(
										final DialogInterface dialog,
										final int id) {
									dialog.cancel();
								}
							});
			final AlertDialog alert = builder.create();
			alert.show();
		}
		/*
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
		criteria.setAltitudeRequired(true);
		criteria.setBearingRequired(false);
		criteria.setSpeedRequired(true);
		criteria.setCostAllowed(true);
		*/
		
//		String provider = locationmanager.getBestProvider(criteria, true);
//		locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.MinTime, Constants.MinDistance, locationlistener);
		locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000*60*10, 20, locationlistener);
	}
	

	private void launchGPSOptions() {
		Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS); 
        startActivity(intent); 
	}

	/** Handle "refresh" title-bar action. */
	public void onRefreshClick(View v) {
		// trigger off background sync
		UIUtils.goManualSync(HomeActivity.this, mState.mReceiver);
	}

	public void onLogListClick(View v) {
		final Intent intent = new Intent(HomeActivity.this, LogsActivity.class);
		this.startActivity(intent);
	}

	public void onQuizClick(View v) {
		final Intent intent = new Intent(HomeActivity.this, QuizActivity.class);
//		if (this.quizId != null && this.quizType != null) {
//			final Uri uri = Quizs.buildQuizUri(quizId);
//			intent.setData(uri);
//			intent.setAction(Intent.ACTION_VIEW);
//			intent.putExtra("quiztype", this.quizType);
//			if (this.quizItemId != null)
//				intent.putExtra("quizitemid", this.quizItemId);
//		}
		this.startActivity(intent);
	}

	public void onNavigate(View v){
//		Intent intent = new Intent(HomeActivity.this, nav.class);
		Intent intent = new Intent(HomeActivity.this, nav.class);
		intent.putExtra("userEmail", ContextUtil.getUsername(this));
		intent.putExtra("userPassword", ContextUtil.getPassword(this));
		startActivity(intent);
	}
	
	public void onSystemClick(View v){
//		Intent in = new Intent(Intent.ACTION_VIEW,Uri.parse(Constants.System_URL));
//		startActivity(in);
		
		Intent  intent = new Intent(HomeActivity.this, FourSelectedImageQuiz.class);
		this.startActivity(intent);
	}
	
	
	public void onHelperClick(View v){
		Intent in = new Intent(Intent.ACTION_VIEW,Uri.parse(Constants.Helper_URL));
		startActivity(in);
	}
	
	/** Handle "search" title-bar action. */
	public void onSearchClick(View v) {
		UIUtils.goSearch(this);
	}
	
	public void onLogEditClick(View v) {
		final Intent intent = new Intent();
		intent.setAction(Intent.ACTION_INSERT);
		intent.setData(Items.CONTENT_URI);
		this.startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
//		menu.add(groupId, startMenuItem, 0, "Start Background");
//		menu.add(groupId, stopMenuItem, 1, "Stop Background");
		menu.add(groupId, logoutMenuItem, 2, "Log Out");
		menu.add(groupId, settingMenuItem, 3, "Setting");
		menu.add(groupId,CacheMenuItem, 4, "Delete Cache");
		menu.add(groupId, SphinxMenuItem, 5, "Go Sphinx");
		menu.findItem(startMenuItem);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
    //		case startMenuItem:
    //			startBackGround(); break;
    //		case stopMenuItem:
    //			stopBackGround(); break;
    		case logoutMenuItem:
    			logout();break;
    		case settingMenuItem:{
    			Intent intent = new Intent(HomeActivity.this, PreferencesSetting.class);
    			this.startActivity(intent);
    //			this.startActivityForResult(intent, ActivityResultSetting);
    			break;
    			}
    		case CacheMenuItem:{
    			try{
    			    // 直接ファイルパスを指定する方法を行なってはならない
    			    // see: https://sites.google.com/a/techdoctranslator.com/jp/android/guide/data-storage
    			    String cachePath = getExternalCacheDir().toString();
    				FileUtil.deleteDirectory(cachePath);
    			}catch(Exception e){
    				
    			}
    			break;
    		}
    		case SphinxMenuItem:
    		    // Sphinx Code...
    		    Intent intent = new Intent(this, MainActivity.class);
    		    startActivity(intent);
    		    break;
		}
		return super.onOptionsItemSelected(item);
	}
	
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		if(resultCode == ActivityResultSetting){
//			String loc_setting = this.getResources().getString(R.string.setting_location_prefer_key);
//			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
//			boolean loc_setting_flg = settings.getBoolean(loc_setting, true);
//			Log.e(TAG, loc_setting_flg+"");
//		}
//	}

	private void logout(){
		SharedPreferences setting = this.getSharedPreferences(
				Constants.SETTING_INFOS_FILE, Context.MODE_PRIVATE);
		Editor editor = setting.edit();
		SharedPreferences CurrentLocation = getSharedPreferences(
				"DATALATANDLNG",Context.MODE_PRIVATE);
		Editor editor2 = CurrentLocation.edit();
		editor2.clear();
		
		
		String tempusername = setting.getString(Constants.SavedUserName, "");
		editor.clear();
		editor.putString(Constants.SavedUserName, tempusername);
		Log.e(TAG, setting.getString(Constants.SavedPassword, "empty"));
		editor.commit();
		Log.e(TAG, setting.getString(Constants.SavedPassword, "empty"));
		
		if(this.mQueryHandler==null)
			mQueryHandler = new NotifyingAsyncQueryHandler(
				this.getContentResolver(), this);
		
		this.mQueryHandler.startDelete(LearningLogContract.Items.CONTENT_URI);
		this.mQueryHandler.startDelete(LearningLogContract.Itemcomments.CONTENT_URI);
		this.mQueryHandler.startDelete(LearningLogContract.Itemtags.CONTENT_URI);
		this.mQueryHandler.startDelete(LearningLogContract.Itemtitles.CONTENT_URI);
		this.mQueryHandler.startDelete(LearningLogContract.Choices.CONTENT_URI);
		this.mQueryHandler.startDelete(LearningLogContract.Quizs.CONTENT_URI);
		this.mQueryHandler.startDelete(LearningLogContract.Settings.CONTENT_URI);
		this.mQueryHandler.startDelete(LearningLogContract.Profiles.CONTENT_URI);
		this.mQueryHandler.startDelete(LearningLogContract.Notifys.CONTENT_URI);
		this.mQueryHandler.startDelete(LearningLogContract.Languages.CONTENT_URI);
		UIUtils.goLogin(this);
		
	}
	
	
	private void updateRefreshStatus() {
		boolean isSyncing = mState.mSyncing;
		if(findViewById(R.id.btn_title_refresh)!=null)
			findViewById(R.id.btn_title_refresh).setVisibility(
				isSyncing ? View.GONE : View.VISIBLE);
		if(findViewById(R.id.title_refresh_progress)!=null)
			findViewById(R.id.title_refresh_progress).setVisibility(
				isSyncing ? View.VISIBLE : View.GONE);
	}

	/** {@inheritDoc} */
	public void onReceiveResult(int resultCode, Bundle resultData) {
		switch (resultCode) {
		case SyncService.STATUS_RUNNING: {
			mState.mSyncing = true;
			updateRefreshStatus();
			break;
		}
		case SyncService.STATUS_FINISHED: {
			mState.mSyncing = false;
			updateRefreshStatus();
			break;
		}
		case SyncService.STATUS_ERROR: {
			// Error happened down in SyncService, show as toast.
			mState.mSyncing = false;
			updateRefreshStatus();
			final String errorText = getString(R.string.toast_sync_error,
					resultData.getString(Intent.EXTRA_TEXT));
			Toast.makeText(HomeActivity.this, errorText, Toast.LENGTH_LONG)
					.show();
			break;
		}
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			default:
				Log.d(TAG, "setting filling success");
			}
		}
	};

	private static class State {
		public DetachableResultReceiver mReceiver;
		public boolean mSyncing = false;
		public boolean mNoResults = false;

		private State() {
			mReceiver = new DetachableResultReceiver(new Handler());
		}
	}

	@Override
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		if(cursor==null)
			return;
		if (token == SettingQuery._TOKEN) {
			Long update = null;
			try {
				if (cursor.moveToFirst()) {
					update = cursor.getLong(SettingQuery.Update);
				}
			} finally {
				if(cursor!=null)
					cursor.close();
			}
			SettingSyncThread thread = new SettingSyncThread(
					getUsername(), getPassword(), update, this.handler, this);
			thread.start();
		} 
//		else if (token == QuizQuery._TOKEN) {
//			try {
//				if (!cursor.moveToFirst())
//					return;
//				this.quizId = cursor.getString(QuizQuery.Quiz_ID);
//				this.quizType = cursor.getInt(QuizQuery.QUIZ_TYPE);
//				this.quizItemId = cursor.getString(QuizQuery.Item_ID);
//			} finally {
//				if(cursor!=null)
//				cursor.close();
//			}
//		} 
		else if (cursor != null) {
			cursor.close();
		}
	}

	private interface SettingQuery {
		public static String[] PROJECTION = { Settings.AUTHOR_ID, Settings.UPDATED,
				Settings.CONTENT, Settings.FIELD, Settings.NUM };
		int Update = 1;
		public static int _TOKEN = 1;
	}

//	private interface QuizQuery {
//		public static String[] PROJECTION = { Quizs.QUIZ_ID, Quizs.QUIZ_TYPE,
//				Quizs.Item_ID, Quizs.AUTHOR_ID, Quizs.QUIZ_CONTENT,
//				Quizs.ANSWER };
//		public static int _TOKEN = 2;
//
//		int Quiz_ID = 0;
//		int QUIZ_TYPE = 1;
//		int Item_ID = 2;
//	}

	public String getUsername() {
		if (this.username == null)
			this.username = ContextUtil.getUsername(this);
		return username;
	}

	public String getPassword() {
		if (password == null)
			this.password = ContextUtil.getPassword(this);
		return password;
	}

	public String getUserId() {
		if (userId == null)
			this.userId = ContextUtil.getUserId(this);
		return userId;
	}

}
