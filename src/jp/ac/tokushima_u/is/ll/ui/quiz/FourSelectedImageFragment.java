
package jp.ac.tokushima_u.is.ll.ui.quiz;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Choices;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Quizs;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.BitmapUtil;
import jp.ac.tokushima_u.is.ll.util.Constants;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler.AsyncQueryListener;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class FourSelectedImageFragment extends SherlockFragment implements AsyncQueryListener {

    private FrameLayout[] frameLayouts = new FrameLayout[4];
    private ImageView[] overlayImageViews = new ImageView[4];
    private TextView descriptionTextView;

    private Integer alarmtype;
    private String notifyId;

    private boolean mQuizCursor = false;
    private boolean mChoiceCursor = false;
    private Boolean isAnswered = Boolean.FALSE;

    private String quizId;
    private NotifyingAsyncQueryHandler mHandler;
    private ProgressDialog pd;

    private LocationManager locationmanager;

    private double lat = 0;
    private double lng = 0;

    private static final String questionPre = "Which image can be linked to ";

    // どのオブジェクトにフォーカスがあるか
    private static boolean[] imageViewFocus = {
            false, false, false, false
    };

    // ユーザが選択した答え
    private static int mSelectedId = -1;

    // クイズの答え
    private int mQuizAnswerId = 2;

    // ここより下の要素は本来Activityで保持しているはず
    // 直接セットされているよりかは、Activityからgetter呼んで格納するほうがいい

    private int[] ImageViewOverlayId = {
            R.id.imageView_select_one_overlay,
            R.id.imageView_select_two_overlay,
            R.id.imageView_select_three_overlay,
            R.id.imageView_select_four_overlay
    };

    private int[] ChoiceViewOverlayId = {
            R.id.imageView_select_one,
            R.id.imageView_select_two,
            R.id.imageView_select_three,
            R.id.imageView_select_four
    };

    private int[] QuizSelectId = {
            R.id.clickable_select_one,
            R.id.clickable_select_two,
            R.id.clickable_select_three,
            R.id.clickable_select_four
    };

    private String[] quizSelectionDescription = new String[4];

    private String[] QuizImageUrls = new String[4];

    // ここまで

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fourselectedimage, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        Intent intent = getSherlockActivity().getIntent();
        if (intent != null) {
            mSelectedId = intent.getIntExtra("FOCUS_OBJECT_ID", -1);
            quizId = intent.getStringExtra("quizid");
            isAnswered = intent.getBooleanExtra("IS_ANSWERED", Boolean.FALSE);
            alarmtype = intent.getIntExtra("alarmType",
                    Constants.AndroidRequestType);
            lat = intent.getDoubleExtra("lat", Constants.defaultValue);
            lng = intent.getDoubleExtra("lng", Constants.defaultValue);
            notifyId = intent.getStringExtra("notifyId");
        }

        mHandler = new NotifyingAsyncQueryHandler(this.getSherlockActivity().getContentResolver(),
                this);

        Uri quizuri = Quizs.buildQuizUri(quizId);
        Uri choiceuri = Quizs.buildQuizChoicesUri(quizId);
        mHandler.startQuery(QuizQuery._TOKEN, quizuri, QuizQuery.PROJECTION);
        mHandler.startQuery(ChoiceQuery._TOKEN, choiceuri,
                ChoiceQuery.PROJECTION, Choices.DEFAULT_SORT);
        // initViews();

        locationmanager = (LocationManager) this.getSherlockActivity().getSystemService(
                this.getSherlockActivity().LOCATION_SERVICE);
    }

    @Override
    public void onPause() {
        super.onPause();

        // flushFocus();
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        flushFocus();
    }

    private void flushFocus() {
        for (int i = 0; i < 4; i++) {
            overlayImageViews[i].setBackgroundDrawable(getResources()
                    .getDrawable(android.R.color.transparent));
            imageViewFocus[i] = false;
        }
    }

    /**
     * Viewの初期化
     */
    private void initViews() {
        int answerState = Constants.NotAnsweredState;
        for (int i = 0; i < 4; i++) {
            frameLayouts[i] = (FrameLayout) getSherlockActivity().findViewById(QuizSelectId[i]);
            overlayImageViews[i] = (ImageView) getSherlockActivity().findViewById(
                    ImageViewOverlayId[i]);
            ImageView choiceImage = (ImageView) getSherlockActivity().findViewById(
                    ChoiceViewOverlayId[i]);

            Bitmap bitmap = BitmapUtil
                    .getBitmap(this.getSherlockActivity(), QuizImageUrls[i],
                            ApiConstants.MiddleSizePostfix);
            choiceImage.setImageBitmap(bitmap);

            // 答え合わせモードの時、Drawableを○×に差し替える
            if (mSelectedId != -1) {

                if (mSelectedId == i) {
                    overlayImageViews[i].setBackgroundDrawable(getResources()
                            .getDrawable(R.drawable.imageview_focused));
                }

                if (i == mQuizAnswerId) {
                    overlayImageViews[i].setImageDrawable(getResources().getDrawable(
                            R.drawable.maru));
                    answerState = Constants.CorrectAnsweredState;
                } else {
                    overlayImageViews[i].setImageDrawable(getResources().getDrawable(
                            R.drawable.batsu));
                    answerState = Constants.WrongAnsweredState;
                }
            }

            frameLayouts[i].setOnClickListener(new OnClickListener() {
                // 各選択肢についてのクリックイベント
                public void onClick(View v) {

                    // タップされた画像に装飾をつける
                    for (int i = 0; i < 4; i++) {

                        if (v.getId() == QuizSelectId[i]) {

                            if (imageViewFocus[i]) { // タップした選択肢が選択されている時
                                /*
                                 * 画像のリソースを渡すのだけど、現在のところdrawableを参照しているので、
                                 * 実際の運用だと手直しが必要になると思われます
                                 */
                                Intent intent = new Intent(getActivity(),
                                        ImagePreviewActivity.class);
                                intent.putExtra("IMG_ID", QuizImageUrls[i]);
                                startActivity(intent);
                            }

                            overlayImageViews[i].setBackgroundDrawable(getResources()
                                    .getDrawable(R.drawable.imageview_focused));

                            imageViewFocus[i] = true;

                        } else {

                            overlayImageViews[i].setBackgroundDrawable(getResources()
                                    .getDrawable(android.R.color.transparent));

                            imageViewFocus[i] = false;
                        }

                        if (mSelectedId != -1) { // 選択した選択肢があるとき(答え合わせモード)

                            if (v.getId() == QuizSelectId[i]) {
                                // 語句の意味を解説する
                                descriptionTextView = (TextView) getSherlockActivity()
                                        .findViewById(R.id.text_selectionDescription);
                                descriptionTextView.setText(quizSelectionDescription[i]);
                            }

                        }
                    }
                }
            });
        }

        if (this.isAnswered.booleanValue() && mSelectedId != -1) {
            this.updateQuiz(answerState);
        }
    }

    /**
     * フォーカスの当たっているImageViewの番号を返す
     * 
     * @return ImageView番号(0-3), フォーカスなし -1
     */
    public int getImageViewFocus() {
        for (int i = 0; i < 4; i++) {
            if (imageViewFocus[i] == true) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (token == QuizQuery._TOKEN) {
            this.onQuizQueryComplete(cursor);
        } else if (token == ChoiceQuery._TOKEN) {
            this.onChoiceQueryComplete(cursor);
        } else if (cursor != null)
            cursor.close();

        if (this.mQuizCursor && this.mChoiceCursor) {
            if (pd != null)
                this.pd.dismiss();

            this.initViews();
        }

    }

    private void updateQuiz(Integer answerState) {
        ContentValues cv = new ContentValues();
        cv.put(Quizs.ANSWER_STATE, answerState);
        cv.put(Quizs.MY_ANSWER, mSelectedId + 1);
        cv.put(Quizs.ALARM_TYPE, this.alarmtype.toString());
        cv.put(Quizs.SYNC_TYPE, Quizs.SYNC_TYPE_CLIENT_UPDATE);
        try {
            if (this.locationmanager == null)
                this.locationmanager = (LocationManager) this.getSherlockActivity()
                        .getSystemService(this.getSherlockActivity().LOCATION_SERVICE);
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

    private void onQuizQueryComplete(Cursor cursor) {
        try {
            this.mQuizCursor = true;
            if (!cursor.moveToFirst()) {
                if (this.pd != null && this.pd.isShowing())
                    this.pd.cancel();
                return;
            }

            this.mQuizAnswerId = cursor.getInt(QuizQuery.Answer) - 1;

            String quizcontent = cursor.getString(QuizQuery.Quiz_CONTENT);

            TextView tv = (TextView) this.getSherlockActivity().findViewById(
                    R.id.text_quizDescription);
            tv.setText(questionPre + quizcontent + "?");
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
                // String filetype = cursor.getString(ChoiceQuery.File_type);
                Integer num = cursor.getInt(ChoiceQuery.NUMBER);
                QuizImageUrls[num - 1] = cursor.getString(ChoiceQuery.Choice_Content);
                quizSelectionDescription[num - 1] = cursor.getString(ChoiceQuery.NOTE);
                // filetypes[num - 1] = filetype;
            } while (cursor.moveToNext());
        } finally {
            cursor.close();
        }
    }

    private interface QuizQuery {
        public static String[] PROJECTION = {
                Quizs.QUIZ_ID,
                Quizs.QUIZ_TYPE,
                Quizs.QUIZ_CONTENT,
                Quizs.FILE_TYPE,
                Quizs.PHOTO_URL,
                Quizs.ANSWER,
                Quizs.Item_ID,
                Quizs.AUTHOR_ID
        };
        public static int _TOKEN = 1;

        int File_Type = 3;
        int Photo_Url = 4;
        int Quiz_CONTENT = 2;
        int Answer = 5;
    }

    private interface ChoiceQuery {
        public static String[] PROJECTION = {
                Choices.CHOICE_ID,
                Choices.QUIZ_ID,
                Choices.CHOICE_CONTENT,
                Choices.File_TYPE,
                Choices.NOTE,
                Choices.NUMBER
        };
        public static int _TOKEN = 2;

        int Choice_Content = 2;
        int File_type = 3;
        int NOTE = 4;
        int NUMBER = 5;
    }

}
