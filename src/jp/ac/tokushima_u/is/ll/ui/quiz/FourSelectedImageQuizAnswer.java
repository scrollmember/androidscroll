
package jp.ac.tokushima_u.is.ll.ui.quiz;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.ui.HomeActivity;
import jp.ac.tokushima_u.is.ll.ui.QuizActivity;
import jp.ac.tokushima_u.is.ll.util.Constants;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class FourSelectedImageQuizAnswer extends SherlockFragmentActivity {

    Spinner mSpinner;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setTheme(R.style.Theme_Sherlock);
        setContentView(R.layout.fourselectedimagequizanswer);

        Intent it = this.getIntent();
        final Double lat = it.getDoubleExtra("lat", Constants.defaultValue);
        final Double lng = it.getDoubleExtra("lng", Constants.defaultValue);
        // final Double alarmtype = it.getIntExtra(name, defaultValue).;

        findViewById(R.id.button_back).setOnClickListener(
                new OnClickListener() {

                    public void onClick(View v) {
                        final Intent intent = new Intent(FourSelectedImageQuizAnswer.this,
                                QuizActivity.class);
                        if (lat != Constants.defaultValue
                                && lng != Constants.defaultValue) {
                            intent.putExtra("lat", lat);
                            intent.putExtra("lng", lng);
                        }
                        // intent.putExtra("alarmType", this.alarmtype);
                        startActivity(intent);
                        finish();
                    }
                });

        findViewById(R.id.button_quiz_difficulty_submit).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // アンケートをsubmitする
                        mSpinner = (Spinner) findViewById(R.id.spinner_quiz_difficulty);

                        int selectId = mSpinner.getSelectedItemPosition();

                        if (selectId == Spinner.INVALID_POSITION
                                || selectId == 0) {
                            // 何もユーザーによって選ばれていない時、あるいは説明文にフォーカスがあるとき
                            Toast.makeText(getApplicationContext(),
                                    "Please select the quiz difficulty.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        } else {
//                            Toast.makeText(getApplicationContext(),
//                                    "itemId:" + selectId + ", Thank you!",
//                                    Toast.LENGTH_SHORT).show();
                        	//TODO
                            Toast.makeText(getApplicationContext(),
                                     "Thank you!",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // フォームをdisableする
                        mSpinner.setEnabled(false);
                        findViewById(R.id.button_quiz_difficulty_submit)
                                .setEnabled(false);
                    }
                });

    }

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

}
