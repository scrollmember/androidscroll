package jp.ac.tokushima_u.is.ll.ui.navTaskselect;

/**
 * 
 * @author Kousuke Mouri University of Tokushima,Japan
 * 
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import jp.ac.tokushima_u.is.ll.ui.navTask.TestActivity;
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
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class RelatedItemHttp extends Activity {
	private static final String TAG = "SyncService";

	private DefaultHttpClient client;
	HttpPost httpMethod;
	private Context context;
	MultipartEntity entity;

	private String taskid;
	private String level;
	private String title;
	private String place;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		info();
		taskscriptRequest();
	}

	private void info() {
		// TODO Auto-generated method stub
		Bundle extras = getIntent().getExtras();
		taskid = extras.getString("taskid");
		level = extras.getString("level");
		title = extras.getString("title");
		place= extras.getString("place");
	}

	private void taskscriptRequest() {
		// TODO Auto-generated method stub
		final ResponseHandler<String> response = new ResponseHandler<String>() {
			public String handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				StatusLine status = response.getStatusLine();
			if(response.getStatusLine().getStatusCode()>=400){
				Toast.makeText(context,"Navigator System can not connect to the server.Your location is weak radio wave",Toast.LENGTH_LONG);
				finish();
			}
			else{
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

						final JSONArray title_id_array = json
								.getJSONArray("title_id");
						final JSONArray image_id_array = json
								.getJSONArray("image_id");

						final JSONArray image_array = json
								.getJSONArray("image");

						String[] idresult = new String[title_array.length()];
						String[] lat_arrayresult = new String[image_array
								.length()];
						String[] idresult2 = new String[title_array.length()];
						String[] lat_arrayresult2 = new String[image_array
								.length()];

						String[] titleid_result = new String[title_id_array
								.length()];
						String[] imageid_result = new String[image_id_array
								.length()];

						if (title_id_array != null) {
							for (int i = 0; i < title_id_array.length(); i++) {
								titleid_result[i] = (String) title_id_array
										.get(i);

							}
						}

						if (image_id_array != null) {
							for (int i = 0; i < image_id_array.length(); i++) {
								imageid_result[i] = (String) image_id_array
										.get(i);

							}
						}

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

						for (int j = 0; j < titleid_result.length; j++) {
							for (int j2 = 0; j2 < imageid_result.length; j2++) {
								if (titleid_result[j]
										.equals(imageid_result[j2])) {
									idresult2[j] = idresult[j];
									lat_arrayresult2[j] = lat_arrayresult[j2];
								}
							}
						}

						Intent Item = new Intent(RelatedItemHttp.this,
								TaskscriptselectHttp.class);
						Item.putExtra("taskid", taskid);
						Item.putExtra("title", title);
						Item.putExtra("level", level);
						Item.putExtra("related_title", idresult2);
						Item.putExtra("related_image", lat_arrayresult2);
						Item.putExtra("place",place);
						startActivity(Item);
						finish();

					}
				} catch (Exception e) {
					Log.e("Exception", e.getMessage());
					// finish();
					Log.d(TAG,
							"An exceptio occured when a array of items are retrieved",
							e);
				}
			}
				return null;
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
				httpMethod = new HttpPost(LocalApiContants.relatedItem);
				entity = new MultipartEntity(null, null, DEFAULT_CHARSET);

				int i = 0;
				try {
					httpMethod.setEntity(entity);
					entity.addPart("taskid", new StringBody(taskid,
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
