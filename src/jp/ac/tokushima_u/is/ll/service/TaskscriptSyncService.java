package jp.ac.tokushima_u.is.ll.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import jp.ac.tokushima_u.is.ll.provider.TaskDatabase;

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

import android.app.ProgressDialog;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

public class TaskscriptSyncService extends Service {
	private static final String TAG = "SyncService";

	private DefaultHttpClient client;
	HttpPost httpMethod;
	private Context context;
	MultipartEntity entity;
	private TextView message;
	/** * 通信中プログレスバー */
	private ProgressDialog prog;
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

	// @Override
	// public void onCreate(Bundle savedInstanceState) {
	// super.onCreate(savedInstanceState);
	// // 　リクエスト実行
	// doRequest();
	//
	// }
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onStart(Intent intent, int startId) {
		// taskRequest();
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
						final JSONArray title_array = json
								.getJSONArray("title");

						final JSONArray image_array = json
								.getJSONArray("image");

						String[] idresult = new String[title_array.length()];
						String[] lat_arrayresult = new String[image_array
								.length()];

						if (title_array != null) {
							for (int i = 0; i < title_array.length(); i++) {
								idresult[i] = (String) title_array.get(i);

							}
						}

						if (image_array != null) {
							for (int i = 0; i < image_array.length(); i++) {
								lat_arrayresult[i] = (String) image_array
										.get(i);

							}
						}

					}
				} catch (Exception e) {
					Log.e("Exception", e.getMessage());
					// finish();
					Log.d(TAG,
							"An exceptio occured when a array of items are retrieved",
							e);
				}

				return result;
			}
		};
		// this.prog = ProgressDialog.show(this, "通信中", "HTTPリクエスト送信中");
		// 通信の実行
		new Thread() {
			public void run() {
				client = null;
				entity = null;
				httpMethod = null;
				client = new DefaultHttpClient();
				// httpMethod = new HttpPost(
				// "http://192.168.0.210:8080/learninglog/TaskscriptSync/taskscript");
				httpMethod = new HttpPost(
						"http://192.168.0.192:8080/learninglog/TaskSync/relatedtask");
				// httpMethod = new
				// HttpPost("http://192.168.0.210:8080/learninglog/future/request");
				//
				entity = new MultipartEntity(null, null, DEFAULT_CHARSET);

				int i = 0;
				try {
					httpMethod.setEntity(entity);
					entity.addPart("sample", new StringBody("test",
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

	private String inputStreamToString(InputStream in) throws IOException {
		InputStreamReader isr = new InputStreamReader(in, "UTF-8");
		BufferedReader reader = new BufferedReader(isr);
		StringBuffer buffer = new StringBuffer();
		String line = null;
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
			buffer.append("\n");
		}
		reader.close();
		isr.close();
		in.close();
		return buffer.toString();
	}

}
