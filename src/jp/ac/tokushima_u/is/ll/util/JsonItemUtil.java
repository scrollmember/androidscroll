package jp.ac.tokushima_u.is.ll.util;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Answers;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemcomments;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Items;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemtags;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemtitles;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Languages;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Questions;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Quizs;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class JsonItemUtil {
	public static ArrayList<ContentProviderOperation> saveItemFromJson(
			JSONObject o, ContentResolver resolver, int sync_mode)
			throws JSONException {
		final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
		if (o == null)
			return batch;

		String itemId = o.getString("itemId");
		String authorId = o.getString("userid");
		if (itemId == null || itemId.length() <= 0)
			return batch;
		ContentValues values = queryItemById(itemId, resolver);

		if (values != null) {
			long updatetime = o.getLong("updatetime");
			Long updatetime_db = values.getAsLong(Items.UPDATE_TIME);
			if (updatetime == updatetime_db)
				return batch;
			else
				batch.addAll(deleteItemBatch(itemId));
		}

		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newInsert(Items.CONTENT_URI);

		final ArrayList<ContentProviderOperation> subbatch = Lists
				.newArrayList();
		if (!StringUtils.isJsonParamNull(o, "titles")) {
			JSONObject jstitle = o.getJSONObject("titles");
			@SuppressWarnings("rawtypes")
			Iterator keys = jstitle.keys();
			while (keys.hasNext()) {
				ContentProviderOperation.Builder titlebuilder = ContentProviderOperation
						.newInsert(Itemtitles.CONTENT_URI);
				titlebuilder.withValue(Itemtitles.ITEMTITLE_ID,
						StringUtils.randomUUID());
				titlebuilder.withValue(Itemtitles.ITEM_ID, itemId);
				String key = (String) keys.next();
				if (StringUtils.isJsonParamNull(key))
					continue;
				titlebuilder.withValue(Itemtitles.LANGUAGE_ID, key);
				String content = (String) jstitle.get(key);
				if (StringUtils.isJsonParamNull(content))
					continue;
				titlebuilder.withValue(Itemtitles.CONTENT, content);
				subbatch.add(titlebuilder.build());
			}
		}
		
		try{
			if (!StringUtils.isJsonParamNull(o, "tags")) {
				JSONObject tags = o.getJSONObject("tags");
				@SuppressWarnings("rawtypes")
				Iterator keys = tags.keys();
				while (keys.hasNext()) {
					ContentProviderOperation.Builder tagbuilder = ContentProviderOperation
							.newInsert(Itemtags.CONTENT_URI);
					tagbuilder.withValue(Itemtags.ITEM_ID, itemId);
					String key = (String) keys.next();
					if (StringUtils.isJsonParamNull(key))
						continue;
					tagbuilder.withValue(Itemtags.ITEMTAG_ID, key);
					String content = (String) tags.get(key);
					if (StringUtils.isJsonParamNull(content))
						continue;
					tagbuilder.withValue(Itemtags.TAG, content);
					subbatch.add(tagbuilder.build());
				}
			}
		}catch(Exception e){
			
		}

		
		try{
			if (!StringUtils.isJsonParamNull(o, "comments")) {
				JSONObject comments = o.getJSONObject("comments");
				@SuppressWarnings("rawtypes")
				Iterator keys = comments.keys();
				while (keys.hasNext()) {
					ContentProviderOperation.Builder commentbuilder = ContentProviderOperation
							.newInsert(Itemcomments.CONTENT_URI);
					commentbuilder.withValue(Itemcomments.ITEMCOMMENT_ID,
							StringUtils.randomUUID());
					commentbuilder.withValue(Itemcomments.ITEM_ID, itemId);
					String key = (String) keys.next();
					if (StringUtils.isJsonParamNull(key))
						continue;
					commentbuilder.withValue(Itemcomments.COMMENT, key);
					String content = (String) comments.get(key);
					if (StringUtils.isJsonParamNull(content))
						continue;
					commentbuilder.withValue(Itemcomments.NICKNAME, content);
					subbatch.add(commentbuilder.build());
				}
			}
		}catch(Exception e){
			
		}

		
		try {
			if (!StringUtils.isJsonParamNull(o, "questionId")
					&& !StringUtils.isJsonParamNull(o, "question")
					&& !StringUtils.isJsonParamNull(o, "quesLanCode")) {
				String questionId = o.getString("question");
				String question = o.getString("question");
				String lancode = o.getString("quesLanCode");
				ContentProviderOperation.Builder questionbuilder = ContentProviderOperation
						.newInsert(Questions.CONTENT_URI);
				questionbuilder.withValue(Questions.QUESTION_ID, questionId);
				questionbuilder.withValue(Questions.CONTENT, question);
				questionbuilder.withValue(Questions.LANGUAGE_CODE, lancode);
				questionbuilder.withValue(Questions.ITEM_ID, itemId);
				questionbuilder.withValue(Questions.AUTHOR_ID, authorId);
				questionbuilder.withValue(Questions.UPDATE_TIME,
						o.getLong("updatetime"));

				int state = Questions.NotAnsweredState;

				if (!StringUtils.isJsonParamNull("answers")) {
					JSONArray answers = o.getJSONArray("answers");
					if (answers.length() > 0)
						state = Questions.AnsweredState;

					for (int i = 0; i < answers.length(); i++) {
						ContentProviderOperation.Builder answerbuilder = ContentProviderOperation
								.newInsert(Answers.CONTENT_URI);
						JSONObject answer = (JSONObject) answers.get(i);
						answerbuilder.withValue(Answers.ANSWER_ID,
								answer.getString("answer_id"));
						answerbuilder.withValue(Answers.Question_ID,
								answer.getString("question_id"));
						answerbuilder.withValue(Answers.ITEM_ID, itemId);
						answerbuilder.withValue(Answers.CONTENT,
								answer.getString("content"));
						answerbuilder.withValue(Answers.NICKNAME,
								answer.getString("nickname"));
						answerbuilder.withValue(Answers.AUTHOR_ID,
								answer.getString("author_id"));
						answerbuilder.withValue(Answers.UPDATE_TIME,
								answer.getLong("updatetime"));
						subbatch.add(answerbuilder.build());
					}
				}

				questionbuilder.withValue(Questions.STATE, state);
				subbatch.add(questionbuilder.build());
			}
		} catch (Exception e) {

		}
		
//		if (values == null) {
			builder = ContentProviderOperation.newInsert(Items.CONTENT_URI);
			builder.withValue(Items.ITEM_ID, itemId);
//		} else {
//			builder = ContentProviderOperation.newUpdate(Items
//					.buildItemUri(itemId));
//			Uri titleUri = Items.buildItemtitlesUri(itemId);
//			Uri tagUri = Items.buildItemtagsUri(itemId);
//			Uri commentUri = Items.buildItemcommentsUri(itemId);
//			ContentProviderOperation.Builder titlebuilder = ContentProviderOperation
//					.newDelete(titleUri);
//			ContentProviderOperation.Builder tagbuilder = ContentProviderOperation
//					.newDelete(tagUri);
//			ContentProviderOperation.Builder commentbuilder = ContentProviderOperation
//					.newDelete(commentUri);
//			batch.add(titlebuilder.build());
//			batch.add(tagbuilder.build());
//			batch.add(commentbuilder.build());
//		}

		if (!StringUtils.isJsonParamNull(o, "userid"))
			builder.withValue(Items.AUTHOR_ID, authorId);
		if (!StringUtils.isJsonParamNull(o, "nickname"))
			builder.withValue(Items.NICK_NAME, o.getString("nickname"));
		if (!StringUtils.isJsonParamNull(o, "photourl"))
			builder.withValue(Items.PHOTO_URL, o.getString("photourl"));
		if (!StringUtils.isJsonParamNull(o, "place"))
			builder.withValue(Items.PLACE, o.getString("place"));
		if (!StringUtils.isJsonParamNull(o, "relate"))
			builder.withValue(Items.RELATE, o.getString("relate"));
		if (!StringUtils.isJsonParamNull(o, "note"))
			builder.withValue(Items.NOTE, o.getString("note"));
		if (!StringUtils.isJsonParamNull(o, "category"))
			builder.withValue(Items.CATEGORY, o.getString("category"));
		if (!StringUtils.isJsonParamNull(o, "file_type"))
			builder.withValue(Items.FILE_TYPE, o.getString("file_type"));
		if (!StringUtils.isJsonParamNull(o, "itemLat"))
			builder.withValue(Items.LATITUTE, o.getDouble("itemLat"));
		if (!StringUtils.isJsonParamNull(o, "itemLng"))
			builder.withValue(Items.LNGITUTE, o.getDouble("itemLng"));
		builder.withValue(Items.UPDATE_TIME, o.getLong("updatetime"));
		if (!StringUtils.isJsonParamNull(o, "disabled")) {
			builder.withValue("disabled", o.getInt("disabled"));
			if (o.getInt("disabled") == 1) {
				Uri quizUri = Quizs.CONTENT_URI;
				ContentProviderOperation.Builder quizBuilder = ContentProviderOperation
						.newUpdate(quizUri);
				quizBuilder.withValue(Quizs.ANSWER_STATE,
						Constants.PassAnsweredState);
				quizBuilder.withSelection(Items.ITEM_ID + "=?",
						new String[] { itemId });
				batch.add(quizBuilder.build());
			}
		}

		builder.withValue(Items.SYNC_TYPE, sync_mode);
		builder.withValue(Items.UPDATED, Calendar.getInstance()
				.getTimeInMillis());
		batch.add(builder.build());
		batch.addAll(subbatch);
		return batch;
	}

	public static ArrayList<ContentProviderOperation> deleteItemBatch(
			String itemId) {
		final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();

		Uri itemUri = Items.buildItemUri(itemId);
		Uri titleUri = Items.buildItemtitlesUri(itemId);
		Uri tagUri = Items.buildItemtagsUri(itemId);
		Uri commentUri = Items.buildItemcommentsUri(itemId);
		Uri answerUri = Items.buildItemAnswerUri(itemId);
		Uri questionUri = Items.buildItemQuestionUri(itemId);
		ContentProviderOperation.Builder titlebuilder = ContentProviderOperation
				.newDelete(titleUri);
		ContentProviderOperation.Builder tagbuilder = ContentProviderOperation
				.newDelete(tagUri);
		ContentProviderOperation.Builder commentbuilder = ContentProviderOperation
				.newDelete(commentUri);
		ContentProviderOperation.Builder answerbuilder = ContentProviderOperation
		.newDelete(answerUri);
		ContentProviderOperation.Builder questionbuilder = ContentProviderOperation
		.newDelete(questionUri);
		ContentProviderOperation.Builder itembuilder = ContentProviderOperation
				.newDelete(itemUri);
		batch.add(titlebuilder.build());
		batch.add(tagbuilder.build());
		batch.add(commentbuilder.build());
		batch.add(answerbuilder.build());
		batch.add(questionbuilder.build());
		batch.add(itembuilder.build());
		return batch;
	}
	

	public static List<ContentValues> searchToUpdateItems(ContentResolver resolver) {
		Cursor cursor = resolver.query(Items.CONTENT_URI,
				ItemsQuery.PROJECTION, Items.SYNC_TYPE + "=? or "+Items.SYNC_TYPE+"=? ",
				new String[] { Items.SYNC_TYPE_CLIENT_INSERT.toString(), Items.SYNC_TYPE_CLIENT_UPDATE.toString() },
				Items.DEFAULT_SORT);

		List<ContentValues> result = new ArrayList<ContentValues>();
		
		ContentValues values = null;
		try {
			if (cursor.moveToFirst()) {
				values = new ContentValues();
				values.put(Items.ITEM_ID, cursor.getString(ItemsQuery.ITEM_ID));
				values.put(Items.AUTHOR_ID,
						cursor.getString(ItemsQuery.AUTHOR_ID));
				values.put(Items.ATTACHED,
						cursor.getString(ItemsQuery.ATTACHED));
				values.put(Items.CATEGORY,
						cursor.getString(ItemsQuery.CATEGORY));
				values.put(Items.LATITUTE,
						cursor.getDouble(ItemsQuery.LATITUTE));
				values.put(Items.LNGITUTE,
						cursor.getDouble(ItemsQuery.LNGITUTE));
				values.put(Items.SPEED,
						cursor.getFloat(ItemsQuery.SPEED));
				values.put(Items.LOCATION_BASED,
						cursor.getString(ItemsQuery.LOCATION_BASED));
				values.put(Items.PLACE,
						cursor.getString(ItemsQuery.PLACE));
				values.put(Items.RELATE,
						cursor.getString(ItemsQuery.RELATE));
				values.put(Items.NOTE,
						cursor.getString(ItemsQuery.NOTE));
				values.put(Items.QUESTION_TYPES,
						cursor.getString(ItemsQuery.QUESTION_TYPES));
				values.put(Items.SHARE,
						cursor.getString(ItemsQuery.SHARE));
				values.put(Items.TAG,
						cursor.getString(ItemsQuery.TAG));
				values.put(Items.SYNC_TYPE, cursor.getInt(ItemsQuery.SYNC_TYPE));
				result.add(values);
			}
		} finally {
			cursor.close();
		}
		return result;
	}

	public static ArrayList<ContentProviderOperation> upload(Context context, ContentValues cv) {
		ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
		try {
			DefaultHttpClient client = HttpClientFactory
					.getInstance(context);
			MultipartEntity entity = new MultipartEntity();
			String itemId = cv.getAsString(Items.ITEM_ID);
			Integer syncType = cv.getAsInteger(Items.SYNC_TYPE);
			
			ContentResolver resolver = context.getContentResolver();
			Uri titleuri = Items.buildItemtitlesUri(itemId);
			Cursor titleCursor = resolver.query(titleuri,
					ItemtitlesQuery.PROJECTION, null, null, null);
			
			Charset charset = Charset.forName("UTF-8");
			
			try {
				if (!titleCursor.moveToFirst())
					return batch;
				do {
					String code = titleCursor
							.getString(ItemtitlesQuery.CODE);
					String value = titleCursor
							.getString(ItemtitlesQuery.CONTENT);
					entity.addPart("titleMap['" + code + "']",
							new StringBody(value, charset));
				} while (titleCursor.moveToNext());
			} finally {
				titleCursor.close();
			}
			
			Uri questionuri = Items.buildItemQuestionUri(itemId);
			Cursor questionCursor = resolver.query(questionuri,
					QuestionQuery.PROJECTION, null, null, null);
			try{
				if(questionCursor.moveToFirst()){
					String code = questionCursor.getString(QuestionQuery.LANGUAGE_CODE);
					String content = questionCursor.getString(QuestionQuery.CONTENT);
					if(code!=null&&code.length()>0&&content!=null&&content.length()>0){
						entity.addPart("quesLan", new StringBody(code));
						entity.addPart("question", new StringBody(content, charset));
					}
				}
			}finally{
				questionCursor.close();
			}
			

			entity.addPart("shareLevel",
					new StringBody(cv.getAsString(Items.SHARE)));
			entity.addPart("categoryId",
					new StringBody(cv.getAsString(Items.CATEGORY)));
			entity.addPart(
					"locationBased",
					new StringBody(String.valueOf(cv
							.getAsString(Items.LOCATION_BASED))));

			String qts = cv.getAsString(Items.QUESTION_TYPES);
			List<String> questionTypes = StringUtils.stringToArray(qts);
			for (String qt : questionTypes) {
				entity.addPart("questionTypeIds", new StringBody(qt));
			}

			if(cv.getAsString(Items.PLACE)!=null){
				entity.addPart("place", new StringBody(cv.getAsString(Items.PLACE), charset));
			}
				
			if(cv.getAsString(Items.RELATE)!=null){
				entity.addPart("relate",new StringBody(cv.getAsString(Items.RELATE),charset));
			}
			if (cv.getAsString(Items.NOTE) != null)
				entity.addPart("note",
						new StringBody(cv.getAsString(Items.NOTE), charset));

			if (cv.getAsDouble(Items.LATITUTE) != null
					&& cv.getAsDouble(Items.LNGITUTE) != null) {
				entity.addPart("itemLat",
						new StringBody(cv.getAsDouble(Items.LATITUTE)
								.toString()));
				entity.addPart("itemLng",
						new StringBody(cv.getAsDouble(Items.LNGITUTE)
								.toString()));
			}
			if (cv.getAsFloat(Items.SPEED) != null)
				entity.addPart("speed",
						new StringBody(cv.getAsFloat(Items.SPEED)
								.toString()));

			if (cv.getAsString(Items.TAG) != null) {
				entity.addPart("tag",
						new StringBody(cv.getAsString(Items.TAG), charset));
			}

			try {
				if (cv.getAsString(Items.ATTACHED) != null) {
					File file = new File(cv.getAsString(Items.ATTACHED));
					entity.addPart("image", new FileBody(file));
				}
			} catch (Exception e) {

			}

			HttpResponse response = null;
			if (Items.SYNC_TYPE_CLIENT_INSERT.equals(syncType)) {
				HttpPost httpPost = new HttpPost(ApiConstants.ITEM_Add_URI);
				httpPost.addHeader("charset", HTTP.UTF_8);  
				httpPost.setEntity(entity);
				response = client.execute(httpPost);
			} else {
				HttpPost httpPost = new HttpPost(ApiConstants.ITEM_Add_URI
						+ "/" + itemId+"/edit");
				httpPost.addHeader("charset", HTTP.UTF_8);
				httpPost.setEntity(entity);
				response = client.execute(httpPost);
			}
			Integer statuse = response.getStatusLine().getStatusCode();
			if (statuse == 200) {
				if (Items.SYNC_TYPE_CLIENT_INSERT.equals(syncType)) {
					return JsonItemUtil.deleteItemBatch(itemId);
				}else{
					ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(Items.buildItemUri(itemId));
					builder.withValue(Items.SYNC_TYPE, Items.SYNC_TYPE_PUSH);
					batch.add(builder.build());
				}
				return batch;
			}
		} catch (Exception e) {
			Log.e("LearningLog", "upload failed", e);
		}
		return batch;
	}

	public static ContentValues queryItemById(String itemId,
			ContentResolver resolver) {
		String sortOrder = ItemsQuery.UPDATE_TIME + " desc";
		// String selection = Items.ITEM_ID + " =? ";
		// String[] selectionArgs = new String[] { itemId };
		Uri uri = Items.buildItemtitlesUri(itemId);
		Cursor cursor = resolver.query(uri, ItemsQuery.PROJECTION, null, null,
				sortOrder);

		ContentValues values = null;
		try {
			if (cursor.moveToFirst()) {
				values = new ContentValues();
				values.put(Items.ITEM_ID, cursor.getString(ItemsQuery.ITEM_ID));
				values.put(Items.AUTHOR_ID,
						cursor.getLong(ItemsQuery.AUTHOR_ID));
				values.put(Items.UPDATE_TIME,
						cursor.getLong(ItemsQuery.UPDATE_TIME));
			}
		} finally {
			cursor.close();
		}
		return values;
	}

	public interface ItemsQuery {
		String[] PROJECTION = { Items.ITEM_ID, Items.AUTHOR_ID,
				Items.UPDATE_TIME, Items.PHOTO_URL, Items.NOTE, Items.TAG,
				Items.ATTACHED, Items.CATEGORY, Items.LATITUTE, Items.LNGITUTE,
				Items.SPEED, Items.SHARE, Items.QUESTION_TYPES,
				Items.LOCATION_BASED, Items.SYNC_TYPE, Items.PLACE, Items.RELATE};

		int ITEM_ID = 0;
		int AUTHOR_ID = 1;
		int UPDATE_TIME = 2;
		int PHOTO_URL = 3;
		int NOTE = 4;
		int TAG = 5;
		int ATTACHED = 6;
		int CATEGORY = 7;
		int LATITUTE = 8;
		int LNGITUTE = 9;
		int SPEED = 10;
		int SHARE = 11;
		int QUESTION_TYPES = 12;
		int LOCATION_BASED = 13;
		int SYNC_TYPE = 14;
		int PLACE = 15;
		int RELATE = 16;
	}
	
    public interface ItemtitlesQuery {
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
    
    public interface QuestionQuery {
    	int _Token = R.drawable.title_button;
        String[] PROJECTION = {
                Questions._ID,
                Questions.LANGUAGE_CODE,
                Questions.CONTENT
        };

        int _ID = 0;
        int LANGUAGE_CODE = 1;
        int CONTENT = 2;
    }
    
    public interface AnswerQuery {
    	int _Token = R.drawable.wrong;
        String[] PROJECTION = {
                Answers._ID,
                Answers.CONTENT,
                Answers.NICKNAME
        };

        int _ID = 0;
        int CONTENT = 1;
        int NICKNAME = 2;
    }
    
}
