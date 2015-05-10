package jp.ac.tokushima_u.is.ll.c2dm;

import java.io.IOException;

import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.HttpClientFactory;
import jp.ac.tokushima_u.is.ll.util.UIUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.c2dm.C2DMBaseReceiver;
import com.google.android.c2dm.C2DMessaging;

/**
 * Broadcast receiver that handles Android Cloud to Data Messaging (AC2DM) messages, initiated
 * by the JumpNote App Engine server and routed/delivered by Google AC2DM servers. The
 * only currently defined message is 'sync'.
 */
public class C2DMReceiver extends C2DMBaseReceiver {
    static final String TAG = Config.makeLogTag(C2DMReceiver.class);

    public C2DMReceiver() {
        super(Config.C2DM_SENDER);
    }

    @Override
    public void onError(Context context, String errorId) {
//        Toast.makeText(context, "Messaging registration error: " + errorId,
//                Toast.LENGTH_LONG).show();
    }
    
    @Override
    public void onRegistered(Context context, String registrationId) throws IOException {
    	try{
    		HttpPost httpPost = new HttpPost(ApiConstants.REGISTER_ID_SAVE_URI);
    		String imsi = ((TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    		DefaultHttpClient client = HttpClientFactory.getInstance(this);
    		MultipartEntity params = new MultipartEntity();
    		params.addPart("registerId", new StringBody(registrationId));
    		params.addPart("imsi", new StringBody(imsi));
    		httpPost.setEntity(params);
    		HttpResponse response = client.execute(httpPost);
    		if(response.getStatusLine().getStatusCode() != 200)
    			C2DMessaging.clearRegistrationId(this);	
    	}catch(Exception e){
    		C2DMessaging.clearRegistrationId(this);
    		Log.e(TAG, "Register with server has errors", e);
    	}
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
//    	Bundle extras = intent.getExtras();
//    	String messageType = extras.getString("messagetype");
//        String accountName = intent.getExtras().getString(Config.C2DM_ACCOUNT_EXTRA);
//        String message = intent.getExtras().getString(Config.C2DM_MESSAGE_EXTRA);
        String collapse = intent.getExtras().getString(Config.C2DM_COLLAPSE_KEY);
        if (Config.COLLAPSE_KEY_SYNC.equals(collapse)) {
        	UIUtils.goSync(this);
        }
    }

    /**
     * Register or unregister based on phone sync settings.
     * Called on each performSync by the SyncAdapter.
     */
    public static void refreshAppC2DMRegistrationState(Context context) {
        // Determine if there are any auto-syncable accounts. If there are, make sure we are
        // registered with the C2DM servers. If not, unregister the application.
        boolean autoSyncDesired = true;
//        boolean autoSyncDesired = false;
//        if (ContentResolver.getMasterSyncAutomatically()) {
//            AccountManager am = AccountManager.get(context);
//            Account[] accounts = am.getAccountsByType(SyncAdapter.GOOGLE_ACCOUNT_TYPE);
//            for (Account account : accounts) {
//                if (ContentResolver.getIsSyncable(account, JumpNoteContract.AUTHORITY) > 0 &&
//                        ContentResolver.getSyncAutomatically(account, JumpNoteContract.AUTHORITY)) {
//                    autoSyncDesired = true;
//                    break;
//                }
//            }
//        }

        String rid = C2DMessaging.getRegistrationId(context);
        boolean autoSyncEnabled = !rid.equals("");

        if (autoSyncEnabled != autoSyncDesired) {
            Log.i(TAG, "System-wide desirability for JumpNote auto sync has changed; " +
                    (autoSyncDesired ? "registering" : "unregistering") +
                    " application with C2DM servers.");

            if (autoSyncDesired == true) {
                C2DMessaging.register(context, Config.C2DM_SENDER);
            } else {
                C2DMessaging.unregister(context);
            }
        }
    }
}
