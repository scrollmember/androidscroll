/**
 * SearchActivity
 * 
 * LogListActivityから引用された部分が大多数を占めている。
 * 引用元が非常にウンコなので、このソースを参考にするくらいなら、
 * MyQuizFragment - QuizDetailAdapterあたりの連携を見たほうが良いと思う
 */

package jp.ac.tokushima_u.is.ll.sphinx;

import java.util.LinkedList;
import java.util.List;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Items;
import jp.ac.tokushima_u.is.ll.sphinx.classes.LogItem;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.BitmapUtil;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler.AsyncQueryListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockActivity;

public class SearchActivity extends SherlockActivity implements AsyncQueryListener {
    @SuppressWarnings("unused")
    private static final String TAG = SearchActivity.class.getSimpleName();
    private final SearchActivity self = this;

    private int QUERYTOKEN = 1;

    private ItemsAdapter mAdapter;
    private NotifyingAsyncQueryHandler mHandler;

    private EditText searchBox;
    private ImageButton searchButton;
    private ListView searchResult;

    private int bundleListPos;
    private String bundleItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sphinx_search);

        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            bundleListPos = bundle.getInt("listPosition");
            bundleItemId = bundle.getString("itemId");
        }

        searchButton = (ImageButton) findViewById(R.id.button_search);
        searchBox = (EditText) findViewById(R.id.edittext_searchbox);
        searchResult = (ListView) findViewById(R.id.list_searchresult);

        mAdapter = new ItemsAdapter();
        mHandler = new NotifyingAsyncQueryHandler(getContentResolver(), this);

        searchButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                search();
            }
        });

        searchBox.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search();
                }

                return true;
            }
        });

        searchResult.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                LogItem item = mAdapter.items.get(position);

                Intent intent = new Intent();
                intent.putExtra("listPosition", bundleListPos);
                intent.putExtra("itemId", item.getPhotoUrl());
                intent.putExtra("itemTitle", item.getTitle());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

    /**
     * Listを生成するアダプター (ソース読んでる人、これが正しいとは言えないけれど、 こういうもんだと思って受け流したほうがいいですよ)
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
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_log, parent,
                        false);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView) convertView
                        .findViewById(R.id.log_image);
                viewHolder.titles = (TextView) convertView
                        .findViewById(R.id.log_titles);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            LogItem item = items.get(position);

            Bitmap bitmap = null;

            if (Items.SYNC_TYPE_CLIENT_INSERT.equals(item.getSyncType())) {
                try {
                    if (item.getAttached() != null)
                        bitmap = BitmapUtil.zoomImage(BitmapFactory.decodeFile(item.getAttached()),
                                80, 60);
                } catch (Exception e) {

                }
            } else {
                if (item.getPhotoUrl() != null && item.getPhotoUrl().length() > 0)
                    bitmap = BitmapUtil
                            .getBitmap(getApplicationContext(),
                                    item.getPhotoUrl(),
                                    ApiConstants.SmallSizePostfix);
            }

            if (bitmap != null)
                viewHolder.imageView.setImageBitmap(bitmap);
            else
                viewHolder.imageView.setImageResource(R.drawable.noimage);

            viewHolder.titles.setText(item.getTitle());
            return convertView;
        }
    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        // tokenの正当性判断は省略
        
        if(!cursor.moveToFirst()) {
            return;
        }

        while (cursor.moveToNext()) {

            Bitmap b = BitmapUtil
                    .getBitmap(getApplicationContext(),
                            cursor.getString(ItemsQuery.PHOTO_URL),
                            ApiConstants.SmallSizePostfix);
            
            if (b != null) {
                LogItem item = new LogItem(
                        cursor.getLong(ItemsQuery._ID),
                        cursor.getString(ItemsQuery.ITEM_ID),
                        cursor.getString(ItemsQuery.PHOTO_URL),
                        cursor.getString(ItemsQuery.TITLES),
                        cursor.getInt(ItemsQuery.SYNC_TYPE),
                        cursor.getString(ItemsQuery.ATTACHED)
                        );
                mAdapter.items.add(item);
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        mAdapter.notifyDataSetChanged();
    }

    /**
     * 検索スタート
     */
    private void search() {
        mAdapter.items.clear();

        String query = searchBox.getText().toString();
        Uri itemsUri = Items.buildItemSearchUri(query);

        if(query == null) {
            query = "";
        }
        String selection = Items.DISABLED + " !=1 AND " + Items.PHOTO_URL + " is not null";

        mHandler.startQuery(
                QUERYTOKEN,
                null,
                itemsUri,
                ItemsQuery.PROJECTION,
                selection,
                null,
                Items.DEFAULT_SORT
                );
        searchResult.setAdapter(mAdapter);
    }

    static class ViewHolder {
        ImageView imageView;
        TextView titles;
    }

    /*
     * マジでこういう設計にしたアホは死ねばいいと思います
     */
    private interface ItemsQuery {
        String[] PROJECTION = {
                BaseColumns._ID,
                Items.ITEM_ID,
                Items.NICK_NAME,
                Items.PHOTO_URL,
                Items.NOTE,
                Items.TITLES,
                Items.UPDATE_TIME,
                Items.SYNC_TYPE,
                Items.ATTACHED
        };

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
}
