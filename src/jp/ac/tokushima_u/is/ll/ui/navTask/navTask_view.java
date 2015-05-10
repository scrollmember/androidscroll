package jp.ac.tokushima_u.is.ll.ui.navTask;

import java.util.ArrayList;
import java.util.HashMap;

import jp.ac.tokushima_u.is.ll.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class navTask_view extends Activity {
	// リストビューに表示するTaskのレベル
	String[] c_code = new String[] { "level:3", "level:2", "level:4", "level3" };
	// リストビューに表示するTaskの名前
	String[] c_name = new String[] { "通帳を作りに行く", "病院にいく", "図書館に行く", "博物館に行く" };
	String[][] task_script = new String[][] {
			{ "印鑑と通帳とお金を持って銀行にいきましょう。銀行に着いたら職員に質問しましょう",
					"口座が開設できたらATMでお金を預けてみましょう" + "" }, { "診察券をもらう", "薬をもらう" } };
	String[] test_knowledge1 = new String[] { "おでん" };
	String[] test_knowledge2 = new String[] { "大根", "ちくわ", "スープ" };
	String[] test_knowledge3 = new String[] { "大根", "鍋" };
	// String[][] test_knowledge=new
	// String[][]{{"おでん"},{"大根","ちくわ","スープ"},{"大根","鍋"}};
	double[] Tasklatw = new double[20];
	double[] Tasklngw = new double[20];
	String[] test1 = new String[10];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.navtask_main);

		// test
		test();

		// データを格納するためのArrayListを宣言
		ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

		// 繰り返し
		for (int i = 0; i < 4; i++) {
			HashMap<String, String> map = new HashMap<String, String>();

			// Task_level
			map.put("code", c_code[i]);

			// Task_name
			map.put("name", c_name[i]);

			// 作成したmapをdataに追加
			data.add(map);
		}
		/*
		 * 作成したdataとカスタマイズしたレイアウトrow.xmlを 紐付けたSimpleAdapterを作成する
		 */
		SimpleAdapter sa = new SimpleAdapter(this, data, R.layout.row,
				new String[] { "code", "name" }, new int[] { R.id.countrycode,
						R.id.countryname });

		ListView lv = (ListView) findViewById(R.id.listview);
		lv.setAdapter(sa);

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Toast.makeText(navTask_view.this, c_name[position],
						Toast.LENGTH_SHORT);
				String test = c_name[position];
				if (position == 0) {
//					Intent logintent = new Intent(navTask_view.this,
//							Taskscript_select.class);
					Intent logintent = new Intent(navTask_view.this,
							task_screen.class);
					// Intent logintent = new Intent(navTask_view.this,
					// Task_main.class);
					logintent.putExtra("ID", 1);
					logintent.putExtra("taskname", c_name[position]);
					logintent.putExtra("Tasklat", Tasklatw[0]);
					logintent.putExtra("Tasklng", Tasklngw[0]);
					for (int i = 0; i < 2; i++) {
						logintent.putExtra("taskcount", i);
						logintent.putExtra("taskscript" + i, task_script[0][i]);
					}
					logintent.putExtra("test1", test_knowledge1);
					logintent.putExtra("test2", test_knowledge2);
					logintent.putExtra("test3", test_knowledge3);

					// for(int i=0;i<test_knowledge.length;i++){
					// for(int j=0;j<test_knowledge[i].length;j++){
					// Log.e("length",String.valueOf(test_knowledge[i].length));
					//
					// test1[j]=test_knowledge[i][j];
					// Log.e("length",String.valueOf(test1[j]));
					// }
					// logintent.putExtra("test"+i,test1);
					// Log.e("length",String.valueOf(test1[0]));
					// logintent.putExtra("testcount",i);
					//
					// }
					navTask_view.this.startActivity(logintent);
				}

			}

		});

	}

	public void test() {
		Tasklatw[0] = 34.07844586944945;
		Tasklngw[0] = 134.554843;
	}

}