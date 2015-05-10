package jp.ac.tokushima_u.is.ll.io;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import jp.ac.tokushima_u.is.ll.provider.LearningLogContract;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemcomments;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Items;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemtags;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemtitles;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Quizs;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.Constants;
import jp.ac.tokushima_u.is.ll.util.ContextUtil;
import jp.ac.tokushima_u.is.ll.util.FormatUtil;
import jp.ac.tokushima_u.is.ll.util.JsonItemUtil;
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
import org.json.JSONObject;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ItemJsonHandler extends JsonHandler {
	private static final String TAG = "SyncService";

	private String systemUrl = ApiConstants.system_url;
	private String syncServiceUrl = "/sync";
	private String itemSearchUrl = "/item.json";
	private DefaultHttpClient client;
	private Context context;
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Log.e("Learning Log", msg.what+"  received");
		}
	};
	
	public ItemJsonHandler(DefaultHttpClient httpClient,Context context) {
		super(LearningLogContract.CONTENT_AUTHORITY);
		this.client = httpClient;
		this.context = context;
	}

//	private void beforeSync(){
//		List<ContentValues> cvs = JsonItemUtil.searchToUpdateItems(context.getContentResolver());
//		for(ContentValues cv:cvs){
//			new UploadThread(context, handler, cv).start();
//		}
//	}
	
	
	public ArrayList<ContentProviderOperation> parse(ContentResolver resolver) {
		final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
		List<ContentValues> cvs = JsonItemUtil.searchToUpdateItems(context.getContentResolver());
		for(ContentValues cv:cvs){
			ArrayList<ContentProviderOperation> updatebatch = JsonItemUtil.upload(context, cv);
			try{
				resolver.applyBatch(LearningLogContract.CONTENT_AUTHORITY, updatebatch);
			}catch(Exception e){
				Log.d(TAG, "server update exception occurred");
			}
		}
		
		String userid = ContextUtil.getUserId(context);
		Uri uri = Items.CONTENT_URI;
		String sortOrder = Items.UPDATE_TIME + " asc";
		String myselection = Items.AUTHOR_ID+"=? and "+Items.SYNC_TYPE+"=?";
		String otherselection = Items.AUTHOR_ID+"!=? and "+Items.SYNC_TYPE+"=?";
		String[] selectionArgs = {userid, Items.SYNC_TYPE_REQUEST.toString()};
		Cursor mycursor = resolver.query(uri, ItemsQuery.PROJECTION, myselection, selectionArgs,
				sortOrder);
		Cursor othercursor = resolver.query(uri, ItemsQuery.PROJECTION, otherselection, selectionArgs,
				sortOrder);
		Long myupdateTime = null;
		try {
			if (mycursor.moveToFirst()) {
				myupdateTime = mycursor.getLong(ItemsQuery.UPDATE_TIME);
				Log.d(TAG, "my latest update is " + myupdateTime);
			}
		} finally {
			mycursor.close();
		}
		
		Long otherupdateTime = null;
		try {
			if (othercursor.moveToFirst()) {
				otherupdateTime = othercursor.getLong(ItemsQuery.UPDATE_TIME);
				Log.d(TAG, "latest update is " + otherupdateTime);
			}
		} finally {
			othercursor.close();
		}

		HttpPost httpPost = new HttpPost(this.systemUrl + this.syncServiceUrl
				+ this.itemSearchUrl);
		
		try{
			MultipartEntity params = new MultipartEntity();
			
			params.addPart("userId", new StringBody(userid));
			if (myupdateTime != null) {
				String upd = FormatUtil.getJpTimeFormat().format(new Date(myupdateTime));
				params.addPart("updateDateFrom", new StringBody(upd));
			}
			httpPost.setEntity(params);
			
			HttpResponse response = client.execute(httpPost);
			HttpEntity entity = response.getEntity();
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK && entity != null){
				batch.addAll(this.analyzeJson(entity, resolver));
			}
		}catch(Exception e){
			Log.e(TAG, "My Item json errors ", e);
		}

		try{
			MultipartEntity params = new MultipartEntity();
			params.addPart("notuserId", new StringBody(userid));
			if (otherupdateTime != null) {
				String upd = FormatUtil.getJpTimeFormat().format(new Date(otherupdateTime));
				params.addPart("updateDateFrom", new StringBody(upd));
			}
			
			params.addPart("num", new StringBody(Items.PAGE_SIZE.toString()));
			
			httpPost.setEntity(params);
			
			HttpResponse response  = client.execute(httpPost);
			HttpEntity entity = response.getEntity();
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK && entity != null){
				batch.addAll(this.analyzeJson(entity, resolver));
			}
		}catch(Exception e){
			Log.e(TAG, "Latest Item json errors ", e);
		}
		
		return batch;
	}
	
	private ArrayList<ContentProviderOperation> analyzeJson(final HttpEntity entity, ContentResolver resolver){
		final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
		try {
			final String respString = EntityUtils.toString(entity);
			final JSONObject json = new JSONObject(respString);
			if (json != null) {
				final JSONArray array = json.getJSONArray("items");
				if (array != null) {
					for (int i = 0; i < array.length(); i++) {
						JSONObject o = array.getJSONObject(i);
						try{
							ArrayList<ContentProviderOperation> sub = JsonItemUtil.saveItemFromJson(o, resolver, Items.SYNC_TYPE_REQUEST);
							if(sub!=null&&sub.size()>0)
								batch.addAll(sub);
						}catch(Exception e){
							Log.d(TAG, "An exception occuren when an item is retrieved ", e);
						}
					}
				}
			}
		} catch (Exception e) {
			Log.d(TAG, "An exceptio occured when a array of items are retrieved", e);
		}
		return batch;
	}


	private ContentValues queryItemById(String itemId, ContentResolver resolver) {
		String sortOrder = ItemsQuery.UPDATE_TIME + " desc";
//		String selection = Items.ITEM_ID + " =? ";
//		String[] selectionArgs = new String[] { itemId };
		Uri uri = Items.buildItemtitlesUri(itemId);
		Cursor cursor = resolver.query(uri,
				ItemsQuery.PROJECTION, null, null, sortOrder);

		ContentValues values = null;
		try {
			if (cursor.moveToFirst()) {
				values = new ContentValues();
				values.put(Items.ITEM_ID, cursor.getString(ItemsQuery.ITEM_ID));
				values.put(Items.AUTHOR_ID,
						cursor.getLong(ItemsQuery.AUTHOR_ID));
			}
		} finally {
			cursor.close();
		}
		return values;
	}

	private interface ItemsQuery {
		String[] PROJECTION = { Items.ITEM_ID, Items.AUTHOR_ID,
				Items.UPDATE_TIME, Items.PHOTO_URL, Items.NOTE, Items.TAG };

		int ITEM_ID = 0;
		int AUTHOR_ID = 1;
		int UPDATE_TIME = 2;
		int PHOTO_URL = 3;
		int NOTE = 4;
		int TAG = 5;
	}
}
