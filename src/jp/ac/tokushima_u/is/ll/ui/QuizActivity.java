package jp.ac.tokushima_u.is.ll.ui;

import java.util.Map;

import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Quizs;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Users;
import jp.ac.tokushima_u.is.ll.ui.quiz.FourSelectedImageQuiz;
import jp.ac.tokushima_u.is.ll.util.Constants;
import jp.ac.tokushima_u.is.ll.util.ContextUtil;
import jp.ac.tokushima_u.is.ll.util.GeoUtils;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler.AsyncQueryListener;
import jp.ac.tokushima_u.is.ll.util.UIUtils;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

public class QuizActivity extends Activity implements AsyncQueryListener {
	private Integer quiztype;
	private String quizId;
	private String quizItemId;

	public static final Integer QuizTypeTextMutiChoice = 1;
	public static final Integer QuizTypeImageMutiChoice = 2;
	public static final Integer QuizTypeYesNoQuestion = 3;

	private String notifyId;
	private ProgressDialog pd;
	private NotifyingAsyncQueryHandler mHandler;

	private double lat = 0;
	private double lng = 0;

	Bundle bundle = null;

	View myView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!UIUtils.checkUser(QuizActivity.this)) {
			UIUtils.goLogin(QuizActivity.this);
			return;
		}

		pd = ProgressDialog.show(this, "Wait..", "Loading...", true, false);

		Intent intent = this.getIntent();

		bundle = intent.getExtras();

		lat = intent.getDoubleExtra("lat", Constants.defaultValue);
		lng = intent.getDoubleExtra("lng", Constants.defaultValue);
		notifyId = intent.getStringExtra("notifyId");
		if (notifyId != null) {
			NotificationManager nm = (NotificationManager) this
					.getSystemService(NOTIFICATION_SERVICE);
			nm.cancel(Constants.QuizNotificationID);
		}

		mHandler = new NotifyingAsyncQueryHandler(this.getContentResolver(),
				this);

		String selection = Quizs.ANSWER_STATE + "=? ";
		String[] args = new String[] { Constants.NotAnsweredState.toString() };
		if (lat != Constants.defaultValue && lng != Constants.defaultValue) {
			Map<String, Double> map = GeoUtils.getKMRange(lat, lng, 0.5);
			Double x1 = map.get("x1");
			Double y1 = map.get("y1");
			Double x2 = map.get("x2");
			Double y2 = map.get("y2");
			if (x1 != null && x2 != null && y1 != null && y2 != null) {
				if (y1 > y2) {
					selection += " and "
					        + Quizs.LATITUTE + " <=? and "
					        + Quizs.LATITUTE + " >=? and "
					        + Quizs.LNGITUTE + " <=? and "
					        + Quizs.LNGITUTE + " >=? ";
				} else {
					selection += " and " + Quizs.LATITUTE + "<=? and "
							+ Quizs.LATITUTE + " >=? and ( (" + Quizs.LNGITUTE
							+ " <=? and " + Quizs.LNGITUTE + " >=-180) or ("
							+ Quizs.LNGITUTE + " >=? and " + Quizs.LNGITUTE
							+ " <=180) )";
				}
				args = new String[] { Constants.NotAnsweredState.toString(),
						x1.toString(), x2.toString(), y1.toString(),
						y2.toString() };
			}
		}

		String userId = ContextUtil.getUserId(this);
		mHandler.startQuery(QuizQuery._TOKEN, null,
				Users.buildUsersQuizUri(userId), QuizQuery.PROJECTION,
				selection, args, Quizs.DEFAULT_SORT);
	}

	public void onHomeClick(View v) {
		UIUtils.goHome(this);
	}

	@Override
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {

		try {
			if (!cursor.moveToFirst()) {
				if (this.pd != null && this.pd.isShowing())
					this.pd.cancel();
				UIUtils.goManualSync(this, null);
				return;
			}

			this.quizId = cursor.getString(QuizQuery.Quiz_ID);
			this.quiztype = cursor.getInt(QuizQuery.QUIZ_TYPE);
			this.quizItemId= cursor.getString(QuizQuery.ITEM_ID);

			if (QuizTypeYesNoQuestion.equals(quiztype) && quizItemId != null) {
				Intent intent = new Intent(QuizActivity.this,
						YesNoQuizActivity.class);
				intent.putExtra("quizid", this.quizId);
				intent.putExtra("quizItemId", this.quizItemId);
				
				intent.putExtra("lat", this.lat);
				intent.putExtra("lng", this.lng);
				if(this.notifyId!=null)
					intent.putExtra("notifyId", this.notifyId);
				
				this.startActivity(intent);
			} else if (QuizTypeImageMutiChoice.equals(quiztype)
					&& quizItemId != null) {
			    // 4択画像
				Intent intent = new Intent(QuizActivity.this,
						FourSelectedImageQuiz.class);
				intent.putExtra("quizid", this.quizId);
				intent.putExtra("lat", this.lat);
				intent.putExtra("lng", this.lng);
				if(this.notifyId!=null)
					intent.putExtra("notifyId", this.notifyId);
				this.startActivity(intent);
			} else if (QuizTypeTextMutiChoice.equals(quiztype)
					&& quizItemId != null) {
				Intent intent = new Intent(QuizActivity.this,
						FourSelectedTextQuizActivity.class);
				intent.putExtra("quizid", this.quizId);
				
				intent.putExtra("lat", this.lat);
				intent.putExtra("lng", this.lng);
				if(this.notifyId!=null)
					intent.putExtra("notifyId", this.notifyId);
				
				this.startActivity(intent);
			}else{
				UIUtils.goHome(QuizActivity.this);
			}

		} finally {
			cursor.close();
			if (pd != null)
				this.pd.dismiss();
		}
	}

	private interface QuizQuery {
		public static String[] PROJECTION = { Quizs.QUIZ_ID, Quizs.QUIZ_TYPE,
				Quizs.QUIZ_CONTENT, Quizs.FILE_TYPE, Quizs.PHOTO_URL,
				Quizs.ANSWER, Quizs.Item_ID, Quizs.AUTHOR_ID };
		public static int _TOKEN = 1;

		int Quiz_ID = 0;
		int QUIZ_TYPE = 1;
		int ITEM_ID = 6;
	}

}
