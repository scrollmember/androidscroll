
package jp.ac.tokushima_u.is.ll.ui.quiz;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.ui.HomeActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class FourSelectedImageQuiz extends SherlockFragmentActivity{
	private final Handler handler = new Handler();
	   
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setTheme(R.style.Theme_Sherlock);
        setContentView(R.layout.fourselectedimagequiz);
        Intent i  = this.getIntent();
//        this.getSherlock().getActionBar().setCustomView(R.layout.naviagte_home);
//        this.getSupportActionBar().setIcon(R.drawable.ic_title_home);
        
      //Inflate the custom view
        
//        this.getSupportActionBar().setDisplayShowCustomEnabled(true);
//        this.getSupportActionBar().setDisplayShowHomeEnabled(false);
//        this.getSupportActionBar().setDisplayShowTitleEnabled(false);
        this.getSupportActionBar().setIcon(R.drawable.ic_title_home_light);
        this.getSupportActionBar().setTitle(R.string.quiz_Activity_name);
        
//        this.getSherlock().setContentView(R.layout.title_home);
        
        final String quizId = i.getStringExtra("quizid");

        findViewById(R.id.button_answer).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                FourSelectedImageFragment fsi = new FourSelectedImageFragment();
                int focusId = fsi.getImageViewFocus();
                
                if (focusId == -1) {
                    Toast.makeText(getApplicationContext(), "Please choose answer!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(FourSelectedImageQuiz.this,
                            FourSelectedImageQuizAnswer.class);
                    intent.putExtra("FOCUS_OBJECT_ID", fsi.getImageViewFocus());
                    intent.putExtra("IS_ANSWERED", Boolean.TRUE);
                    intent.putExtra("quizid", quizId);
                    startActivity(intent);
                }
            }
        });
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//    	   getSupportMenuInflater().inflate(R.menu.menu_quiz, menu);
//
//           final MenuItem refresh = (MenuItem) menu.findItem(R.id.menu_refresh);
//           refresh.setOnMenuItemClickListener(new OnMenuItemClickListener() {
//               // on selecting show progress spinner for 1s
//               public boolean onMenuItemClick(MenuItem item) {
//                   // item.setActionView(R.layout.progress_action);
//                   handler.postDelayed(new Runnable() {
//                       public void run() {
//                           refresh.setActionView(null);
//                       }
//                   }, 1000);
//                   return false;
//               }
//           });
//           return super.onCreateOptionsMenu(menu);
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
