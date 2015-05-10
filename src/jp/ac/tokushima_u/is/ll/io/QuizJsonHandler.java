package jp.ac.tokushima_u.is.ll.io;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.ac.tokushima_u.is.ll.provider.LearningLogContract;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Quizs;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.Constants;
import jp.ac.tokushima_u.is.ll.util.FormatUtil;
import jp.ac.tokushima_u.is.ll.util.JsonQuizUtil;
import jp.ac.tokushima_u.is.ll.util.Lists;

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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class QuizJsonHandler extends JsonHandler {
	private static final String TAG = "SessionsHandler";
	private static final String systemUrl = ApiConstants.system_url;
	private static final String syncServiceUrl = "/sync";
	private static final String quizSearchUrl = "/quiz.json";
	private DefaultHttpClient client;
	private Context context;

	public QuizJsonHandler(DefaultHttpClient httpClient,Context context) {
		super(LearningLogContract.CONTENT_AUTHORITY);
		this.client = httpClient;
		this.context = context;
	}

	public ArrayList<ContentProviderOperation> parse(ContentResolver resolver) {
		final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
		
		List<ContentValues> cvs = JsonQuizUtil.searchToUpdateQuiz(resolver);
		for(ContentValues cv:cvs){
			batch.addAll(JsonQuizUtil.update(context, cv));
		}
		
		Cursor quizzes = resolver.query(Quizs.CONTENT_URI, JsonQuizUtil.QuizsQuery.PROJECTION, Quizs.ANSWER_STATE+"=?", new String[]{Constants.NotAnsweredState.toString()}, Quizs.DEFAULT_SORT);
		int size = -1;
		try{
			size = quizzes.getCount();
		}finally{
			quizzes.close();
		}
		
		SharedPreferences setting = context.getSharedPreferences(
				Constants.SETTING_INFOS_FILE, Context.MODE_PRIVATE);
		String userid = setting.getString(Constants.SavedUserId, "");
		
		Uri uri = Quizs.CONTENT_URI;
		String sortOrder = Quizs.CREATE_TIME + " desc";
		String selection = Quizs.AUTHOR_ID+"=? and "+ Quizs.SYNC_TYPE+"=? ";
		String[]selectionArgs = {userid,Quizs.SYNC_TYPE_REQUEST.toString()}; 
		Cursor cursor = resolver.query(uri, QuizsQuery.PROJECTION, selection, selectionArgs,
				sortOrder);
		Long createTime = null;
		try {
			if (cursor.moveToFirst()) {
				createTime = cursor.getLong(QuizsQuery.CREATE_TIME);
				Log.d(TAG, "latest update is " + createTime);
			}
		} finally {
			cursor.close();
		}

		HttpPost httpPost = new HttpPost(systemUrl + syncServiceUrl
				+ quizSearchUrl);

		try {
			MultipartEntity params  = new MultipartEntity();
			if (createTime != null) {
				String ctd = FormatUtil.getJpTimeFormat().format(new Date(createTime));
				params.addPart("createDate", new StringBody(ctd));
			}
			params.addPart("userid", new StringBody(userid));
			if (createTime != null) {
				String upd = FormatUtil.getJpTimeFormat().format(new Date(createTime));
				params.addPart("createTimeFrom", new StringBody(upd));
			}
			if(size!=-1){
				params.addPart("quizsize", new StringBody(String.valueOf(size)));
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
				final JSONArray array = json.getJSONArray("quizzes");
				if (array != null) {
					for (int i = 0; i < array.length(); i++) {
						JSONObject o = array.getJSONObject(i);
						try{
							List<ContentProviderOperation> sub =JsonQuizUtil.saveQuizFromJson(o, context, Quizs.SYNC_TYPE_REQUEST);
							if(sub!=null&&sub.size()>0)
								batch.addAll(sub);
						}catch(Exception e){
							Log.d(TAG, "find id is error", e);
						}
					}
				}
			}
		} catch (Exception e) {
			Log.d(TAG, "find id is error", e);
		}

		return batch;
	}
	
	private interface QuizsQuery {
		String[] PROJECTION = { Quizs.QUIZ_ID, Quizs.AUTHOR_ID,Quizs.CREATE_TIME};
		int CREATE_TIME = 2;
	}
}
