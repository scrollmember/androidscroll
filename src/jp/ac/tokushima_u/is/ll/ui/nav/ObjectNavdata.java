package jp.ac.tokushima_u.is.ll.ui.nav;
/**
 * 
 * @author Kousuke Mouri University of Tokushima,Japan
 * 
 */
	import java.util.ArrayList;
	import java.util.HashMap;
	import java.util.List;
	import java.util.Map;

import jp.ac.tokushima_u.is.ll.R;

	import android.app.Activity;
import android.content.Context;
	import android.content.SharedPreferences;
	import android.os.Bundle;
	import android.view.View;
	import android.widget.ExpandableListAdapter;
	import android.widget.ExpandableListView;
	import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

	public class ObjectNavdata extends Activity {
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.main3);
	        
	        // ExpandableListViewインスタンスを取得
	        ExpandableListView listview = (ExpandableListView) findViewById(R.id.list);
	        
	        // 親のリストを格納するArrayListインスタンスを生成
	        List<Map<String, Object>> parentsList = new ArrayList<Map<String,Object>>();
	        
	        // 子のリストを格納するArrayListインスタンスを生成
	        List<List<Map<String, Object>>> childrenList = new ArrayList<List<Map<String,Object>>>();

	        // 親リストのデータを格納するHashMapインスタンスを作成
	        Map<String, Object> parentData;
	        
	        // ※ここから親のデータを生成
	        SharedPreferences DATA = getSharedPreferences("ARDATA",Context.MODE_PRIVATE);  	
	        int count = DATA.getInt("COUNT",0);
			 float[] itemlat=new float[count];
		     float[] itemlng=new float[count];
			 String[] nicname= new String[count];
		     String[] title = new String[count];
		     for(int i=0;i<count;i++){
		     itemlat[i] = DATA.getFloat("lat"+i, 0);
			 itemlng[i] = DATA.getFloat("lng"+i, 0);
			 nicname[i] = DATA.getString("name"+i,"");
			 title[i] = DATA.getString("title"+i,"");
			 }
	        // 親１をparentsListに追加
	        parentData = new HashMap<String, Object>();
	        parentData.put("parent_text","LLO情報");
	        parentsList.add(parentData);
	        
	        // 親２をparentsListに追加
	        parentData = new HashMap<String, Object>();
	        parentData.put("parent_text","System");
	        parentsList.add(parentData);
	        
	        // 子データを一時的に格納するMapとListを定義
	        Map<String, Object> childData;
	        List<Map<String, Object>> childList;
	        
	        // ※ここから親１に紐付く子のデータを生成
	        
	        // 親１に紐付くに表示する子データ用インスタンスを生成
	        childList = new ArrayList<Map<String,Object>>();
	        
	        // 親１に紐付く、子１竏窒PのデータchildDataを作成しchildListに追加
	        childData = new HashMap<String, Object>();
	        childData.put("child_text", nicname[0]+":"+title[0]);
	        childList.add(childData);
	        
	        // 親１に紐付く、子１竏窒QのデータchildDataを作成しchildListに追加
	        childData = new HashMap<String, Object>();
	        childData.put("child_text",nicname[1]+":"+title[1]);
	        childList.add(childData);
	        childData = new HashMap<String, Object>();
	        childData.put("child_text",nicname[2]+":"+title[2]);
	        childList.add(childData);
	        childData = new HashMap<String, Object>();
	        childData.put("child_text",nicname[3]+":"+title[3]);
	        childList.add(childData);
	        childData = new HashMap<String, Object>();
	        childData.put("child_text",nicname[4]+":"+title[4]);
	        childList.add(childData);
	        childData = new HashMap<String, Object>();
	        childData.put("child_text",nicname[5]+":"+title[5]);
	        childList.add(childData);
	        // 親１に紐付くchildListをchildrenListに追加
	        childrenList.add(childList);
	  
	        // ※ここから親２に紐付く子のデータを生成
	        
	        // 親２に紐付くに表示する子データ用インスタンスを生成
	        childList = new ArrayList<Map<String,Object>>();
	        
	        // 親２に紐付く、子２竏窒PのデータchildDataを作成しchildListに追加
	        childData = new HashMap<String, Object>();
	        childData.put("child_text", "子２竏窒P");
	        childList.add(childData);
	        
	        // 親２に紐付く、子２竏窒QのデータchildDataを作成しchildListに追加
	        childData = new HashMap<String, Object>();
	        childData.put("child_text", "子２竏窒Q");
	        childList.add(childData);
	        
	        // 親２に紐付くchildListをchildrenListに追加
	        childrenList.add(childList);

	        // SimpleExpandableListAdapterインスタンスを生成
	        SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
	        		this,
	        		parentsList,
	        		android.R.layout.simple_expandable_list_item_1,
	        		new String []{"parent_text"},
	        		new int []{android.R.id.text1},
	        		childrenList,
	        		R.layout.raw,
	        		new String []{"child_text"},
	        		new int []{R.id.child_text}
	        );
	        
	        // 作成したアダプターをExpandableListViewにセットする
	        listview.setAdapter(adapter);
	        
	        // クリック時のリスナーを設定
	        listview.setOnChildClickListener(
	    		new ExpandableListView.OnChildClickListener(){
					@Override
					public boolean onChildClick(
						ExpandableListView parent,
						View v, 
						int groupPosition, 
						int childPosition, 
						long id
					) {
						// アダプターからデータを取得してトーストで表示
						ExpandableListAdapter adapter = parent.getExpandableListAdapter();
						Map<String, Object> childMap = (Map<String, Object>) adapter.getChild(
								groupPosition,
								childPosition
							);
						Toast.makeText(ObjectNavdata.this, 
								childMap.get("child_text").toString(), 
								Toast.LENGTH_SHORT).show();
						return false;
					}
	    		}
	        );
	    }
	}

