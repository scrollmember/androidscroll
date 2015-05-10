package jp.ac.tokushima_u.is.ll.ui.navTask;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import jp.ac.tokushima_u.is.ll.R;

import jp.ac.tokushima_u.is.ll.util.LocalApiContants;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class task_screen extends Activity implements OnClickListener {

	private String Task;
	private int taskcount;
	String[] Taskscript = new String[15];
	String[] selectscript = new String[15];
	String[] Target_Tasklat = new String[15];
	String[] Target_Tasklng = new String[15];
	static String[] image = new String[15];
	String[] related_title = new String[20];
	static String[] related_image = new String[20];
	private String level_values;
	String[] location_info = new String[15];
    String[] image_name=new String[12];

	private String place;
	ViewFlipper viewFlipper;
	int Count;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_screen);
		// Intent infomation
		Taskscript = null;
		intent_info();
		// task_name
		Taskname();
		// Language
		Language();
		// Level
		Level();
		// previous_knowledge
		previous_knowledge();
		// task_script
		Task_script();
		// task_image
		// task_image();
		ViewFi();
		// start button
		start_button();
		// back_button
		back_button();
		// image button

	}

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		finish();
	}

	private void ViewFi() {
		// TODO Auto-generated method stub
		viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);

		SampleView sample = new SampleView(this, related_image);
		// SampleView sample2 = new SampleView(this,related_image[1]);
		// SampleView sample3 = new SampleView(this,related_image[2]);

		if (related_title.length >= 1) {
			if (related_title[0] != null) {
				ImageView image1 = (ImageView) findViewById(R.id.timage1);
				image1.setImageDrawable(sample.d);
				TextView t1 = (TextView) findViewById(R.id.textView1);
				t1.setText(related_title[0]);
				t1.setTextColor(Color.BLACK);
				t1.setTypeface(Typeface.create(Typeface.SANS_SERIF,
						Typeface.BOLD_ITALIC));
				t1.setTextSize(18);
			}
		}
		if (related_title.length >= 2) {
			if (related_title[1] != null) {
				ImageView image2 = (ImageView) findViewById(R.id.timage2);
				image2.setImageDrawable(sample.d2);
				TextView t2 = (TextView) findViewById(R.id.textView2);
				t2.setText(related_title[1]);
				t2.setTextColor(Color.BLACK);
				t2.setTypeface(Typeface.create(Typeface.SANS_SERIF,
						Typeface.BOLD_ITALIC));
				t2.setTextSize(18);
			}
		}
		if (related_title.length >= 3) {
			if (related_title[2] != null) {
				ImageView image3 = (ImageView) findViewById(R.id.timage3);
				image3.setImageDrawable(sample.d3);
				TextView t3 = (TextView) findViewById(R.id.textView3);
				t3.setText(related_title[2]);
				t3.setTextColor(Color.BLACK);
				t3.setTypeface(Typeface.create(Typeface.SANS_SERIF,
						Typeface.BOLD_ITALIC));
				t3.setTextSize(18);
			}
		}
		if (related_title.length >= 4) {
			if (related_title[3] != null) {
				ImageView image4 = (ImageView) findViewById(R.id.timage4);
				image4.setImageDrawable(sample.d4);
				TextView t4 = (TextView) findViewById(R.id.textView4);
				t4.setText(related_title[3]);
				t4.setTextColor(Color.BLACK);
				t4.setTypeface(Typeface.create(Typeface.SANS_SERIF,
						Typeface.BOLD_ITALIC));
				t4.setTextSize(18);
			}
		}
		if (related_title.length >= 5) {
			if (related_title[4] != null) {
				ImageView image5 = (ImageView) findViewById(R.id.timage5);
				image5.setImageDrawable(sample.d5);
				TextView t5 = (TextView) findViewById(R.id.textView5);
				t5.setText(related_title[4]);
				t5.setTextColor(Color.BLACK);
				t5.setTypeface(Typeface.create(Typeface.SANS_SERIF,
						Typeface.BOLD_ITALIC));
				t5.setTextSize(18);
			}
		}
		viewFlipper.setDisplayedChild(0);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.starttaskbutton:
//			if (location_info[0].equals("0")) {
//				Intent logintent = new Intent(task_screen.this,
//						Taskscript_select.class);
//				logintent.putExtra("Tasklat", Target_Tasklat);
//				logintent.putExtra("Tasklng", Target_Tasklng);
//				logintent.putExtra("task", Task);
//				logintent.putExtra("taskscript", Taskscript);
//				logintent.putExtra("image", image);
//				logintent.putExtra("location_info", location_info);
//				task_screen.this.startActivity(logintent);
//			} else {
				Intent logintent = new Intent(task_screen.this,
						taskandtaskscript.class);
				logintent.putExtra("Tasklat", Target_Tasklat);
				logintent.putExtra("Tasklng", Target_Tasklng);
				logintent.putExtra("task", Task);
				logintent.putExtra("taskscript", Taskscript);
				logintent.putExtra("image", image);
				logintent.putExtra("location_info", location_info);
				logintent.putExtra("place", place);
				logintent.putExtra("count", 1);
				logintent.putExtra("image_name", image_name);
				task_screen.this.startActivity(logintent);
//			}

			break;
		case R.id.backtaskbutton:
			finish();

			break;

		case R.id.imagebutton1:

			viewFlipper.showNext();
			break;
		case R.id.imagebutton2:
			viewFlipper.showPrevious();
			break;

		case R.id.selectscript:
			
			
			
			
			new AlertDialog.Builder(task_screen.this)
					.setTitle("TaskScriptを選択してください")
					.setSingleChoiceItems(selectscript, 0,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int item) {
									Count = item;
									Intent logintent = new Intent(
											task_screen.this,
											taskandtaskscript.class);
									logintent.putExtra("Tasklat",
											Target_Tasklat);
									logintent.putExtra("Tasklng",
											Target_Tasklng);
									logintent.putExtra("task", Task);
									logintent
											.putExtra("taskscript", Taskscript);
									logintent.putExtra("image", image);
									logintent.putExtra("location_info",
											location_info);
									logintent.putExtra("place", place);
									logintent.putExtra("count", Count + 1);
									logintent.putExtra("location_info", location_info);
									logintent.putExtra("image_name", image_name);
									task_screen.this.startActivity(logintent);
									finish();
								}
							})
					.setNegativeButton("閉じる",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
									
								}
							}).show();

			break;
		}
	}

	private void intent_info() {
		// TODO Auto-generated method stub
		Bundle extras = getIntent().getExtras();
		Task = extras.getString("taskname");
		Taskscript = extras.getStringArray("taskscript");
		Target_Tasklat = extras.getStringArray("Tasklat");
		Target_Tasklng = extras.getStringArray("Tasklng");
		related_image = extras.getStringArray("related_image");
		related_title = extras.getStringArray("related_title");
		level_values = extras.getString("level");
		image = extras.getStringArray("image");
		location_info = extras.getStringArray("location_info");
		place = extras.getString("place");
		selectscript=extras.getStringArray("selectscript");
		image_name=extras.getStringArray("image_name");
	}

	private void Taskname() {
		// TODO Auto-generated method stub
		TextView task = (TextView) findViewById(R.id.task);
		task.setText(" Task : ");
		task.setTextColor(Color.BLACK);
		task.setTypeface(Typeface.create(Typeface.SANS_SERIF,
				Typeface.BOLD_ITALIC));
		task.setTextSize(18);

		if (task != null) {
			TextView taskname = (TextView) findViewById(R.id.taskname);
			taskname.setText(Task);
			taskname.setTextColor(Color.BLACK);
			taskname.setGravity(Gravity.LEFT | Gravity.TOP);
			taskname.setTextSize(15);
		}

	}

	private void Language() {
		// TODO Auto-generated method stub
		TextView language = (TextView) findViewById(R.id.language);
		language.setText(" Language : ");
		language.setTextColor(Color.BLACK);
		language.setTypeface(Typeface.create(Typeface.SANS_SERIF,
				Typeface.BOLD_ITALIC));
		language.setTextSize(18);

		TextView languagename = (TextView) findViewById(R.id.languagename);
		languagename.setText("Japanese");
		languagename.setTextColor(Color.BLACK);
		languagename.setGravity(Gravity.LEFT | Gravity.TOP);
		languagename.setTextSize(15);

	}

	private void Level() {
		// TODO Auto-generated method stub
		TextView level = (TextView) findViewById(R.id.level);
		level.setText(" Level : ");
		level.setTextColor(Color.BLACK);
		level.setTypeface(Typeface.create(Typeface.SANS_SERIF,
				Typeface.BOLD_ITALIC));
		level.setTextSize(18);

		TextView levelname = (TextView) findViewById(R.id.levelname);
		levelname.setText(level_values);
		levelname.setTextColor(Color.BLACK);
		levelname.setGravity(Gravity.LEFT | Gravity.TOP);
		levelname.setTextSize(15);
	}

	private void previous_knowledge() {
		// TODO Auto-generated method stub
		TextView previous = (TextView) findViewById(R.id.previous);
		previous.setText(" Related object : ");
		previous.setTextColor(Color.BLACK);
		previous.setTypeface(Typeface.create(Typeface.SANS_SERIF,
				Typeface.BOLD_ITALIC));
		previous.setTextSize(18);
		String a = "";
		for (int i = 0; i < related_title.length; i++) {
			if (related_title.equals("null")) {

			} else {
				a = a.concat(related_title[i]).concat("、 ");
			}
		}
		TextView previousname = (TextView) findViewById(R.id.previousname);
		// previousname.setText("  印鑑、身分証明書、銀行、職員、口座、ATM、お金、質問、開設、預ける");
		Bundle extras = getIntent().getExtras();

		previousname.setText(a);
		previousname.setTextColor(Color.BLACK);
		previousname.setGravity(Gravity.LEFT | Gravity.TOP);
		previousname.setTextSize(15);
	}

	private void Task_script() {
		// TODO Auto-generated method stub

		// TextView taskscript = (TextView) findViewById(R.id.taskscript);
		// taskscript.setText("  Script : ");
		// taskscript.setTextColor(Color.BLACK);
		// taskscript.setTypeface(Typeface.create(Typeface.SANS_SERIF,
		// Typeface.BOLD_ITALIC));
		// taskscript.setTextSize(18);
		LinearLayout linearlayout = (LinearLayout) findViewById(R.id.layout1);
		if (Taskscript.length >= 1) {
			TextView taskscript1 = new TextView(this);
			taskscript1.setText(" Script : " + 1 + "  ");
			taskscript1.setTextColor(Color.BLACK);
			taskscript1.setTypeface(Typeface.create(Typeface.SANS_SERIF,
					Typeface.BOLD_ITALIC));
			taskscript1.setTextSize(18);
			linearlayout.addView(taskscript1);
			linearlayout.setOrientation(LinearLayout.VERTICAL);
			if (Taskscript[0] != null) {
				TextView taskscript2 = new TextView(this);
				taskscript2.setText(Taskscript[0]);
				taskscript2.setTextColor(Color.BLACK);
				taskscript2.setGravity(Gravity.LEFT | Gravity.TOP);
				taskscript2.setTextSize(15);
				linearlayout.addView(taskscript2);
			}

		}
		if (Taskscript.length >= 2) {
			TextView taskscript1 = new TextView(this);
			taskscript1.setText(" Script : " + 2 + ".");
			taskscript1.setTextColor(Color.BLACK);
			taskscript1.setTypeface(Typeface.create(Typeface.SANS_SERIF,
					Typeface.BOLD_ITALIC));
			taskscript1.setTextSize(18);
			linearlayout.addView(taskscript1);
			if (Taskscript[1] != null) {
				TextView taskscript2 = new TextView(this);
				taskscript2.setText(Taskscript[1]);
				taskscript2.setTextColor(Color.BLACK);
				taskscript2.setGravity(Gravity.LEFT | Gravity.TOP);
				taskscript2.setTextSize(15);
				linearlayout.addView(taskscript2);
			}

		}
		if (Taskscript.length >= 3) {
			TextView taskscript1 = new TextView(this);
			taskscript1.setText(" Script :  " + 3 + ".");
			taskscript1.setTextColor(Color.BLACK);
			taskscript1.setTypeface(Typeface.create(Typeface.SANS_SERIF,
					Typeface.BOLD_ITALIC));
			taskscript1.setTextSize(18);
			linearlayout.addView(taskscript1);
			if (Taskscript[2] != null) {
				TextView taskscript2 = new TextView(this);
				taskscript2.setText(Taskscript[2]);
				taskscript2.setTextColor(Color.BLACK);
				taskscript2.setGravity(Gravity.LEFT | Gravity.TOP);
				taskscript2.setTextSize(15);
				linearlayout.addView(taskscript2);
			}

		}
		if (Taskscript.length >= 4) {
			TextView taskscript1 = new TextView(this);
			taskscript1.setText(" Script :  " + 4 + ".");
			taskscript1.setTextColor(Color.BLACK);
			taskscript1.setTypeface(Typeface.create(Typeface.SANS_SERIF,
					Typeface.BOLD_ITALIC));
			taskscript1.setTextSize(18);
			linearlayout.addView(taskscript1);
			if (Taskscript[3] != null) {
				TextView taskscript2 = new TextView(this);
				taskscript2.setText(Taskscript[3]);
				taskscript2.setTextColor(Color.BLACK);
				taskscript2.setGravity(Gravity.LEFT | Gravity.TOP);
				taskscript2.setTextSize(15);
				linearlayout.addView(taskscript2);
			}

		}
		if (Taskscript.length >= 5) {
			TextView taskscript1 = new TextView(this);
			taskscript1.setText(" Script :  " + 5 + ".");
			taskscript1.setTextColor(Color.BLACK);
			taskscript1.setTypeface(Typeface.create(Typeface.SANS_SERIF,
					Typeface.BOLD_ITALIC));
			taskscript1.setTextSize(18);
			linearlayout.addView(taskscript1);
			if (Taskscript[4] != null) {
				TextView taskscript2 = new TextView(this);
				taskscript2.setText(Taskscript[4]);
				taskscript2.setTextColor(Color.BLACK);
				taskscript2.setGravity(Gravity.LEFT | Gravity.TOP);
				taskscript2.setTextSize(15);
				linearlayout.addView(taskscript2);
			}

		}
		if (Taskscript.length >= 6) {
			TextView taskscript1 = new TextView(this);
			taskscript1.setText(" Script :  " + 6 + ".");
			taskscript1.setTextColor(Color.BLACK);
			taskscript1.setTypeface(Typeface.create(Typeface.SANS_SERIF,
					Typeface.BOLD_ITALIC));
			taskscript1.setTextSize(18);
			linearlayout.addView(taskscript1);
			if (Taskscript[5] != null) {
				TextView taskscript2 = new TextView(this);
				taskscript2.setText(Taskscript[5]);
				taskscript2.setTextColor(Color.BLACK);
				taskscript2.setGravity(Gravity.LEFT | Gravity.TOP);
				taskscript2.setTextSize(15);
				linearlayout.addView(taskscript2);
			}

		}
		if (Taskscript.length >= 7) {
			TextView taskscript1 = new TextView(this);
			taskscript1.setText(" Script :  " + 7 + ".");
			taskscript1.setTextColor(Color.BLACK);
			taskscript1.setTypeface(Typeface.create(Typeface.SANS_SERIF,
					Typeface.BOLD_ITALIC));
			taskscript1.setTextSize(18);
			linearlayout.addView(taskscript1);
			if (Taskscript[6] != null) {
				TextView taskscript2 = new TextView(this);
				taskscript2.setText(Taskscript[6]);
				taskscript2.setTextColor(Color.BLACK);
				taskscript2.setGravity(Gravity.LEFT | Gravity.TOP);
				taskscript2.setTextSize(15);
				linearlayout.addView(taskscript2);
			}

		}
		if (Taskscript.length >= 8) {
			TextView taskscript1 = new TextView(this);
			taskscript1.setText(" Script :  " + 8 + ".");
			taskscript1.setTextColor(Color.BLACK);
			taskscript1.setTypeface(Typeface.create(Typeface.SANS_SERIF,
					Typeface.BOLD_ITALIC));
			taskscript1.setTextSize(18);
			linearlayout.addView(taskscript1);
			if (Taskscript[7] != null) {
				TextView taskscript2 = new TextView(this);
				taskscript2.setText(Taskscript[7]);
				taskscript2.setTextColor(Color.BLACK);
				taskscript2.setGravity(Gravity.LEFT | Gravity.TOP);
				taskscript2.setTextSize(15);
				linearlayout.addView(taskscript2);
			}

		}
		if (Taskscript.length >= 9) {
			TextView taskscript1 = new TextView(this);
			taskscript1.setText(" Script :  " + 9 + ".");
			taskscript1.setTextColor(Color.BLACK);
			taskscript1.setTypeface(Typeface.create(Typeface.SANS_SERIF,
					Typeface.BOLD_ITALIC));
			taskscript1.setTextSize(18);
			linearlayout.addView(taskscript1);
			if (Taskscript[8] != null) {
				TextView taskscript2 = new TextView(this);
				taskscript2.setText(Taskscript[8]);
				taskscript2.setTextColor(Color.BLACK);
				taskscript2.setGravity(Gravity.LEFT | Gravity.TOP);
				taskscript2.setTextSize(15);
				linearlayout.addView(taskscript2);
			}

		}
		if (Taskscript.length >= 10) {
			TextView taskscript1 = new TextView(this);
			taskscript1.setText(" Script :  " + 10 + ".");
			taskscript1.setTextColor(Color.BLACK);
			taskscript1.setTypeface(Typeface.create(Typeface.SANS_SERIF,
					Typeface.BOLD_ITALIC));
			taskscript1.setTextSize(18);
			linearlayout.addView(taskscript1);
			if (Taskscript[9] != null) {
				TextView taskscript2 = new TextView(this);
				taskscript2.setText(Taskscript[9]);
				taskscript2.setTextColor(Color.BLACK);
				taskscript2.setGravity(Gravity.LEFT | Gravity.TOP);
				taskscript2.setTextSize(15);
				linearlayout.addView(taskscript2);
			}

		}
	

	}

	// private void task_image() {
	// // TODO Auto-generated method stub
	// ImageView image = (ImageView) findViewById(R.id.taskimage);
	// image.setImageResource(R.drawable.banksample);
	//
	// }

	private void start_button() {
		// TODO Auto-generated method stub
		Button btn = (Button) findViewById(R.id.starttaskbutton);
		btn.setOnClickListener(this);
	}

	private void back_button() {
		// TODO Auto-generated method stub
		Button btn = (Button) findViewById(R.id.backtaskbutton);
		btn.setOnClickListener(this);
		Button select = (Button) findViewById(R.id.selectscript);
		select.setOnClickListener(this);
		Button btn4 = (Button) findViewById(R.id.imagebutton1);
		Button btn5 = (Button) findViewById(R.id.imagebutton2);

		btn4.setOnClickListener(this);
		btn5.setOnClickListener(this);
	}

	private static class SampleView extends View {
		private Drawable d;
		private Drawable d2;
		private Drawable d3;
		private Drawable d4;
		private Drawable d5;
		Context context;

		public SampleView(Context context, String[] related_image) {
			super(context);
			for (int i = 0; i < related_image.length; i++) {
				if (i == 0) {
					d = loadImage(LocalApiContants.task_image
							+ related_image[0]
							+ LocalApiContants.LargeSizePostfix, i);
				}

				if (i == 1) {
					d2 = loadImage(LocalApiContants.task_image
							+ related_image[1]
							+ LocalApiContants.LargeSizePostfix, i);
				}

				if (i == 2) {
					d3 = loadImage(LocalApiContants.task_image
							+ related_image[2]
							+ LocalApiContants.LargeSizePostfix, i);
				}
				if (i == 3) {
					d4 = loadImage(LocalApiContants.task_image
							+ related_image[3]
							+ LocalApiContants.LargeSizePostfix, i);
				}

				if (i == 4) {
					d5 = loadImage(LocalApiContants.task_image
							+ related_image[4]
							+ LocalApiContants.LargeSizePostfix, i);
				}
			}
		}

		public Drawable loadImage(String str, int i) {
			Drawable d = null;

			if (related_image[0] != null && i == 0) {

				try {
					URL url = new URL(str);
					HttpURLConnection http = (HttpURLConnection) url
							.openConnection();
					// http.setRequestMethod("GET");
					http.connect();
					InputStream in = http.getInputStream();
					d = Drawable.createFromStream(in, "a");
					in.close();
				} catch (Exception e) {
				}
				d.setBounds(20, 20, 143, 59);
				return d;
			} else if (related_image[1] != null && i == 1) {
				try {
					URL url = new URL(str);
					HttpURLConnection http = (HttpURLConnection) url
							.openConnection();
					// http.setRequestMethod("GET");
					http.connect();
					InputStream in = http.getInputStream();
					d2 = Drawable.createFromStream(in, "a");
					in.close();
				} catch (Exception e) {
				}
				d2.setBounds(20, 20, 143, 59);

				return d2;
			}

			else if (related_image[2] != null && i == 2) {
				try {
					URL url = new URL(str);
					HttpURLConnection http = (HttpURLConnection) url
							.openConnection();
					// http.setRequestMethod("GET");
					http.connect();
					InputStream in = http.getInputStream();
					d3 = Drawable.createFromStream(in, "a");
					in.close();
				} catch (Exception e) {
				}
				d3.setBounds(20, 20, 143, 59);
				return d3;
			} else if (related_image[3] != null && i == 3) {
				try {
					URL url = new URL(str);
					HttpURLConnection http = (HttpURLConnection) url
							.openConnection();
					// http.setRequestMethod("GET");
					http.connect();
					InputStream in = http.getInputStream();
					d4 = Drawable.createFromStream(in, "a");
					in.close();
				} catch (Exception e) {
				}
				d4.setBounds(20, 20, 143, 59);
				return d4;
			} else if (related_image[4] != null && i == 4) {
				try {
					URL url = new URL(str);
					HttpURLConnection http = (HttpURLConnection) url
							.openConnection();
					// http.setRequestMethod("GET");
					http.connect();
					InputStream in = http.getInputStream();
					d5 = Drawable.createFromStream(in, "a");
					in.close();
				} catch (Exception e) {
				}
				d5.setBounds(20, 20, 143, 59);
				return d5;
			}

			else {

				// if (related_image[2] != "26f57f066df7443da89c93ba4372cd1f") {
				// Drawable mIcon = context.getResources().getDrawable(
				// R.drawable.noimage);
				//
				// return mIcon;
				// }
				Drawable mIcon = context.getResources().getDrawable(
						R.drawable.noimage);

				return mIcon;
			}

		}

		@Override
		protected void onDraw(Canvas canvas) {
			d.draw(canvas);
			d2.draw(canvas);
			d3.draw(canvas);
			d4.draw(canvas);
			d5.draw(canvas);
		}

	}
}
