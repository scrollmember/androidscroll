package jp.ac.tokushima_u.is.ll.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Choices;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Items;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemtags;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemtitles;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Languages;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Notifys;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Quizs;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Users;
import jp.ac.tokushima_u.is.ll.service.SyncService;
import jp.ac.tokushima_u.is.ll.ui.media.AudioPlayer;
import jp.ac.tokushima_u.is.ll.ui.media.ShowPhoto;
import jp.ac.tokushima_u.is.ll.ui.media.VideoPlayer;
import jp.ac.tokushima_u.is.ll.ui.quiz.FourSelectedImageQuiz;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.BitmapUtil;
import jp.ac.tokushima_u.is.ll.util.Constants;
import jp.ac.tokushima_u.is.ll.util.ContextUtil;
import jp.ac.tokushima_u.is.ll.util.GeoUtils;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler.AsyncQueryListener;
import jp.ac.tokushima_u.is.ll.util.UIUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class CopyOfFourSelectedTextQuizActivity extends Activity implements AsyncQueryListener {
	private Integer quiztype;
	private String quizId;
	private String quizItemId;
	// private Integer nextquiztype;
	// private String nextquizId;
	// private String nextquizItemId;
	private String answer;
	private int myanswer = -1;
	// private boolean isPassed = false;
	private int pass = 0;
	private String[] notes = new String[4];
	private String[] choicecontents = new String[4];
	private String[] filetypes = new String[4];

	private static final String TAG = "QuizActivity";

	public static final Integer QuizTypeTextMutiChoice = 1;
	public static final Integer QuizTypeImageMutiChoice = 2;
	public static final Integer QuizTypeYesNoQuestion = 3;

	private boolean mQuizCursor = false;
	private boolean mChoiceCursor = false;
	private boolean mItemCursor = false;
	private boolean mItemtitleCursor = false;
	private boolean mItemtagCursor = false;

	private static final SimpleDateFormat sTimeFormat = new SimpleDateFormat(
			"yyyy/MM/dd hh:mm:ss", Locale.JAPAN);

	private Integer alarmtype;
	private String notifyId;

	
	private static final int CodeKey = R.color.white;
	private static final int ContentKey = R.color.black;
	
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

		if (!UIUtils.checkUser(CopyOfFourSelectedTextQuizActivity.this)) {
			UIUtils.goLogin(CopyOfFourSelectedTextQuizActivity.this);
			return;
		}

		this.setContentView(R.layout.activity_quiz);

		Intent intent = this.getIntent();

		alarmtype = intent.getIntExtra("alarmType",
				Constants.AndroidRequestType);
		lat = intent.getDoubleExtra("lat", Constants.defaultValue);
		lng = intent.getDoubleExtra("lng", Constants.defaultValue);
		notifyId = intent.getStringExtra("notifyId");
		if (notifyId != null) {
			NotificationManager nm = (NotificationManager) this
					.getSystemService(NOTIFICATION_SERVICE);
			nm.cancel(Constants.QuizNotificationID);
		}
		// Uri uri = intent.getData();
		// String itemid = intent.getStringExtra("itemid");
		// this.quiztype = intent.getIntExtra("quiztype", 0);
		// quizItemId = intent.getStringExtra("quizitemid");

		// if (uri != null) {
		pd = ProgressDialog.show(this, "Wait..", "Loading...", true, false);
		mHandler = new NotifyingAsyncQueryHandler(this.getContentResolver(),
				this);
		// quizId = Quizs.getQuizId(uri);
		// mHandler.startQuery(QuizQuery._TOKEN, uri, QuizQuery.PROJECTION);
		// if (QuizTypeYesNoQuestion.equals(quiztype) && quizItemId != null)
		// {
		// Uri itemuri = Items.buildItemUri(quizItemId);
		// Uri titleuri = Items.buildItemtitlesUri(quizItemId);
		// Uri taguri = Items.buildItemtagsUri(quizItemId);
		// mHandler.startQuery(ItemsQuery._TOKEN, itemuri,
		// ItemsQuery.PROJECTION);
		// mHandler.startQuery(ItemtitlesQuery._TOKEN, titleuri,
		// ItemtitlesQuery.PROJECTION);
		// mHandler.startQuery(ItemtagsQuery._TOKEN, taguri,
		// ItemtagsQuery.PROJECTION);
		// } else {
		// Uri choiceuri = Quizs.buildQuizChoicesUri(quizId);
		// mHandler.startQuery(ChoiceQuery._TOKEN, choiceuri,
		// ChoiceQuery.PROJECTION, Choices.DEFAULT_SORT);
		// }
		//
		// this.mHandler.startQuery(QuizQuery._TOKEN_QUIZS, null,
		// Users.buildUsersQuizUri(ContextUtil.getUserId(this)),
		// QuizQuery.PROJECTION, Quizs.QUIZ_ID + "!=? and "
		// + Quizs.ANSWER_STATE + " =? ", new String[] {
		// quizId, Constants.NotAnsweredState.toString() },
		// Quizs.DEFAULT_SORT);
		String selection = Quizs.ANSWER_STATE + "=? and "+Quizs.QUIZ_TYPE+"=? ";
		String[] args = new String[] { Constants.NotAnsweredState.toString() };
		if (lat != Constants.defaultValue && lng != Constants.defaultValue) {
			Map<String, Double> map = GeoUtils.getKMRange(lat, lng, 0.5);
			Double x1 = map.get("x1");
			Double y1 = map.get("y1");
			Double x2 = map.get("x2");
			Double y2 = map.get("y2");
			if (x1 != null && x2 != null && y1 != null && y2 != null) {
				if (y1 > y2) {
					selection += " and " + Quizs.LATITUTE + "<=? and "
							+ Quizs.LATITUTE + " >=? and " + Quizs.LNGITUTE
							+ " <=? and " + Quizs.LNGITUTE + " >=? ";
				} else {
					selection += " and " + Quizs.LATITUTE + "<=? and "
							+ Quizs.LATITUTE + " >=? and ( (" + Quizs.LNGITUTE
							+ " <=? and " + Quizs.LNGITUTE + " >=-180) or ("
							+ Quizs.LNGITUTE + " >=? and " + Quizs.LNGITUTE
							+ " <=180) )";
				}
				args = new String[] { Constants.NotAnsweredState.toString(),
						x1.toString(), x2.toString(), y1.toString(),
						y2.toString(), QuizTypeImageMutiChoice+""};
			}
		}

		String userId = ContextUtil.getUserId(this);
		mHandler.startQuery(QuizQuery._TOKEN, null,
				Users.buildUsersQuizUri(userId), QuizQuery.PROJECTION,
				selection, args, Quizs.DEFAULT_SORT);

		locationmanager = (LocationManager) getSystemService(LOCATION_SERVICE);
		// this.initComponentSize(this);
		// }
	}

	public void onHomeClick(View v) {
		UIUtils.goHome(this);
	}

	public void onAnswerClick(View v1) {
		if (QuizTypeYesNoQuestion == this.quiztype) {
			this.myanswer = Constants.YesNoQuizRemember;
			this.updateQuiz(Constants.CorrectAnsweredState);
			this.onMoreClick(null);
		} else {
			this.pd_answer = ProgressDialog.show(this, "Wait..", "Checking...",
					true, false);
			Integer answerState = this.checkMultiChoiceAnswer();
			if (this.pd_answer != null && this.pd_answer.isShowing())
				this.pd_answer.dismiss();

			this.updateQuiz(answerState);
		}
	}
	
	public Integer checkMultiChoiceAnswer(){
		boolean mState = false;
		RadioGroup rg = (RadioGroup) this.findViewById(R.id.quiz_choice_rg);
		int selectedId = rg.getCheckedRadioButtonId();
		// int myanswer = -1;
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
		this.findViewById(R.id.quiz_comment_block).setVisibility(
				View.VISIBLE);
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
		if (QuizTypeYesNoQuestion == this.quiztype) {
			if (v.getId() == R.id.btn_quiz_easy) {
				this.myanswer = Constants.YesNoQuizForget;
				this.updateQuiz(Constants.WrongAnsweredState);
				this.onMoreClick(null);
			} else if (v.getId() == R.id.btn_quiz_pass) {
				this.pass = 1;
				this.updateQuiz(Constants.PassAnsweredState);
				this.onMoreClick(null);
			}
		} else {
			if (v.getId() == R.id.btn_quiz_easy) {
				this.pass = 2;
			} else if (v.getId() == R.id.btn_quiz_difficult) {
				this.pass = 3;
			} else if (v.getId() == R.id.btn_quiz_pass) {
				this.pass = 1;
			}
			this.updateQuiz(Constants.PassAnsweredState);
			if(this.pass == 1)
				this.onMoreClick(null);
			else
				this.checkMultiChoiceAnswer();
		}
	}

	public void onMoreClick(View v) {
		final Intent intent = new Intent(CopyOfFourSelectedTextQuizActivity.this, CopyOfFourSelectedTextQuizActivity.class);
		// if (this.nextquizId != null && this.nextquiztype != null) {
		// final Uri uri = Quizs.buildQuizUri(this.nextquizId);
		// intent.setData(uri);
		// intent.setAction(Intent.ACTION_VIEW);
		// intent.putExtra("quiztype", this.nextquiztype);
		// if (this.nextquizItemId != null)
		// intent.putExtra("quizitemid", this.nextquizItemId);
		// }
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
		}
		// else if (token == QuizQuery._TOKEN_QUIZS) {
		// this.onQuizPrepareQueryComplete(cursor);
		// }
		else if (token == ItemsQuery._TOKEN) {
			this.onItemQueryComplete(cursor);
		} else if (token == ItemtitlesQuery._TOKEN) {
			this.onItemtitleQueryComplete(cursor);
		} else if (token == ItemtagsQuery._TOKEN) {
			this.onItemtagQueryComplete(cursor);
		} else if (token == ChoiceQuery._TOKEN) {
			this.onChoiceQueryComplete(cursor);
		} else if (cursor != null)
			cursor.close();

		if (pd != null
				&& this.mQuizCursor
				&& (this.mChoiceCursor || (this.mItemCursor
						&& this.mItemtagCursor && this.mItemtitleCursor)))
			this.pd.dismiss();
	}

	private void onQuizQueryComplete(Cursor cursor) {
		try {
			this.mQuizCursor = true;
			if (!cursor.moveToFirst()) {
				if (this.pd != null && this.pd.isShowing())
					this.pd.cancel();
//				final Intent intent = new Intent(Intent.ACTION_SYNC, null, this,
//						SyncService.class);
//				startService(intent);
				UIUtils.goManualSync(this, null);
				return;
			}

			this.quizId = cursor.getString(QuizQuery.Quiz_ID);
			this.quiztype = cursor.getInt(QuizQuery.QUIZ_TYPE);
			this.answer = cursor.getString(QuizQuery.Answer);
			this.quizItemId = cursor.getString(QuizQuery.Item_Id);
			String quizcontent = cursor.getString(QuizQuery.Quiz_CONTENT);
			String photourl = cursor.getString(QuizQuery.Photo_Url);
			String filetype = cursor.getString(QuizQuery.Fiel_Type);

			if (QuizTypeYesNoQuestion.equals(quiztype) && quizItemId != null) {
				Uri itemuri = Items.buildItemUri(quizItemId);
				Uri titleuri = Items.buildItemtitlesUri(quizItemId);
				Uri taguri = Items.buildItemtagsUri(quizItemId);
				mHandler.startQuery(ItemsQuery._TOKEN, itemuri,
						ItemsQuery.PROJECTION);
				mHandler.startQuery(ItemtitlesQuery._TOKEN, titleuri,
						ItemtitlesQuery.PROJECTION);
				mHandler.startQuery(ItemtagsQuery._TOKEN, taguri,
						ItemtagsQuery.PROJECTION);
			}else if(QuizTypeImageMutiChoice.equals(quiztype) && quizItemId != null) {
				Intent  intent = new Intent(CopyOfFourSelectedTextQuizActivity.this, FourSelectedImageQuiz.class);
				intent.putExtra("quizid", this.quizId);
				this.startActivity(intent);
			} else {
				Uri choiceuri = Quizs.buildQuizChoicesUri(quizId);
				mHandler.startQuery(ChoiceQuery._TOKEN, choiceuri,
						ChoiceQuery.PROJECTION, Choices.DEFAULT_SORT);
			}

			// if(this.lat!=Constants.defaultValue &&
			// this.lng!=Constants.defaultValue){
			// new FeedbackThread(QuizActivity.this, this.lat, this.lng,
			// this.quizItemId, FeedbackThread.QuizFeedBackType).start();
			// }

			if (this.notifyId != null) {
				Uri uri = Notifys.buildNotifyUri(notifyId);
				ContentValues cv = new ContentValues();
				cv.put(Notifys.Feedback, Notifys.NOTIFY_FEEDBACK);
				cv.put(Notifys.SYNC_TYPE, Notifys.SYNC_TYPE_CLIENT_UPDATE);
				cv.put(Notifys.UPDATE_TIME, Calendar.getInstance().getTimeInMillis());
				this.mHandler.startUpdate(uri, cv);
			}

			this.initQuizView(quizcontent, photourl, filetype);
		} finally {
			cursor.close();
		}
	}

	// private void onQuizPrepareQueryComplete(Cursor cursor) {
	// try {
	// if (!cursor.moveToFirst())
	// return;
	//
	// this.nextquiztype = cursor.getInt(QuizQuery.QUIZ_TYPE);
	// this.nextquizId = cursor.getString(QuizQuery.Quiz_ID);
	// this.nextquizItemId = cursor.getString(QuizQuery.Item_Id);
	// } finally {
	// cursor.close();
	// }
	// }

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
				choicecontents[num - 1] = content;
				filetypes[num - 1] = filetype;
				notes[num - 1] = note;
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}
		this.setChoiceView(false, null);
	}

	private void initQuizView(String quizcontent, String photourl,
			String filetype) {
		String qc = "";
		if (QuizTypeTextMutiChoice.equals(this.quiztype)) {
			qc = "Select the right word: " + quizcontent + " ?";
			this.findViewById(R.id.quiz_choice_block).setVisibility(
					View.VISIBLE);
		}
		if (QuizTypeImageMutiChoice.equals(this.quiztype)) {
			qc = "Which image can be linked to " + quizcontent
					+ " ?";
			this.findViewById(R.id.quiz_choice_block).setVisibility(
					View.VISIBLE);
		}
		if (QuizTypeYesNoQuestion.equals(this.quiztype)) {
			qc = "Do you remember? ";
			this.findViewById(R.id.quiz_yesno_block)
					.setVisibility(View.VISIBLE);
			((Button) this.findViewById(R.id.btn_quiz_answer))
					.setText(R.string.quiz_Yes_label);
			((Button) this.findViewById(R.id.btn_quiz_pass))
					.setText(R.string.quiz_pass_label);
			((Button)this.findViewById(R.id.btn_quiz_easy)).setText(R.string.quiz_No_label);
			this.findViewById(R.id.btn_quiz_difficult).setVisibility(View.GONE);
		}
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

			BitmapDrawable bd = null;
			if (this.quiztype.equals(QuizTypeTextMutiChoice)) {
				radioButton.setText(choicecontents[i]);
				// if (answered && notes[i] != null)
			} else if (this.quiztype.equals(QuizTypeImageMutiChoice)) {
				Bitmap bitmap = BitmapUtil
						.getBitmap(CopyOfFourSelectedTextQuizActivity.this, choicecontents[i], ApiConstants.SmallSizePostfix);
				if (bitmap != null)
					bd = new BitmapDrawable(bitmap);
				radioButton.setCompoundDrawablesWithIntrinsicBounds(null, null,
						bd, null);
				if (answered && notes[i] != null)
					radioButton.setText(notes[i]);
				radioButton.setTag(ApiConstants.Image_Server_Url
						+ choicecontents[i] + ApiConstants.MiddleSizePostfix);
				radioButton.setLongClickable(true);
				radioButton.setOnLongClickListener(new View.OnLongClickListener() {
					
					@Override
					public boolean onLongClick(View v) {
						
						LayoutInflater factory = LayoutInflater
						.from(CopyOfFourSelectedTextQuizActivity.this);
						final View textEntryView = factory.inflate(
								R.layout.alert_quiz_image_expand, null);
						ImageView iv = (ImageView) textEntryView
								.findViewById(R.id.answer_image);
						String path = "";
						if (v.getTag() != null)
							path = v.getTag().toString();
						Bitmap bp = BitmapUtil.getURLBitmap(CopyOfFourSelectedTextQuizActivity.this, path);
						if (bp == null)
							return false;
						iv.setImageBitmap(bp);
						AlertDialog dlg = new AlertDialog.Builder(
								CopyOfFourSelectedTextQuizActivity.this).setView(textEntryView)
								.setCancelable(true).create();
						dlg.show();
						dlg.setCanceledOnTouchOutside(true);
						
						return false;
					}
				});
//				radioButton.setOnClickListener(new View.OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						LayoutInflater factory = LayoutInflater
//								.from(QuizActivity.this);
//						final View textEntryView = factory.inflate(
//								R.layout.alert_quiz_image_expand, null);
//						ImageView iv = (ImageView) textEntryView
//								.findViewById(R.id.answer_image);
//						String path = "";
//						if (v.getTag() != null)
//							path = v.getTag().toString();
//						Bitmap bp = BitmapUtil.getURLBitmap(QuizActivity.this, path);
//						if (bp == null)
//							return;
//						iv.setImageBitmap(bp);
//						AlertDialog dlg = new AlertDialog.Builder(
//								QuizActivity.this).setView(textEntryView)
//								.setCancelable(true).create();
//						dlg.show();
//						dlg.setCanceledOnTouchOutside(true);
//					}
//				});
			}
			if (answered) {
				if (i == (myanswer - 1) && aState) {
					radioButton.setCompoundDrawablesWithIntrinsicBounds(
							new BitmapDrawable(BitmapFactory.decodeResource(
									this.getResources(),
									R.drawable.correctanswer)), null, bd, null);
				} else if (i == (myanswer - 1) && !aState) {
					radioButton.setCompoundDrawablesWithIntrinsicBounds(
							new BitmapDrawable(
									BitmapFactory.decodeResource(
											this.getResources(),
											R.drawable.wronganswer)), null, bd,
							null);
				} else if (i == (intAnswer - 1)) {
					radioButton.setCompoundDrawablesWithIntrinsicBounds(
							new BitmapDrawable(BitmapFactory.decodeResource(
									this.getResources(),
									R.drawable.correctanswer)), null, bd, null);
				}
			}
		}
	}

	public void onItemQueryComplete(Cursor cursor) {
		try {
			this.mItemCursor = true;
			if (!cursor.moveToFirst())
				return;

			String authorInfo = "";
			String nickname = cursor.getString(ItemsQuery.NICK_NAME);
			if (nickname != null) {
				authorInfo = authorInfo + "created by " + nickname;
			}

			Long updateTime = cursor.getLong(ItemsQuery.UPDATE_TIME);
			if (updateTime != null) {
				authorInfo = authorInfo + " at "
						+ sTimeFormat.format(new Date(updateTime));
			}

			String photoUrl = cursor.getString(ItemsQuery.PHOTO_URL);
			String filetype = cursor.getString(ItemsQuery.FILE_TYPE);
			if (photoUrl != null && filetype != null) {
				Bitmap bitmap = BitmapUtil
				.getBitmap(CopyOfFourSelectedTextQuizActivity.this, photoUrl, ApiConstants.MiddleSizePostfix);

				String filepath = ApiConstants.Image_Server_Url + photoUrl;
				if (Constants.FileTypeVideo.equals(filetype)) {
					filepath = filepath + "_320x240.mp4";
				} else if (Constants.FileTypeAudio.equals(filetype)) {
					filepath = filepath + ".mp3";
				} else if (Constants.FileTypeImage.equals(filetype)) {
					filepath = filepath + ApiConstants.MiddleSizePostfix;
				}

				Bundle args = new Bundle();
				args.putString(Constants.FileTypeKey, filetype);
				args.putString(Constants.FilePathKey, filepath);

				// Uri uri =
				// Uri.parse(ApiConstants.Image_Server_Url+photoUrl+ApiConstants.MiddleSizePostfix);
				if (bitmap != null) {
					LinearLayout fileBlock = (LinearLayout) this
							.findViewById(R.id.log_file_block);
					fileBlock.setVisibility(View.VISIBLE);
					ImageView imageView = new ImageView(this);
					imageView.setImageBitmap(bitmap);
					imageView.setTag(args);
					fileBlock.addView(imageView);
					imageView.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Bundle args = (Bundle) v.getTag();
							if (args == null)
								return;
							String path = args.getString(Constants.FilePathKey);
							String type = args.getString(Constants.FileTypeKey);
							if (Constants.FileTypeAudio.equals(type)) {
								Intent intent = new Intent(CopyOfFourSelectedTextQuizActivity.this,
										AudioPlayer.class);
								intent.setData(Uri.parse(path));
								startActivity(intent);
							} else if (Constants.FileTypeImage.equals(type)) {
								Intent intent = new Intent(CopyOfFourSelectedTextQuizActivity.this,
										ShowPhoto.class);
								intent.setData(Uri.parse(path));
								startActivity(intent);
							} else if (Constants.FileTypeVideo.equals(type)) {
								Intent intent = new Intent(CopyOfFourSelectedTextQuizActivity.this,
										VideoPlayer.class);
								intent.setData(Uri.parse(path));
								startActivity(intent);
							}
						}
					});
				}
			}

			TextView txtAuthor = (TextView) this.findViewById(R.id.log_author);
			txtAuthor.setText(authorInfo);

			String note = cursor.getString(ItemsQuery.NOTE);
			if (note != null) {
				LinearLayout noteBlock = (LinearLayout) this
						.findViewById(R.id.log_note_block);
				noteBlock.setVisibility(View.VISIBLE);
				TextView txtNote = (TextView) this.findViewById(R.id.log_note);
				txtNote.setText(note);
			}

		} finally {
			cursor.close();
		}
	}

	public void onItemtitleQueryComplete(Cursor cursor) {
		try {
			this.mItemtitleCursor = true;
			if (!cursor.moveToFirst())
				return;
			LinearLayout title_part = (LinearLayout) this
					.findViewById(R.id.log_title_block);
			do {
				String name = cursor.getString(ItemtitlesQuery.NAME);
				String content = cursor.getString(ItemtitlesQuery.CONTENT);
				String code = cursor.getString(ItemtitlesQuery.CODE);
				
				
				View titleView = this.getLayoutInflater().inflate(
						R.layout.list_item_title, null);
				TextView nameText = (TextView) titleView
						.findViewById(R.id.txt_title_name);
				nameText.setText(name + ":");
				TextView contentText = (TextView) titleView
						.findViewById(R.id.txt_title_content);
				contentText.setText(content);
				
				ImageView tts_button = (ImageView)titleView.findViewById(R.id.btn_pronounce);
				tts_button.setTag(CodeKey, code);
				tts_button.setTag(ContentKey, content);
				
				tts_button.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
//						 if (isServiceRunning(
//			                "net.npaka.serviceex.PlayerService")) return;
						String code = (String)v.getTag(CodeKey);
						String content = (String)v.getTag(ContentKey);
						Intent intent = new Intent(CopyOfFourSelectedTextQuizActivity.this, jp.ac.tokushima_u.is.ll.service.TTSService.class);
						String url = ApiConstants.Pronounce_URI+"?ie=UTF-8&lang="+code+"&text=";
						try{
							url = url+URLEncoder.encode(content,"UTF-8");
						}catch(UnsupportedEncodingException e){
							
						}
			        	Uri uri = Uri.parse(url);
						intent.setData(uri);
						startService(intent);
						//Log.d("test", "clicked");
					}
				});
				
				title_part.addView(titleView);
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}
	}

	public void onItemtagQueryComplete(Cursor cursor) {
		try {
			this.mItemtagCursor = true;
			if (!cursor.moveToFirst())
				return;
			String tags = "";
			do {
				String tag = cursor.getString(ItemtagsQuery.TAG);
				if (tag != null && tag.length() > 0) {
					if (tags.length() > 0)
						tags = tags + ", ";
					tags = tags + tag;
				}
			} while (cursor.moveToNext());

			if (tags.length() > 0) {
				LinearLayout tagBlock = (LinearLayout) this
						.findViewById(R.id.log_tag_block);
				tagBlock.setVisibility(View.VISIBLE);
				TextView txtTag = (TextView) this.findViewById(R.id.log_tag);
				txtTag.setText(tags);
			}

		} finally {
			cursor.close();
		}
	}

	private interface QuizQuery {
		public static String[] PROJECTION = { Quizs.QUIZ_ID, Quizs.QUIZ_TYPE,
				Quizs.QUIZ_CONTENT, Quizs.FILE_TYPE, Quizs.PHOTO_URL,
				Quizs.ANSWER, Quizs.Item_ID, Quizs.AUTHOR_ID };
		public static int _TOKEN = 1;
		public static int _TOKEN_QUIZS = 6;

		int Quiz_ID = 0;
		int QUIZ_TYPE = 1;
		int Quiz_CONTENT = 2;
		int Fiel_Type = 3;
		int Photo_Url = 4;
		int Answer = 5;
		int Item_Id = 6;
	}

	private interface ChoiceQuery {
		public static String[] PROJECTION = { Choices.CHOICE_ID,
				Choices.QUIZ_ID, Choices.CHOICE_CONTENT, Choices.File_TYPE,
				Choices.NOTE, Choices.NUMBER };
		public static int _TOKEN = 2;

		int Choice_ID = 0;
		int Quiz_ID = 1;
		int Choice_Content = 2;
		int File_type = 3;
		int NOTE = 4;
		int NUMBER = 5;
	}

	private interface ItemsQuery {
		int _TOKEN = 3;
		String[] PROJECTION = { BaseColumns._ID, Items.ITEM_ID,
				Items.NICK_NAME, Items.PHOTO_URL, Items.FILE_TYPE, Items.TAG,
				Items.NOTE,
				// Items.PLACE,
				Items.LNGITUTE, Items.LATITUTE, Items.UPDATE_TIME };

		int _ID = 0;
		int ITEM_ID = 1;
		int NICK_NAME = 2;
		int PHOTO_URL = 3;
		int FILE_TYPE = 4;
		int TAG = 5;
		int NOTE = 6;
		// int PLACE = 7;
		int LNGITUTE = 7;
		int LATITUTE = 8;
		int UPDATE_TIME = 9;
	}

	private interface ItemtitlesQuery {
		int _TOKEN = 4;
		String[] PROJECTION = { Itemtitles._ID, Itemtitles.ITEMTITLE_ID,
				Itemtitles.ITEM_ID, Itemtitles.LANGUAGE_ID, Itemtitles.CONTENT,
				Languages.NAME, Languages.CODE };

		int _ID = 0;
		int ITEMTITLE_ID = 1;
		int ITEM_ID = 2;
		int LANGUAGE_ID = 3;
		int CONTENT = 4;
		int NAME = 5;
		int CODE = 6;
	}

	private interface ItemtagsQuery {
		int _TOKEN = 5;
		String[] PROJECTION = { Itemtags._ID, Itemtags.ITEMTAG_ID,
				Itemtags.ITEM_ID, Itemtags.TAG };

		int _ID = 0;
		int ITEMTAG_ID = 1;
		int ITEM_ID = 2;
		int TAG = 3;
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

	// private class FeedbackThread extends Thread {
	// private Double lat;
	// private Double lng;
	// private String itemid;
	//
	// FeedbackThread(Double lat, Double lng, String itemid) {
	// this.lat = lat;
	// this.lng = lng;
	// this.itemid = itemid;
	// }
	//
	// public void run() {
	// try {
	// DefaultHttpClient client =
	// HttpClientFactory.getInstance(QuizActivity.this);
	// HttpPost httpPost = new HttpPost(
	// ApiConstants.Context_Aware_Feedback_URL);
	// MultipartEntity params = new MultipartEntity();
	// if (itemid != null)
	// params.addPart("itemid", new StringBody(itemid));
	// if(lat!=null&&lng!=null){
	// params.addPart("lat", new StringBody(lat.toString()));
	// params.addPart("lng", new StringBody(lng.toString()));
	// }
	// if(locationmanager!=null){
	// locationmanager = (LocationManager) getSystemService(LOCATION_SERVICE);
	// Location location =
	// locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	// if(location!=null){
	// params.addPart("speed", new
	// StringBody(String.valueOf(location.getSpeed())));
	// }
	// }
	//
	// params.addPart("alarmType", new StringBody(String.valueOf(1)));
	// httpPost.setEntity(params);
	// client.execute(httpPost);
	// } catch (Exception e) {
	// Log.e("LearningLogTest Exception", e.getMessage());
	// }
	// }
	// };

	// private Handler handler = new Handler() {
	// public void handleMessage(Message msg) {
	// // setContent();
	// pd.dismiss();
	// if (btnpass != null)
	// btnpass.setVisibility(View.VISIBLE);
	// }
	// };

	private void vibrate() {
		Vibrator vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		vibrator.vibrate(1000);
	}

	// public void run() {
	// try {
	// DefaultHttpClient client = HttpClientFactory.getInstance(this);
	// MultipartEntity entity = new MultipartEntity();
	// HttpPost httpPost = new HttpPost(ApiConstants.Quiz_Checker_URL);
	// entity.addPart("quizid", new StringBody(this.quizId));
	// entity.addPart("answer",
	// new StringBody(String.valueOf(this.myanswer)));
	// entity.addPart("pass", new StringBody(String.valueOf(this.pass)));
	// if(this.alarmtype!=null)
	// entity.addPart("alarmtype", new StringBody(this.alarmtype.toString()));
	//
	// try {
	// if (this.locationmanager == null)
	// this.locationmanager = (LocationManager)
	// getSystemService(LOCATION_SERVICE);
	// Location loc = this.locationmanager
	// .getLastKnownLocation(LocationManager.GPS_PROVIDER);
	// if (loc != null) {
	// entity.addPart("lat",
	// new StringBody(String.valueOf(loc.getLatitude())));
	// entity.addPart("lng",
	// new StringBody(String.valueOf(loc.getLongitude())));
	// entity.addPart("speed",
	// new StringBody(String.valueOf(loc.getSpeed())));
	// }
	// } catch (Exception e) {
	//
	// }
	// httpPost.setEntity(entity);
	// client.execute(httpPost);
	// } catch (Exception e) {
	// Log.e(TAG, "LearningLogTest Exception", e);
	// }
	// }

	// // obtain the size of the screen
	// public void initComponentSize(Context cx) {
	// DisplayMetrics dm = new DisplayMetrics();
	// dm = cx.getApplicationContext().getResources().getDisplayMetrics();
	// this.screenWidth = dm.widthPixels;
	// this.screenHeight = dm.heightPixels;
	// this.MaxHeight = (int) (this.screenHeight * 0.4);
	// this.MaxWidth = (int) (this.screenWidth * 0.5);
	// }

}
