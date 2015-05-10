
package jp.ac.tokushima_u.is.ll.sphinx.fragment;

import java.util.ArrayList;
import java.util.Date;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Choices;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Quizs;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Users;
import jp.ac.tokushima_u.is.ll.sphinx.QuizCreatorList;
import jp.ac.tokushima_u.is.ll.sphinx.adapter.QuizDetailAdapter;
import jp.ac.tokushima_u.is.ll.sphinx.classes.Quiz;
import jp.ac.tokushima_u.is.ll.util.Constants;
import jp.ac.tokushima_u.is.ll.util.ContextUtil;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler.AsyncQueryListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

public class MyQuizListFragment extends ListFragment implements AsyncQueryListener {
    @SuppressWarnings("unused")
    private static final String TAG = MyQuizListFragment.class.getSimpleName();

    public static final String NAME = "MyQuiz";

    private NotifyingAsyncQueryHandler mHandler;
    ArrayList<Quiz> items;

    private QuizDetailAdapter mAdapter;

    // http://d.hatena.ne.jp/ktdk/20110309/1299681439

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);

        // setListAdapter(new ArrayAdapter<String>(getActivity(),
        // android.R.layout.simple_list_item_1, ITEM));

        items = new ArrayList<Quiz>();

        mAdapter = new QuizDetailAdapter(getActivity(), R.layout.list_quiz_detail, items);

        // SQL文の生成
        String selection = Quizs.ANSWER_STATE + " = -1" + " AND " +
                Quizs.Item_ID + " IS NOT NULL" + " AND " +// NULLでないもの
                Quizs.QUIZ_TYPE + " = 2";// 画像つき4択クイズを示す
        
        String[] args = new String[] {
                Constants.NotAnsweredState.toString() // -1 (まだ答えてないクイズ)
        };
        
        String userId = ContextUtil.getUserId(getActivity());
        mHandler = new NotifyingAsyncQueryHandler(this.getActivity().getContentResolver(), this);
        mHandler.startQuery(
                QuizQuery._TOKEN,
                null,
                Users.buildUsersQuizUri(userId),
                QuizQuery.PROJECTION,
                selection,
                null, // args
                Quizs.DEFAULT_SORT
                );

        // add Header
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View header = inflater.inflate(R.layout.list_quiz_detail_header, null, false);
        getListView().addHeaderView(header);

        setListAdapter(mAdapter);
        
        Log.d(TAG, "Activity OK");
    }

    /**
     * リストの項目をタップしたら実行される
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        
        if( 0 == position) {
            
            Intent intent = new Intent(getActivity(), QuizCreatorList.class);
            intent.putExtra("status", "create");
            
            startActivity(intent);
        }else {
            // positionは"Create Quiz"も含んでいるのでこうする
            Quiz item = items.get(position - 1);

            Intent intent = new Intent(getActivity(), QuizCreatorList.class);

            // Extra作成
            intent.putExtra("status", "edit");
            for (int i = 0; i < item.getQuizId().length; i++) {
                intent.putExtra("id" + i, item.getQuizId()[i]);
                intent.putExtra("name" + i, item.getName()[i]);
            }
            intent.putExtra("answer", item.getAnswer());
            intent.putExtra("author", item.getAuthor());

            startActivity(intent);
        }


    }

    /** AsyncQueryListenerが完了したらコール */
    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {

        switch (token) {
            /*
             * 基本的な情報をまず収集する
             */
            case QuizQuery._TOKEN:

                if (!cursor.moveToFirst()) {
                    return;
                }
                
                // 最初に必要なぶんだけ確保する
                items.ensureCapacity(cursor.getCount());
                while(items.size() < cursor.getCount()) {
                    items.add(null);
                }

                for (int i = 0; i < cursor.getCount(); i++) {
                    // Quiz一覧DBから
                    String quizId = cursor.getString(QuizQuery.Quiz_ID);
                    
                    int quiztype = cursor.getInt(QuizQuery.QUIZ_TYPE);
                    String quizItemId = cursor.getString(QuizQuery.ITEM_ID);
                    
                    // クイズの作成日時
                    
                    long date = cursor.getLong(QuizQuery.CREATE_TIME);
                    
                    // クイズの名前(どれを選ばせるか)
                    String quizTargetName = cursor.getString(QuizQuery.QUIZ_CONTENT);
                    
                    // クイズの製作者
                    String quizAuthor = cursor.getString(QuizQuery.QUIZ_AUTHOR);
                    
                    // クイズの答え(1,2,3,4 → 配列の0,1,2,3 に置き換え)
                    int quizAnswer = cursor.getInt(QuizQuery.QUIZ_ANSWER) - 1;
                    
                    // クイズの画像情報を受け取る部分
                    Uri choiceuri = Quizs.buildQuizChoicesUri(quizId);
                    mHandler.startQuery(
                            ChoiceQuery._TOKEN, 
                            i,
                            choiceuri,
                            ChoiceQuery.PROJECTION, 
                            null,
                            null,
                            Choices.DEFAULT_SORT
                            );
                    
                    Quiz q = new Quiz();
                    
                    String[] quizname = {
                            quizTargetName,
                            quizTargetName,
                            quizTargetName,
                            quizTargetName
                    };
                    
                    q.setQuizId(null);
                    q.setName(quizname);
                    q.setAuthor(quizAuthor);
                    q.setAnswer(quizAnswer);
                    q.setCreatedAt(new Date(date));
                    
                    items.set(i, q);

                    cursor.moveToNext();
                }

                cursor.close();

                break;
                
            /*
             * 画像情報を取得後こちらが呼ばれる
             */
            case ChoiceQuery._TOKEN:
                
                if (!cursor.moveToFirst()) {
                    return;
                }
                
                String[] imageUrls = new String[4];
                for (int i = 0; i < cursor.getCount(); i++) {
                    Integer num = cursor.getInt(ChoiceQuery.NUMBER);
                    imageUrls[num - 1] = cursor.getString(ChoiceQuery.Choice_Content);
                    
                    cursor.moveToNext();
                }
                
                int index = Integer.valueOf(cookie.toString());
                Quiz temp = items.get(index);
                
                temp.setQuizId(imageUrls);
                
                items.set(index, temp);
                
                cursor.close();
                
                mAdapter.notifyDataSetChanged();
                
                break;

            default:
                break;
        }
    }

    // なんやねんこれは・・・
    private interface QuizQuery {
        public static String[] PROJECTION = {
                Quizs.QUIZ_ID, // 0
                Quizs.QUIZ_TYPE, // 1
                Quizs.QUIZ_CONTENT, // 2
                Quizs.FILE_TYPE, // 3
                Quizs.PHOTO_URL, // 4
                Quizs.ANSWER, // 5
                Quizs.Item_ID, // 6
                Quizs.AUTHOR_ID, // 7
                Quizs.CREATE_TIME //8
        };
        public static int _TOKEN = 1;

        // Projectionの順番らしい
        int Quiz_ID = 0;
        int QUIZ_TYPE = 1;
        int QUIZ_CONTENT = 2;
        int QUIZ_ANSWER = 5;
        int ITEM_ID = 6;
        int CREATE_TIME = 8;
        int QUIZ_AUTHOR = 7;
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
