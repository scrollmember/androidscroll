package jp.ac.tokushima_u.is.ll.ui.navTask;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.ac.tokushima_u.is.ll.R;

import jp.ac.tokushima_u.is.ll.provider.TaskDatabase;
import jp.ac.tokushima_u.is.ll.ui.navTaskselect.ChangeTaskscript_select2;
import jp.ac.tokushima_u.is.ll.ui.navTaskselect.TaskClearScreen;
import jp.ac.tokushima_u.is.ll.ui.navTaskselect.TaskNavigator;
import jp.ac.tokushima_u.is.ll.util.LocalApiContants;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.Context;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.EditText;

import android.widget.ImageView;

import android.widget.TextView;

public class taskandtaskscript extends Activity implements OnClickListener {

	// intent info
	String task = "";
	String[] taskscript = new String[10];
	String[] location_info = new String[10];
	int taskcount;
	// int[] Target_lat = new int[10];
	// int[] Target_lng = new int[10];
	String[] Tasklat = new String[8];
	String[] Tasklng = new String[8];
	static String[] image = new String[10];
	String[] image_name = new String[15];
	int count;
	private String infoplace;

	// text view
	
	TextView taskscript1;
	TextView place2;
	TextView Image;
	TextView Comment;

	// database
	Cursor cursor;
	Cursor cursor3;
	public static SQLiteDatabase db;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.taskandscript);

		// intent information
		info();

		// task 判定

		// task name
		task_step();

		// place name
		place();

		// taskscript name
		task_script();

		// image
		image_script();

		// comment
		comment();

		// button
		button();
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

	private void info() {
		// TODO Auto-generated method stub

		Bundle extras = getIntent().getExtras();
		task = extras.getString("task");
		taskscript = extras.getStringArray("taskscript");
		Tasklat = extras.getStringArray("Tasklat");
		Tasklng = extras.getStringArray("Tasklng");
		image = extras.getStringArray("image");
		count = extras.getInt("count");
		infoplace = extras.getString("place");
		location_info = extras.getStringArray("location_info");
		image_name = extras.getStringArray("image_name");
		SharedPreferences.Editor editor;
		SharedPreferences gpsdata = getSharedPreferences("taskandtaskscript",
				Activity.MODE_PRIVATE);
		editor = gpsdata.edit();
		editor.putString("task", task);
		editor.putInt("count", taskscript.length);
		String[] xmltest = new String[taskscript.length];
		for (int i = 0; i < taskscript.length; i++) {
			editor.putString("taskscript" + i, taskscript[i]);
		}

		editor.commit();
	}

	private void task_step() {
		// TODO Auto-generated method stub
		taskscript1 = (TextView) findViewById(R.id.step);
		taskscript1.setText(" Step" + " of " + count + " : ");
		taskscript1.setTextColor(Color.BLACK);
		taskscript1.setTypeface(Typeface.create(Typeface.SANS_SERIF,
				Typeface.BOLD_ITALIC));
		taskscript1.setTextSize(18);
		if (task != null) {
			place2 = (TextView) findViewById(R.id.step2);
			place2.setText(task);
			place2.setTextColor(Color.BLACK);
			place2.setGravity(Gravity.LEFT | Gravity.TOP);
			place2.setTextSize(15);
		}
	}

	private void place() {
		// TODO Auto-generated method stub
		TextView place = (TextView) findViewById(R.id.place);
		place.setText(" Place : ");
		place.setTextColor(Color.BLACK);
		place.setTypeface(Typeface.create(Typeface.SANS_SERIF,
				Typeface.BOLD_ITALIC));
		place.setTextSize(18);

		TextView placename = (TextView) findViewById(R.id.placename);
		placename.setText(infoplace);
		placename.setTextColor(Color.BLACK);
		placename.setGravity(Gravity.LEFT | Gravity.TOP);
		placename.setTextSize(15);

	}

	private void task_script() {
		// TODO Auto-generated method stub
		TextView place3 = (TextView) findViewById(R.id.script);
		place3.setText("  Script : ");
		place3.setTextColor(Color.BLACK);
		place3.setTypeface(Typeface.create(Typeface.SANS_SERIF,
				Typeface.BOLD_ITALIC));
		place3.setTextSize(18);

		TextView place = (TextView) findViewById(R.id.step3);
		place.setText(" ");
		place.setTextColor(Color.BLACK);
		place.setTypeface(Typeface.create(Typeface.SANS_SERIF,
				Typeface.BOLD_ITALIC));
		place.setTextSize(18);
		// RelativeLayout relativeLayout = (RelativeLayout)
		// findViewById(R.id.reativelayout);
		if (taskscript.length != (count - 1)) {
			if (taskscript[count - 1] != null) {
				TextView place4 = (TextView) findViewById(R.id.step4);
				place4.setText(taskscript[count - 1]);
				place4.setTextColor(Color.BLACK);
				place4.setGravity(Gravity.LEFT | Gravity.TOP);
				place4.setTextSize(15);
			}
		}
	}

	private void image_script() {
		// TODO Auto-generated method stub
		Image = (TextView) findViewById(R.id.image);
		if (image_name[count - 1] != null) {
			Image.setText("  Image :  " + image_name[count - 1]);
		}else{
		Image.setText("  Image :  ");
		}
		Image.setTextColor(Color.BLACK);
		Image.setTypeface(Typeface.create(Typeface.SANS_SERIF,
				Typeface.BOLD_ITALIC));
		Image.setTextSize(18);

		SampleView sample = new SampleView(this);
		ImageView imageView = (ImageView) findViewById(R.id.ImageView);
		// Drawable mIcon = context.getResources().getDrawable(
		// R.drawable.noimage);
		imageView.setImageDrawable(sample.d);

	}

	private void comment() {
		Comment = (TextView) findViewById(R.id.question);
		Comment.setText("  Description and Question :  ");
		Comment.setTextColor(Color.BLACK);
		Comment.setTypeface(Typeface.create(Typeface.SANS_SERIF,
				Typeface.BOLD_ITALIC));
		Comment.setTextSize(18);

	}

	private void button() {
		// TODO Auto-generated method stub

		Button btn = (Button) findViewById(R.id.nextbutton);
		Button btn2 = (Button) findViewById(R.id.navigatorbutton);
		// Button btn3 = (Button) findViewById(R.id.commentbutton);
		// Button btn4 = (Button) findViewById(R.id.imagebutton);

		btn.setOnClickListener(this);
		btn2.setOnClickListener(this);
		// btn3.setOnClickListener(this);
		// btn4.setOnClickListener(this);
		if (location_info[count - 1].equals("1")) {
			Button btn5 = (Button) findViewById(R.id.locationbutton);
			btn5.setOnClickListener(this);
		} else {
			Button btn5 = (Button) findViewById(R.id.locationbutton);
			btn5.setSelected(false);
			btn5.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.nextbutton:
			finish();
			break;

		case R.id.navigatorbutton:
			if (taskscript.length != 1 && count != taskscript.length) {
				EditText hukku_note = (EditText) this
						.findViewById(R.id.comment);
				String note = hukku_note.getText().toString();
				TaskDatabase helper = new TaskDatabase(getApplicationContext());
				db = helper.getReadableDatabase();
				cursor = db.rawQuery("select * from script_info;", null);
				db = helper.getWritableDatabase();
				ContentValues values = new ContentValues();
				values.put("task_id", task);
				values.put("script_id", taskscript[count - 1]);
				values.put("step", count);
				values.put("description", note);
				db.insert("script_info", null, values);
				db.close();
				cursor.close();

				new AlertDialog.Builder(taskandtaskscript.this)
						.setTitle(
								"Do you start Next Script? :" + (count)
										+ taskscript[count])
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										if (taskscript.length != 1
												&& count != taskscript.length) {
											Intent logintent = new Intent(
													taskandtaskscript.this,
													taskandtaskscript.class);
											List<String> list = new ArrayList<String>(
													Arrays.asList(taskscript));
											if (list.size() != 0) {
												list.remove(0);
											}
											String[] convert_taskscript = (String[]) list
													.toArray(new String[list
															.size()]);

											String nullcompare = "null";
											System.gc();
											logintent.putExtra("task", task);
											logintent.putExtra("taskscript",
													taskscript);
											logintent.putExtra("image", image);
											logintent.putExtra("count",
													count + 1);
											logintent.putExtra("location_info",
													location_info);
											logintent.putExtra("Tasklat",
													Tasklat);
											logintent.putExtra("Tasklng",
													Tasklng);
											logintent.putExtra("image_name",
													image_name);
											logintent.putExtra("place",
													infoplace);
											taskandtaskscript.this
													.startActivity(logintent);

											finish();

										} else {
											Intent logintent = new Intent(
													taskandtaskscript.this,
													TaskClearScreen.class);
											taskandtaskscript.this
													.startActivity(logintent);
											finish();
										}
									}
								})
						.setNegativeButton("No",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								}).show();

			} else {
				Intent logintent = new Intent(taskandtaskscript.this,
						TaskClearScreen.class);
				taskandtaskscript.this.startActivity(logintent);
				finish();
			}

			// Intent logintent = new Intent(taskandtaskscript.this,
			// Task_main.class);
			// Intent logintent = new Intent(taskandtaskscript.this,
			// taskandtaskscript.class);
			//
			// logintent.putExtra("task", task);
			//
			// logintent.putExtra("taskscript", taskscript);
			// logintent.putExtra("image", image);
			// taskandtaskscript.this.startActivity(logintent);
			//
			// this.finish();
			break;

		case R.id.locationbutton:
			Intent logintent = new Intent(taskandtaskscript.this,
					TaskNavigator.class);
			logintent.putExtra("Tasklat", Tasklat);
			logintent.putExtra("Tasklng", Tasklng);
			taskandtaskscript.this.startActivity(logintent);
			break;

		case R.id.mapbutton:

			break;
		case R.id.imagebutton:

			// SampleView sample = new SampleView(this);
			//
			// ImageView image = (ImageView) findViewById(R.id.taskimage);
			// image.setImageDrawable(sample.d);
			// viewFlipper.setDisplayedChild(0);
			break;

		}
	}

	class SampleView extends View {
		private Drawable d;
		Context context;

		public SampleView(Context context) {
			super(context);
			d = loadImage(LocalApiContants.task_image + image[count - 1]
					+ LocalApiContants.LargeSizePostfix);
		}

		public Drawable loadImage(String str) {
			Drawable d = null;
			if (image[count - 1] != null) {
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
			} else {

				Drawable mIcon = context.getResources().getDrawable(
						R.drawable.noimage);

				return mIcon;
			}
		}

		@Override
		protected void onDraw(Canvas canvas) {
			d.draw(canvas);
		}

	}
	//

}
