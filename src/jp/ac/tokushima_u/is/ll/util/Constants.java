package jp.ac.tokushima_u.is.ll.util;

import jp.ac.tokushima_u.is.ll.R;

public class Constants {
	public static Integer LoginSuccessCode = 0; 
	public static Integer InvaliUsernameOrPassword = 1; 
	
//	public static final Integer WithImage = 1;
//	public static final Integer WithoutImage = 2;
	
    public static final Integer NotAnsweredState = -1;
    public static final Integer WrongAnsweredState = 0;
    public static final Integer CorrectAnsweredState = 1;
    public static final Integer PassAnsweredState = 2;
    
    public static final Integer YesNoQuizRemember = 1;
    public static final Integer YesNoQuizForget = 0;
    
    public static final double defaultValue = 0;
    
//	public static final Long QuizTypeTextMutiChoice = 1l;
//	public static final Long QuizTypeImageMutiChoice = 2l;
//	public static final Long QuizTypeYesNoQuestion= 3l;	
	
	public static final int MailLevel = 1;
	public static int SmartPhoneLevel = 2;
	public static int NormalLevel = 3;
	public static int IconLevel = 4;
	
	public static final int MinTime = 300000;
	public static final int MinDistance = 10;
	
	public static int ItemNumber = 20; 
	
	public static final String SETTING_INFOS_FILE = "SETTING_Infos";
	public static final String SavedUserName = "username";
	public static final String SavedPassword = "password";
	public static final String SavedUserId = "userid";
	public static final String SavedNickname = "nickname";
	public static final String SavedDefaultCategory = "default_category";
	public static final String SavedRegisterId = "registerid";
	public static final String SavedLatestCircle = "latest_circle";
	public static final String SavedLatestLoop = "latest_loop";
	public static final String SavedLatestSyncTime= "latest_sync_time";
//	public static final String SavedCategoryKeys = "category_keys";
//	public static final String SavedCategoryValues = "category_values";
//	public static final String SavedLocationBackgroundRun = "location_background_run";
//	public static final String SavedTimeBackgroundRun = "time_background_run";
//	public static final String SavedAutoStartup = "auto_start_up";
	
	
	public static Integer Item_SUCCESS = 1;
	public static Integer Item_USER_EMAIL_EMPTY = 2;
	public static Integer Item_USER_PASSWORD_EMPTY = 3;
	public static Integer Item_USER_NOT_FOUND = 4;
	public static Integer Item_USER_PASSWORD_ERROR = 5;
	public static Integer Item_ITEM_CREATE_FAILED = 6;
	public static Integer Item_DATA_ACCESS_ERROR = 7;
	public static Integer Item_FILE_NAME_EMPTY = 8;
	public static Integer Item_FORM_DATA_EMPTY = 9;

	public static Integer ErrorCode_No_Error = 0;
	public static Integer ErrorCode_Sever_Access_Deny = 1;
	public static Integer ErrorCode_No_User = 2;
	public static Integer ErrorCode_No_Quiz = 3;
	
	public static final int NotificationID = 1222333;
	public static final int QuizNotificationID = R.id.btn_quiz_more;
	public static final int LogsNotificationID = R.id.btn_quiz_pass;
	public static final int NavNotificationID = 565656755;
	public static final int LoginNotificationID = 134353452;
	public static final String LOG_TAG = "LearningLogLogs";
	
	public static final Integer QuizTypeImageChoice = 2;
	
	public static final Integer WithTimeFlg = new Integer(1);
	
	public static final long VibrateTime = 5000;
	
	public static final String FileTypeVideo = "video";
	public static final String FileTypeAudio = "audio";
	public static final String FileTypeImage = "image";
	public static final String FileTypeFile = "file";
	
	public static final String FileTypeKey = "filetype";
	public static final String FilePathKey = "filepath";
	
//	public static final Integer TimeAlarmType = 1;
//	public static final Integer LocationAlarmType = 2;
	public static final Integer AndroidRequestType = 3;
	public static final Integer LocationRequestType = 4;
	public static final Integer TimeReuestType = 5;
	public static final Integer ContextAwareRequestType = 10;
	public static final Integer ContextAwareRandomType = 11;
	
	public static final Integer NotifyTypeTextQuiz = 1;
	public static final Integer NotifyTypeMessage = 2;
	
	public static final String Helper_URL = "http://ll.is.tokushima-u.ac.jp/learninglog/help";
	public static final String System_URL = ApiConstants.system_url;
	public static final String Setting_URL = ApiConstants.system_url+"/mysetting";
	
	public static final String AudioFilePath = "Audio_File_Path"; 
	
	
	public static final String DEBUG_TAG = "mrecorder";

	public static final String ActionMode = "actionmode";
	// 音声
	public static final Integer AudioMode = 1;
	// 写真
	public static final Integer PhotoMode = 2;
	// 動画
	public static final Integer VideoMode = 3;
	// ファイルパス
	public static final String MR_FILE_PATH = "filepath";

//	public static final String ChoiceMode = "choicemode";

	// 音声録音ファイルの拡張子
	public static final String EXTENSION_MP3 = ".mp3";
	// 写真撮影ファイルの拡張子
	public static final String EXTENSION_JPG = ".jpg";
	// 動画撮影ファイルの拡張子
	public static final String EXTENSION_MP4 = ".mp4";
}
