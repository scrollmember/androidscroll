
package jp.ac.tokushima_u.is.ll.sphinx.fragment;

import java.util.ArrayList;
import java.util.Date;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.sphinx.adapter.QuizDetailAdapter;
import jp.ac.tokushima_u.is.ll.sphinx.classes.Quiz;
import jp.ac.tokushima_u.is.ll.sphinx.database.DBAdapter;
import jp.ac.tokushima_u.is.ll.sphinx.quiz.v2.FourSelectedImageQuiz;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class QuizInboxListFragment extends ListFragment {
    @SuppressWarnings("unused")
    private static final String TAG = QuizInboxListFragment.class.getSimpleName();

    public static final String NAME = "Inbox";

    QuizDetailAdapter adapter;

    ArrayList<Quiz> items = new ArrayList<Quiz>();

    // http://d.hatena.ne.jp/ktdk/20110309/1299681439

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();

        searchDatabase();
        adapter = new QuizDetailAdapter(getActivity(), R.layout.list_quiz_detail,
                items);
        setListAdapter(adapter);
        // adapter.notifyDataSetChanged();
    }

    /**
     * データベースを検索してリストに値を挿入する
     */
    private void searchDatabase() {
        items.clear();

        DBAdapter dbAdapter = new DBAdapter(getActivity());
        dbAdapter.open();

        // デバッグのために自分が作ったやつを表示させている
        Cursor c = dbAdapter.getSameDirection(DBAdapter.DIRECTION_OUTGOING);
        if (c.moveToFirst()) {
            for (int i = 0; i < c.getCount(); i++) {

                Quiz q = new Quiz();
                String[] quizid = {
                        c.getString(c.getColumnIndex(DBAdapter.COL_LLO1)),
                        c.getString(c.getColumnIndex(DBAdapter.COL_LLO2)),
                        c.getString(c.getColumnIndex(DBAdapter.COL_LLO3)),
                        c.getString(c.getColumnIndex(DBAdapter.COL_LLO4))
                };
                String[] quizname = {
                        c.getString(c.getColumnIndex(DBAdapter.COL_NAME1)),
                        c.getString(c.getColumnIndex(DBAdapter.COL_NAME2)),
                        c.getString(c.getColumnIndex(DBAdapter.COL_NAME3)),
                        c.getString(c.getColumnIndex(DBAdapter.COL_NAME4))
                };

                q.setQuizId(quizid);
                q.setName(quizname);
                q.setAuthor(c.getString(c.getColumnIndex(DBAdapter.COL_AUTHOR)));
                q.setAnswer(c.getInt(c.getColumnIndex(DBAdapter.COL_ANSWER)));
                q.setCreatedAt(new Date(c.getLong(c.getColumnIndex(DBAdapter.COL_CREATEDAT))));

                items.add(q);

                c.moveToNext();
            }
            c.close();
        }

        dbAdapter.close();
    }

    /**
     * リストの項目をタップしたら実行される
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Quiz item = items.get(position);

        Intent intent = new Intent(getActivity(), FourSelectedImageQuiz.class);

        // Extra作成
        //intent.putExtra("status", "edit");
        for (int i = 0; i < item.getQuizId().length; i++) {
            intent.putExtra("id" + i, item.getQuizId()[i]);
            intent.putExtra("name" + i, item.getName()[i]);
        }
        intent.putExtra("answer", item.getAnswer());
        intent.putExtra("author", item.getAuthor());

        startActivity(intent);
    }
}
