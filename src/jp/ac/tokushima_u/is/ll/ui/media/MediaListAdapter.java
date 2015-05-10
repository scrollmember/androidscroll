package jp.ac.tokushima_u.is.ll.ui.media;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MediaListAdapter extends SimpleAdapter{

	private ListView listView;

	public MediaListAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to, ListView listView) {
		super(context, data, resource, from, to);
		this.listView = listView;
	}

	public MediaListAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to) {
		super(context, data, resource, from, to);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View nView = super.getView(position, convertView, parent);
		SparseBooleanArray pos =  listView.getCheckedItemPositions();
		HashMap<Integer,Boolean> map = new HashMap<Integer, Boolean>();
		for(int i = 0; i < pos.size(); i++){
			map.put(pos.keyAt(i), pos.valueAt(i));
		}
		if(map.get(position)!= null && map.get(position)){
			nView.setBackgroundColor(Color.BLUE);
		}else{
			nView.setBackgroundColor(Color.TRANSPARENT);
		}
		return nView;
	}
}
