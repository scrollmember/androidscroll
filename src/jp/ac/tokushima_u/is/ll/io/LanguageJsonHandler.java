/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.ac.tokushima_u.is.ll.io;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import jp.ac.tokushima_u.is.ll.provider.LearningLogContract;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Languages;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.SyncColumns;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.HttpClientFactory;
import jp.ac.tokushima_u.is.ll.util.Lists;

/**
 * Handle a remote {@link XmlPullParser} that defines a set of {@link Sessions}
 * entries. Assumes that the remote source is a Google Spreadsheet.
 */
public class LanguageJsonHandler extends JsonHandler {
    private static final String TAG = "LanguageJsonHandler";
	private String systemUrl= ApiConstants.system_url;
	private String syncServiceUrl = "/sync";
	private String languageSearchUrl = "/language.json";
    
    public LanguageJsonHandler() {
        super(LearningLogContract.CONTENT_AUTHORITY);
    }

    public ArrayList<ContentProviderOperation> parse(ContentResolver resolver){
		final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
		
    	Uri uri = Languages.CONTENT_URI;
    	String sortOrder = SyncColumns.UPDATED +" desc";
    	Cursor cursor = resolver.query(uri, LanguagesQuery.PROJECTION , null, null, sortOrder);
        try {
            if (cursor.moveToFirst()) {
                Log.d(TAG, "Language has been inserted");
                int count = cursor.getCount();
                if(count>3){
                	return batch; 
                }
            } 
        } finally {
            cursor.close();
        }
        
		HttpPost httpPost = new HttpPost(this.systemUrl+this.syncServiceUrl+this.languageSearchUrl);
		try{
			DefaultHttpClient client = HttpClientFactory.createHttpClient();
			MultipartEntity params = new MultipartEntity();
			httpPost.setEntity(params);
			HttpResponse response = client.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				String result = convertStreamToString(instream);
				instream.close();
				JSONObject json=new JSONObject(result);
				if(json!=null){
					JSONArray array = json.getJSONArray("languages");
					if(array!=null){
						for(int i=0;i<array.length();i++){
						   JSONObject o = array.getJSONObject(i);
						   String languageId = o.getString("id");
						   if(languageId==null||languageId.length()<=0)
							   continue;

							ContentProviderOperation.Builder builder = ContentProviderOperation
									.newInsert(Languages.CONTENT_URI);
							
							builder.withValue(Languages.LANGUAGE_ID , languageId);
			            	if(o.getString("code")!=null&&o.getString("code").length()>0&&!"null".equals(o.getString("code")))
			            		builder.withValue(Languages.CODE , o.getString("code"));
			            	if(o.getString("name")!=null&&o.getString("name").length()>0&&!"null".equals(o.getString("name")))
			            		builder.withValue(Languages.NAME , o.getString("name"));
			            	builder.withValue(SyncColumns.UPDATED, Calendar.getInstance().getTimeInMillis());
							batch.add(builder.build());
						}
					}
				}
			}	
		}catch(Exception e){
			Log.d(TAG, "exception occured", e);
		}
		
		return batch;
    }
    
	private static String convertStreamToString(InputStream is)throws UnsupportedEncodingException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
    
    private interface LanguagesQuery {
        String[] PROJECTION = {
        		Languages.LANGUAGE_ID,
        		Languages.CODE,
        		Languages.NAME,
        		SyncColumns.UPDATED
        };
        
        int LANGUAGE_ID = 0;
        int CODE = 1;
        int NAME = 2;
        int UPDATEd = 3;
    }
}
