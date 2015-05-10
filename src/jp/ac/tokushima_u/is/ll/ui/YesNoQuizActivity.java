package jp.ac.tokushima_u.is.ll.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Items;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemtags;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemtitles;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Languages;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Quizs;
import jp.ac.tokushima_u.is.ll.ui.media.AudioPlayer;
import jp.ac.tokushima_u.is.ll.ui.media.ShowPhoto;
import jp.ac.tokushima_u.is.ll.ui.media.VideoPlayer;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.BitmapUtil;
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
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.BaseColumns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class YesNoQuizActivity extends Activity implements AsyncQueryListener {
	private String quizId;
	private String quizItemId;
	private int myanswer = -1;
	private int pass = 0;

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
	private NotifyingAsyncQueryHandler mHandler;

	private double lat = 0;
	private double lng = 0;

	View myView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!UIUtils.checkUser(YesNoQuizActivity.this)) {
			UIUtils.goLogin(YesNoQuizActivity.this);
			return;
		}

		this.setContentView(R.layout.activity_quiz);

		Intent intent = this.getIntent();

		alarmtype = intent.getIntExtra("alarmType",
				Constants.AndroidRequestType);
		lat = intent.getDoubleExtra("lat", Constants.defaultValue);
		lng = intent.getDoubleExtra("lng", Constants.defaultValue);

		this.quizId = intent.getStringExtra("quizid");
		this.quizItemId = intent.getStringExtra("quizItemId");

		if(this.quizId != null && this.quizItemId!=null){
			pd = ProgressDialog.show(this, "Wait..", "Loading...", true, false);
			mHandler = new NotifyingAsyncQueryHandler(this.getContentResolver(),
					this);
			
			Uri itemuri = Items.buildItemUri(quizItemId);
			Uri titleuri = Items.buildItemtitlesUri(quizItemId);
			Uri taguri = Items.buildItemtagsUri(quizItemId);
			mHandler.startQuery(ItemsQuery._TOKEN, itemuri, ItemsQuery.PROJECTION);
			mHandler.startQuery(ItemtitlesQuery._TOKEN, titleuri,
					ItemtitlesQuery.PROJECTION);
			mHandler.startQuery(ItemtagsQuery._TOKEN, taguri,
					ItemtagsQuery.PROJECTION);
		}
	}

	public void onHomeClick(View v) {
		UIUtils.goHome(this);
	}

	public void onAnswerClick(View v1) {
		this.myanswer = Constants.YesNoQuizRemember;
		this.updateQuiz(Constants.CorrectAnsweredState);
		this.onMoreClick(null);
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
			this.myanswer = Constants.YesNoQuizForget;
			this.updateQuiz(Constants.WrongAnsweredState);
			this.onMoreClick(null);
		} else if (v.getId() == R.id.btn_quiz_pass) {
			this.pass = 1;
			this.updateQuiz(Constants.PassAnsweredState);
			this.onMoreClick(null);
		}
	}

	public void onMoreClick(View v) {
		final Intent intent = new Intent(YesNoQuizActivity.this,
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
		if (token == ItemsQuery._TOKEN) {
			this.onItemQueryComplete(cursor);
		} else if (token == ItemtitlesQuery._TOKEN) {
			this.onItemtitleQueryComplete(cursor);
		} else if (token == ItemtagsQuery._TOKEN) {
			this.onItemtagQueryComplete(cursor);
		} else if (cursor != null)
			cursor.close();

		if (pd != null && this.mItemCursor && this.mItemtagCursor && this.mItemtitleCursor)
			this.pd.dismiss();
	}

	private void initQuizView() {
		String qc = "";
		qc = "Do you remember? ";
		this.findViewById(R.id.quiz_yesno_block).setVisibility(View.VISIBLE);
		((Button) this.findViewById(R.id.btn_quiz_answer))
				.setText(R.string.quiz_Yes_label);
		((Button) this.findViewById(R.id.btn_quiz_pass))
				.setText(R.string.quiz_pass_label);
		((Button) this.findViewById(R.id.btn_quiz_easy))
				.setText(R.string.quiz_No_label);
		this.findViewById(R.id.btn_quiz_difficult).setVisibility(View.GONE);
		this.findViewById(R.id.quiz_btn_block).setVisibility(View.VISIBLE);
		this.findViewById(R.id.quiz_no_info).setVisibility(View.GONE);
		TextView title = (TextView) this.findViewById(R.id.txt_quiz_title);
		title.setText(qc);
	}

	public void onItemQueryComplete(Cursor cursor) {
		try {
			this.mItemCursor = true;
			if (!cursor.moveToFirst())
				return;
			
			this.initQuizView();

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
				Bitmap bitmap = BitmapUtil.getBitmap(YesNoQuizActivity.this,
						photoUrl, ApiConstants.MiddleSizePostfix);

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
								Intent intent = new Intent(
										YesNoQuizActivity.this,
										AudioPlayer.class);
								intent.setData(Uri.parse(path));
								startActivity(intent);
							} else if (Constants.FileTypeImage.equals(type)) {
								Intent intent = new Intent(
										YesNoQuizActivity.this, ShowPhoto.class);
								intent.setData(Uri.parse(path));
								startActivity(intent);
							} else if (Constants.FileTypeVideo.equals(type)) {
								Intent intent = new Intent(
										YesNoQuizActivity.this,
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

				ImageView tts_button = (ImageView) titleView
						.findViewById(R.id.btn_pronounce);
				tts_button.setTag(CodeKey, code);
				tts_button.setTag(ContentKey, content);

				tts_button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// if (isServiceRunning(
						// "net.npaka.serviceex.PlayerService")) return;
						String code = (String) v.getTag(CodeKey);
						String content = (String) v.getTag(ContentKey);
						Intent intent = new Intent(
								YesNoQuizActivity.this,
								jp.ac.tokushima_u.is.ll.service.TTSService.class);
						String url = ApiConstants.Pronounce_URI
								+ "?ie=UTF-8&lang=" + code + "&text=";
						try {
							url = url + URLEncoder.encode(content, "UTF-8");
						} catch (UnsupportedEncodingException e) {

						}
						Uri uri = Uri.parse(url);
						intent.setData(uri);
						startService(intent);
						// Log.d("test", "clicked");
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
