
package jp.ac.tokushima_u.is.ll.sphinx.quiz.v2;

import java.util.Arrays;
import java.util.Date;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.sphinx.database.DBAdapter;
import jp.ac.tokushima_u.is.ll.sphinx.quiz.v2.fragment.FourSelectedImageFragment;
import jp.ac.tokushima_u.is.ll.ui.HomeActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class FourSelectedImageQuiz extends SherlockFragmentActivity {
    @SuppressWarnings("unused")
    private static final String TAG = FourSelectedImageQuiz.class.getSimpleName();
    private static final String PRE_TEXT = "Which image can be linked to";

    private final Handler handler = new Handler();

    private FourSelectedImageFragment fragment;
    private long startTime;
    private boolean isButtonClicked = false;
    private Button button;
    private TextView textView;

    String[] imageId = new String[4];
    String[] imageName = new String[4];
    long databaseId;
    int answer; // ややこしくなったので調べた。これはリストの番地(0,1,2,3)で指定する
    String author;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Sherlock);
        setContentView(R.layout.fourselectedimagequiz2);

        // ActionBarの設定
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.ic_sphinx_launcher);
        actionBar.setTitle("Sphnx Question");
        
        // 画像表示をランダムにしたいので、ここで配列番号をシャッフルする
        int[] table = generateShuffleArray(4);
        Log.d(TAG, "table:" + Arrays.toString(table) );
                
        // Extra情報を収集
        Intent intent = getIntent();

        databaseId = intent.getLongExtra("databaseId", -1);
        answer = intent.getIntExtra("answer", -1);
        author = intent.getStringExtra("author");
        
        for (int i = 0; i < 4; i++) {
            imageId[table[i]] = intent.getStringExtra("id" + i);
            imageName[table[i]] = intent.getStringExtra("name" + i);
        }
        
        answer = table[answer];
        Log.d(TAG, "answer:" + answer);

        fragment = (FourSelectedImageFragment) getSupportFragmentManager().findFragmentById(
                R.id.fragment_fourselected);

        // クイズの説明文
        textView = (TextView) findViewById(R.id.text_quizDescription);
        textView.setText(PRE_TEXT + " " + imageName[answer]);

        // ボタン
        button = (Button) findViewById(R.id.button_answer);
        button.setOnClickListener(new OnClickListener() {
            // 回答ボタンをタップした時の処理
            public void onClick(View v) {

                if (isButtonClicked == false) {
                    // クイズに回答するボタンのとき

                    int focusId = fragment.getImageViewFocus();

                    if (focusId == -1) {
                        // 回答が選ばれていない状態
                        Toast.makeText(getApplicationContext(),
                                "Please choose answer!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // 回答が選択されているとき
                        isButtonClicked = true;
                        fragment.visibleAnswer();

                        Date date = new Date();
                        long end = date.getTime();

                        long time = end - startTime;
                        Toast.makeText(getApplicationContext(), "TIME:" + time / 1000 + "sec.",
                                Toast.LENGTH_LONG)
                                .show();

                        DBAdapter dbAdapter = new DBAdapter(getApplicationContext());
                        dbAdapter.open();
                        dbAdapter.pickupRewrite(databaseId, DBAdapter.COL_TIME, time);
                        dbAdapter.close();

                        button.setText("Exit");
                    }

                } else {
                    // Activityを閉じるモードの時
                    isButtonClicked = false;
                    fragment.hideAnswer();
                    finish();
                }
            }
        });

        // Activityの起動時刻を回収
        Date date = new Date();
        startTime = date.getTime();
    }

    /**
     * メニュー項目の生成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getSupportMenuInflater().inflate(R.menu.menu_quiz, menu);

        final MenuItem refresh = (MenuItem) menu.findItem(R.id.menu_refresh);
        refresh.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            // on selecting show progress spinner for 1s
            public boolean onMenuItemClick(MenuItem item) {
                // item.setActionView(R.layout.progress_action);
                handler.postDelayed(new Runnable() {
                    public void run() {
                        refresh.setActionView(null);
                    }
                }, 1000);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * メニュー項目のクリックイベントを管理
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // App Icon is clicked
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String[] getImageId() {
        return imageId;
    }

    public String[] getImageName() {
        return imageName;
    }

    public String getAuthor() {
        return author;
    }

    public int getAnswer() {
        return answer;
    }

    public boolean isButtonClicked() {
        return isButtonClicked;
    }

    /**
     * 配列数を指定することで、ランダムな数列を入力した数だけ生成します
     * 
     * @param num 配列の長さ(0以上)
     * @return ランダムな順番の配列
     */
    public static int[] generateShuffleArray(int num) {

        // 数列を生成する
        // 0,1,2,3,...
        int[] ret = new int[num];
        for (int i = 0; i < num; i++) {
            ret[i] = i;
        }

        // 数列をシャッフルする
        // see: http://homepage3.nifty.com/teranet/JavaAlgorithm/Shuffle.html
        for (int i = ret.length - 1; i > 0; i--) {
            int t = (int) (Math.random() * i); // 0～i-1の中から適当に選ぶ

            // 選ばれた値と交換する
            int tmp = ret[i];
            ret[i] = ret[t];
            ret[t] = tmp;
        }
        
        return ret;
    }
}
