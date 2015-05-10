package jp.ac.tokushima_u.is.ll.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Notifys;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class JsonNotifyUtil {

	public static List<ContentValues> searchToUpdateNotifys(ContentResolver resolver) {
		final List<ContentValues> cvs = new ArrayList<ContentValues>();
		String sortOrder = Notifys.CREATE_TIME + " desc";
		String selection = Notifys.SYNC_TYPE + " =? or  " + Notifys.SYNC_TYPE +" =? ";
		String[] selectionArgs = new String[] { Notifys.SYNC_TYPE_CLIENT_INSERT.toString(), Notifys.SYNC_TYPE_CLIENT_UPDATE.toString() };
		Cursor cursor = resolver.query(Notifys.CONTENT_URI,
				NotifysQuery.PROJECTION, selection, selectionArgs, sortOrder);

		ContentValues values = null;
		try {
			while (cursor.moveToNext()) {
				int feedback = cursor.getInt(NotifysQuery.Feedback);
//				if(i==1&&feedback==0){
//					i++;
//					continue;
//				}
				values = new ContentValues();
				values.put(Notifys._ID, cursor.getInt(NotifysQuery.ID));
				values.put(Notifys.NOTIFY_ID, cursor.getString(NotifysQuery.NOTIFY_ID));
				values.put(Notifys.Feedback,feedback);
				values.put(Notifys.CREATE_TIME, cursor.getLong(NotifysQuery.CREATE_TIME));
				values.put(Notifys.UPDATE_TIME, cursor.getLong(NotifysQuery.UPDATE_TIME));
				values.put(Notifys.SPEED, cursor.getFloat(NotifysQuery.SPEED));
				values.put(Notifys.LATITUTE, cursor.getDouble(NotifysQuery.LATITUTE));
				values.put(Notifys.LNGITUTE, cursor.getDouble(NotifysQuery.LNGITUTE));
				values.put(Notifys.SYNC_TYPE, cursor.getInt(NotifysQuery.SYNC_TYPE));
				values.put(Notifys.NOTIFY_TYPE, cursor.getInt(NotifysQuery.NOTIFY_TYPE));
				cvs.add(values);
			}
		} finally {
			cursor.close();
		}
		return cvs;
	}
	
	public static List<ContentProviderOperation> update(Context context, ContentValues cv){
		ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
		Integer notifyId = cv.getAsInteger(Notifys._ID);
		try {
			DefaultHttpClient client = HttpClientFactory.getInstance(context);
			HttpPost httpPost = new HttpPost(
					ApiConstants.Context_Aware_Feedback_URL);
			MultipartEntity params = new MultipartEntity();
			Double lat = cv.getAsDouble(Notifys.LATITUTE);
			Double lng = cv.getAsDouble(Notifys.LNGITUTE);
			if(lat!=null&&lng!=null){
				params.addPart("lat", new StringBody(lat.toString()));
				params.addPart("lng", new StringBody(lng.toString()));
			}
			Float speed = cv.getAsFloat(Notifys.SPEED);
			if(speed!=null)
				params.addPart("speed", new StringBody(speed.toString()));
			
			Integer notifyType = cv.getAsInteger(Notifys.NOTIFY_TYPE);	
			if(notifyType!=null)
				params.addPart("alarmType", new StringBody(notifyType.toString()));
			params.addPart("feeback", new StringBody(cv.getAsInteger(Notifys.Feedback).toString()));
			params.addPart("createtime", new StringBody(cv.getAsLong(Notifys.CREATE_TIME).toString()));
			params.addPart("updatetime", new StringBody(cv.getAsLong(Notifys.UPDATE_TIME).toString()));
			params.addPart("notifyCode", new StringBody(cv.getAsString(Notifys.NOTIFY_ID)));
			httpPost.setEntity(params);
			HttpResponse  response = client.execute(httpPost);
			Integer statuse = response.getStatusLine().getStatusCode();
			if (statuse == 200) {
				ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(Notifys.buildNotifyUri(notifyId.toString()));
				builder.withValue(Notifys.SYNC_TYPE, Notifys.SYNC_TYPE_PUSH);
				batch.add(builder.build());
			}
			
		} catch (Exception e) {
			Log.e("LearningLogTest Exception", e.getMessage());
		}
		
		return batch;
	}
	
	
	public static String insertNotify(Context context, Integer notify_type, Double lat, Double lng, Float speed) {
		Uri uri = Notifys.CONTENT_URI;
		ContentValues cv = new ContentValues();
		cv.put(Notifys.NOTIFY_ID, StringUtils.randomUUID());
		cv.put(Notifys.NOTIFY_TYPE, notify_type);
		cv.put(Notifys.CREATE_TIME, Calendar.getInstance().getTimeInMillis());
		cv.put(Notifys.UPDATE_TIME, Calendar.getInstance().getTimeInMillis());
		if(lat!=null&&lng!=null){
			cv.put(Notifys.LATITUTE, lat);
			cv.put(Notifys.LNGITUTE, lng);
		}
		if(speed!=null)
			cv.put(Notifys.SPEED, speed);
		cv.put(Notifys.Feedback, Notifys.NOTIFY_NOT_FEEDBACK);
		cv.put(Notifys.SYNC_TYPE, Notifys.SYNC_TYPE_CLIENT_INSERT);
		Uri notifyuri = context.getContentResolver().insert(uri, cv);
		return Notifys.getNotifyId(notifyuri);
	}

	public static long latestNotify(Context context) {
		Uri uri = Notifys.CONTENT_URI;
		Cursor cursor = context.getContentResolver().query(uri,
				new String[] { Notifys.CREATE_TIME }, null, null,
				Notifys.DEFAULT_SORT);
		try {
			if (cursor.moveToFirst()) {
				return cursor.getLong(0);
			}
		} finally {
			cursor.close();
		}
		return 0;
	}

	public static int countNotifies(Context context, Integer notifyType) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		String select = Notifys.CREATE_TIME + ">=?";
		String[] args = new String[] { String.valueOf(cal.getTimeInMillis()) };
		if (notifyType != null) {
			select = select + " and " + Notifys.NOTIFY_TYPE + " =?";
			args = new String[] { String.valueOf(cal.getTimeInMillis()),
					notifyType.toString() };
		}

		int num = 0;
		Cursor cursor = context.getContentResolver().query(Notifys.CONTENT_URI,
				new String[] { Notifys.CREATE_TIME }, select, args,
				Notifys.DEFAULT_SORT);
		try {
			if (!cursor.moveToFirst())
				return num;
			do {
				num++;
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}
		return num;
	}

	public interface NotifysQuery {
		String[] PROJECTION = { Notifys._ID, Notifys.LATITUTE,
				Notifys.LNGITUTE, Notifys.SPEED, Notifys.Feedback,
				Notifys.CREATE_TIME, Notifys.NOTIFY_TYPE, Notifys.SYNC_TYPE, Notifys.NOTIFY_ID, Notifys.UPDATE_TIME};
		int ID = 0;
		int LATITUTE = 1;
		int LNGITUTE = 2;
		int SPEED = 3;
		int Feedback = 4;
		int CREATE_TIME = 5;
		int NOTIFY_TYPE = 6;
		int SYNC_TYPE = 7;
		int NOTIFY_ID = 8;
		int UPDATE_TIME = 9;
	}
}
