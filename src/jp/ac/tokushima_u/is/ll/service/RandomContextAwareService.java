package jp.ac.tokushima_u.is.ll.service;

import java.sql.Time;
import java.util.Calendar;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Notifys;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Profiles;
import jp.ac.tokushima_u.is.ll.ui.LoginActivity;
import jp.ac.tokushima_u.is.ll.ui.QuizActivity;
import jp.ac.tokushima_u.is.ll.util.Constants;
import jp.ac.tokushima_u.is.ll.util.ContextUtil;
import jp.ac.tokushima_u.is.ll.util.DateUtil;
import jp.ac.tokushima_u.is.ll.util.JsonNotifyUtil;
import jp.ac.tokushima_u.is.ll.util.UIUtils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

/**
 * 評価実験用のクラス
 * randomでNotifyする
 * @author li
 */


public class RandomContextAwareService extends Service {
//	private static final String TAG = "RandomContextAwareService";
//	private NotificationManager nm;
//	private AlarmManager alarmManager;
//	private static final int NotificationID = R.id.btnsubmit;
//
//	public static final String LAT_KEY = "latitude";
//	public static final String LNG_KEY = "longitude";
//	public static final String SPEED_KEY = "speed";
//	public static final Double DEFAULT_VALUE = 0d;
//	private int battery_percent;
//
//	public static final int CircleMessage = 1;
//	public static final int LowBatteryMessage = 2;
//	public static final int NotifiedMessage = 3;
//	//for release 
////	private static final int Circle_WATCHDOG_DELAY = 2 * 60 * 1000; // 60
////	private static final int Notified_WATCHDOG_DELAY = 5 * 60 * 1000; // 15
//	
//	private static final int Circle_WATCHDOG_DELAY = 60 * 60 * 1000; // 60
//	private static final int Notified_WATCHDOG_DELAY = 60 * 60 * 1000; // 15
//	
//
//	public RandomContextAwareService() {
//	}
//
//	@Override
//	public void onCreate() {
//		super.onCreate();
//		nm = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
//		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//	}
//
//	@Override
//	public int onStartCommand(Intent intent, int flags, int startId) {
//		super.onStartCommand(intent, flags, startId);
//		Log.d(TAG, "onHandleIntent(intent=" + intent.toString()
//				+ ") battery left  " + battery_percent);
//		
//		if (!UIUtils.checkUser(RandomContextAwareService.this)) {
//			 showNotification(R.drawable.login, R.string.login_notification, new Intent(RandomContextAwareService.this, LoginActivity.class), Constants.LoginNotificationID, null, true);
//			 return START_NOT_STICKY;
//		} 
//		
//		new CheckThread().start();
//		return START_NOT_STICKY;
//	}
//
//	private void setWatchdog(long delay) {
//		Intent i = new Intent();
//		i.setClass(this, RandomContextAwareService.class);
//		PendingIntent pi = PendingIntent.getService(this, 0, i,
//				PendingIntent.FLAG_UPDATE_CURRENT);
//		long timeNow = SystemClock.elapsedRealtime();
//		long nextCheckTime = timeNow + delay;
//		this.alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//				nextCheckTime, pi);
//		ContextUtil.setLatestCircle(this, Calendar.getInstance()
//				.getTimeInMillis());
//		this.stopSelf();
//	}
//
//
//	@Override
//	public void onDestroy() {
//		nm.cancel(NotificationID);
//		RandomContextAwareService.this.stopSelf();
//	}
//
//	private Handler handler = new Handler() {
//
//		@Override
//		public void handleMessage(Message msg) {
//			super.handleMessage(msg);
//			if (msg.what == NotifiedMessage){
//				int r = (int)(Math.random()*4)+1;
//				long delay = Notified_WATCHDOG_DELAY * r;
//				setWatchdog(delay);
//			}else{
//				setWatchdog(Circle_WATCHDOG_DELAY);
//			}
//		}
//
//	};
//
//	
//	class CheckThread extends Thread{
//		@Override
//		public void run() {
//			int notifies = JsonNotifyUtil.countNotifies(RandomContextAwareService.this,null);
//			
//			if(notifies>5){
//				handler.sendEmptyMessage(LowBatteryMessage);
//				return;
//			}
//			
//			Long last = ContextUtil.getLatestLoop(RandomContextAwareService.this);
//			Calendar lastCal = Calendar.getInstance();
//			Calendar nowCal = Calendar.getInstance();
//			lastCal.setTimeInMillis(last);
//			
//			ContextUtil.setLatestLoop(RandomContextAwareService.this, nowCal.getTimeInMillis());
//			
//			//Same day
//			if (lastCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
//					&& lastCal.get(Calendar.MONTH) == nowCal
//							.get(Calendar.MONTH)
//					&& lastCal.get(Calendar.DATE) == nowCal.get(Calendar.DATE)) {
//				if(notifies==0){
//					if((nowCal.getTimeInMillis()-last)>Circle_WATCHDOG_DELAY){
//						String notifyId = JsonNotifyUtil.insertNotify(RandomContextAwareService.this, Notifys.NOTIFY_RANDOM_QUIZ_ID, 0d, 0d, 0f);						
//						Intent i = new Intent(RandomContextAwareService.this, QuizActivity.class);
//						i.putExtra("alarmType", Constants.ContextAwareRandomType);
//						i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//						if(notifyId!=null)
//							i.putExtra("notifyId", notifyId);
//						showNotification(R.drawable.location_notify,
//								R.string.Notify_Info_Setting_Quiz, i,
//								Constants.QuizNotificationID, null, true);
//						handler.sendEmptyMessage(NotifiedMessage);
//						return;
//					}
//				}else{
//					long lastnotify = JsonNotifyUtil.latestNotify(RandomContextAwareService.this);
//					if((nowCal.getTimeInMillis()-lastnotify)>Notified_WATCHDOG_DELAY){
//						String notifyId = JsonNotifyUtil.insertNotify(RandomContextAwareService.this, Notifys.NOTIFY_RANDOM_QUIZ_ID, 0d, 0d, 0f);						
//						Intent i = new Intent(RandomContextAwareService.this, QuizActivity.class);
//						i.putExtra("alarmType", Constants.ContextAwareRandomType);
//						i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//						if(notifyId!=null)
//							i.putExtra("notifyId", notifyId);
//						showNotification(R.drawable.location_notify,
//								R.string.Notify_Info_Setting_Quiz, i,
//								Constants.QuizNotificationID, null, true);
//						handler.sendEmptyMessage(NotifiedMessage);
//						return;
//					}
//				}
//			}
////			else{
////				String notifyId = JsonNotifyUtil.insertNotify(RandomContextAwareService.this, Notifys.NOTIFY_RANDOM_QUIZ_ID, 0d, 0d, 0f);						
////				Intent i = new Intent(RandomContextAwareService.this, QuizActivity.class);
////				i.putExtra("alarmType", Constants.ContextAwareRandomType);
////				i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
////				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////				if(notifyId!=null)
////					i.putExtra("notifyId", notifyId);
////				showNotification(R.drawable.location_notify,
////						R.string.Notify_Info_Setting_Quiz, i,
////						Constants.QuizNotificationID, null, true);
////				handler.sendEmptyMessage(NotifiedMessage);
////				return;
////			}
//			handler.sendEmptyMessage(CircleMessage);
//		}
//	}
//	
//
//	private void showNotification(int iconId, int textId, Intent intent,
//			int notification_id, Integer flg, boolean isvibrate) {
//		CharSequence text = getText(textId);
//
//		Notification notification = new Notification(iconId, null,
//				System.currentTimeMillis());
//
//		// The PendingIntent to launch our activity if the user selects this
//		// notification
//		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//				intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//		if (isvibrate) {
//			notification.defaults = Notification.DEFAULT_VIBRATE;
//			notification.vibrate = new long[] { Constants.VibrateTime };
//			notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//		}
//
//		// Set the info for the views that show in the notification panel.
//		notification.setLatestEventInfo(this, getText(R.string.app_name), text,
//				contentIntent);
//
//		if (flg != null)
//			notification.flags = flg;
//
//		// Send the notification.
//		// We use a layout id because it is a unique number. We use it later to
//		// cancel.
//		nm.notify(notification_id, notification);
//	}
//
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
//
//	public boolean checkLocationTime(Double lat, Double lng) {
//		Time currentTime = new Time(Calendar.getInstance().getTimeInMillis());
//		Uri uri = Profiles.CONTENT_URI;
//		String select_area = Profiles.MIN_X1 + ">=? and " + Profiles.MIN_X2
//				+ "<=? and " + Profiles.MIN_Y1 + ">=? and " + Profiles.MIN_Y2
//				+ " <=? and " + Profiles.FIELD + "=? ";
//		Cursor cr_area = this.getContentResolver().query(
//				uri,
//				new String[] { Profiles.PROFILE_ID },
//				select_area,
//				new String[] { lat.toString(), lat.toString(), lng.toString(),
//						lng.toString(),
//						Profiles.PROFILE_AREA_FIELD_ID.toString() },
//				Profiles.DEFAULT_SORT);
//
//		boolean studyarea = false;
//		try {
//			studyarea = cr_area.moveToFirst();
//		} finally {
//			if (cr_area != null)
//				cr_area.close();
//		}
//		if (!studyarea)
//			return false;
//
//		String select_time = Profiles.MIN_X1 + "<=? and " + Profiles.MIN_Y1
//				+ ">=? and " + Profiles.FIELD + "=? ";
//
//		Cursor cr_time = this.getContentResolver().query(
//				uri,
//				new String[] { Profiles.PROFILE_ID },
//				select_time,
//				new String[] { DateUtil.getSeconds(currentTime).toString(),
//						DateUtil.getSeconds(currentTime).toString(),
//						Profiles.PROFILE_TIME_FIELD_ID.toString() },
//				Profiles.DEFAULT_SORT);
//
//		boolean studytime = false;
//
//		try {
//			studytime = cr_time.moveToFirst();
//		} finally {
//			if (cr_time != null)
//				cr_time.close();
//		}
//
//		return studyarea && studytime;
//	}

	

}
