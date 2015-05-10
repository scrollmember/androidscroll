package jp.ac.tokushima_u.is.ll.ui.nav;

/**
 * 
 * @author Kousuke Mouri University of Tokushima,Japan
 * 
 */
//Radar Interface
import jp.ac.tokushima_u.is.ll.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class radar extends Activity implements OnItemLongClickListener {
	// リストビューに表示するデータ
	String[] words = new String[] { "GPS:DISTANCE=10M", "GPS:DISTANCE=20M",
			"GPS:DISTANCE=30M", "GPS:DISTANCE=40M", "GPS:DISTANCE=50M",
			"GPS:DISTANCE=60M", "GPS:DISTANCE=70M", "GPS:DISTANCE=80M",
			"GPS:DISTANCE=90M", "GPS:DISTANCE=100M", "レーダーOFFモード", };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// リストアダプターを作成
		ListAdapter la = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, words);

		// 　作成したリストアダプターをリストビューにセットする
		ListView lv = (ListView) findViewById(R.id.listview);
		lv.setAdapter(la);

		// リスナーを登録する
		lv.setOnItemLongClickListener(this);
	}

	// onItemLongClickをオーバーライドする
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		Log.d("onItemClick", "position: " + String.valueOf(position));
		Toast.makeText(this, words[position] + "設定しました", Toast.LENGTH_SHORT)
				.show();
		Intent intent = new Intent();
		setResult(Activity.RESULT_OK, intent);
		int mode = Context.MODE_PRIVATE;
		SharedPreferences.Editor editor;
		SharedPreferences radar = getSharedPreferences("RADARVALUES", mode);
		editor = radar.edit();
		if (words[position] == "GPS:DISTANCE=10M") {
			int distance = 100;
			editor.putInt("distance", distance);
			editor.putInt("hantei", 1);
		} else if (words[position] == "GPS:DISTANCE=20M") {
			int distance = 200;
			editor.putInt("distance", distance);
			editor.putInt("hantei", 2);
		} else if (words[position] == "GPS:DISTANCE=30M") {
			int distance = 300;
			editor.putInt("distance", distance);
			editor.putInt("hantei", 3);
		} else if (words[position] == "GPS:DISTANCE=40M") {
			int distance = 400;
			editor.putInt("distance", distance);
			editor.putInt("hantei", 4);
		} else if (words[position] == "GPS:DISTANCE=50M") {
			int distance = 500;
			editor.putInt("distance", distance);
			editor.putInt("hantei", 5);
		} else if (words[position] == "GPS:DISTANCE=60M") {
			int distance = 600;
			editor.putInt("distance", distance);
			editor.putInt("hantei", 6);
		} else if (words[position] == "GPS:DISTANCE=70M") {
			int distance = 700;
			editor.putInt("distance", distance);
			editor.putInt("hantei", 7);

		} else if (words[position] == "GPS:DISTANCE=80M") {
			int distance = 800;
			editor.putInt("distance", distance);
			editor.putInt("hantei", 8);
		} else if (words[position] == "GPS:DISTANCE=90M") {
			int distance = 900;
			editor.putInt("distance", distance);
			editor.putInt("hantei", 9);
		} else if (words[position] == "レーダーOFFモード") {
			int distance = 0;
			editor.putInt("distance", distance);
			editor.putInt("hantei", 11);
		} else {
			int distance = 1000;
			editor.putInt("distance", distance);
			editor.putInt("hantei", 10);
		}
		editor.commit();
		finish();
		return false;
	}
}