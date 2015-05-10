package jp.ac.tokushima_u.is.ll.ui;

import java.util.Calendar;
import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Choices;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Notifys;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Quizs;
import jp.ac.tokushima_u.is.ll.util.Constants;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler.AsyncQueryListener;
import jp.ac.tokushima_u.is.ll.util.UIUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class FourSelectedTextQuizActivity extends Activity implements
		AsyncQueryListener {
	private String quizId;
	private String answer;
	private int myanswer = -1;
	private int pass = 0;
	private String[] notes = new String[4];
	private String[] choicecontents = new String[4];
	private String[] filetypes = new String[4];

	private boolean mQuizCursor = false;
	private boolean mChoiceCursor = false;

	private Integer alarmtype;
	private String notifyId;

	private LocationManager locationmanager;

	private static final int DIALOG1 = 1;
	private static final int DIALOG2 = 2;
	private static final int DIALOG3 = 3;
	private ProgressDialog pd;
	private ProgressDialog pd_answer;
	private NotifyingAsyncQueryHandler mHandler;

	private double lat = 0;
	private double lng = 0;

	View myView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!UIUtils.checkUser(FourSelectedTextQuizActivity.this)) {
			UIUtils.goLogin(FourSelectedTextQuizActivity.this);
			return;
		}

		this.setContentView(R.layout.activity_quiz);

		Intent intent = this.getIntent();

		alarmtype = intent.getIntExtra("alarmType",
				Constants.AndroidRequestType);
		lat = intent.getDoubleExtra("lat", Constants.defaultValue);
		lng = intent.getDoubleExtra("lng", Constants.defaultValue);
		notifyId = intent.getStringExtra("notifyId");

		this.quizId = intent.getStringExtra("quizid");
		mHandler = new NotifyingAsyncQueryHandler(this.getContentResolver(),
				this);

		Uri quizuri = Quizs.buildQuizUri(quizId);
		Uri choiceuri = Quizs.buildQuizChoicesUri(quizId);
		mHandler.startQuery(QuizQuery._TOKEN, quizuri, QuizQuery.PROJECTION);
		mHandler.startQuery(ChoiceQuery._TOKEN, choiceuri,
				ChoiceQuery.PROJECTION, Choices.DEFAULT_SORT);

		locationmanager = (LocationManager) getSystemService(LOCATION_SERVICE);

	}

	public void onHomeClick(View v) {
		UIUtils.goHome(this);
	}

	public void onAnswerClick(View v1) {
		this.pd_answer = ProgressDialog.show(this, "Wait..", "Checking...",
				true, false);
		Integer answerState = this.checkMultiChoiceAnswer();
		if (this.pd_answer != null && this.pd_answer.isShowing())
			this.pd_answer.dismiss();

		this.updateQuiz(answerState);
	}

	public Integer checkMultiChoiceAnswer() {
		boolean mState = false;
		RadioGroup rg = (RadioGroup) this.findViewById(R.id.quiz_choice_rg);
		int selectedId = rg.getCheckedRadioButtonId();
		switch (selectedId) {
		case R.id.quiz_choice_1:
			myanswer = 1;
			break;
		case R.id.quiz_choice_2:
			myanswer = 2;
			break;
		case R.id.quiz_choice_3:
			myanswer = 3;
			break;
		case R.id.quiz_choice_4:
			myanswer = 4;
			break;
		}
		int intAnswer = Integer.valueOf(this.answer);

		if (intAnswer == myanswer)
			mState = true;

		String comment = "";
		this.findViewById(R.id.quiz_comment_block).setVisibility(View.VISIBLE);
		TextView quizComment = (TextView) this
				.findViewById(R.id.txt_quiz_comment);
		ImageView img_icon = (ImageView) this
				.findViewById(R.id.img_icon_comment);
		if (mState) {
			comment = "The right answer is " + this.answer
					+ ". Congratulations, Your answer is right!";
			quizComment.setText(comment);
			img_icon.setImageResource(this.getRandomIcon(true));
			quizComment.setTextColor(Color.GREEN);
		} else {
			comment = "The right answer is " + this.answer
					+ ". Sorry! Your answer is not right!";
			vibrate();
			quizComment.setText(comment);
			img_icon.setImageResource(this.getRandomIcon(false));
			quizComment.setTextColor(Color.RED);
		}
		this.setChoiceView(true, myanswer);
		this.findViewById(R.id.btn_quiz_answer).setVisibility(View.GONE);
		this.findViewById(R.id.btn_quiz_pass).setVisibility(View.GONE);
		this.findViewById(R.id.btn_quiz_difficult).setVisibility(View.GONE);
		this.findViewById(R.id.btn_quiz_easy).setVisibility(View.GONE);
		this.findViewById(R.id.btn_quiz_more).setVisibility(View.VISIBLE);
		Integer answerState = Constants.NotAnsweredState;
		if (mState)
			answerState = Constants.CorrectAnsweredState;
		else
			answerState = Constants.WrongAnsweredState;
		return answerState;
	}

	private void updateQuiz(Integer answerState) {
		ContentValues cv = new ContentValues();
		cv.put(Quizs.ANSWER_STATE, answerState);
		cv.put(Quizs.MY_ANSWER, this.myanswer);
		cv.put(Quizs.PASS, this.pass);
		cv.put(Quizs.ALARM_TYPE, this.alarmtype.toString());
		cv.put(Quizs.SYNC_TYPE, Quizs.SYNC_TYPE_CLIENT_UPDATE);
		try {
			if (this.locationmanager == null)
				this.locationmanager = (LocationManager) getSystemService(LOCATION_SERVICE);
			Location loc = this.locationmanager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (loc != null) {
				cv.put(Quizs.LATITUTE, loc.getLatitude());
				cv.put(Quizs.LNGITUTE, loc.getLongitude());
				cv.put(Quizs.SPEED, loc.getSpeed());
			}
		} catch (Exception e) {

		}
		this.mHandler.startUpdate(Quizs.buildQuizUri(quizId), cv);
	}

	public void onPassClick(View v) {
		if (v.getId() == R.id.btn_quiz_easy) {
			this.pass = 2;
		} else if (v.getId() == R.id.btn_quiz_difficult) {
			this.pass = 3;
		} else if (v.getId() == R.id.btn_quiz_pass) {
			this.pass = 1;
		}
		this.updateQuiz(Constants.PassAnsweredState);
		if (this.pass == 1)
			this.onMoreClick(null);
		else
			this.checkMultiChoiceAnswer();
	}

	public void onMoreClick(View v) {
		final Intent intent = new Intent(FourSelectedTextQuizActivity.this,
				QuizActivity.class);
		if (this.lat != Constants.defaultValue
				&& this.lng != Constants.defaultValue) {
			intent.putExtra("lat", this.lat);
			intent.putExtra("lng", this.lng);
		}
		intent.putExtra("alarmType", this.alarmtype);
		this.finish();
		this.startActivity(intent);
	}

	@Override
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		if (token == QuizQuery._TOKEN) {
			this.onQuizQueryComplete(cursor);
		} else if (token == ChoiceQuery._TOKEN) {
			this.onChoiceQueryComplete(cursor);
		} else if (cursor != null)
			cursor.close();

		if (pd != null && this.mQuizCursor && this.mChoiceCursor)
			this.pd.dismiss();
	}

	private void onQuizQueryComplete(Cursor cursor) {
		try {
			this.mQuizCursor = true;
			if (!cursor.moveToFirst()) {
				if (this.pd != null && this.pd.isShowing())
					this.pd.cancel();
				UIUtils.goManualSync(this, null);
				return;
			}

			this.answer = cursor.getString(QuizQuery.Answer);
			String quizcontent = cursor.getString(QuizQuery.Quiz_CONTENT);
			String lancode = cursor.getString(QuizQuery.LAN_CODE);

			if (this.notifyId != null) {
				Uri uri = Notifys.buildNotifyUri(notifyId);
				ContentValues cv = new ContentValues();
				cv.put(Notifys.Feedback, Notifys.NOTIFY_FEEDBACK);
				cv.put(Notifys.SYNC_TYPE, Notifys.SYNC_TYPE_CLIENT_UPDATE);
				cv.put(Notifys.UPDATE_TIME, Calendar.getInstance()
						.getTimeInMillis());
				this.mHandler.startUpdate(uri, cv);
			}

			this.initQuizView(quizcontent);
		} finally {
			cursor.close();
		}
	}

	private void onChoiceQueryComplete(Cursor cursor) {
		try {
			this.mChoiceCursor = true;
			if (!cursor.moveToFirst())
				return;

			do {
				String content = cursor.getString(ChoiceQuery.Choice_Content);
				String filetype = cursor.getString(ChoiceQuery.File_type);
				String note = cursor.getString(ChoiceQuery.NOTE);
				Integer num = cursor.getInt(ChoiceQuery.NUMBER);
				String lancode = cursor.getString(ChoiceQuery.LAN_CODE);
				choicecontents[num - 1] = content;
				filetypes[num - 1] = filetype;
				notes[num - 1] = note;
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}
		this.setChoiceView(false, null);
	}

	private void initQuizView(String quizcontent) {
		String qc = "";
		qc = "Select the right word: " + quizcontent + " ?";
		this.findViewById(R.id.quiz_choice_block).setVisibility(View.VISIBLE);

		this.findViewById(R.id.quiz_btn_block).setVisibility(View.VISIBLE);
		this.findViewById(R.id.quiz_no_info).setVisibility(View.GONE);
		TextView title = (TextView) this.findViewById(R.id.txt_quiz_title);
		title.setText(qc);
	}

	private void setChoiceView(boolean answered, Integer myanswer) {
		Integer intAnswer = Integer.valueOf(this.answer);
		boolean aState = false;
		if (intAnswer.equals(myanswer))
			aState = true;
		for (int i = 0; i < choicecontents.length; i++) {
			RadioButton radioButton = null;
			switch (i) {
			case 0:
				radioButton = (RadioButton) this
						.findViewById(R.id.quiz_choice_1);
				break;
			case 1:
				radioButton = (RadioButton) this
						.findViewById(R.id.quiz_choice_2);
				break;
			case 2:
				radioButton = (RadioButton) this
						.findViewById(R.id.quiz_choice_3);
				break;
			case 3:
				radioButton = (RadioButton) this
						.findViewById(R.id.quiz_choice_4);
				break;
			}

			if (radioButton == null || choicecontents[i] == null)
				continue;

			radioButton.setText(choicecontents[i]);
		}
	}

	private interface QuizQuery {
		public static String[] PROJECTION = { Quizs.QUIZ_ID, Quizs.QUIZ_TYPE,
				Quizs.QUIZ_CONTENT, Quizs.FILE_TYPE, Quizs.PHOTO_URL,
				Quizs.ANSWER, Quizs.Item_ID, Quizs.AUTHOR_ID, Quizs.LAN_CODE };
		public static int _TOKEN = 1;

		int Quiz_CONTENT = 2;
		int Answer = 5;
		int LAN_CODE = 8;
	}

	private interface ChoiceQuery {
		public static String[] PROJECTION = { Choices.CHOICE_ID,
				Choices.QUIZ_ID, Choices.CHOICE_CONTENT, Choices.File_TYPE,
				Choices.NOTE, Choices.NUMBER, Choices.LAN_CODE };
		public static int _TOKEN = 2;

		int Choice_Content = 2;
		int File_type = 3;
		int NOTE = 4;
		int NUMBER = 5;
		int LAN_CODE = 6;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG1:
			return this.buildDialog(this, R.string.error_no_quiz);
		case DIALOG2:
			return this.buildDialog(this, R.string.server_exception_occurred);
		case DIALOG3:
			return this.buildDialog(this, R.string.error_unable_access_server);
		}

		return null;
	}

	private int getRandomIcon(boolean happy) {
		if (happy) {
			int[] happyIdList = { R.drawable.happy1, R.drawable.happy2,
					R.drawable.happy3, R.drawable.happy4, R.drawable.happy5,
					R.drawable.happy6, R.drawable.happy7 };
			int index = (int) (Math.random() * happyIdList.length);
			return happyIdList[index];
		} else {
			int[] sadIdList = { R.drawable.sad1, R.drawable.sad2,
					R.drawable.sad3, R.drawable.sad4, R.drawable.sad5,
					R.drawable.sad6, R.drawable.sad7 };
			int index = (int) (Math.random() * sadIdList.length);
			return sadIdList[index];
		}

	}

	private Dialog buildDialog(Context context, int title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(R.drawable.alert_dialog_icon);
		builder.setTitle(title);
		builder.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		return builder.create();
	}

	private void vibrate() {
		Vibrator vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		vibrator.vibrate(1000);
	}

}
