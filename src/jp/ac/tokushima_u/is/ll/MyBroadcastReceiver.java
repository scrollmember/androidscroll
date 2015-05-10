package jp.ac.tokushima_u.is.ll;

import jp.ac.tokushima_u.is.ll.service.BackgroundService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyBroadcastReceiver extends BroadcastReceiver {
	static final String ACTION = "android.intent.action.BOOT_COMPLETED";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ACTION)) 
        {
              context.startService(new Intent(context, 
                   BackgroundService.class));
        }
	}

}
