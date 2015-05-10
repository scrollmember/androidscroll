package jp.ac.tokushima_u.is.ll.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.ac.tokushima_u.is.ll.provider.LearningLogContract;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Settings;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.Constants;
import jp.ac.tokushima_u.is.ll.util.HttpClientFactory;
import jp.ac.tokushima_u.is.ll.util.Lists;
import jp.ac.tokushima_u.is.ll.util.StringUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

public class SettingSyncThread extends Thread{
	private String username;
	private String password;
	private Handler handler;
	private Context context;
	private Long updated;
	
	public static final int MsgServerError = 1;
	public static final int MsgUserError = 2;
	public static final int MsgSettingError = 3;
	public static final int MsgInnerError= 4;
	public static final int MsgLoginSuccess = 5;

	private static String TAG = "SetttingSyncThread";
	
	public SettingSyncThread(String username, String password, Long update, Handler handler, Context context){
		this.username = username;
		this.password = password;
		this.handler = handler;
		this.context = context;
		this.updated = update;
	}
	
	public SettingSyncThread(Handler handler, Context context){
		this.handler = handler;
		this.context = context;
	}
	
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	@Override
	public void run() {
		final DefaultHttpClient client = HttpClientFactory.createHttpClient();
		client.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
		String url = ApiConstants.Authority_URL;
		final String userid;
		final long update;
		String nickname = "";
		String defcategory = "";
		try{
//			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			if(updated!=null)
				url = url+"?update="+updated.toString();
	        final HttpGet httpGet = new HttpGet(url);
			final HttpResponse response = client.execute(httpGet);
			final HttpEntity entity = response.getEntity();
			final int statusCode = response.getStatusLine().getStatusCode();

			if(statusCode != HttpStatus.SC_OK || entity == null){
				handler.sendEmptyMessage(MsgServerError);
				return;
			}	
			
			JSONObject object = null;
			final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
			
			try{
				final String result = EntityUtils.toString(entity);
				final JSONObject json=new JSONObject(result);
				object = json.getJSONObject("userinfo");
			}catch(JSONException e){
				handler.sendEmptyMessage(MsgUserError);
				Log.d(TAG, "Log in error", e);
				return;
			}
			
			if(object==null){
				handler.sendEmptyMessage(MsgUserError);
				return;
			}	
				
			ContentProviderOperation.Builder duri = ContentProviderOperation.newDelete(Settings.CONTENT_URI);
			batch.add(duri.build());
			
			if(object!=null){
				try{
					userid = object.getString("userid");
					update = object.getLong("update");
					JSONObject mylans = object.getJSONObject("mylans");
					JSONObject studylans = object.getJSONObject("studylans");
					
					if (mylans == null || mylans.length() == 0
							|| studylans == null || studylans.length() == 0) {
						handler.sendEmptyMessage(MsgSettingError);
						return;
					}
					
					Iterator<Object>  keys = mylans.keys();
					int i = 1;
					while(keys.hasNext()){
						String key = keys.next().toString();
						String cat = mylans.getString(key);
						ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(Settings.CONTENT_URI);
						builder.withValue(Settings.AUTHOR_ID, userid);
						builder.withValue(Settings.FIELD, Settings.SETTING_MYLAN_FIELD_ID);
						builder.withValue(Settings.SETTING_ID, StringUtils.randomUUID());
						builder.withValue(Settings.CONTENT, key);
						builder.withValue(Settings.NAME, cat);
						builder.withValue(Settings.NUM,i);
						builder.withValue(Settings.UPDATED, update);
						i++;
						batch.add(builder.build());
					}
					
					keys = studylans.keys();
					
					i = 1;
					while(keys.hasNext()){
						String key = keys.next().toString();
						String cat = studylans.getString(key);
						ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(Settings.CONTENT_URI);
						builder.withValue(Settings.AUTHOR_ID, userid);
						builder.withValue(Settings.FIELD, Settings.SETTING_STUDYLAN_FIELD_ID);
						builder.withValue(Settings.SETTING_ID, StringUtils.randomUUID());
						builder.withValue(Settings.CONTENT, key);
						builder.withValue(Settings.NAME, cat);
						builder.withValue(Settings.NUM,i);
						builder.withValue(Settings.UPDATED, update);
						i++;
						batch.add(builder.build());
					}
				}catch(JSONException e){
					handler.sendEmptyMessage(MsgUserError);
					Log.d(TAG, "Log in error", e);
					return;
				}
				
				
				try{
					JSONObject categorys = object.getJSONObject("categorys");
					if (categorys == null || categorys.length() == 0) {
						handler.sendEmptyMessage(MsgSettingError);
						return;
					}
					
					Iterator<Object> keys = categorys.keys();
					int i = 1;
					while(keys.hasNext()){
						String key = keys.next().toString();
						String cat = categorys.getString(key);
						ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(Settings.CONTENT_URI);
						builder.withValue(Settings.AUTHOR_ID, userid);
						builder.withValue(Settings.FIELD, Settings.SETTING_CATEGORY_FIELD_ID);
						builder.withValue(Settings.SETTING_ID, StringUtils.randomUUID());
						builder.withValue(Settings.CONTENT, key);
						builder.withValue(Settings.NAME, cat);
						builder.withValue(Settings.NUM,i);
						builder.withValue(Settings.UPDATED, update);
						i++;
						batch.add(builder.build());
					}
					nickname = object.getString("nickname");
					defcategory = object.getString("defcategory");
				}catch(JSONException e){
//					handler.sendEmptyMessage(MsgUserError);
					Log.d(TAG, "Log in error", e);
//					return;
				}
				
				this.context.getContentResolver().applyBatch(LearningLogContract.CONTENT_AUTHORITY, batch);
				
				SharedPreferences setting = this.context.getSharedPreferences(Constants.SETTING_INFOS_FILE, Context.MODE_PRIVATE);
				Editor editor = setting.edit();
				editor.putString(Constants.SavedUserName, username);
				editor.putString(Constants.SavedPassword, password);
				editor.putString(Constants.SavedUserId, userid);
				editor.putString(Constants.SavedNickname, nickname);
				editor.putString(Constants.SavedDefaultCategory, defcategory);
				editor.commit();
				
				handler.sendEmptyMessage(MsgLoginSuccess);
				return;
			}	
		}catch(ClientProtocolException e){
			handler.sendEmptyMessage(MsgServerError);
			Log.d(TAG, "Log in error", e);
			return;
		}catch(IOException e){
			handler.sendEmptyMessage(MsgServerError);
			Log.d(TAG, "Log in error", e);
			return;
		}catch(OperationApplicationException e){
			handler.sendEmptyMessage(MsgInnerError);
			Log.d(TAG, "Log in error", e);
			return;
		}catch(RemoteException e){
			handler.sendEmptyMessage(MsgInnerError);
			Log.d(TAG, "Log in error", e);
			return;
		}
	}
	

}
