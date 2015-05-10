
package jp.ac.tokushima_u.is.ll.sphinx;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.sphinx.fragment.MyQuizListFragment;
import jp.ac.tokushima_u.is.ll.sphinx.fragment.QuizInboxListFragment;
import jp.ac.tokushima_u.is.ll.ui.HomeActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity implements ActionBar.TabListener {

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    // スワイプで展開できる面の数
    private static final int PAGE_SIZE = 2;

    // 各FragmentのID
    private static final int MY_QUIZ_LIST_FRAGMENT = 0;
    private static final int QUIZ_INBOX_LIST_FRAGMENT = 1;

    private MyAdapter mAdapter;

    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sphinx_main);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setIcon(R.drawable.ic_sphinx_launcher);
        actionBar.setTitle("SCROLL Sphinx");

        mAdapter = new MyAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }

        });

        for (int i = 0; i < mAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab()
                    .setText(mAdapter.getPageTitle(i))
                    .setTabListener(this));
        }

    }

    private static class MyAdapter extends FragmentStatePagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {

            ListFragment fragment = null;

            // ページごとにそれぞれの画面を作成する
            switch (pos) {
                case MY_QUIZ_LIST_FRAGMENT:
                    fragment = new MyQuizListFragment();
                    break;

                case QUIZ_INBOX_LIST_FRAGMENT:
                    fragment = new QuizInboxListFragment();
                    break;

                default:
                    fragment = null;
                    break;
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return PAGE_SIZE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case MY_QUIZ_LIST_FRAGMENT:
                    return MyQuizListFragment.NAME;
                case QUIZ_INBOX_LIST_FRAGMENT:
                    return QuizInboxListFragment.NAME;
                default:
                    return null;
            }
        }

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getSupportActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getSupportActionBar().getSelectedNavigationIndex());
    }

    // @Override
    // public boolean onCreateOptionsMenu(Menu menu) {
    // // メニューまだ要らん
    // // getMenuInflater().inflate(R.menu.activity_main, menu);
    // return false;
    // }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }

    /**
     * ActionBarのアイコンをタップした時の挙動
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

}
