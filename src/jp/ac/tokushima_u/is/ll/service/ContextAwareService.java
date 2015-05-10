package jp.ac.tokushima_u.is.ll.service;

import java.io.File;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Items;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Notifys;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Profiles;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Quizs;
import jp.ac.tokushima_u.is.ll.ui.LogListActivity;
import jp.ac.tokushima_u.is.ll.ui.LoginActivity;
import jp.ac.tokushima_u.is.ll.ui.QuizActivity;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.Constants;
import jp.ac.tokushima_u.is.ll.util.ContextUtil;
import jp.ac.tokushima_u.is.ll.util.DateUtil;
import jp.ac.tokushima_u.is.ll.util.HttpClientFactory;
import jp.ac.tokushima_u.is.ll.util.JsonItemUtil;
import jp.ac.tokushima_u.is.ll.util.JsonNotifyUtil;
import jp.ac.tokushima_u.is.ll.util.JsonQuizUtil;
import jp.ac.tokushima_u.is.ll.util.StringUtils;
import jp.ac.tokushima_u.is.ll.util.UIUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;

public class ContextAwareService extends Service {
	private static final String TAG = "ContextAwareService";
	private NotificationManager nm;
	private LocationManager locationmanager;
	private AlarmManager alarmManager;
	private static final int NotificationID = R.id.btnsubmit;

	// private ConditionVariable mCondition;

	public static final String LAT_KEY = "latitude";
	public static final String LNG_KEY = "longitude";
	public static final String SPEED_KEY = "speed";
	public static final Double DEFAULT_VALUE = 0d;
	private int battery_percent;

	private MyLocationListener locationlistener = new MyLocationListener();

	public static final int CircleMessage = 1;
	public static final int LowBatteryMessage = 2;
	public static final int NotifiedMessage = 3;

	private String locationProvider;
	
	// private static final int WATCHDOG_DELAY = 10 * 60 * 1000; // 10 minutes
	

	// private static final int Circle_WATCHDOG_DELAY = 60 * 1000; // 10
	// private static final int Low_Battery_WATCHDOG_DELAY = 60 * 1000; // 120
	// private static final int Notified_WATCHDOG_DELAY = 2 * 60 * 1000; // 15

	// for debug
//	private static final int Circle_WATCHDOG_DELAY =  60 * 1000; // 10
//	private static final int Low_Battery_WATCHDOG_DELAY = 60 * 1000;
//	private static final int Notified_WATCHDOG_DELAY = 60 * 1000; // 15
//	
//	private static final int gps_circle_time = 0;
//	private static final int gps_circle_distance = 2;
//
//	private static Integer latest = 22 * 60 * 60 + 30 * 60;
//	private static Integer earliest = 8 * 60 * 60 + 30 * 60;
	
	
	//for release 
	private static final int Circle_WATCHDOG_DELAY = 10 * 60 * 1000; // 10
	private static final int Low_Battery_WATCHDOG_DELAY = 2 * 60 * 60 * 1000;
	private static final int Notified_WATCHDOG_DELAY = 20 * 60 * 1000; // 15
	
//	private static final int gps_circle_time = 5 * 60 * 1000;
//	private static final int gps_circle_distance = 10;
	
	private static final int gps_circle_time = 0;
	private static final int gps_circle_distance = 0;

	private static Integer latest = 22 * 60 * 60 + 30 * 60;
	private static Integer earliest = 8 * 60 * 60 + 30 * 60;

	public ContextAwareService() {
	}

	@Override
	public void onCreate() {
		super.onCreate();
		nm = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
		this.registerReceiver(mBatteryReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		this.locationmanager = (LocationManager) this
				.getSystemService(LOCATION_SERVICE);
		
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAltitudeRequired(true);
		criteria.setBearingRequired(false);
		criteria.setSpeedRequired(true);
		criteria.setCostAllowed(false);
		locationProvider = locationmanager.getBestProvider(criteria, true);
		
		//TODO  locationprovider is null
		if(locationProvider!=null)
			locationmanager.requestLocationUpdates(locationProvider, gps_circle_time, gps_circle_distance, locationlistener);
//		
//		showNotification(R.drawable.location_notify,
//				R.string.Notify_Info_Setting_Quiz, new Intent(ContextAwareService.this, QuizActivity.class),
//				Constants.QuizNotificationID, null, true);
		
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if (!UIUtils.checkUser(ContextAwareService.this)) {
			 showNotification(R.drawable.login, R.string.login_notification, new Intent(ContextAwareService.this, LoginActivity.class), Constants.LoginNotificationID, null, true);
			 return START_NOT_STICKY;
		} 
		
		new CheckThread().start();
		return START_NOT_STICKY;
	}

	private void setWatchdog(long delay) {
		Intent i = new Intent();
		i.setClass(this, ContextAwareService.class);
		PendingIntent pi = PendingIntent.getService(this, 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		long timeNow = SystemClock.elapsedRealtime();
		long nextCheckTime = timeNow + delay;
		this.alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				nextCheckTime, pi);
		ContextUtil.setLatestCircle(this, Calendar.getInstance()
				.getTimeInMillis());
		this.locationmanager.removeUpdates(locationlistener);
		ContextAwareService.this.stopSelf();
	}

	private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
				int current = intent.getExtras().getInt("level");
				int total = intent.getExtras().getInt("scale");
				battery_percent = current * 100 / total;
			}
		}
	};

	@Override
	public void onDestroy() {
		this.unregisterReceiver(mBatteryReceiver);
		nm.cancel(NotificationID);
		ContextAwareService.this.stopSelf();
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == NotifiedMessage)
				setWatchdog(Notified_WATCHDOG_DELAY);
			else if (msg.what == CircleMessage){
//				PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//				 boolean isScreenOn = pm.isScreenOn();
//				if(!isScreenOn){
//					Intent intent = new Intent(ContextAwareService.this, AlarmActivity.class);
//					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//					intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//					intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//					intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//					intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
//					startActivity(intent);
//				}
				setWatchdog(Circle_WATCHDOG_DELAY);
			}else if (msg.what == LowBatteryMessage)
				setWatchdog(Low_Battery_WATCHDOG_DELAY);
		}

	};

	class CheckThread extends Thread{
		@Override
		public void run() {
			
			int total_num = JsonNotifyUtil.countNotifies(ContextAwareService.this, null);
			if (total_num > 7) {
				handler.sendEmptyMessage(LowBatteryMessage);
				return;
			}

			// check the time
			Time t = new Time(Calendar.getInstance().getTimeInMillis());
			Integer now_second = DateUtil.getSeconds(t);

			if (now_second > latest || now_second < earliest) {
				handler.sendEmptyMessage(LowBatteryMessage);
				return;
			}

			// check the battery
			if (battery_percent != 0 && battery_percent < 20) {
				handler.sendEmptyMessage(LowBatteryMessage);
				return;
			}

			final Long now = Calendar.getInstance().getTimeInMillis();

			// check last notify
			long latest = JsonNotifyUtil.latestNotify(ContextAwareService.this);
			if ((now - latest) < Notified_WATCHDOG_DELAY) {
				handler.sendEmptyMessage(NotifiedMessage);
				return;
			}
			
			Double lat = null;
			Double lng = null;
			Float speed = null;
			Location location = locationlistener.getLocation();
			
			int j = 0;
			while((location==null||!location.hasAccuracy()||location.getAccuracy()>30)&&j<=6){
				j++;
				try{
					sleep(30000);
					location = locationlistener.getLocation();
				}catch(Exception e){
					
				}
			}
			
			if(location==null||!location.hasAccuracy()||location.getAccuracy()>50){
				handler.sendEmptyMessage(CircleMessage);
				return;
			}
			
			if (location != null) {
				lat = location.getLatitude();
				lng = location.getLongitude();
				speed = location.getSpeed();
			}

			boolean hasquiz = hasQuizs();

			Calendar cal = Calendar.getInstance();
			Time currentTime = new Time(cal.getTimeInMillis());
			if (hasquiz) {
				String select_send = Profiles.MIN_X1 + "<=? and " + Profiles.MIN_X1
						+ ">=? and " + Profiles.FIELD + "=? ";

				Long last = ContextUtil.getLatestCircle(ContextAwareService.this);

				cal.add(Calendar.MINUTE, -15);
				if (last < cal.getTimeInMillis())
					last = cal.getTimeInMillis();
				Time lastTime = new Time(last);

				Cursor cr_send = getContentResolver().query(
						Profiles.CONTENT_URI,
						new String[] { Profiles.PROFILE_ID },
						select_send,
						new String[] { DateUtil.getSeconds(currentTime).toString(),
								DateUtil.getSeconds(lastTime).toString(),
								Profiles.PROFILE_SEND_TIME_FIELD_ID.toString() },
						Profiles.DEFAULT_SORT);

				boolean sendtime = false;

				try {
					sendtime = cr_send.moveToFirst();
				} finally {
					cr_send.close();
				}

				if (sendtime) {
					String notifyId = JsonNotifyUtil.insertNotify(ContextAwareService.this, Notifys.NOTIFY_SENDTIME_QUIZ_ID, lat, lng, speed);
					Intent i = new Intent(ContextAwareService.this, QuizActivity.class);
					i.putExtra("alarmType", Constants.TimeReuestType);
					i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					if(notifyId!=null)
						i.putExtra("notifyId", notifyId);
					showNotification(R.drawable.location_notify,
							R.string.Notify_Info_Setting_Quiz, i,
							Constants.QuizNotificationID, null, true);
					handler.sendEmptyMessage(NotifiedMessage);
					return;
				}
			}


			if (lat != null && lng != null && speed != null && speed <= 4) {
				int location_num = JsonNotifyUtil.countNotifies(ContextAwareService.this, Notifys.NOTIFY_LOCATION_TIME_QUIZ_ID);

				if (hasquiz && location_num <= 4) {
					if (checkLocationTime(lat, lng)) {
						String notifyId = JsonNotifyUtil.insertNotify(ContextAwareService.this, Notifys.NOTIFY_LOCATION_TIME_QUIZ_ID, lat, lng, speed);
						Intent i = new Intent(ContextAwareService.this, QuizActivity.class);
						i.putExtra("alarmType", Constants.LocationRequestType);
						i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						if(notifyId!=null)
							i.putExtra("notifyId", notifyId);
						showNotification(R.drawable.location_notify,
								R.string.Notify_Info_Location_Quiz, i,
								Constants.QuizNotificationID, null, true);
						handler.sendEmptyMessage(NotifiedMessage);
						return;
					}
				}

				int context_quiz_num = JsonNotifyUtil.countNotifies(ContextAwareService.this, Notifys.NOTIFY_CONTEXT_QUIZ_ID);
				int context_item_num = JsonNotifyUtil.countNotifies(ContextAwareService.this, Notifys.NOTIFY_ITEM_ID);

				if (context_quiz_num + context_item_num > 5) {
					handler.sendEmptyMessage(CircleMessage);
					return;
				}

				final DefaultHttpClient client = HttpClientFactory
						.getInstance(ContextAwareService.this);
				MultipartEntity params = new MultipartEntity();
				JSONObject json = null;
				try {
					params.addPart("latitude", new StringBody(lat + ""));
					params.addPart("longitude", new StringBody(lng + ""));
					final HttpPost httpPost = new HttpPost(
							ApiConstants.Context_Aware_URL);
					httpPost.setEntity(params);
					HttpResponse response = client.execute(httpPost);
					HttpEntity entity = response.getEntity();
					int statusCode = response.getStatusLine().getStatusCode();
					if (statusCode == HttpStatus.SC_OK && entity != null) {
						final String respString = EntityUtils.toString(entity);
						json = new JSONObject(respString);
						if (!StringUtils.isJsonParamNull(json, "result")) {
							int result = json.getInt("result");
							if (result == 0) {
								handler.sendEmptyMessage(Circle_WATCHDOG_DELAY);
								return;
							}
						}
					}
				} catch (Exception e) {
				}

				if (json != null) {
					JSONArray quizs = null;
					try {
						// quiz = json.getJSONObject("quiz");
						quizs = json.getJSONArray("quizzes");
						if (quizs != null && quizs.length() > 0) {
							String notifyId = JsonNotifyUtil.insertNotify(ContextAwareService.this, Notifys.NOTIFY_CONTEXT_QUIZ_ID, lat, lng, speed);
							ArrayList<ContentProviderOperation> batch = JsonQuizUtil
									.saveQuizArrayFromJson(quizs,
											ContextAwareService.this,
											Quizs.SYNC_TYPE_PUSH);
							getContentResolver().applyBatch(
									LearningLogContract.CONTENT_AUTHORITY, batch);
							Intent i_q = new Intent(ContextAwareService.this, QuizActivity.class);
							i_q.putExtra("alarmType",
									Constants.ContextAwareRequestType);
							if(notifyId!=null)
								i_q.putExtra("notifyId", notifyId);
							i_q.putExtra("lat", lat);
							i_q.putExtra("lng", lng);
							i_q.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
							i_q.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							showNotification(R.drawable.location_notify,
									R.string.Notify_Info_Context_Quiz, i_q,
									Constants.QuizNotificationID, null, true);
							handler.sendEmptyMessage(NotifiedMessage);
							return;
						}
					} catch (JSONException e) {
					} catch (OperationApplicationException e) {

					} catch (RemoteException e) {

					}
					if (quizs == null || quizs.length() == 0) {
						try {
							final JSONArray array = json.getJSONArray("items");
							ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
							if (array != null) {
								for (int i = 0; i < array.length(); i++) {
									JSONObject o = array.getJSONObject(i);
									ArrayList<ContentProviderOperation> sub = JsonItemUtil
											.saveItemFromJson(o,
													getContentResolver(),
													Items.SYNC_TYPE_PUSH);
									if (sub != null)
										batch.addAll(sub);
								}
								getContentResolver().applyBatch(
										LearningLogContract.CONTENT_AUTHORITY,
										batch);
								if (array.length() > 0) {
									String notifyId = JsonNotifyUtil.insertNotify(ContextAwareService.this, Notifys.NOTIFY_ITEM_ID, lat, lng, speed);
									Intent i_it = new Intent(ContextAwareService.this,
											LogListActivity.class);
									if(notifyId!=null)
										i_it.putExtra("notifyId", notifyId);
									i_it.setAction(Intent.ACTION_SEARCH);
									String query = "geo:" + lat.toString() + ","
											+ lng.toString();
									i_it.putExtra("lat", lat);
									i_it.putExtra("lng", lng);
									i_it.putExtra(SearchManager.QUERY, query);
									i_it.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
									i_it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									showNotification(
											R.drawable.location_notify,
											R.string.Notify_Info_Context_Logs, i_it,
											Constants.LogsNotificationID, null,
											true);
									handler.sendEmptyMessage(NotifiedMessage);
									return;
								}
							}
						} catch (JSONException e) {
						} catch (OperationApplicationException e) {

						} catch (RemoteException e) {

						}
					}
				}
			}
			handler.sendEmptyMessage(CircleMessage);
		}
	}
	

	private void showNotification(int iconId, int textId, Intent intent,
			int notification_id, Integer flg, boolean isvibrate) {
		CharSequence text = getText(textId);

		Notification notification = new Notification(iconId, null,
				System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		//TO play a music
//		if (isvibrate) {
			notification.defaults = Notification.DEFAULT_VIBRATE;
			notification.vibrate = new long[] { 6000 };
//			notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//			notification.sound = Uri.parse("android.resource://jp.ac.tokushima_u.is.ll/"+R.raw.notification);
			
//		}

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.app_name), text,
				contentIntent);

		if (flg != null)
			notification.flags = flg;

		// Send the notification.
		// We use a layout id because it is a unique number. We use it later to
		// cancel.
		nm.notify(notification_id, notification);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean checkLocationTime(Double lat, Double lng) {
		Time currentTime = new Time(Calendar.getInstance().getTimeInMillis());
		Uri uri = Profiles.CONTENT_URI;
		String select_area = Profiles.MIN_X1 + ">=? and " + Profiles.MIN_X2
				+ "<=? and " + Profiles.MIN_Y1 + ">=? and " + Profiles.MIN_Y2
				+ " <=? and " + Profiles.FIELD + "=? ";
		Cursor cr_area = this.getContentResolver().query(
				uri,
				new String[] { Profiles.PROFILE_ID },
				select_area,
				new String[] { lat.toString(), lat.toString(), lng.toString(),
						lng.toString(),
						Profiles.PROFILE_AREA_FIELD_ID.toString() },
				Profiles.DEFAULT_SORT);

		boolean studyarea = false;
		try {
			studyarea = cr_area.moveToFirst();
		} finally {
			if (cr_area != null)
				cr_area.close();
		}
		if (!studyarea)
			return false;

		String select_time = Profiles.MIN_X1 + "<=? and " + Profiles.MIN_Y1
				+ ">=? and " + Profiles.FIELD + "=? ";

		Cursor cr_time = this.getContentResolver().query(
				uri,
				new String[] { Profiles.PROFILE_ID },
				select_time,
				new String[] { DateUtil.getSeconds(currentTime).toString(),
						DateUtil.getSeconds(currentTime).toString(),
						Profiles.PROFILE_TIME_FIELD_ID.toString() },
				Profiles.DEFAULT_SORT);

		boolean studytime = false;

		try {
			studytime = cr_time.moveToFirst();
		} finally {
			if (cr_time != null)
				cr_time.close();
		}

		return studyarea && studytime;
	}

	public boolean hasQuizs() {
		Uri uri = Quizs.CONTENT_URI;
		Cursor cursor = this.getContentResolver().query(uri,
				new String[] { Quizs.QUIZ_ID }, Quizs.ANSWER_STATE + "=?",
				new String[] { Constants.NotAnsweredState.toString() },
				Quizs.DEFAULT_SORT);
		try {
			return cursor.moveToFirst();
		} finally {
			cursor.close();
		}
	}

	
	public class MyLocationListener implements LocationListener {
		private Location location;
		
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
}
