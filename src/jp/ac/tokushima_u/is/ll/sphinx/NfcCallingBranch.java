
package jp.ac.tokushima_u.is.ll.sphinx;

import java.io.IOException;

import jp.ac.tokushima_u.is.ll.sphinx.classes.MessagedQuiz;
import jp.ac.tokushima_u.is.ll.sphinx.database.DBAdapter;
import jp.ac.tokushima_u.is.ll.sphinx.quiz.v2.FourSelectedImageQuiz;

import org.msgpack.MessagePack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.toro.android.lib.androidbeam.BeamHelper;

/**
 * このActivityはViewを生成せず、 何らかの処理をしつつ他のActivityを起動させる程度にとどまる。
 */
public class NfcCallingBranch extends Activity {
    @SuppressWarnings("unused")
    private static final String TAG = NfcCallingBranch.class.getSimpleName();
    private final NfcCallingBranch self = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();

        byte[] nfcMsg = BeamHelper.getReceivedBeam(getIntent());
        if (nfcMsg != null) {
         // データ受信できた時
            try {
                // MessagePack をUnpackする
                MessagePack msgPack = new MessagePack();
                MessagedQuiz quiz = msgPack.read(nfcMsg, MessagedQuiz.class);
                
                // データベースに登録
                DBAdapter dbAdapter = new DBAdapter(getApplicationContext());
                dbAdapter.open();
                long id = dbAdapter.create(
                        quiz.imageId[0],
                        quiz.imageId[1],
                        quiz.imageId[2],
                        quiz.imageId[3],
                        quiz.name[0],
                        quiz.name[1],
                        quiz.name[2],
                        quiz.name[3],
                        0,
                        DBAdapter.DIRECTION_INCOMING,
                        quiz.author
                        );
                dbAdapter.close();
                
                // Intent準備してActivity起動
                // FIXME 4択クイズ画面に飛ばす
                Intent intent = new Intent(self, FourSelectedImageQuiz.class);
                intent.putExtra("databaseId", id);
                intent.putExtra("status", "edit");
                intent.putExtra("answer", quiz.answer);
                intent.putExtra("author", quiz.author);
                for (int i = 0; i < 4; i++) {
                    intent.putExtra("id" + i, quiz.imageId[i]);
                    intent.putExtra("name" + i, quiz.name[i]);
                }
                startActivity(intent);
                
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            // データが存在しないとき
            
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }
}
