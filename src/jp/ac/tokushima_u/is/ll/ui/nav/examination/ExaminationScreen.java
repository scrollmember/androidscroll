package jp.ac.tokushima_u.is.ll.ui.nav.examination;

import java.util.ArrayList;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.ui.navTask.MyListView;
import jp.ac.tokushima_u.is.ll.ui.navTask.TestActivity;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TabHost.TabSpec;

public class ExaminationScreen extends TabActivity {

	/** Id for the toggle rotation menu item */
	private static final int TOGGLE_ROTATION_MENU_ITEM = 0;

	/** Id for the toggle lighting menu item */
	private static final int TOGGLE_LIGHTING_MENU_ITEM = 1;
	private WebView webview;
	/** The list view */
	private MyListView mListView;

	private static class Contact {

		String mName;

		String mNumber;

		public Contact(final String name, final String number) {
			mName = name;
			mNumber = number;
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Tab();

		// listView setting
		list();

	}

	private void list() {
		// TODO Auto-generated method stub
		final ArrayList<Contact> contacts = createContactList(20);
		final MyAdapter adapter = new MyAdapter(this, contacts);

		mListView = (MyListView) findViewById(R.id.my_list);
		mListView.setAdapter(adapter);

		webview = new WebView(this);
		webview.getSettings().setLoadWithOverviewMode(true);
		webview.getSettings().setUseWideViewPort(true);
		webview.loadUrl("http://www.nihongokentei.jp/about/aboutnk/about.html");
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.tab1);
		linearLayout.addView(webview);
		webview.reload();

		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(final AdapterView<?> parent,
					final View view, final int position, final long id) {
				final String message = "Taskを開始します";
				// Toast.makeText(TestActivity.this, message,
				// Toast.LENGTH_SHORT)
				// .show();
				Intent Item = new Intent(ExaminationScreen.this,
						TestActivity.class);
				if (position == 0) {
					Item.putExtra("japaneselevel", "1");
				}
				if (position == 1) {
					Item.putExtra("japaneselevel", "2");
				}
				if (position == 2) {
					Item.putExtra("japaneselevel", "3");
				}
				if (position == 3) {
					Item.putExtra("japaneselevel", "4");
				}
				if (position == 4) {
					Item.putExtra("japaneselevel", "5");
				}
				startActivity(Item);

				finish();

			}
		});

		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(final AdapterView<?> parent,
					final View view, final int position, final long id) {
				final String message = "OnLongClick: "
						+ contacts.get(position).mName;
				// Toast.makeText(TestActivity.this, message,
				// Toast.LENGTH_SHORT)
				// .show();
				return true;
			}
		});

	}

	// Tabの設定
	public void Tab() {
		TabHost tabHost = getTabHost();
		LayoutInflater inflater = getLayoutInflater();
		inflater.inflate(R.layout.examination2, tabHost.getTabContentView(),
				true);

		TabSpec tab1 = tabHost.newTabSpec("Registration");
		tab1.setIndicator("Your Japanese Level");
		tab1.setContent(R.id.my_list);
		inflater.inflate(R.layout.examination1, tabHost.getTabContentView(),
				true);
		TabSpec tab2 = tabHost.newTabSpec("Registration2");
		tab2.setIndicator("About Japanese Examination");
		tab2.setContent(R.id.tab1);
		tabHost.addTab(tab1);
		tabHost.addTab(tab2);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case TOGGLE_ROTATION_MENU_ITEM:
			mListView.enableRotation(!mListView.isRotationEnabled());
			return true;

		case TOGGLE_LIGHTING_MENU_ITEM:
			mListView.enableLight(!mListView.isLightEnabled());
			return true;

		default:
			return false;
		}
	}

	private ArrayList<Contact> createContactList(final int size) {
		final ArrayList<Contact> contacts = new ArrayList<Contact>();

		contacts.add(new Contact("Japanese Examination", "nihongo 5級"));
		contacts.add(new Contact("Japanese Examination", "nihongo 4級"));
		contacts.add(new Contact("Japanese Examination", "nihongo 3級"));
		contacts.add(new Contact("Japanese Examination", "nihongo 2級"));
		contacts.add(new Contact("Japanese Examination", "nihongo 1級"));
		return contacts;
	}

	/**
	 * Adapter class to use for the list
	 */
	private static class MyAdapter extends ArrayAdapter<Contact> {

		/** Re-usable contact image drawable */
		private final Drawable contactImage;

		/**
		 * Constructor
		 * 
		 * @param context
		 *            The context
		 * @param contacts
		 *            The list of contacts
		 */
		public MyAdapter(final Context context,
				final ArrayList<Contact> contacts) {
			super(context, 0, contacts);
			contactImage = context.getResources().getDrawable(
					R.drawable.taskicon);
		}

		@Override
		public View getView(final int position, final View convertView,
				final ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				view = LayoutInflater.from(getContext()).inflate(
						R.layout.list_item, null);
			}

			final TextView name = (TextView) view
					.findViewById(R.id.contact_name);
			if (position == 14) {
				name.setText("This is a long text that will make this box big. "
						+ "Really big. Bigger than all the other boxes. Biggest of them all.");
			} else {
				name.setText(getItem(position).mName);
			}

			final TextView number = (TextView) view
					.findViewById(R.id.contact_number);
			number.setText(getItem(position).mNumber);

			final ImageView photo = (ImageView) view
					.findViewById(R.id.contact_photo);
			photo.setImageDrawable(contactImage);

			return view;
		}
	}

}
