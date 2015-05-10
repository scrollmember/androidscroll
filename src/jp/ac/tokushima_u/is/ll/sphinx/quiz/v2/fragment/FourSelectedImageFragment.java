
package jp.ac.tokushima_u.is.ll.sphinx.quiz.v2.fragment;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.sphinx.quiz.v2.FourSelectedImageQuiz;
import jp.ac.tokushima_u.is.ll.sphinx.quiz.v2.ImagePreviewActivity;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.BitmapUtil;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class FourSelectedImageFragment extends SherlockFragment implements OnClickListener {
    @SuppressWarnings("unused")
    private static final String TAG = FourSelectedImageFragment.class.getSimpleName();

    private FrameLayout[] frameLayouts = new FrameLayout[4];
    private ImageView[] overlayImageViews = new ImageView[4];
    private TextView descriptionTextView;
    private TextView quizHeader;
    FourSelectedImageQuiz activity;

    // どのオブジェクトにフォーカスがあるか
    private static boolean[] imageViewFocus = {
            false, false, false, false
    };

    // クイズのデータ
    private String[] mQuizImageUrl = new String[4]; // クイズのイメージURL
    private String[] mQuizName = new String[4]; // クイズの名前、説明
    private int mQuizAnswerId = -1; // クイズの答え

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fourselectedimage, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        Intent intent = getSherlockActivity().getIntent();

        activity = (FourSelectedImageQuiz) getActivity();
        mQuizImageUrl = activity.getImageId();
        mQuizName = activity.getImageName();
        mQuizAnswerId = activity.getAnswer();

        initViews();
    }

    @Override
    public void onStop() {
        super.onStop();
        flushFocus();
    }

    /**
     * Viewの初期化
     */
    public void initViews() {
        for (int i = 0; i < 4; i++) {
            overlayImageViews[i] = (ImageView) getSherlockActivity()
                    .findViewById(ImageViewOverlayId[i]);
            frameLayouts[i] = (FrameLayout) getSherlockActivity()
                    .findViewById(QuizSelectId[i]);

            ImageView choiceImage = (ImageView) getSherlockActivity()
                    .findViewById(ChoiceViewOverlayId[i]);
            Bitmap bitmap = BitmapUtil
                    .getBitmap(this.getSherlockActivity(), mQuizImageUrl[i],
                            ApiConstants.MiddleSizePostfix);
            choiceImage.setImageBitmap(bitmap);

            frameLayouts[i].setOnClickListener(this);
        }
    }

    /**
     * 答えを表示する
     */
    public void visibleAnswer() {
        if (isAdded()) {
            // 答え合わせモードの時、Drawableを○×に差し替える
            int select = getImageViewFocus();
            if (select != -1) {
                for (int i = 0; i < 4; i++) {

                    if (select == i) {
                        overlayImageViews[i].setBackgroundDrawable(getResources()
                                .getDrawable(R.drawable.imageview_focused));
                    }

                    if (i == mQuizAnswerId) {
                        overlayImageViews[i].setImageDrawable(getResources().getDrawable(
                                R.drawable.maru));
                    } else {
                        overlayImageViews[i].setImageDrawable(getResources().getDrawable(
                                R.drawable.batsu));
                    }
                }
            }
        }
    }

    /**
     * 答えを隠す、非表示にする
     */
    public void hideAnswer() {
        if (isAdded()) {
            for (int i = 0; i < 4; i++) {
                overlayImageViews[i].setBackgroundDrawable(getResources()
                        .getDrawable(android.R.color.transparent));
            }
        }
    }

    /**
     * フォーカスしてない状態にする
     */
    private void flushFocus() {
        for (int i = 0; i < 4; i++) {
            overlayImageViews[i].setBackgroundDrawable(getResources()
                    .getDrawable(android.R.color.transparent));
            imageViewFocus[i] = false;
        }
    }

    /**
     * 画像をクリックするときの処理
     * 
     * @param v
     */
    public void onClick(View v) {

        // タップされた画像に装飾をつける
        for (int i = 0; i < 4; i++) {
            if (v.getId() == QuizSelectId[i]) {
                // タップされた画像に対しての処理
                // フォーカスを与える。フォーカスが既にあるならプレビューさせる

                if (imageViewFocus[i]) { // タップした選択肢が選択されている時
                    Intent intent = new Intent(getActivity(), ImagePreviewActivity.class);
                    intent.putExtra("IMG_ID", mQuizImageUrl[i]);
                    startActivity(intent);
                }
                overlayImageViews[i].setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.imageview_focused));
                imageViewFocus[i] = true;
            } else {
                // タップされていない画像に対しての処理
                // フォーカスを取り除く
                overlayImageViews[i].setBackgroundDrawable(getResources()
                        .getDrawable(android.R.color.transparent));
                imageViewFocus[i] = false;
            }

            if (activity.isButtonClicked()) { // 選択した選択肢があるとき(答え合わせモード)

                if (v.getId() == QuizSelectId[i]) {
                    // 語句の意味を解説する
                    descriptionTextView = (TextView) getSherlockActivity()
                            .findViewById(R.id.text_selectionDescription);
                    descriptionTextView.setText(mQuizName[i]);

                    Toast.makeText(getSherlockActivity(), mQuizName[i],
                            Toast.LENGTH_SHORT).show();
                }
            }
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

    /**
     * v1(今中作の新版QuizUI)を納品したあと、リーさんか誰かが Fragmentにデータベース取得系の処理を書いていたが、
     * UIとロジックの分離という意味では非常に良くない。 そういう処理はActivityに書くほうがいいと僕は考えている。
     * Fragmentは状況にもよるのだがUIパーツなので、 UIをいじるような操作や関連操作しか書かないようにするのが一貫性を保てると思う。
     * ・・・まぁそれ以前に今までActivityしか知らなかった連中だからなー。マジゴミ。
     */
}
