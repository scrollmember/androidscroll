package jp.ac.tokushima_u.is.ll.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.ac.tokushima_u.is.ll.provider.LearningLogContract;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Profiles;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Quizs;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.ContextUtil;
import jp.ac.tokushima_u.is.ll.util.FormatUtil;
import jp.ac.tokushima_u.is.ll.util.JsonQuizUtil;
import jp.ac.tokushima_u.is.ll.util.Lists;
import jp.ac.tokushima_u.is.ll.util.StringUtils;

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

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
/**
 * 
 * @author li
 * <p>
 * 		This class is used to sync the personalized data from the server, 
 * 		e.g. the area or time period learners often study
 * </p>
 *
 */

public class ProfileJsonHandler extends JsonHandler{
	private static String TAG = "SetttingSyncHandler";
	private DefaultHttpClient client;
	private Context context;
	private static final String personalizeUrl = "/personalize.json";

	public ProfileJsonHandler(DefaultHttpClient httpClient,Context context) {
		super(LearningLogContract.CONTENT_AUTHORITY);
		this.client = httpClient;
		this.context = context;
	}
	
	@Override
	public ArrayList<ContentProviderOperation> parse(ContentResolver resolver)
			throws IOException {
		final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
		String authorId = ContextUtil.getUserId(context);
		
		ContentValues area_cv = this.queryProfileById(authorId, Profiles.PROFILE_AREA_FIELD_ID, resolver);
		Long area_update = null;
		if(area_cv!=null){
			area_update = area_cv.getAsLong(Profiles.UPDATED);
		}
		
		ContentValues time_cv = this.queryProfileById(authorId, Profiles.PROFILE_TIME_FIELD_ID, resolver);
		Long time_update = null;
		if(time_cv!=null){
			time_update = time_cv.getAsLong(Profiles.UPDATED);
		}
		
		ContentValues send_cv = this.queryProfileById(authorId, Profiles.PROFILE_SEND_TIME_FIELD_ID, resolver);
		Long send_update = null;
		if(send_cv!=null){
			time_update = send_cv.getAsLong(Profiles.UPDATED);
		}
		
		HttpPost httpPost = new HttpPost(ApiConstants.Sync_URI + personalizeUrl);
		
		try {
			MultipartEntity params  = new MultipartEntity();
			if (area_update != null) {
				String upd_area = FormatUtil.getJpTimeFormat().format(new Date(area_update));
				params.addPart("areaUpdateTime", new StringBody(upd_area));
			}
			if (time_update != null) {
				String upd_time = FormatUtil.getJpTimeFormat().format(new Date(time_update));
				params.addPart("timeUpdateTime", new StringBody(upd_time));
			}
			
			if (send_update != null) {
				String upd_send = FormatUtil.getJpTimeFormat().format(new Date(send_update));
				params.addPart("sendUpdateTime", new StringBody(upd_send));
			}			
			httpPost.setEntity(params);

			final HttpResponse response = client.execute(httpPost);
			final HttpEntity entity = response.getEntity();

			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK || entity == null)
				return batch;
			final String respString = EntityUtils.toString(entity);
			final JSONObject json = new JSONObject(respString);
			if (json != null) {
				try{
					final JSONArray array = json.getJSONArray("areas");
					if (array != null) {
						ContentProviderOperation.Builder cleaerBuilder = ContentProviderOperation.newDelete(Profiles.CONTENT_URI);
						cleaerBuilder.withSelection(Profiles.FIELD+" =? ", new String[]{Profiles.PROFILE_AREA_FIELD_ID.toString()});
						batch.add(cleaerBuilder.build());
						for (int i = 0; i < array.length(); i++) {
							JSONObject o = array.getJSONObject(i);
							ContentProviderOperation.Builder areaBuilder = ContentProviderOperation.newInsert(Profiles.CONTENT_URI);
							areaBuilder.withValue(Profiles.PROFILE_ID, StringUtils.randomUUID());
							areaBuilder.withValue(Profiles.FIELD, Profiles.PROFILE_AREA_FIELD_ID);
							areaBuilder.withValue(Profiles.AUTHOR_ID, authorId);
							areaBuilder.withValue(Profiles.MIN_X1, o.getDouble("maxlat"));
							areaBuilder.withValue(Profiles.MIN_Y1, o.getDouble("maxlng"));
							areaBuilder.withValue(Profiles.MIN_X2, o.getDouble("minlat"));
							areaBuilder.withValue(Profiles.MIN_Y2, o.getDouble("minlng"));
							areaBuilder.withValue(Profiles.UPDATED, o.getLong("createDate"));
							batch.add(areaBuilder.build());
						}
					}
				}catch(JSONException e){
					
				}
				
				try{
					final JSONArray array = json.getJSONArray("times");
					if (array != null) {
						ContentProviderOperation.Builder cleaerBuilder = ContentProviderOperation.newDelete(Profiles.CONTENT_URI);
						cleaerBuilder.withSelection(Profiles.FIELD+" =? ", new String[]{Profiles.PROFILE_TIME_FIELD_ID.toString()});
						batch.add(cleaerBuilder.build());
						for (int i = 0; i < array.length(); i++) {
							JSONObject o = array.getJSONObject(i);
							ContentProviderOperation.Builder areaBuilder = ContentProviderOperation.newInsert(Profiles.CONTENT_URI);
							areaBuilder.withValue(Profiles.PROFILE_ID, StringUtils.randomUUID());
							areaBuilder.withValue(Profiles.FIELD, Profiles.PROFILE_TIME_FIELD_ID);
							areaBuilder.withValue(Profiles.AUTHOR_ID, authorId);
							areaBuilder.withValue(Profiles.MIN_X1, o.getLong("starttime"));
							areaBuilder.withValue(Profiles.MIN_Y1, o.getLong("endtime"));
							areaBuilder.withValue(Profiles.UPDATED, o.getLong("createDate"));
							batch.add(areaBuilder.build());
						}
					}
				}catch(JSONException e){
					
				}
				
				try{
					final JSONArray array = json.getJSONArray("sends");
					if (array != null) {
						ContentProviderOperation.Builder cleaerBuilder = ContentProviderOperation.newDelete(Profiles.CONTENT_URI);
						cleaerBuilder.withSelection(Profiles.FIELD+" =? ", new String[]{Profiles.PROFILE_SEND_TIME_FIELD_ID.toString()});
						batch.add(cleaerBuilder.build());
						for (int i = 0; i < array.length(); i++) {
							JSONObject o = array.getJSONObject(i);
							ContentProviderOperation.Builder areaBuilder = ContentProviderOperation.newInsert(Profiles.CONTENT_URI);
							areaBuilder.withValue(Profiles.PROFILE_ID, StringUtils.randomUUID());
							areaBuilder.withValue(Profiles.FIELD, Profiles.PROFILE_SEND_TIME_FIELD_ID);
							areaBuilder.withValue(Profiles.AUTHOR_ID, authorId);
							areaBuilder.withValue(Profiles.MIN_X1, o.getLong("sendtime"));
							areaBuilder.withValue(Profiles.SUB_TYPE, o.getInt("typ"));
							areaBuilder.withValue(Profiles.UPDATED, o.getLong("createDate"));
							batch.add(areaBuilder.build());
						}
					}
				}catch(JSONException e){
					
				}
				
			}
		} catch (Exception e) {
			Log.d(TAG, "find id is error", e);
		}
		
		return batch;
	}
	
	
	private ContentValues queryProfileById(String authorId, Integer field, ContentResolver resolver) {
		String sortOrder = ProfilesQuery.UPDATED + " desc";
		String selection = Profiles.AUTHOR_ID + " =? and "+Profiles.FIELD+" =?";
		String[] selectionArgs = new String[] { authorId, field.toString() };
		Cursor cursor = resolver.query(Profiles.CONTENT_URI,
				ProfilesQuery.PROJECTION, selection, selectionArgs, sortOrder);

		ContentValues values = null;
		try {
			if (cursor.moveToFirst()) {
				values = new ContentValues();
				values.put(Profiles.PROFILE_ID, cursor.getString(ProfilesQuery.PROFILE_ID));
				values.put(Profiles.UPDATED,
						cursor.getLong(ProfilesQuery.UPDATED));
			}
		} finally {
			cursor.close();
		}
		return values;
	}
	
	private interface ProfilesQuery {
		String[] PROJECTION = { Profiles.PROFILE_ID, Profiles.FIELD,Profiles.AUTHOR_ID, Profiles.UPDATED};

		int PROFILE_ID = 0;
		int UPDATED = 3;
	}

}
