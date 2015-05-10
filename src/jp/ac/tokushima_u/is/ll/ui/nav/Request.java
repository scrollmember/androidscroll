package jp.ac.tokushima_u.is.ll.ui.nav;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.widget.Toast;

public class Request extends BroadcastReceiver{
	@Override
	public void onReceive(Context context,Intent intent){
 if(intent.hasExtra(LocationManager.KEY_PROXIMITY_ENTERING)){
	 Toast.makeText(context,"100m以内に入りました",Toast.LENGTH_LONG).show();
 }
}
}

