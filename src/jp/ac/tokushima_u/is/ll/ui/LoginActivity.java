package jp.ac.tokushima_u.is.ll.ui;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.io.SettingSyncThread;
import jp.ac.tokushima_u.is.ll.util.Constants;
import jp.ac.tokushima_u.is.ll.util.UIUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends Activity{
	private static final String TAG = "LoginActivity";
//	private Button btnsubmit;
//	private Button btnreset;
	private EditText txtusername;
	private EditText txtpassword;
//	private TextView text_regist;
//	private TextView text_forget;
//	private TextView text_help;
	private SettingSyncThread settingThread;
	
	
	private static final int DIALOG_ServerError=1;
	private static final int DIALOG_UserError=2;
	private static final int DIALOG_SettingError=3;
	private static final int DIALOG_InnerError=4;
	private static final int DIALOG_Invidate_User=5;
	
	private static final int MsgServerError = 1;
	private static final int MsgUserError = 2;
	private static final int MsgSettingError = 3;
	private static final int MsgInnerError= 4;
	private static final int MsgLoginSuccess = 5;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        txtusername = (EditText) this.findViewById(R.id.username);
        txtpassword = (EditText) this.findViewById(R.id.password);
//        this.text_regist = (TextView)this.findViewById(R.id.textRegist);
//        this.text_regist.setMovementMethod(LinkMovementMethod.getInstance());
//        this.text_forget = (TextView)this.findViewById(R.id.textForget);
//        this.text_forget.setMovementMethod(LinkMovementMethod.getInstance());
//        this.text_help = (TextView)this.findViewById(R.id.textHelp);
//        this.text_help.setMovementMethod(LinkMovementMethod.getInstance());
        
        SharedPreferences setting = this.getSharedPreferences(Constants.SETTING_INFOS_FILE, Context.MODE_PRIVATE);
        String nameval = setting.getString(Constants.SavedUserName, "");
        String passwordval = setting.getString(Constants.SavedPassword, "");
        txtusername.setText(nameval);
        txtpassword.setText(passwordval);
        
    	NotificationManager nm = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(Constants.LoginNotificationID);
    }

	public void onResetOnClick(View view){
		 txtusername.setText("");
	     txtpassword.setText("");
	     
		SharedPreferences setting = this.getSharedPreferences(
				Constants.SETTING_INFOS_FILE, Context.MODE_PRIVATE);
		Editor editor = setting.edit();
		editor.putString(Constants.SavedUserName, null);
		editor.putString(Constants.SavedPassword, null);
		editor.commit();
	}
	
	public void onSubmitOnClick(View view){
		if(this.invalidate()){
			showDialog(DIALOG_Invidate_User);
			return;
		}
		settingThread = new SettingSyncThread(txtusername.getText().toString().trim(),txtpassword.getText().toString().trim(), null, this.handler,this);
		settingThread.start();
	}
	
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MsgServerError:
				showDialog(DIALOG_SettingError);break;
			case MsgUserError:
				showDialog(DIALOG_UserError);break;
			case MsgSettingError:
				showDialog(DIALOG_InnerError);break;
			case MsgInnerError:
				showDialog(DIALOG_ServerError);break;	
			case MsgLoginSuccess:
				UIUtils.goHome(LoginActivity.this);
			}
		}
		
	};
	
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id){
		case DIALOG_ServerError:
			return this.buildDialog(LoginActivity.this, R.string.error_inner_info);
		case DIALOG_UserError:
			return this.buildDialog(LoginActivity.this, R.string.error_username_password);
		case DIALOG_SettingError:
			return this.buildDialog(LoginActivity.this, R.string.error_unable_access_server);
		case DIALOG_InnerError:
			return this.buildInfoDialog(LoginActivity.this);
		case DIALOG_Invidate_User:
			return this.buildDialog(LoginActivity.this, R.string.error_invali_username_password);	
		}
		
		return null;
	}

	private Dialog buildDialog(Context context, int title){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(R.drawable.alert_dialog_icon);
		builder.setTitle(title);
		builder.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		
		return builder.create();
		
	}
	
	private Dialog buildInfoDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(
				R.drawable.alert_dialog_icon).setTitle(R.string.error)
				.setMessage(R.string.error_no_category).setPositiveButton(
						R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(Constants.Setting_URL));
								startActivity(intent);
							}
						}).setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						});
		return builder.create();

	}
	
	private boolean invalidate(){
		String u = this.txtusername.getText().toString();
		String p = this.txtpassword.getText().toString();
		if(u==null||u.length()<=0||p==null||p.length()<=0)
			return true;
		else
			return false;
	}
	
	
	
}