package jp.ac.tokushima_u.is.ll.service;

import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.HttpClientFactory;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class FeedbackThread extends Thread {
	 private Double lat;
	 private Double lng;
	 private String itemid;
	 private Integer feedtype;
	 private Context context;
	 private LocationManager locationmanager;
	 
	 public static final Integer QuizFeedBackType = 1;
	 public static final Integer ItemsFeedBackType = 2;
	
	 public FeedbackThread(Context context, Double lat, Double lng, String itemid, Integer feedtype) {
		 	this.context = context;
			this.lat = lat;
			this.lng = lng;
			this.itemid = itemid;
			this.feedtype = feedtype;
		}

		public void run() {
			try {
				DefaultHttpClient client = HttpClientFactory.getInstance(context);
				HttpPost httpPost = new HttpPost(
						ApiConstants.Context_Aware_Feedback_URL);
				MultipartEntity params = new MultipartEntity();
				if (itemid != null)
					params.addPart("itemid", new StringBody(itemid));
				if(lat!=null&&lng!=null){
					params.addPart("lat", new StringBody(lat.toString()));
					params.addPart("lng", new StringBody(lng.toString()));
				}
				if(locationmanager!=null){
					locationmanager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
					Location location = locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					if(location!=null){
						params.addPart("speed", new StringBody(String.valueOf(location.getSpeed())));
					}
				}
					
				params.addPart("alarmType", new StringBody(feedtype.toString()));
				httpPost.setEntity(params);
				client.execute(httpPost);
			} catch (Exception e) {
				Log.e("LearningLogTest Exception", e.getMessage());
			}
		}
}
