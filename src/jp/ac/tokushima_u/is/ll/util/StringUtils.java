package jp.ac.tokushima_u.is.ll.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class StringUtils {
	public static boolean isBlank(String s) {
		if (s == null || "".equals(s.toString().trim())) {
			return true;
		} else {
			return false;
		}
	}

	public static int index(String[] array, String value) {
		for (int i = 0; i < array.length; i++) {
			if (value.equals(array[i]))
				return i;
		}
		return -1;
	}
	
	public static List<String> stringToArray(String value){
		List<String> values = new ArrayList<String>();
		try{
			if(value!=null){
				value = value.replace("[", "");
				value = value.replace("]", "");
				String[] splits = value.split(",");
				for(int i=0;i<splits.length;i++)
					values.add(splits[i].trim());
			}
		}catch(Exception e){
			Log.e("Learning Log", value, e);
		}
		return values;
	}

	public static String randomUUID() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	public static boolean isJsonParamNull(String value) {
		if (value == null || value.length() == 0 || "null".equals(value))
			return true;
		else
			return false;

	}
	
	public static boolean isJsonParamNull(JSONObject o, String name) {
		return isJsonParamNull(getJsonValue(o, name));
	}
	
	public static String getJsonValue(JSONObject o, String name) {
		try{
			return o.getString(name);
		}catch(JSONException e){
			return null;
		}

	}

}