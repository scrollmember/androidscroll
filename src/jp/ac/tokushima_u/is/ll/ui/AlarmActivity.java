package jp.ac.tokushima_u.is.ll.ui;

import jp.ac.tokushima_u.is.ll.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class AlarmActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.alarm_dialog);
		Intent intent = this.getIntent();
		int type = intent.getIntExtra("alarmType", 1);
		TextView info = (TextView)this.findViewById(R.id.info_alarm_dialog);
		info.setText(this.getResources().getString(R.string.Notify_Info_Context_Quiz));
	}
	
	public void btnOkAction(View v) {
		Intent intent = new Intent(AlarmActivity.this,QuizActivity.class);
		this.startActivity(intent);
		this.finish();
	}
	
	public void btnCancelAction(View v) {
		this.finish();
	}
	
}
