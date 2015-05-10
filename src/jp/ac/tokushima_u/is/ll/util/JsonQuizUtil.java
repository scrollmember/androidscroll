package jp.ac.tokushima_u.is.ll.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Choices;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemcomments;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Items;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemtags;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemtitles;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Quizs;
import jp.ac.tokushima_u.is.ll.ui.QuizActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class JsonQuizUtil {
	public static ArrayList<ContentProviderOperation> saveQuizArrayFromJson(JSONArray array, Context context, int sync_mode)throws JSONException{
		final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
		if (array != null) {
			for (int i = 0; i < array.length(); i++) {
				JSONObject o = array.getJSONObject(i);
				List<ContentProviderOperation> sub = saveQuizFromJson(o, context, sync_mode);
				if(sub!=null&&sub.size()>0)
					batch.addAll(sub);
			}
		}
		return batch;
	}
	public static ArrayList<ContentProviderOperation> saveQuizFromJson(JSONObject object, Context context, int sync_mode)throws JSONException{
		final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
		if(object==null)
			return batch;
		
		if(StringUtils.isJsonParamNull(object, "itemid"))
			return batch;
		String itemId = object.getString("itemid");
		
		if(!StringUtils.isJsonParamNull(object, "disabled")){
			int disabled = object.getInt("disabled");
			if(disabled == 1){
				Uri deleteUri = Quizs.CONTENT_URI;
				ContentProviderOperation.Builder quizBuilder = ContentProviderOperation
				.newUpdate(deleteUri);
				quizBuilder.withValue(Quizs.ANSWER_STATE, Constants.PassAnsweredState);
				quizBuilder.withSelection(Items.ITEM_ID+"=?", new String[]{itemId});
				batch.add(quizBuilder.build());
				return batch;
			}
		}
		
		ContentResolver resolver = context.getContentResolver();
		
		Uri repeatquizUri = Quizs.CONTENT_URI;
		String repeatSelect = Quizs.Item_ID +"=? and "+Quizs.ANSWER_STATE+"=? ";
		String[]repeatArgs = new String[]{itemId,Constants.NotAnsweredState.toString()}; 
		Cursor repeatCursor = resolver.query(repeatquizUri, QuizsQuery.PROJECTION, repeatSelect, repeatArgs,
				Quizs.DEFAULT_SORT);
		try{
			if(repeatCursor.moveToFirst()){
				return batch;
			}
			if(repeatCursor!=null)
				repeatCursor.close();
		}catch(Exception e){
			Log.d("JSONQuizUtil", "repeat cursor exception", e);
		}
		
		
		
		String quizId = object.getString("quizid");
		if (quizId == null || quizId.length() <= 0)
			return batch;
		
		ContentValues values = queryQuizById(quizId, resolver);
		if(values!=null)
			return batch;
		
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newInsert(Quizs.CONTENT_URI);

		
		builder.withValue(Quizs.Item_ID, itemId);
		builder.withValue(Quizs.QUIZ_ID, quizId);
		if (!StringUtils.isJsonParamNull(object, "authorid"))
			builder.withValue(Quizs.AUTHOR_ID, object.getString("authorid"));
		
		if (!StringUtils.isJsonParamNull(object, "lanCode"))
			builder.withValue(Quizs.LAN_CODE, object.getString("lanCode"));
		
		Integer quizType = null;
		if (!StringUtils.isJsonParamNull(object, "quiztype")){
			quizType = object.getInt("quiztype"); 
			builder.withValue(Quizs.QUIZ_TYPE,quizType);
		}
		
		final ArrayList<ContentProviderOperation> choicebatch = Lists
				.newArrayList();
		if (!StringUtils.isJsonParamNull(object, "choices")) {
			JSONArray choices = object.getJSONArray("choices");
			for(int j=0;j<choices.length();j++) {
				JSONObject choice = (JSONObject) choices.get(j);
				if (choice==null)
					return batch;
				
				ContentProviderOperation.Builder choicebuilder = ContentProviderOperation
						.newInsert(Choices.CONTENT_URI);
				choicebuilder.withValue(Choices.QUIZ_ID,
						quizId);
				if (!StringUtils.isJsonParamNull(choice, "choiceid"))
					choicebuilder.withValue(Choices.CHOICE_ID,
							choice.get("choiceid"));
				String photoUrl = "";
				if (!StringUtils.isJsonParamNull(choice, "content")){
					photoUrl = choice.getString("content");
					choicebuilder.withValue(Choices.CHOICE_CONTENT,
							photoUrl);
				}
				if (!StringUtils.isJsonParamNull(choice, "lanCode"))
					choicebuilder.withValue(Choices.LAN_CODE,
						choice.get("lanCode"));
				
				if (!StringUtils.isJsonParamNull(choice, "number"))
					choicebuilder.withValue(Choices.NUMBER,
						choice.get("number"));
				if (!StringUtils.isJsonParamNull(choice, "note"))
					choicebuilder.withValue(Choices.NOTE,
						choice.get("note"));
				if (!StringUtils.isJsonParamNull(choice, "filetype"))
					choicebuilder.withValue(Choices.File_TYPE,
						choice.get("filetype"));
				
//				if(QuizActivity.QuizTypeImageMutiChoice.equals(quizType)){
//					BitmapUtil.getBitmap(context, photoUrl, ApiConstants.MiddleSizePostfix);
//					BitmapUtil.getBitmap(context, photoUrl, ApiConstants.SmallSizePostfix);
//				}
				
				choicebatch.add(choicebuilder.build());
			}
			batch.addAll(choicebatch);
		}
		
		if (!StringUtils.isJsonParamNull(object, "itemform")) {
			JSONObject itemjson = object.getJSONObject("itemform");
			builder.withValue(Quizs.Item_ID, itemId);
			
//			String itemselection = Items.ITEM_ID + " =? ";
//			String[] itemselectionArgs = new String[] { itemId };
			String[] itemprojection = { Items.ITEM_ID, Items.AUTHOR_ID};
			Cursor itemcursor = resolver.query(Items.buildItemUri(itemId),
					itemprojection, null, null, Items.DEFAULT_SORT);
			try{
				if (!itemcursor.moveToFirst()) {
					final ArrayList<ContentProviderOperation> subbatch = Lists.newArrayList();
					if (!StringUtils.isJsonParamNull(itemjson, "titles")) {
						JSONObject jstitle = itemjson.getJSONObject("titles");
						@SuppressWarnings("rawtypes")
						Iterator keys = jstitle.keys();
						while (keys.hasNext()) {
							ContentProviderOperation.Builder titlebuilder = ContentProviderOperation
									.newInsert(Itemtitles.CONTENT_URI);
							titlebuilder.withValue(Itemtitles.ITEMTITLE_ID,
									StringUtils.randomUUID());
							titlebuilder.withValue(Itemtitles.ITEM_ID,
									itemId);
							String key = (String) keys.next();
							if (StringUtils.isJsonParamNull(key))
								continue;
							titlebuilder.withValue(Itemtitles.LANGUAGE_ID,
									key);
							String content = (String) jstitle.get(key);
							if (StringUtils.isJsonParamNull(content))
								continue;
							titlebuilder.withValue(Itemtitles.CONTENT,
									content);
							subbatch.add(titlebuilder.build());
						}
					}
					
					
					if (!StringUtils.isJsonParamNull(itemjson, "tags")) {
						JSONObject tags = itemjson.getJSONObject("tags");
						@SuppressWarnings("rawtypes")
						Iterator keys = tags.keys();
						while (keys.hasNext()) {
							ContentProviderOperation.Builder tagbuilder = ContentProviderOperation
									.newInsert(Itemtags.CONTENT_URI);
							tagbuilder.withValue(Itemtags.ITEM_ID,
									itemId);
							String key = (String) keys.next();
							if (StringUtils.isJsonParamNull(key))
								continue;
							tagbuilder.withValue(Itemtags.ITEMTAG_ID,
									key);
							String content = (String) tags.get(key);
							if (StringUtils.isJsonParamNull(content))
								continue;
							tagbuilder.withValue(Itemtags.TAG,
									content);
							subbatch.add(tagbuilder.build());
						}
					}

					if (!StringUtils.isJsonParamNull(itemjson, "comments")) {
						JSONObject comments = itemjson.getJSONObject("comments");
						@SuppressWarnings("rawtypes")
						Iterator keys = comments.keys();
						while (keys.hasNext()) {
							ContentProviderOperation.Builder commentbuilder = ContentProviderOperation
									.newInsert(Itemcomments.CONTENT_URI);
							commentbuilder.withValue(Itemcomments.ITEMCOMMENT_ID,
									StringUtils.randomUUID());
							commentbuilder.withValue(Itemcomments.ITEM_ID,
									itemId);
							String key = (String) keys.next();
							if (StringUtils.isJsonParamNull(key))
								continue;
							commentbuilder.withValue(Itemcomments.COMMENT,
									key);
							String content = (String) comments.get(key);
							if (StringUtils.isJsonParamNull(content))
								continue;
							commentbuilder.withValue(Itemcomments.NICKNAME,
									content);
							subbatch.add(commentbuilder.build());
						}
					}
					
					Builder itembuilder = ContentProviderOperation
							.newInsert(Items.CONTENT_URI);
					itembuilder.withValue(Items.ITEM_ID, itemId);
					
					if (!StringUtils.isJsonParamNull(itemjson, "userid"))
						itembuilder.withValue(Items.AUTHOR_ID,
								itemjson.getString("userid"));
					if (!StringUtils.isJsonParamNull(itemjson, "nickname"))
						itembuilder.withValue(Items.NICK_NAME,
								itemjson.getString("nickname"));
					if (!StringUtils.isJsonParamNull(itemjson, "photourl"))
						itembuilder.withValue(Items.PHOTO_URL,
								itemjson.getString("photourl"));
					if (!StringUtils.isJsonParamNull(itemjson, "file_type"))
						itembuilder.withValue(Items.FILE_TYPE,
								itemjson.getString("file_type"));
					if (!StringUtils.isJsonParamNull(itemjson, "note"))
						itembuilder.withValue(Items.NOTE,
								itemjson.getString("note"));
					if (!StringUtils.isJsonParamNull(itemjson, "category"))
						itembuilder.withValue(Items.CATEGORY,
								itemjson.getString("category"));
					itembuilder.withValue(Items.UPDATE_TIME, itemjson.getLong("updatetime"));
					itembuilder.withValue(Items.SYNC_TYPE, Items.SYNC_TYPE_PUSH);	
					itembuilder.withValue(Items.UPDATED, Calendar.getInstance().getTimeInMillis());
					batch.addAll(subbatch);
					batch.add(itembuilder.build());
				}
			}finally{
				itemcursor.close();
			}
		}
		
	
		if (!StringUtils.isJsonParamNull(object, "content"))
			builder.withValue(Quizs.QUIZ_CONTENT,
					object.getString("content"));
		if (!StringUtils.isJsonParamNull(object, "weight"))
			builder.withValue(Quizs.WEIGHT,
					object.getInt("weight"));
		if (!StringUtils.isJsonParamNull(object,
				"answer"))
			builder.withValue(Quizs.ANSWER,
					object.getString("answer"));
		if (!StringUtils.isJsonParamNull(object,
				"createDate"))
			builder.withValue(Quizs.CREATE_TIME,
					object.getString("createDate"));
		if (!StringUtils.isJsonParamNull(object,
				"filetype"))
			builder.withValue(Quizs.FILE_TYPE,
					object.getString("filetype"));
		
		if (!StringUtils.isJsonParamNull(object,
				"quizLat")&&!StringUtils.isJsonParamNull(object,
						"quizLng")){
			builder.withValue(Quizs.LATITUTE,
					object.getString("quizLat"));
			builder.withValue(Quizs.LNGITUTE,
					object.getString("quizLng"));
		}
		
		if (!StringUtils.isJsonParamNull(object, 
				"photourl"))
			builder.withValue(Quizs.PHOTO_URL,
					object.getString("photourl"));
		builder.withValue(Quizs.ANSWER_STATE, Constants.NotAnsweredState);
		builder.withValue(Quizs.SYNC_TYPE, Quizs.SYNC_TYPE_REQUEST);
		builder.withValue(Quizs.UPDATED,Calendar.getInstance().getTimeInMillis());
		batch.add(builder.build());
		return batch;
	}

	public static List<ContentValues> searchToUpdateQuiz(ContentResolver resolver) {
		final List<ContentValues> cvs = new ArrayList<ContentValues>();
		String sortOrder = QuizsQuery.CREATE_TIME + " desc";
		String selection = Quizs.SYNC_TYPE + " =? ";
		String[] selectionArgs = new String[] { Quizs.SYNC_TYPE_CLIENT_UPDATE.toString() };
		Cursor cursor = resolver.query(Quizs.CONTENT_URI,
				QuizsQuery.PROJECTION, selection, selectionArgs, sortOrder);

		ContentValues values = null;
		try {
			while (cursor.moveToNext()) {
				values = new ContentValues();
				values.put(Quizs.QUIZ_ID, cursor.getString(QuizsQuery.QUIZ_ID));
				values.put(Quizs.AUTHOR_ID,
						cursor.getLong(QuizsQuery.AUTHOR_ID));
				values.put(Quizs.ALARM_TYPE, cursor.getInt(QuizsQuery.ALARM_TYPE));
				values.put(Quizs.SPEED, cursor.getFloat(QuizsQuery.SPEED));
				values.put(Quizs.LATITUTE, cursor.getDouble(QuizsQuery.LATITUTE));
				values.put(Quizs.LNGITUTE, cursor.getDouble(QuizsQuery.LNGITUTE));
				values.put(Quizs.MY_ANSWER, cursor.getString(QuizsQuery.MY_ANSWER));
				values.put(Quizs.PASS, cursor.getInt(QuizsQuery.PASS));
				values.put(Quizs.SYNC_TYPE, cursor.getInt(QuizsQuery.SYNC_TYPE));
				cvs.add(values);
			}
		} finally {
			cursor.close();
		}
		return cvs;
	}
	
	
	public static List<ContentProviderOperation> update(Context context, ContentValues cv){
		ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
		String quizId = cv.getAsString(Quizs.QUIZ_ID);
		try {
			DefaultHttpClient client = HttpClientFactory.getInstance(context);
			MultipartEntity entity = new MultipartEntity();
			HttpPost httpPost = new HttpPost(ApiConstants.Quiz_Checker_URL);
			entity.addPart("quizid", new StringBody(quizId));
			entity.addPart("answer",
					new StringBody(cv.getAsString(Quizs.MY_ANSWER)));
			entity.addPart("pass", new StringBody(String.valueOf(cv.getAsInteger(Quizs.PASS))));
			entity.addPart("alarmtype", new StringBody(String.valueOf(cv.getAsInteger(Quizs.ALARM_TYPE))));
			Double lat = cv.getAsDouble(Quizs.LATITUTE);
			Double lng = cv.getAsDouble(Quizs.LNGITUTE);
			Float speed = cv.getAsFloat(Quizs.SPEED);
			if(lat!=null&&lng!=null){
				entity.addPart("lat",
						new StringBody(lat.toString()));
				entity.addPart("lng",
						new StringBody(lng.toString()));
			}
			if(speed!=null)
				entity.addPart("speed",
						new StringBody(speed.toString()));
			httpPost.setEntity(entity);
			HttpResponse  response = client.execute(httpPost);
			
			Integer statuse = response.getStatusLine().getStatusCode();
			if (statuse == 200) {
				ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(Quizs.buildQuizUri(quizId));
				builder.withValue(Quizs.SYNC_TYPE, Quizs.SYNC_TYPE_PUSH);
				batch.add(builder.build());
			}
			
		} catch (Exception e) {
			Log.e("LearningLog", "LearningLogTest Exception", e);
		}
		return batch;
	}
	
	public static ContentValues queryQuizById(String quizId, ContentResolver resolver) {
		String sortOrder = QuizsQuery.CREATE_TIME + " desc";
		String selection = Quizs.QUIZ_ID + " =? ";
		String[] selectionArgs = new String[] { quizId };
		Cursor cursor = resolver.query(Quizs.CONTENT_URI,
				QuizsQuery.PROJECTION, selection, selectionArgs, sortOrder);

		ContentValues values = null;
		try {
			if (cursor.moveToFirst()) {
				values = new ContentValues();
				values.put(Quizs.QUIZ_ID, cursor.getString(QuizsQuery.QUIZ_ID));
				values.put(Quizs.AUTHOR_ID,
						cursor.getLong(QuizsQuery.AUTHOR_ID));
			}
		} finally {
			cursor.close();
		}
		return values;
	}
	

	public interface QuizsQuery {
		String[] PROJECTION = { Quizs.QUIZ_ID, Quizs.AUTHOR_ID,Quizs.CREATE_TIME, Quizs.ALARM_TYPE, Quizs.LATITUTE, Quizs.LNGITUTE, Quizs.SPEED, Quizs.MY_ANSWER, Quizs.PASS, Quizs.SYNC_TYPE};
		int QUIZ_ID = 0;
		int AUTHOR_ID = 1;
		int CREATE_TIME = 2;
		int ALARM_TYPE = 3;
		int LATITUTE = 4;
		int LNGITUTE = 5;
		int SPEED = 6;
		int MY_ANSWER = 7;
		int PASS = 8;
		int SYNC_TYPE = 9;
	}
	
	public interface ChoicesQuery {
		String[] PROJECTION = { Choices.CHOICE_ID, Choices.CHOICE_CONTENT
				};
		int CHOICE_ID = 0;
		int CHOICE_CONTENT = 1;
	}
	
}
