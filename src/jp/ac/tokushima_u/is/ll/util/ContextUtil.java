package jp.ac.tokushima_u.is.ll.util;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ContextUtil {
	public static String getUserId(Context context){
	    SharedPreferences setting = context.getSharedPreferences(Constants.SETTING_INFOS_FILE, Context.MODE_PRIVATE);
        return setting.getString(Constants.SavedUserId, "");
    }  
	
	public static String getUsername(Context context){
	    SharedPreferences setting = context.getSharedPreferences(Constants.SETTING_INFOS_FILE, Context.MODE_PRIVATE);
        return setting.getString(Constants.SavedUserName, "");
    }  
	
	public static String getPassword(Context context){
	    SharedPreferences setting = context.getSharedPreferences(Constants.SETTING_INFOS_FILE, Context.MODE_PRIVATE);
        return setting.getString(Constants.SavedPassword, "");
    }  
	
	public static String getDefaultCat(Context context){
	    SharedPreferences setting = context.getSharedPreferences(Constants.SETTING_INFOS_FILE, Context.MODE_PRIVATE);
        return setting.getString(Constants.SavedDefaultCategory, "");
    }  
	
	public static long getLatestCircle(Context context){
	    SharedPreferences setting = context.getSharedPreferences(Constants.SETTING_INFOS_FILE, Context.MODE_PRIVATE);
        return setting.getLong(Constants.SavedLatestCircle, 0);
    }  

	public static void setLatestCircle(Context context, long circle_time){
	    SharedPreferences setting = context.getSharedPreferences(Constants.SETTING_INFOS_FILE, Context.MODE_PRIVATE);
		Editor editor = setting.edit();
		editor.putLong(Constants.SavedLatestCircle, circle_time);
		editor.commit();
    }  
	
	//評価事件用　Randomcontextawareservice
	public static long getLatestLoop(Context context){
	    SharedPreferences setting = context.getSharedPreferences(Constants.SETTING_INFOS_FILE, Context.MODE_PRIVATE);
        return setting.getLong(Constants.SavedLatestLoop, 0);
    }  
	
	//評価事件用　Randomcontextawareservice
	public static void setLatestLoop(Context context, long circle_time){
	    SharedPreferences setting = context.getSharedPreferences(Constants.SETTING_INFOS_FILE, Context.MODE_PRIVATE);
		Editor editor = setting.edit();
		editor.putLong(Constants.SavedLatestLoop, circle_time);
		editor.commit();
    }  
	
	public static long getLastSyncTime(Context context){
	    SharedPreferences setting = context.getSharedPreferences(Constants.SETTING_INFOS_FILE, Context.MODE_PRIVATE);
        return setting.getLong(Constants.SavedLatestSyncTime, 0);
    }  

	public static void setLatestSyncTime(Context context){
	    SharedPreferences setting = context.getSharedPreferences(Constants.SETTING_INFOS_FILE, Context.MODE_PRIVATE);
		Editor editor = setting.edit();
		editor.putLong(Constants.SavedLatestSyncTime, Calendar.getInstance().getTimeInMillis());
		editor.commit();
    } 
	
	
	public static void clearSettingSharePreferences(Context context){
		SharedPreferences setting = context.getSharedPreferences(Constants.SETTING_INFOS_FILE, Context.MODE_PRIVATE);
		Editor editor = setting.edit();
		Map<String, ?> map = setting.getAll();
		Set<String> keys = map.keySet();
//		for(String key:keys){
//			editor.
//		}
		
//		editor.putString(Constants.SavedCategoryKeys, tempusername);
		editor.commit();
		
	}
	
//	public static String getRegisterId(Context context){
//	    SharedPreferences setting = context.getSharedPreferences(Constants.SETTING_INFOS_FILE, Context.MODE_PRIVATE);
//        return setting.getString(Constants.SavedRegisterId, "");
//    }  
}
