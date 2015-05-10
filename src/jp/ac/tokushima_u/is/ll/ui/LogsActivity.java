package jp.ac.tokushima_u.is.ll.ui;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Items;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Users;
import jp.ac.tokushima_u.is.ll.util.ContextUtil;
import jp.ac.tokushima_u.is.ll.util.UIUtils;

/**
 * {@link Activity} that displays details about a specific
 * {@link Tracks#TRACK_ID}, as requested through {@link Intent#getData()}.
 */
public class LogsActivity extends TabActivity {

    public static final String EXTRA_FOCUS_TAG = "jp.ac.tokushima_u.is.ll.extra.FOCUS_TAG";

    public static final String TAG_MY_LOGA= "My Logs";
    public static final String TAG_LATEST_LOGS = "Latest Logs";
    public static final String TAG_SEARCH = "Search";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        setupMyLogsTab();
        setupLatestLogsTab();

        // Show specific focus tag when requested, otherwise default
        String focusTag = getIntent().getStringExtra(EXTRA_FOCUS_TAG);
        if (focusTag == null) focusTag = TAG_MY_LOGA;

        getTabHost().setCurrentTabByTag(focusTag);

    }


	private void setupMyLogsTab() {
		final TabHost host = getTabHost();
		final Uri useritemsUri = Users.buildUsersItemUri(ContextUtil.getUserId(this));
		final Intent intent = new Intent(Intent.ACTION_VIEW, useritemsUri);
		intent.addCategory(Intent.CATEGORY_TAB);

		host.addTab(host.newTabSpec(TAG_MY_LOGA)
				.setIndicator(buildIndicator(R.string.my_objects_title))
				.setContent(intent));

	}

    private void setupLatestLogsTab() {
        final TabHost host = getTabHost();
        final Uri itemsUri = Items.CONTENT_URI;

        final Intent intent = new Intent(Intent.ACTION_VIEW, itemsUri);
        intent.addCategory(Intent.CATEGORY_TAB);

        // Vendors content comes from reused activity
        host.addTab(host.newTabSpec(TAG_LATEST_LOGS)
                .setIndicator(buildIndicator(R.string.latest_objects_title))
                .setContent(intent));
    }

    /**
     * Build a {@link View} to be used as a tab indicator, setting the requested
     * string resource as its label.
     */
    private View buildIndicator(int textRes) {
        final TextView indicator = (TextView) getLayoutInflater().inflate(R.layout.tab_indicator,
                getTabWidget(), false);
        indicator.setText(textRes);
        return indicator;
    }


//    /** Handle "home" title-bar action. */
    public void onHomeClick(View v) {
        UIUtils.goHome(this);
    }

    /** Handle "search" title-bar action. */
    public void onSearchClick(View v) {
        UIUtils.goSearch(this);
    }

    public void onMapClick(View v) {
    	final Intent intent = new Intent(LogsActivity.this, MapTestActivity.class);
		this.startActivity(intent);
    }

}
