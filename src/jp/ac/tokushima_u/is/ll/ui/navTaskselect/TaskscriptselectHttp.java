package jp.ac.tokushima_u.is.ll.ui.navTaskselect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import jp.ac.tokushima_u.is.ll.provider.TaskDatabase;
import jp.ac.tokushima_u.is.ll.ui.navTask.task_screen;
import jp.ac.tokushima_u.is.ll.util.LocalApiContants;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

public class TaskscriptselectHttp extends Activity {
	private static final String TAG = "SyncService";

	private DefaultHttpClient client;
	HttpPost httpMethod;
	private Context context;
	MultipartEntity entity;
	private TextView message;
	/** * 通信中プログレスバー */
	private ProgressDialog prog;
	private String taskid;
	private String title;
	private String level;
	private String locationinfo;
	private String place;
	String[] related_title = new String[20];
	String[] related_image = new String[20];

	Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
	public static SQLiteDatabase db;

	private final Handler handler = new Handler() {
		/** * レスポンス取得でUIを更新する */
		public void handleMessage(Message msg) {
			prog.dismiss();
			String bmess = msg.getData().getString("RESPONSE");

			// message.setText(bmess);
		}
	};


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		taskid = extras.getString("taskid");
		title = extras.getString("title");
		level = extras.getString("level");
		related_title = extras.getStringArray("related_title");
		related_image = extras.getStringArray("related_image");
		place=extras.getString("place");
		System.gc();
		taskscriptRequest();

	}

	private void taskscriptRequest() {
		// TODO Auto-generated method stub
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

						final JSONArray lat_array = json.getJSONArray("lat");
						final JSONArray lng_array = json.getJSONArray("lng");
						final JSONArray num_array = json.getJSONArray("num");
						final JSONArray script_array = json
								.getJSONArray("script");
						final JSONArray task_id_array = json
								.getJSONArray("task_id");
						final JSONArray image_id_array = json
								.getJSONArray("image_id");
						
						final JSONArray location_array = json
								.getJSONArray("location");
						final JSONArray image_name_array = json
								.getJSONArray("image_name");
						String[] idresult = new String[id_array.length()];
						String[] lat_arrayresult = new String[lat_array
								.length()];
						String[] lng_arrayresult = new String[lng_array
								.length()];
						String[] num_arrayresult = new String[num_array
								.length()];
						String[] script_arrayresult = new String[script_array
								.length()];
						String[] task_id_arrayresult = new String[task_id_array
								.length()];
						String[] image_id_arrayresult = new String[image_id_array
								.length()];

						String[] location_arrayresult = new String[location_array
						           								.length()];
						String[] image_name_arrayresult = new String[image_name_array
							           								.length()];
						
						if (id_array != null) {
							for (int i = 0; i < id_array.length(); i++) {
								idresult[i] = (String) id_array.get(i);

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
						if (num_array != null) {
							for (int i = 0; i < num_array.length(); i++) {
								num_arrayresult[i] = (String) num_array.get(i);

							}
						}
						if (script_array != null) {
							for (int i = 0; i < script_array.length(); i++) {
								script_arrayresult[i] = (String) script_array
										.get(i);

							}
						}
						if (task_id_array != null) {
							for (int i = 0; i < task_id_array.length(); i++) {
								task_id_arrayresult[i] = (String) task_id_array
										.get(i);

							}
						}
						if (image_id_array != null) {
							for (int i = 0; i < image_id_array.length(); i++) {
								image_id_arrayresult[i] = (String) image_id_array
										.get(i);

							}
						}
						if (location_array != null) {
							for (int i = 0; i < location_array.length(); i++) {
								location_arrayresult[i] = (String) location_array
										.get(i);

							}
						}
						if (image_name_array != null) {
							for (int i = 0; i < image_name_array.length(); i++) {
								image_name_arrayresult[i] = (String) image_name_array
										.get(i);

							}
						}
						String script[]=new String[script_arrayresult.length];
						for(int i=0;i<script_arrayresult.length;i++){
							script[i]="Step "+(i+1)+" : "+script_arrayresult[i];
						}
						Intent logintent = new Intent(
								TaskscriptselectHttp.this, task_screen.class);
						logintent.putExtra("ID", 1);
						logintent.putExtra("taskname", title);
						logintent.putExtra("Tasklat", lat_arrayresult);
						logintent.putExtra("Tasklng", lng_arrayresult);
						logintent.putExtra("level", level);
						logintent.putExtra("taskscript", script_arrayresult);
						logintent.putExtra("selectscript", script);
						logintent.putExtra("image", image_id_arrayresult);
						logintent.putExtra("related_image",related_image);
						logintent.putExtra("related_title",related_title);
						logintent.putExtra("location_info",location_arrayresult);
						logintent.putExtra("place",place);
						logintent.putExtra("image_name",image_name_arrayresult);
						TaskscriptselectHttp.this.startActivity(logintent);

					}
				} catch (Exception e) {
					Log.e("Exception", e.getMessage());
					finish();
					Log.d(TAG,
							"An exceptio occured when a array of items are retrieved",
							e);
				}
				finish();
				return result;
			}
		};

		// 通信の実行
		new Thread() {
			public void run() {
				client = null;
				entity = null;
				httpMethod = null;
				client = new DefaultHttpClient();
				httpMethod = new HttpPost(LocalApiContants.taskscript_url);
				
				entity = new MultipartEntity(null, null, DEFAULT_CHARSET);

				int i = 0;
				try {
					httpMethod.setEntity(entity);
					entity.addPart("taskId", new StringBody(taskid,
							DEFAULT_CHARSET));
					client.execute(httpMethod, response);

				} catch (Exception e) {
					if (e != null)
						Log.e("ERROR", e.getMessage());

				}

				finally {

				}
			}
		}.start();
	}



}