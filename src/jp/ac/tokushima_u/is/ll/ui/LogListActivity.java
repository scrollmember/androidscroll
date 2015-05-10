package jp.ac.tokushima_u.is.ll.ui;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Items;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Notifys;
import jp.ac.tokushima_u.is.ll.ui.nav.nav;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.BitmapUtil;
import jp.ac.tokushima_u.is.ll.util.Constants;
import jp.ac.tokushima_u.is.ll.util.ContextUtil;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler.AsyncQueryListener;
import jp.ac.tokushima_u.is.ll.util.UIUtils;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * {@link ListActivity} that displays a set of {@link Items}, as requested
 * through {@link Intent#getData()}.
 */
public class LogListActivity extends ListActivity implements AsyncQueryListener,OnScrollListener {

	private ItemsAdapter mAdapter = new ItemsAdapter();

	private NotifyingAsyncQueryHandler mHandler;
	private Handler mMessageQueueHandler = new Handler();
	private int lastItem = 0;
	
	private long start;
	private long stop;

	private int pageSize = 10;
	
	private Uri itemsUri = null;
	
	private int QueryTokenInit = R.id.btn_qa;
	private int QueryTokenMore = R.id.txt_query;
	private boolean isQuerying = true;
	private boolean isAll = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		start = Calendar.getInstance().getTimeInMillis();
		super.onCreate(savedInstanceState);

		if (!getIntent().hasCategory(Intent.CATEGORY_TAB)) {
			setContentView(R.layout.activity_logs_list);

			final String customTitle = getIntent().getStringExtra(
					Intent.EXTRA_TITLE);
			((TextView) findViewById(R.id.title_text))
					.setText(customTitle != null ? customTitle : getTitle());

		} else {
			setContentView(R.layout.activity_log_list_content);
		}

		mHandler = new NotifyingAsyncQueryHandler(getContentResolver(), this);
		
//		double lat = Constants.defaultValue;
//		double lng = Constants.defaultValue;
		final Intent intent = getIntent();
		final String action = intent.getAction();
		itemsUri = intent.getData();
		if (Intent.ACTION_SEARCH.equals(action)) {
//			lat = intent.getDoubleExtra("lat", Constants.defaultValue);
//			lng = intent.getDoubleExtra("lng", Constants.defaultValue);

			String notifyId = intent.getStringExtra("notifyId");
			if (notifyId != null) {
				Uri uri = Notifys.buildNotifyUri(notifyId);
				ContentValues cv = new ContentValues();
				cv.put(Notifys.Feedback, Notifys.NOTIFY_FEEDBACK);
				cv.put(Notifys.SYNC_TYPE, Notifys.SYNC_TYPE_CLIENT_UPDATE);
				cv.put(Notifys.UPDATE_TIME, Calendar.getInstance().getTimeInMillis());
				this.mHandler.startUpdate(uri, cv);

				NotificationManager nm = (NotificationManager) this
						.getSystemService(NOTIFICATION_SERVICE);
				nm.cancel(Constants.LogsNotificationID);
			}
			String mQuery = intent.getStringExtra(SearchManager.QUERY);
			itemsUri = Items.buildItemSearchUri(mQuery);
			this.findViewById(R.id.title_compass).setVisibility(View.VISIBLE);
		}

//		String[] projection;
		// if (!Sessions.isSearchUri(sessionsUri)) {
		// mAdapter = new SessionsAdapter(this);
		// projection = SessionsQuery.PROJECTION;
		//
		// } else {
//		projection = ItemsQuery.PROJECTION;
		// }

		setListAdapter(mAdapter);
		// If caller launched us with specific track hint, pass it along when
		// launching session details.
		// mTrackUri =
		// intent.getParcelableExtra(SessionDetailActivity.EXTRA_TRACK);

	
		mHandler.startQuery(QueryTokenInit, null, itemsUri, ItemsQuery.PROJECTION, Items.DISABLED + "!=? ",
				new String[] { "1" }, Items.DEFAULT_SORT+" limit "+pageSize);
		this.getListView().setOnScrollListener(this);
		
//		mHandler.startQuery(itemsUri, projection, Items.DISABLED + "!=? ",
//				new String[] { "1" }, Items.DEFAULT_SORT+" limit 10");
	}

	/** {@inheritDoc} */
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		this.isQuerying = false;
		this.findViewById(R.id.progress_logs_load_more).setVisibility(View.GONE);
		this.findViewById(R.id.progress_logs_loading).setVisibility(View.GONE);
		try {
			if (QueryTokenInit == token
					&& (cursor == null || cursor.getCount() == 0)) {
				this.findViewById(R.id.logs_empty).setVisibility(View.VISIBLE);
				return;
			}
			
			if(cursor.getCount()<pageSize)
				this.isAll = true;
			
			while(cursor.moveToNext()){
				this.mAdapter.items.add(new LogItem(cursor));
			}
			
			mAdapter.notifyDataSetChanged();
		} finally {
			if (cursor != null)
				cursor.close();
		}
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		mMessageQueueHandler.post(mRefreshSessionsRunnable);
	}

	@Override
	protected void onPause() {
		mMessageQueueHandler.removeCallbacks(mRefreshSessionsRunnable);
		super.onPause();
	}

	/** {@inheritDoc} */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final LogItem item = (LogItem) mAdapter.getItem(position);
		final Uri itemUri = Items.buildItemUri(item.getItemId());
		final Intent intent = new Intent(Intent.ACTION_VIEW, itemUri);
		startActivity(intent);
	}

	/** Handle "home" title-bar action. */
	public void onHomeClick(View v) {
		UIUtils.goHome(this);
	}

	/** Handle "search" title-bar action. */
	public void onSearchClick(View v) {
		UIUtils.goSearch(this);
	}

	public void onNavigateClick(View v){
		Intent intent = new Intent(LogListActivity.this, nav.class);
		intent.putExtra("userEmail", ContextUtil.getUsername(this));
		intent.putExtra("userPassword", ContextUtil.getPassword(this));
		startActivity(intent);
	}
	
	/**
	 * {@link CursorAdapter} that renders a {@link SessionsQuery}.
	 */
	private class ItemsAdapter extends BaseAdapter {
		List<LogItem> items = new LinkedList<LogItem>();

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return items.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if(convertView == null){
				convertView = getLayoutInflater().inflate(R.layout.list_item_log, parent,
						false);
				viewHolder = new ViewHolder();
				viewHolder.imageView = (ImageView) convertView
					.findViewById(R.id.log_image);
				viewHolder.titles = (TextView) convertView
					.findViewById(R.id.log_titles);
				convertView.setTag(viewHolder);
			}else{
				viewHolder = (ViewHolder)convertView.getTag();
			}
			
			LogItem item = items.get(position);
			
			Bitmap bitmap = null;
			
			if(Items.SYNC_TYPE_CLIENT_INSERT.equals(item.getSyncType())){
				try{
					if(item.getAttached()!=null)
						bitmap = BitmapUtil.zoomImage(BitmapFactory.decodeFile(item.getAttached()), 80, 60);
				}catch(Exception e){
					
				}
			}else{
				if(item.getPhotoUrl()!=null&&item.getPhotoUrl().length()>0)
					bitmap = BitmapUtil
						.getBitmap(LogListActivity.this, item.getPhotoUrl(), ApiConstants.SmallestSizePostfix);
			}

			if (bitmap != null)
				viewHolder.imageView.setImageBitmap(bitmap);
			else
				viewHolder.imageView.setImageResource(R.drawable.noimage);

			viewHolder.titles.setText(item.getTitle());
			return convertView;
		}
	}

	/**
	 * {@link CursorAdapter} that renders a {@link SearchQuery}.
	 */
	// private class SearchAdapter extends CursorAdapter {
	// public SearchAdapter(Context context) {
	// super(context, null);
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public View newView(Context context, Cursor cursor, ViewGroup parent) {
	// return getLayoutInflater().inflate(R.layout.list_item_session, parent,
	// false);
	// }
	//
	// /** {@inheritDoc} */
	// @Override
	// public void bindView(View view, Context context, Cursor cursor) {
	// ((TextView) view.findViewById(R.id.session_title)).setText(cursor
	// .getString(SearchQuery.TITLE));
	//
	// final String snippet = cursor.getString(SearchQuery.SEARCH_SNIPPET);
	// final Spannable styledSnippet = buildStyledSnippet(snippet);
	// ((TextView)
	// view.findViewById(R.id.session_subtitle)).setText(styledSnippet);
	//
	// final boolean starred = cursor.getInt(SearchQuery.STARRED) != 0;
	// final CheckBox starButton = (CheckBox)
	// view.findViewById(R.id.star_button);
	// starButton.setVisibility(starred ? View.VISIBLE : View.INVISIBLE);
	// starButton.setChecked(starred);
	// }
	// }

	private Runnable mRefreshSessionsRunnable = new Runnable() {
		public void run() {
			if (mAdapter != null) {
				// This is used to refresh session title colors.
				mAdapter.notifyDataSetChanged();
			}

			// Check again on the next quarter hour, with some padding to
			// account for network
			// time differences.
			long nextQuarterHour = (SystemClock.uptimeMillis() / 900000 + 1) * 900000 + 5000;
			mMessageQueueHandler.postAtTime(mRefreshSessionsRunnable,
					nextQuarterHour);
		}
	};
	
	static class ViewHolder{
		ImageView imageView;
		TextView titles;
	}
	
	class LogItem{
		private Long id;
		private String itemId;
		private String photoUrl;
		private String title;
		private Integer syncType;
		private String attached;
		
		public LogItem(){
			
		}
		
		public LogItem(Cursor cursor){
			if(cursor!=null){
				this.id = cursor.getLong(ItemsQuery._ID);
				this.itemId = cursor.getString(ItemsQuery.ITEM_ID);
				this.photoUrl = cursor.getString(ItemsQuery.PHOTO_URL);
				this.title = cursor.getString(ItemsQuery.TITLES);
				this.syncType = cursor.getInt(ItemsQuery.SYNC_TYPE);
				this.attached = cursor.getString(ItemsQuery.ATTACHED);
			}
		}
		
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getItemId() {
			return itemId;
		}
		public void setItemId(String itemId) {
			this.itemId = itemId;
		}
		public String getPhotoUrl() {
			return photoUrl;
		}
		public void setPhotoUrl(String photoUrl) {
			this.photoUrl = photoUrl;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}

		public Integer getSyncType() {
			return syncType;
		}

		public void setSyncType(Integer syncType) {
			this.syncType = syncType;
		}

		public String getAttached() {
			return attached;
		}

		public void setAttached(String attached) {
			this.attached = attached;
		}
	}
	

	/** {@link Sessions} query parameters. */
	private interface ItemsQuery {
		String[] PROJECTION = { BaseColumns._ID, Items.ITEM_ID,
				Items.NICK_NAME, Items.PHOTO_URL, Items.NOTE, Items.TITLES,
				Items.UPDATE_TIME, Items.SYNC_TYPE, Items.ATTACHED };

		int _ID = 0;
		int ITEM_ID = 1;
		int NICK_NAME = 2;
		int PHOTO_URL = 3;
		int NOTE = 4;
		int TITLES = 5;
		int UPDATE_TIME = 6;
		int SYNC_TYPE = 7;
		int ATTACHED = 8;
	}


	@Override
	public void onScrollStateChanged(AbsListView view, int state) {
		if (lastItem == this.mAdapter.getCount()
				&& state == OnScrollListener.SCROLL_STATE_IDLE && !this.isQuerying && !this.isAll) {
			int start = this.mAdapter.getCount();
			mHandler.startQuery(QueryTokenMore, null, itemsUri, ItemsQuery.PROJECTION, Items.DISABLED + "!=? ",
					new String[] { "1" }, Items.DEFAULT_SORT+" limit "+start+", "+ pageSize);
			this.isQuerying = true;
			this.findViewById(R.id.progress_logs_load_more).setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		lastItem = firstVisibleItem + visibleItemCount;
	}
}
