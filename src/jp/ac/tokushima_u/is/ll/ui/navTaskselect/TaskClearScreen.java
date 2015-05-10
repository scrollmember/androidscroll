package jp.ac.tokushima_u.is.ll.ui.navTaskselect;

import java.util.Calendar;
import java.util.Date;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.ui.nav.nav;
import jp.ac.tokushima_u.is.ll.ui.navTask.Task_main;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class TaskClearScreen extends Activity implements
		OnCheckedChangeListener,OnClickListener{
	private String nitizi;

	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.taskclearscreen);
		// 日時取得関数
		nitizi();
		// task and taskscript evaluation

		ImageView image = (ImageView) findViewById(R.id.ImageView);

		
		important();
		end();
	}

	private void end() {
		// TODO Auto-generated method stub
		Button btn = (Button) findViewById(R.id.backtaskbutton);
		Button btn2 = (Button) findViewById(R.id.starttaskbutton);
		btn.setOnClickListener(this);
		btn2.setOnClickListener(this);
	}

	private void important() {
		// TODO Auto-generated method stub
		SharedPreferences taskinfo = this.getSharedPreferences(
				"taskandtaskscript", Activity.MODE_PRIVATE);

		String xmltask = taskinfo.getString("task", "");
		int count = taskinfo.getInt("count", 0);
		String[] xmlscript = new String[count];
		for (int i = 0; i < count; i++) {
			xmlscript[i] = taskinfo.getString("taskscript" + i, "");
		}
		
		
		
		TextView description = (TextView) findViewById(R.id.important);
		description.setText("Task : "+xmltask);
		description.setTextSize(17);
		TextView description1 = (TextView) findViewById(R.id.important2);
		description1.setText("TaskScript Step1: "+xmlscript[0]);
		description1.setTextSize(17);
		if(count>1){
		TextView description2 = (TextView) findViewById(R.id.important3);
		description2.setText("TaskScript Step2: "+xmlscript[1]);
		description2.setTextSize(17);
		}
		if(count>2){
		TextView description3 = (TextView) findViewById(R.id.important4);
		description3.setText("TaskScript Step3: "+xmlscript[2]);
		description3.setTextSize(17);
		}
		if(count>3){
		TextView description4 = (TextView) findViewById(R.id.important5);
		description4.setText("TaskScript Step4: "+xmlscript[3]);
		description4.setTextSize(17);
		}
		if(count>4){
			TextView description4 = (TextView) findViewById(R.id.important6);
			description4.setText("TaskScript Step5: "+xmlscript[4]);
			description4.setTextSize(17);
			}
		if(count>5){
			TextView description4 = (TextView) findViewById(R.id.important7);
			description4.setText("TaskScript Step6: "+xmlscript[5]);
			description4.setTextSize(17);
			}
		if(count>6){
			TextView description4 = (TextView) findViewById(R.id.important8);
			description4.setText("TaskScript Step7: "+xmlscript[6]);
			description4.setTextSize(17);
			}
		if(count>7){
			TextView description4 = (TextView) findViewById(R.id.important9);
			description4.setText("TaskScript Step8: "+xmlscript[7]);
			description4.setTextSize(17);
			}
		if(count>8){
			TextView description4 = (TextView) findViewById(R.id.important10);
			description4.setText("TaskScript Step9: "+xmlscript[8]);
			description4.setTextSize(17);
			}
		if(count>9){
			TextView description4 = (TextView) findViewById(R.id.important11);
			description4.setText("TaskScript Step10: "+xmlscript[9]);
			description4.setTextSize(17);
			}
	}

	public void nitizi() {
		// Dateクラスによる現在時表示
		Date date = new Date();

		// デフォルトのCalendarオブジェクト
		Calendar cal = Calendar.getInstance();

		// Calendarクラスによる現在時表示
		String tmp = cal.get(Calendar.YEAR) + "/"
				+ (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DATE)
				+ " " + cal.get(Calendar.HOUR_OF_DAY) + ":"
				+ cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);
		String samplecsv = cal.get(Calendar.YEAR) + "."
				+ (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.DATE)
				+ "." + cal.get(Calendar.HOUR_OF_DAY) + "."
				+ cal.get(Calendar.MINUTE) + "." + cal.get(Calendar.SECOND);

		nitizi = tmp;

		TextView parent = (TextView) findViewById(R.id.parent);
		// TextView tv = new TextView(this);
		// tv.setTypeface(Typeface.MONOSPACE);
		// 等幅フォントの指定
		parent.setText(nitizi);
		parent.setTextSize(19);
		parent.setTextColor(Color.BLACK);
		parent.setTypeface(Typeface.create(Typeface.SANS_SERIF,
				Typeface.BOLD_ITALIC));

		TextView text = (TextView) findViewById(R.id.text);
		// TextView tv = new TextView(this);
		// tv.setTypeface(Typeface.MONOSPACE);
		// 等幅フォントの指定
		text.setText(" You've finished all of the task script.Let's try the following evaluation to yourself.");
		text.setTextSize(18);

	}

	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.backtaskbutton:
			finish();
			break;
		case R.id.starttaskbutton:
			Intent logintent = new Intent(TaskClearScreen.this,
					nav.class);
			TaskClearScreen.this.startActivity(logintent);
			break;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		
	}
}
