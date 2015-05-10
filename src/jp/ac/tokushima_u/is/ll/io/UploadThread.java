package jp.ac.tokushima_u.is.ll.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.ac.tokushima_u.is.ll.provider.LearningLogContract;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Items;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.Constants;
import jp.ac.tokushima_u.is.ll.util.HttpClientFactory;
import jp.ac.tokushima_u.is.ll.util.JsonItemUtil;
import jp.ac.tokushima_u.is.ll.util.JsonItemUtil.ItemtitlesQuery;
import jp.ac.tokushima_u.is.ll.util.StringUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class UploadThread extends Thread {
	Context context; 
	Handler handler; 
	ContentValues cv;
	
	UploadThread(Context context, Handler handler, ContentValues cv){
		this.context = context;
		this.handler = handler;
		this.cv = cv;
	}
	
	public void run() {
		try {
			DefaultHttpClient client = HttpClientFactory
					.getInstance(context);
			MultipartEntity entity = new MultipartEntity();
			String itemId = cv.getAsString(Items.ITEM_ID);

			ContentResolver resolver = context.getContentResolver();
			Uri titleuri = Items.buildItemtitlesUri(itemId);
			Cursor titleCursor = resolver.query(titleuri,
					ItemtitlesQuery.PROJECTION, null, null, null);
			try {
				if (!titleCursor.moveToFirst())
					return;
				do {
					String code = titleCursor
							.getString(ItemtitlesQuery.CODE);
					String value = titleCursor
							.getString(ItemtitlesQuery.CONTENT);
					entity.addPart("titleMap['" + code + "']",
							new StringBody(value));
				} while (titleCursor.moveToNext());
			} finally {
				titleCursor.close();
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

			if (cv.getAsString(Items.NOTE) != null)
				entity.addPart("note",
						new StringBody(cv.getAsString(Items.NOTE)));

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
						new StringBody(cv.getAsString(Items.TAG)));
			}

			try {
				if (cv.getAsString(Items.ATTACHED) != null) {
					File file = new File(cv.getAsString(Items.ATTACHED));
					entity.addPart("image", new FileBody(file));
				}
			} catch (Exception e) {

			}

			Integer syncType = cv.getAsInteger(Items.SYNC_TYPE);
			HttpResponse response = null;
			if (Items.SYNC_TYPE_CLIENT_INSERT.equals(syncType)) {
				HttpPost httpPost = new HttpPost(ApiConstants.ITEM_Add_URI);
				httpPost.setEntity(entity);
				response = client.execute(httpPost);
			} else {
				HttpPut httpPut = new HttpPut(ApiConstants.ITEM_Add_URI
						+ "/" + itemId);
				// List<NameValuePair> pairs = new
				// ArrayList<NameValuePair>();
				// for(String key:titles.keySet()){
				// String value = titles.get(key);
				// // entity.addPart("titleMap['"+key+"']", new
				// StringBody(value));
				// pairs.add(new BasicNameValuePair("titleMap['"+key+"']",
				// value));
				// }
				//
				// httpPut.setEntity(new UrlEncodedFormEntity(pairs));
				// // httpPut.setEntity(entity);
				// response = client.execute(httpPut);

				HttpPost httpPost = new HttpPost(ApiConstants.ITEM_Add_URI
						+ "/" + itemId);
				entity.addPart("_method", new StringBody("put"));
				httpPost.setEntity(entity);
				response = client.execute(httpPost);
			}
			Integer result = response.getStatusLine().getStatusCode() == 200 ? 1
					: 2;
			Message msg = new Message();
			if (Constants.Item_SUCCESS.equals(result)) {
				if (Items.SYNC_TYPE_CLIENT_INSERT.equals(syncType)) {
					ArrayList<ContentProviderOperation> batch = JsonItemUtil.deleteItemBatch(itemId);
					context.getContentResolver().applyBatch(LearningLogContract.CONTENT_AUTHORITY, batch);
				}
				
				Bundle data = new Bundle();
				data.putString("itemId", itemId);
				msg.what = 1;
				msg.setData(data);
				handler.sendMessage(msg);
				// .sendEmptyMessage(1);
				// UIUtils.goSync(this);
			} else {
				msg.what = 2;
				handler.sendMessage(msg);
			}
			Log.d("LEARNINGLOG", String.valueOf(result));
		} catch (Exception e) {
			Message msg = new Message();
			msg.what = 2;
			handler.sendMessage(msg);
			Log.e("LEARNINGLOG", "Error", e);
		}
	}
}
