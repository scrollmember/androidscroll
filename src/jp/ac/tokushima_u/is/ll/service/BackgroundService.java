package jp.ac.tokushima_u.is.ll.service;


import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.ui.nav.nav;
import jp.ac.tokushima_u.is.ll.ui.HomeActivity;
import jp.ac.tokushima_u.is.ll.ui.LoginActivity;
import jp.ac.tokushima_u.is.ll.ui.QuizActivity;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.Constants;
import jp.ac.tokushima_u.is.ll.util.HttpClientFactory;
import jp.ac.tokushima_u.is.ll.util.ResponseConverter;
import jp.ac.tokushima_u.is.ll.util.UIUtils;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;


public class BackgroundService extends Service{

    private NotificationManager nm;
    private static final int NotificationID = 1222333;
    
    
	Location location;
	LocationManager locationmanager;
	private String username;
	private String password;
    
    private ConditionVariable mCondition;
    private boolean flg;
	
	@Override
	public void onCreate() {
		super.onCreate();
		nm = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
		flg = true;
		
		SharedPreferences setting = this.getSharedPreferences(
				Constants.SETTING_INFOS_FILE, Context.MODE_PRIVATE);
		this.username = setting.getString(Constants.SavedUserName, "");
		this.password = setting.getString(Constants.SavedPassword, "");
//		boolean locationFlg = setting.getBoolean(Constants.SavedLocationBackgroundRun, true);
		boolean locationFlg = true;
		
//		if(username==null||username.length()<=0||password==null||password.length()<=0){
		if (!UIUtils.checkUser(BackgroundService.this)) {
			 showNotification(R.drawable.login, R.string.login_notification, new Intent(BackgroundService.this, LoginActivity.class), Constants.LoginNotificationID, null, true);
			return;
		}else{
			 Intent intent =  new Intent(this, ContextAwareService.class);
			 this.startService(intent);
		}
		
		 showNotification(R.drawable.favicon, R.string.LLQStateText, new Intent(BackgroundService.this, HomeActivity.class), NotificationID, Notification.FLAG_NO_CLEAR, false);
		 
		
		
//		try{
//			if(locationFlg){
//				this.locationmanager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
//				
//				Criteria criteria = new Criteria();
//				criteria.setPowerRequirement(Criteria.POWER_LOW);
//				criteria.setAccuracy(Criteria.ACCURACY_FINE);
//				criteria.setAltitudeRequired(false);
//				criteria.setSpeedRequired(true);
//				criteria.setBearingRequired(false);
//				criteria.setCostAllowed(false);
//				
//				String provider = this.locationmanager.getBestProvider(criteria, true);
//				locationmanager.requestLocationUpdates(provider, Constants.MinTime, Constants.MinDistance, loctionlistener);
//			}
//			
//			Thread notifyingThread = new Thread(null, mTask, "LearningLogQService");
//			notifyingThread.start();
//			mCondition = new ConditionVariable(false);
//		}catch(Exception e){
//			if(e!=null)
//				Log.e(Constants.LOG_TAG, e.getMessage());
//		}
	}

	
	private LocationListener loctionlistener = new LocationListener(){
	
			@Override
			public void onLocationChanged(Location location) {
				updateWithNewLocation(location);
			}
	
			@Override
			public void onProviderDisabled(String provider) {
				
			}
	
			@Override
			public void onProviderEnabled(String provider) {
				
			}
	
			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				
			}
		};
  
	void updateWithNewLocation(Location loc){
		this.location = loc;
		try{
			DefaultHttpClient client = HttpClientFactory.createHttpClient();
			client.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username,password));
			HttpPost httpPost = new HttpPost(ApiConstants.Context_Aware_URL);
			MultipartEntity params = new MultipartEntity();
			params.addPart("latitude", new StringBody(loc.getLatitude()+""));
			params.addPart("longitude", new StringBody(loc.getLongitude()+""));
			params.addPart("speed", new StringBody(loc.getSpeed()+""));
			httpPost.setEntity(params);
			HttpResponse response =	client.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				String result = ResponseConverter.convertStreamToString(instream);
				if(result==null)
					return;
				
				instream.close();
				
				JSONObject json=new JSONObject(result);
				JSONObject object = json.getJSONObject("notifyform");
				Integer notifyMode = object.getInt("notifyMode");
				String itemnotifyid = object.getString("itemnotifyid");
				if(Constants.NotifyTypeTextQuiz.equals(notifyMode)){
					String quizid = object.getString("quizid");
					Integer alarmType = object.getInt("alarmType");
					Intent intent = new Intent(this, QuizActivity.class);
					intent.putExtra("quizid", quizid);
					intent.putExtra("alarmType", alarmType);
					intent.putExtra("itemnotifyid", itemnotifyid);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					Log.e(Constants.LOG_TAG, "quiz type location based quizid:"+quizid+"   itemnotifyid:"+itemnotifyid);
					this.showNotification(R.drawable.location_notify, R.string.Notify_Info_Context_Quiz, intent, Constants.QuizNotificationID, null, true);
				}else if(Constants.NotifyTypeMessage.equals(notifyMode)){
					JSONArray itemids = object.getJSONArray("itemids");
					itemids.length();
					Intent intent = new Intent(this, nav.class);
					intent.putExtra("userEmail", username);
					intent.putExtra("userPassword", password);
					intent.putExtra("lat", this.location.getLatitude());
					intent.putExtra("lng", this.location.getLongitude());
					intent.putExtra("itemnotifyid", itemnotifyid);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
					Log.e(Constants.LOG_TAG, "message type location based itemnotifyid:"+itemnotifyid);
					this.showNotification(R.drawable.object_notify, R.string.NavNotificationInfo, intent, Constants.NavNotificationID, null, true);
				}
			}
			
		}catch(Exception e){
			Log.e(Constants.LOG_TAG, e.getMessage());
		}
	}
	
	
//	private void vibrate(){
//		Vibrator vibrator = (Vibrator)this.getSystemService(VIBRATOR_SERVICE);
//		vibrator.vibrate(Constants.VibrateTime);
//	}
	
    @Override
    public void onDestroy() {
        nm.cancel(NotificationID);
        this.flg = false;
        // Stop the thread from generating further notifications
        mCondition.open();
        this.locationmanager.removeUpdates(loctionlistener);
        BackgroundService.this.stopSelf();
    }


	
    private Runnable mTask = new Runnable() {
    	private static final long cycleTime = 300000;
        public void run() {
//        	 showNotification(R.drawable.favicon, R.string.LLQStateText, new Intent(BackgroundService.this, MenuActivity.class), NotificationID, Notification.FLAG_NO_CLEAR);
        	 
            SharedPreferences setting = getSharedPreferences(Constants.SETTING_INFOS_FILE, Context.MODE_PRIVATE);
//        	flg = setting.getBoolean(Constants.SavedTimeBackgroundRun, true);
        	flg = true;
        	 
        	 while(true){
//				QuizCondition quizCon = new QuizCondition();
//				quizCon.setImageLevel(Constants.SmartPhoneLevel);
//				quizCon.setTimeflg(Constants.WithTimeFlg);
//				quizCon.setAlarmtype(Constants.TimeReuestType);
//        		 try{
//        			 QuizForm newform = quizService.composeQuizByUsername(username,
//     						password, quizCon);
//     				if(newform!=null&&!Constants.ErrorCode_No_Quiz.equals(newform.getErrorCode())){
//     					Intent intent = new Intent(BackgroundService.this, LearningLogTest.class);
//     					intent.putExtra("alarmtype", Constants.TimeReuestType);
//     					showNotification(R.drawable.time_notify, R.string.ReminderQuizNotificationInfo, intent, Constants.QuizNotificationID, null);
//     					vibrate();
//     				}
//        			 
//        			 Thread.sleep(cycleTime);
//        		 }catch(Exception e){
//        		 }
        		try{
        			DefaultHttpClient client = HttpClientFactory.createHttpClient();
        			client.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username,password));
        			HttpPost httpPost = new HttpPost(ApiConstants.Context_Aware_URL);
        			MultipartEntity params = new MultipartEntity();
        			if(location!=null){
        				params.addPart("latitude", new StringBody(location.getLatitude()+""));
        				params.addPart("longitude", new StringBody(location.getLongitude()+""));
        				params.addPart("speed", new StringBody(location.getSpeed()+""));
        			}
        			httpPost.setEntity(params);
        			HttpResponse response =	client.execute(httpPost);
        			HttpEntity entity = response.getEntity();
        			if (entity != null) {
        				InputStream instream = entity.getContent();
        				String result = ResponseConverter.convertStreamToString(instream);
        				if(result==null)
        					return;
        				instream.close();
        				
        				JSONObject json=new JSONObject(result);
        				JSONObject object = json.getJSONObject("notifyform");
        				Integer notifyMode = object.getInt("notifyMode");
        				String itemnotifyid = object.getString("itemnotifyid");
        				if(Constants.NotifyTypeTextQuiz.equals(notifyMode)){
        					String quizid = object.getString("quizid");
        					Integer alarmType = object.getInt("alarmType");
        					Intent intent = new Intent(BackgroundService.this, QuizActivity.class);
        					intent.putExtra("quizid", quizid);
        					intent.putExtra("itemnotifyid", itemnotifyid);
        					intent.putExtra("alarmType", alarmType);
        					intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        					showNotification(R.drawable.location_notify, R.string.Notify_Info_Context_Quiz, intent, Constants.QuizNotificationID, null, true);
        				}
        			}
        		}catch(Exception e){
        			if(e!=null&&e.getMessage()!=null)
        				Log.e(Constants.LOG_TAG, e.getMessage());
        		}
        		try{
        			Thread.sleep(cycleTime);
        		}catch(Exception e){
        			if(e!=null)
        				Log.e(Constants.LOG_TAG, e.getMessage());
        		}
//        		 flg = setting.getBoolean(Constants.SavedTimeBackgroundRun, true); 
        	 }
        } 	 
	};
    
    private void showNotification(int iconId, int textId, Intent intent, int notification_id, Integer flg, boolean isvibrate) {
        CharSequence text = getText(textId);

        Notification notification = new Notification(iconId, null, System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,intent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        if(isvibrate){
        	notification.defaults = Notification.DEFAULT_VIBRATE;
        	notification.vibrate = new long[]{Constants.VibrateTime};
        }

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.app_name),
                       text, contentIntent);
        
        if(flg!=null)
        	notification.flags = flg;

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        nm.notify(notification_id, notification);
    }

	
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}


	//TODO
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply,
                int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };
}
