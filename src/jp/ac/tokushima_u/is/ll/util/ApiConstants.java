package jp.ac.tokushima_u.is.ll.util;

public class ApiConstants {
//	public static final String system_url = "http://192.168.0.88:8080/learninglog1";
	public static final String system_url = "http://ll.is.tokushima-u.ac.jp/learninglog";
//	public static final String system_url = "http://192.168.0.210:8080/learninglog";
//	public static final String system_url = "http://192.168.0.193:8080/learninglog/";
//	public static final String system_url = "http://ll.is.tokushima-u.ac.jp/ecourse";
//	public static final String system_url = "http://ll.is.tokushima-u.ac.jp/jaist";
//	public static final String Image_Server_Url = "http://192.168.0.193:8080/static/learninglog/";
	public static final String Image_Server_Url = "http://ll.is.tokushima-u.ac.jp/static/learninglog/";
//	public static final String Image_Server_Url = "http://ll.is.tokushima-u.ac.jp/static/jaist/";
//	public static final String Image_Server_Url = "http://ll.is.tokushima-u.ac.jp/static/ecourse/";
	public static final String LargeSizePostfix = "_800x600.png";
	public static final String MiddleSizePostfix = "_320x240.png";
	public static final String SmallSizePostfix = "_160x120.png";
	public static final String SmallestSizePostfix = "_80x60.png";
	
	
//	public static final String system_url = "http://ll.is.tokushima-u.ac.jp/eval";
//	public static final String system_url = "http://ll.is.tokushima-u.ac.jp/learninglog";
	public static final String Authority_URL = system_url+"/userinfo";
	public static final String Quiz_Create_URL = system_url+"/quiz/create.json";
	public static final String Quiz_Checker_URL = system_url+"/quiz/check.json";
	public static final String Context_Aware_URL = system_url+"/contextaware.json";
	public static final String Context_Aware_Feedback_URL = system_url+"/contextaware/feedback.json";
	public static final String Item_View_URL = system_url+"/item/view.json";
	public static final String ITEM_Add_URI = system_url+"/item";
	public static final String REGISTER_ID_SAVE_URI = system_url+"/c2dm.json";
	public static final String TRANSLATE_TITLE_URI = system_url+"/api/translate/itemTitle";
	public static final String Sync_URI = system_url+"/sync";
	public static final String Pronounce_URI = "http://ll.is.tokushima-u.ac.jp/learninglog/api/translate/tts";
}
