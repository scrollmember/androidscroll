package jp.ac.tokushima_u.is.ll.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

import jp.ac.tokushima_u.is.ll.provider.TaskDatabase;
import jp.ac.tokushima_u.is.ll.util.LocalApiContants;

//import jp.ac.tokushima_u.is.ll.util.ApiConstants;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TaskSyncService extends Service {
	private static final String TAG = "SyncService";

	private DefaultHttpClient client;
	HttpPost httpMethod;
	private Context context;
	MultipartEntity entity;
	private TextView message;
	/** * 通信中プログレスバー */
	private ProgressDialog prog;
	Cursor cursor;
	Cursor cursor3;
	Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
	public static SQLiteDatabase db;

	private final Handler handler = new Handler() {
		/** * レスポンス取得でUIを更新する */
		public void handleMessage(Message msg) {
			prog.dismiss();
			String bmess = msg.getData().getString("RESPONSE");

		}
	};

@Override
public IBinder onBind(Intent intent){
	return null;
}
	
	public void onStart(Intent intent,int startId){
	taskRequest();
	}


	private void taskRequest() {

		final ResponseHandler<String> response = new ResponseHandler<String>() {
			public String handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				StatusLine status = response.getStatusLine();
				response.getParams();
				HttpEntity entity = response.getEntity();
				String result = null;
			
				try {
					final String respString = EntityUtils.toString(entity,
							"UTF-8");
					final JSONObject json = new JSONObject(respString);
					int a = 1;
					if (json != null) {
						final JSONArray id_array = json.getJSONArray("id");
						JSONArray level_array = json.getJSONArray("level");
						final JSONArray title_array = json
								.getJSONArray("title");
						final JSONArray create_time_array = json
								.getJSONArray("create_time");
						final JSONArray lat_array = json.getJSONArray("lat");
						final JSONArray lng_array = json.getJSONArray("lng");
						final JSONArray place_array = json
								.getJSONArray("place");
						final JSONArray update_time_array = json
								.getJSONArray("update_time");
						// final JSONArray language_id_array = json
						// .getJSONArray("language_id");
						String[] idresult = new String[id_array.length()];
						String[] levelresult = new String[level_array.length()];
						String[] title_arrayresult = new String[title_array
								.length()];
						String[] create_time_arrayresult = new String[create_time_array
								.length()];
						String[] lat_arrayresult = new String[lat_array
								.length()];
						String[] lng_arrayresult = new String[lng_array
								.length()];
						String[] place_arrayresult = new String[place_array
								.length()];
						String[] update_time_arrayresult = new String[update_time_array
								.length()];
						// String[] language_id_arrayresult = new
						// String[language_id_array
						// .length()];
						if (id_array != null) {
							for (int i = 0; i < id_array.length(); i++) {
								idresult[i] = (String) id_array.get(i);

							}
						}
						if (level_array != null) {
							for (int i = 0; i < level_array.length(); i++) {
								levelresult[i] = (String) level_array.get(i);

							}
						}
						if (title_array != null) {
							for (int i = 0; i < title_array.length(); i++) {
								title_arrayresult[i] = (String) title_array
										.get(i);

							}
						}
						if (create_time_array != null) {
							for (int i = 0; i < create_time_array.length(); i++) {
								create_time_arrayresult[i] = (String) create_time_array
										.get(i);

							}
						}
						if (lat_array != null) {
							for (int i = 0; i < lat_array.length(); i++) {
								lat_arrayresult[i] = (String) lat_array.get(i);

							}
						}
						if (lng_array != null) {
							for (int i = 0; i < lng_array.length(); i++) {
								lng_arrayresult[i] = (String) lng_array.get(i);

							}
						}
						if (place_array != null) {
							for (int i = 0; i < place_array.length(); i++) {
								place_arrayresult[i] = (String) place_array
										.get(i);

							}
						}
						if (update_time_array != null) {
							for (int i = 0; i < update_time_array.length(); i++) {
								update_time_arrayresult[i] = (String) update_time_array
										.get(i);

							}
						}
						// if (language_id_array != null) {
						// for (int i = 0; i < language_id_array.length(); i++)
						// {
						// language_id_arrayresult[i] = (String)
						// language_id_array.get(i);
						//
						// }
						// }
						boolean flag = true;
						int flagi = 0;
						TaskDatabase helper = new TaskDatabase(
								getApplicationContext());

						db = helper.getReadableDatabase();
						cursor3 = db.rawQuery(
								"select * from TASK_TABLE;", null);

						if (cursor3.getCount() != 0) {
							db.close();
							db = helper.getReadableDatabase();
							cursor = db.rawQuery(
									"select * from TASK_TABLE;", null);
							for (int ci = 0; ci < id_array.length(); ci++) {
								while (cursor.moveToNext()) {
									flag = false;
									if (cursor.getString(1)
											.equals(idresult[ci])) {
										flag = true;
										break;
										// break;
									}
									flagi = ci;
								}
								cursor.moveToFirst();
								
								if (flag == false) {
									// db.close();
									db = helper.getWritableDatabase();
									ContentValues values = new ContentValues();
									values.put("user_id", idresult[flagi]);
									values.put("create_time",
											create_time_arrayresult[flagi]);
									values.put("lat", lat_arrayresult[flagi]);
									values.put("lng", lng_arrayresult[flagi]);
									values.put("place",
											place_arrayresult[flagi]);
									values.put("title",
											title_arrayresult[flagi]);
									values.put("update_time",
											update_time_arrayresult[flagi]);
									values.put("level", levelresult[flagi]);
									db.insert("TASK_TABLE", null, values);
								}
							}
						} else {
							db = helper.getWritableDatabase();
							for (int i = 0; i < place_array.length(); i++) {
								ContentValues values = new ContentValues();
								values.put("user_id", idresult[i]);
								values.put("create_time",
										create_time_arrayresult[i]);
								values.put("lat", lat_arrayresult[i]);
								values.put("lng", lng_arrayresult[i]);
								values.put("place", place_arrayresult[i]);
								values.put("title", title_arrayresult[i]);
								values.put("update_time",
										update_time_arrayresult[i]);
								values.put("level", levelresult[i]);
								db.insert("TASK_TABLE", null, values);
							}

						}
						int ssss = 0;
					}
				} catch (Exception e) {
					Log.e("Exception", e.getMessage());
					// finish();
					Log.d(TAG,
							"An exceptio occured when a array of items are retrieved",
							e);
				}

				finally{
					cursor.close();
					cursor3.close();
					db.close();
//					 TaskSyncService.this.stopSelf();
					
				}
				return result;
			}
		};

		new Thread() {
			public void run() {
				client = null;
				entity = null;
				httpMethod = null;
				client = new DefaultHttpClient();
				httpMethod = new HttpPost(LocalApiContants.task_url);
				entity = new MultipartEntity(null, null, DEFAULT_CHARSET);

				int i = 0;
				try {
					httpMethod.setEntity(entity);
					entity.addPart("sample", new StringBody("test",
							DEFAULT_CHARSET));
					client.execute(httpMethod, response);

				} catch (Exception e) {

				}

				finally {
					

					
				}
			}
		}.start();
	}

}
