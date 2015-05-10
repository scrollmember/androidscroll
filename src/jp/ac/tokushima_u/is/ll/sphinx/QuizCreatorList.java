
package jp.ac.tokushima_u.is.ll.sphinx;

import java.io.IOException;
import java.util.ArrayList;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.sphinx.adapter.LLOThumbnailAdapter;
import jp.ac.tokushima_u.is.ll.sphinx.classes.LLO;
import jp.ac.tokushima_u.is.ll.sphinx.classes.MessagedQuiz;
import jp.ac.tokushima_u.is.ll.sphinx.database.DBAdapter;
import jp.ac.tokushima_u.is.ll.util.ContextUtil;

import org.msgpack.MessagePack;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.toro.android.lib.androidbeam.BeamHelper;

public class QuizCreatorList extends SherlockActivity implements OnItemClickListener,
        OnClickListener {
    @SuppressWarnings("unused")
    private static final String TAG = QuizCreatorList.class.getSimpleName();
    private static final int REQ_CODE_SEARCH = 109;
    private final QuizCreatorList self = this;

    ArrayList<LLO> items = new ArrayList<LLO>();
    MessagedQuiz mQuiz = new MessagedQuiz();

    private LLOThumbnailAdapter mAdapter;

    private ListView mListView;
    private LinearLayout buttonView;
    private Button discardButton;
    private Button saveButton;
    private BeamHelper mBeamHelper = null;

    private boolean[] isChanged = {
            false, false, false, false
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sphinx_editor);

        // ActionBarの設定
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.ic_sphinx_launcher);
        actionBar.setTitle("Sphinx: Quiz Maker");

        Intent intent = getIntent();
        if (intent == null) {
            Toast.makeText(getApplicationContext(), "Something is wrong. (Extra is not found)",
                    Toast.LENGTH_LONG).show();
            finish();
        }

        // Viewの初期化
        mListView = (ListView) findViewById(R.id.list_edit);
        buttonView = (LinearLayout) findViewById(R.id.buttonView);
        saveButton = (Button) findViewById(R.id.button_discard);
        discardButton = (Button) findViewById(R.id.button_save);
        mAdapter = new LLOThumbnailAdapter(this, R.layout.list_llo_detail, items);

        String status = intent.getStringExtra("status");
        if ("edit".equals(status)) {
            // 編集モード
            String[] imageId = new String[4];
            int answer = intent.getIntExtra("answer", -1);

            if (answer == -1) {
                Toast.makeText(getApplicationContext(), "Something is wrong.(Mismatch answer)",
                        Toast.LENGTH_LONG).show();
                finish();
            }
            // @formatter:off
            /*
             * 0,Correct Answer(サブタイトル) 
             * 1,項目1 
             * 2,Wrong Answer(サブタイトル) 
             * 3,項目2 
             * 4,項目3
             * 5,項目4
             * 
             * "[#" で始まる文字は装飾される。
             * この処理はLLOThumbnailAdapterにて記述されている。
             * 
             */
            // @formatter:on

            items.add(new LLO("[#Correct Answer", ""));
            items.add(new LLO("test1", "testRes"));
            items.add(new LLO("[#Wrong Answer", ""));

            for (int i = 0; i < imageId.length; i++) {
                if (answer == i) {
                    items.set(1, new LLO(
                            intent.getStringExtra("name" + i),
                            intent.getStringExtra("id" + i)
                            ));
                } else {
                    items.add(new LLO(
                            intent.getStringExtra("name" + i),
                            intent.getStringExtra("id" + i)
                            ));
                }
            }
        } else if ("create".equals(status)) {
            // 新規作成モード
            items.add(new LLO("[#Correct Answer", ""));
            items.add(new LLO("Please set answer.", "none"));
            items.add(new LLO("[#Wrong Answer", ""));
            items.add(new LLO("Please set answer.", "none"));
            items.add(new LLO("Please set answer.", "none"));
            items.add(new LLO("Please set answer.", "none"));
        }
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        discardButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isAllChanged()) {
            // 保存ボタンを出す
            buttonView.setVisibility(View.VISIBLE);
        } else {
            // 保存ボタンを消す
            buttonView.setVisibility(View.GONE);
        }

        // 送るデータの用意
        String[] quizId = {
                items.get(1).getImage(),
                items.get(3).getImage(),
                items.get(4).getImage(),
                items.get(5).getImage(),

        };
        String[] name = {
                items.get(1).getName(),
                items.get(3).getName(),
                items.get(4).getName(),
                items.get(5).getName(),
        };

        mQuiz.imageId = quizId;
        mQuiz.name = name;
        mQuiz.answer = 0;// リストの0番目が正解だから
        mQuiz.author = ContextUtil.getUsername(this);

        // Android Beamの初期化
        MessagePack messagePack = new MessagePack();
        byte[] pushMessage = null;
        try {
            pushMessage = messagePack.write(mQuiz);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // for Log
        String debugMsg = "";
        for (byte b : pushMessage) {
            debugMsg += Integer.toHexString(b & 0xFF) + " ";
        }
        // Log.d(TAG, "MessagePack(" + pushMessage.length + "):" + debugMsg);

        mBeamHelper = new BeamHelper(self, "jp.ac.tokushima_u.is.ll.sphinx", pushMessage, false);
    }

    /**
     * ボタンをクリックするとコール
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_discard:
                // 破棄ボタン
                Toast.makeText(getApplicationContext(), "Your change was canceled.",
                        Toast.LENGTH_LONG).show();
                finish();
                break;
            case R.id.button_save:
                // 保存ボタン
                Toast.makeText(getApplicationContext(), "OK!", Toast.LENGTH_LONG).show();

                DBAdapter dbAdapter = new DBAdapter(getApplicationContext());
                dbAdapter.open();
                // TODO
                // 番号がずれているのは、タイトル行を弾いているため。
                // 別な場所でアイテムリストを組み直したほうがいいかもしれない
                dbAdapter.create(
                        items.get(1).getImage(),
                        items.get(3).getImage(),
                        items.get(4).getImage(),
                        items.get(5).getImage(),
                        items.get(1).getName(),
                        items.get(3).getName(),
                        items.get(4).getName(),
                        items.get(5).getName(),
                        0,
                        DBAdapter.DIRECTION_OUTGOING,
                        ContextUtil.getUsername(this)
                        );
                dbAdapter.close();

                finish();
                break;
            default:
                Toast.makeText(getApplicationContext(), "!?", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * リストの要素をクリックするとコール
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
            case 2:
                break;
            default:
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra("listPosition", position);
                intent.putExtra("itemId", items.get(position).getImage());
                startActivityForResult(intent, REQ_CODE_SEARCH);
        }
    }

    /**
     * すべて変更されているかどうかを全件チェックして調べます
     * 
     * @return true: all is true, false: something is false
     */
    private boolean isAllChanged() {
        for (int i = 0; i < isChanged.length; i++) {
            if (isChanged[i] == false) {
                return false;
            }
        }

        return true;
    }

    /**
     * 事情により複雑になったリストのpositionをindexに改めます
     * 
     * @param pos
     * @return index / -1 : error
     */
    private int posToIndex(int pos) {
        switch (pos) {
            case 0:
            case 2:
                return -1;
            case 1:
                return 0;
            case 3:
            case 4:
            case 5:
                return pos - 2;
            default:
                return -1;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult");

        // mAdapter = new LLOThumbnailAdapter(this, R.layout.list_llo_detail,
        // items);

        if (REQ_CODE_SEARCH == requestCode && data != null) {

            Log.d(TAG, "reqcode = ok");

            int pos = data.getIntExtra("listPosition", -1);
            String itemId = data.getStringExtra("itemId");
            String itemTitle = data.getStringExtra("itemTitle");

            Log.d(TAG, "itemId:" + itemId);
            Log.d(TAG, "itemTitle:" + itemTitle);
            Log.d(TAG, "Position:" + pos + "/ itemSize:" + items.size());

            if (pos != -1) {
                items.set(pos, new LLO(itemTitle, itemId));
                isChanged[posToIndex(pos)] = true;
            }
        }

        mAdapter.notifyDataSetChanged();
    }
}
