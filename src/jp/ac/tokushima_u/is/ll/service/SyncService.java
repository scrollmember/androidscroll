package jp.ac.tokushima_u.is.ll.service;

import java.util.Calendar;

import jp.ac.tokushima_u.is.ll.io.AudioCacheHandler;
import jp.ac.tokushima_u.is.ll.io.ImageCacheHandler;
import jp.ac.tokushima_u.is.ll.io.ItemJsonHandler;
import jp.ac.tokushima_u.is.ll.io.LanguageJsonHandler;
import jp.ac.tokushima_u.is.ll.io.NotifyJsonHandler;
import jp.ac.tokushima_u.is.ll.io.ProfileJsonHandler;
import jp.ac.tokushima_u.is.ll.io.QuizJsonHandler;
import jp.ac.tokushima_u.is.ll.io.RemoteExecutor;
import jp.ac.tokushima_u.is.ll.util.ContextUtil;
import jp.ac.tokushima_u.is.ll.util.HttpClientFactory;
import jp.ac.tokushima_u.is.ll.util.UIUtils;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

public class SyncService extends IntentService {
    private static final String TAG = "SyncService";
    
    public static final String EXTRA_STATUS_RECEIVER =
        "jp.ac.tokushima_u.is.ll.learninglog.extra.STATUS_RECEIVER";
    
    public static final String EXTRA_SYNC_TYPE =
        "jp.ac.tokushima_u.is.ll.learninglog.extra.SYNC_TYPE";
    
    public static final Integer SYNC_TYPE_MANUAL = 1;
    public static final Integer SYNC_TYPE_AUTO = 0;
    
    public static final int STATUS_RUNNING = 0x1;
    public static final int STATUS_ERROR = 0x2;
    public static final int STATUS_FINISHED = 0x3;

    private RemoteExecutor mRemoteExecutor;
//    private DefaultHttpClient httpClient ;
    
	public SyncService() {
		super(TAG);
	}
	
    @Override
    public void onCreate() {
        super.onCreate();
//        httpClient = HttpClientFactory.getInstance(this);
        final ContentResolver resolver = getContentResolver();
        mRemoteExecutor = new RemoteExecutor(resolver);
    }
	

	@Override
	protected void onHandleIntent(Intent intent) {
		
		if (!UIUtils.checkUser(SyncService.this)) {
			return;
		} 
		
		int syncType = intent.getIntExtra(EXTRA_SYNC_TYPE, SYNC_TYPE_AUTO);
		
		long latestSync = ContextUtil.getLastSyncTime(SyncService.this);
		long now = Calendar.getInstance().getTimeInMillis();
		
		if(syncType!=SYNC_TYPE_MANUAL && (now-latestSync)<5*60*1000)
			return;
		
		
		 Log.d(TAG, "onHandleIntent(intent=" + intent.toString() + ")");
	        final ResultReceiver receiver = intent.getParcelableExtra(EXTRA_STATUS_RECEIVER);
	        if (receiver != null) receiver.send(STATUS_RUNNING, Bundle.EMPTY);

	        try {
	        	final long start = Calendar.getInstance().getTimeInMillis(); 
	            mRemoteExecutor.execute(new LanguageJsonHandler());
	        	final long stop1 = Calendar.getInstance().getTimeInMillis();
	        	Log.d(TAG, "languages synchronize costed "+(stop1-start)/1000+" seconds");
	        } catch (Exception e) {
	            Log.e(TAG, "Problem while Language syncing", e);
	            if (receiver != null) {
	                final Bundle bundle = new Bundle();
	                bundle.putString(Intent.EXTRA_TEXT, e.toString());
	                receiver.send(STATUS_ERROR, bundle);
	            }
	        }
	        
	        try {
	        	final long start = Calendar.getInstance().getTimeInMillis(); 
	            mRemoteExecutor.execute(new ItemJsonHandler(HttpClientFactory.getInstance(this),SyncService.this));
	            final long stop = Calendar.getInstance().getTimeInMillis();
	        	Log.d(TAG, "items synchronize costed "+(stop-start)/1000+" seconds");
	        } catch (Exception e) {
	            Log.e(TAG, "Problem while Item syncing", e);
	            if (receiver != null) {
	                final Bundle bundle = new Bundle();
	                bundle.putString(Intent.EXTRA_TEXT, e.toString());
	                receiver.send(STATUS_ERROR, bundle);
	            }
	        }

	        try {
	        	final long start = Calendar.getInstance().getTimeInMillis(); 
	            mRemoteExecutor.execute(new QuizJsonHandler(HttpClientFactory.getInstance(this),SyncService.this));
	            final long stop = Calendar.getInstance().getTimeInMillis();
	        	Log.d(TAG, "quizzs synchronize costed "+(stop-start)/1000+" seconds");
	        } catch (Exception e) {
	            Log.e(TAG, "Problem while quiz syncing", e);
	            if (receiver != null) {
	                final Bundle bundle = new Bundle();
	                bundle.putString(Intent.EXTRA_TEXT, e.toString());
	                receiver.send(STATUS_ERROR, bundle);
	            }
	        }

	        
	        try {
	        	final long start = Calendar.getInstance().getTimeInMillis(); 
	        	mRemoteExecutor.execute(new ProfileJsonHandler(HttpClientFactory.getInstance(this),SyncService.this));
	        	final long stop = Calendar.getInstance().getTimeInMillis(); 
		        Log.d(TAG, "personalized synchronize costed "+(stop-start)/1000+" seconds");
	        } catch (Exception e) {
	            Log.e(TAG, "Problem while profile syncing", e);
	            if (receiver != null) {
	                final Bundle bundle = new Bundle();
	                bundle.putString(Intent.EXTRA_TEXT, e.toString());
	                receiver.send(STATUS_ERROR, bundle);
	            }
	        }
	        
	        try {
	        	final long start = Calendar.getInstance().getTimeInMillis(); 
		        mRemoteExecutor.execute(new NotifyJsonHandler(SyncService.this));
		        final long stop = Calendar.getInstance().getTimeInMillis();
		        Log.d(TAG, "personalized synchronize costed "+(stop-start)/1000+" seconds");
	        } catch (Exception e) {
	            Log.e(TAG, "Problem while notify syncing", e);
	            if (receiver != null) {
	                final Bundle bundle = new Bundle();
	                bundle.putString(Intent.EXTRA_TEXT, e.toString());
	                receiver.send(STATUS_ERROR, bundle);
	            }
	        }
	        
	        
	        //画像cache
//	        try{
//	        	 mRemoteExecutor.execute(new ImageCacheHandler(SyncService.this));
//	        }catch(Exception e){
//	        	  Log.e(TAG, "Problem while image syncing", e);
//	        }
	        
//	        
//	        try{
//	        	 mRemoteExecutor.execute(new AudioCacheHandler(SyncService.this));
//	        }catch(Exception e){
//	        	  Log.e(TAG, "Problem while audio syncing", e);
//	        }
//	        
	        
	        ContextUtil.setLatestSyncTime(SyncService.this);
	        
	        Log.d(TAG, "sync finished");
	        if (receiver != null) receiver.send(STATUS_FINISHED, Bundle.EMPTY);
	}
}
