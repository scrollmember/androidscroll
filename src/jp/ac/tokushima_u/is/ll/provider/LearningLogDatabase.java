package jp.ac.tokushima_u.is.ll.provider;

import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.AnswerColumns;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.ChoicesColumns;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.ItemCommentColumns;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.ItemTagColumns;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.ItemTitleColumns;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.ItemsColumns;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.LanguagesColumns;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.NotifysColumns;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.ProfilesColumns;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.QuestionColumns;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.QuizsColumns;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.SettingsColumns;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.SyncColumns;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Helper for managing {@link SQLiteDatabase} that stores data for
 * {@link LearningLogProvider}.
 */
public class LearningLogDatabase extends SQLiteOpenHelper {
    private static final String TAG = "LearningLogDatabase";

    private static final String DATABASE_NAME = "learninglog.db";

    // NOTE: carefully update onUpgrade() when bumping database versions to make
    // sure user data is saved.

    private static final int VER_LAUNCH = 18;
    private static final int VER_SESSION_HASHTAG = 19;

    private static final int DATABASE_VERSION = 15;

    interface Tables {
        String ITEMS = "items";
        String QUESTIONS = "questions";
        String ANSWERS = "answers";
        String LANGUAGES = "languages";
        String ITEMS_TITLES = "itemtitles";
        String ITEMS_TAGS = "itemtags";
        String ITEMS_COMMENTS = "itemcomments";
        String QUIZS = "quizs";
        String CHOICES = "choices";
        String SETTING = "settings";
        String PROFILES = "profiles";
        String NOTIFYS = "notifys";

//        String SESSIONS_SEARCH = "sessions_search";
        
        String ITEMS_JOIN_ITEMSTITLES = "items "
            + "LEFT OUTER JOIN itemtitles ON items.item_id=itemtitles.item_id ";
        
        String QUESTIONS_JOIN_ITEMS= "questions LEFT OUTER JOIN items on questions.item_id = items.item_id ";
        String ITEMS_JOIN_QUESTIONS= "items LEFT OUTER JOIN questions on items.item_id = questions.item_id ";
        String ITEMS_JOIN_ANSWERS = "items LEFT OUTER JOIN answers on items.item_id = answers.item_id ";
        
        String ITEMSTITLES_JOIN_ITEMS_LANGUAGES = "itemtitles "
                + "LEFT OUTER JOIN items ON itemtitles.item_id=items.item_id "
                + "LEFT OUTER JOIN languages ON itemtitles.language_id=languages.language_id";
        
        String ITEMSTITLES_JOIN_LANGUAGES = "itemtitles "
            + "LEFT OUTER JOIN languages ON itemtitles.language_id=languages.language_id";
        
        String SETTING_JOIN_LANGUAGES = " settings LEFT OUTER JOIN languages ON settings.content=languages.language_id ";

//        String VENDORS_JOIN_TRACKS = "vendors "
//                + "LEFT OUTER JOIN tracks ON vendors.track_id=tracks.track_id";
//
//        String SESSIONS_SPEAKERS_JOIN_SPEAKERS = "sessions_speakers "
//                + "LEFT OUTER JOIN speakers ON sessions_speakers.speaker_id=speakers.speaker_id";
//
//        String SESSIONS_SPEAKERS_JOIN_SESSIONS_BLOCKS_ROOMS = "sessions_speakers "
//                + "LEFT OUTER JOIN sessions ON sessions_speakers.session_id=sessions.session_id "
//                + "LEFT OUTER JOIN blocks ON sessions.block_id=blocks.block_id "
//                + "LEFT OUTER JOIN rooms ON sessions.room_id=rooms.room_id";
//
//        String SESSIONS_TRACKS_JOIN_TRACKS = "sessions_tracks "
//                + "LEFT OUTER JOIN tracks ON sessions_tracks.track_id=tracks.track_id";
//
//        String SESSIONS_TRACKS_JOIN_SESSIONS_BLOCKS_ROOMS = "sessions_tracks "
//                + "LEFT OUTER JOIN sessions ON sessions_tracks.session_id=sessions.session_id "
//                + "LEFT OUTER JOIN blocks ON sessions.block_id=blocks.block_id "
//                + "LEFT OUTER JOIN rooms ON sessions.room_id=rooms.room_id";
//
//        String SESSIONS_SEARCH_JOIN_SESSIONS_BLOCKS_ROOMS = "sessions_search "
//                + "LEFT OUTER JOIN sessions ON sessions_search.session_id=sessions.session_id "
//                + "LEFT OUTER JOIN blocks ON sessions.block_id=blocks.block_id "
//                + "LEFT OUTER JOIN rooms ON sessions.room_id=rooms.room_id";
//
//        String VENDORS_SEARCH_JOIN_VENDORS_TRACKS = "vendors_search "
//                + "LEFT OUTER JOIN vendors ON vendors_search.vendor_id=vendors.vendor_id "
//                + "LEFT OUTER JOIN tracks ON vendors.track_id=tracks.track_id";

    }

//    private interface Triggers {
//        String SESSIONS_SEARCH_INSERT = "sessions_search_insert";
//        String SESSIONS_SEARCH_DELETE = "sessions_search_delete";
//
//        String VENDORS_SEARCH_INSERT = "vendors_search_insert";
//        String VENDORS_SEARCH_DELETE = "vendors_search_delete";
//    }


    public LearningLogDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.ITEMS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ItemsColumns.ITEM_ID + " TEXT NOT NULL,"
                + ItemsColumns.TAG + " TEXT NULL,"
                + ItemsColumns.NOTE + " TEXT NULL,"
                + ItemsColumns.PHOTO_URL + " TEXT NULL,"
                + ItemsColumns.FILE_TYPE +" TEXT NULL, "
                + ItemsColumns.NICK_NAME  + " TEXT NULL,"
                + ItemsColumns.PLACE  + " TEXT NULL,"
                + ItemsColumns.RELATE + " TEXT NULL,"
                + ItemsColumns.AUTHOR_ID  + " TEXT NULL,"
                + ItemsColumns.CATEGORY  + " TEXT NULL,"
                + ItemsColumns.UPDATE_TIME + " INTEGER NULL, "
                + ItemsColumns.LATITUTE + " REAL NULL, "
                + ItemsColumns.LNGITUTE + " REAL NULL, "
                + ItemsColumns.SPEED + " REAL NULL, "
                + ItemsColumns.LOCATION_BASED + " TEXT NULL, "
                + ItemsColumns.QUESTION_TYPES + " TEXT NULL, "
                + ItemsColumns.SHARE + " TEXT NULL, "
                + ItemsColumns.ATTACHED + " TEXT NULL, "
                + ItemsColumns.DISABLED + " INTEGER NULL,"
                + SyncColumns.SYNC_TYPE + " INTEGER NOT NULL,"
                + SyncColumns.UPDATED + " INTEGER NOT NULL, "
                + "UNIQUE (" + ItemsColumns.ITEM_ID + ") ON CONFLICT REPLACE)");
        
        db.execSQL("CREATE TABLE " + Tables.LANGUAGES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + LanguagesColumns.LANGUAGE_ID + " TEXT NOT NULL,"
                + LanguagesColumns.CODE + " TEXT NOT NULL,"
                + LanguagesColumns.NAME + " TEXT NOT NULL,"
                + SyncColumns.UPDATED + " INTEGER NULL, "
                + "UNIQUE (" + LanguagesColumns.LANGUAGE_ID + ") " 
                + "UNIQUE (" + LanguagesColumns.CODE + ") ON CONFLICT REPLACE)");
        
        db.execSQL("CREATE TABLE " + Tables.ITEMS_TITLES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ItemTitleColumns.ITEMTITLE_ID + " TEXT NOT NULL,"
                + ItemTitleColumns.LANGUAGE_ID + " TEXT NOT NULL,"
                + ItemTitleColumns.ITEM_ID + " TEXT NOT NULL,"
                + ItemTitleColumns.CONTENT + " TEXT  NULL,"
                + "UNIQUE (" + ItemTitleColumns.ITEMTITLE_ID + ")  "
                + "UNIQUE (" + ItemTitleColumns.LANGUAGE_ID+", "+ ItemTitleColumns.ITEM_ID+", "+ItemTitleColumns.CONTENT + ") ON CONFLICT REPLACE)");
        
        db.execSQL("CREATE TABLE " + Tables.QUESTIONS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + QuestionColumns.QUESTION_ID + " TEXT NOT NULL,"
                + QuestionColumns.AUTHOR_ID  + " TEXT NOT NULL,"
                + QuestionColumns.NICKNAME  + " TEXT NULL,"
                + QuestionColumns.LANGUAGE_CODE + " TEXT NOT NULL,"
                + QuestionColumns.ITEM_ID + " TEXT NOT NULL,"
                + QuestionColumns.CONTENT + " TEXT NOT NULL,"
                + QuestionColumns.STATE + " INTEGER NOT NULL,"
                + QuestionColumns.UPDATE_TIME + " INTEGER NOT NULL,"
                + "UNIQUE (" + QuestionColumns.QUESTION_ID + ") ON CONFLICT REPLACE)");
        
        db.execSQL("CREATE TABLE " + Tables.ANSWERS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + AnswerColumns.ANSWER_ID + " TEXT NOT NULL,"
                + AnswerColumns.Question_ID + " TEXT NOT NULL,"
                + AnswerColumns.ITEM_ID + " TEXT NOT NULL,"
                + AnswerColumns.AUTHOR_ID  + " TEXT NOT NULL,"
                + AnswerColumns.NICKNAME  + " TEXT NULL,"
                + AnswerColumns.CONTENT + " TEXT NOT NULL,"
                + AnswerColumns.UPDATE_TIME + " INTEGER NOT NULL,"
                + "UNIQUE (" + AnswerColumns.ANSWER_ID + ") ON CONFLICT REPLACE)");
        
        
        db.execSQL("CREATE TABLE " + Tables.ITEMS_TAGS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ItemTagColumns.ITEMTAG_ID + " TEXT NOT NULL,"
                + ItemTagColumns.ITEM_ID + " TEXT NOT NULL,"
                + ItemTagColumns.TAG + " TEXT NOT NULL,"
                + "UNIQUE (" + ItemTagColumns.ITEMTAG_ID + ") ON CONFLICT REPLACE)");
        
        db.execSQL("CREATE TABLE " + Tables.ITEMS_COMMENTS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ItemCommentColumns.ITEMCOMMENT_ID + " TEXT NOT NULL,"
                + ItemCommentColumns.NICKNAME + " TEXT NULL,"
                + ItemCommentColumns.ITEM_ID + " TEXT NOT NULL,"
                + ItemCommentColumns.COMMENT + " TEXT NOT NULL,"
                + "UNIQUE (" + ItemCommentColumns.ITEMCOMMENT_ID + ") ON CONFLICT REPLACE)");
        
        db.execSQL("CREATE TABLE " + Tables.QUIZS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + QuizsColumns.QUIZ_ID + " TEXT NOT NULL,"
                + QuizsColumns.Item_ID + " TEXT ULL,"
                + QuizsColumns.QUIZ_CONTENT + " TEXT NULL,"
                + QuizsColumns.ANSWER + " TEXT NULL,"
                + QuizsColumns.LAN_CODE + " TEXT NULL,"
                + QuizsColumns.MY_ANSWER + " TEXT NULL,"
                + QuizsColumns.AUTHOR_ID + " TEXT NOT NULL,"
                + QuizsColumns.QUIZ_TYPE + " TEXT NOT NULL,"
                + QuizsColumns.FILE_TYPE + " TEXT NULL,"
                + QuizsColumns.LATITUTE + " REAL NULL, "
                + QuizsColumns.LNGITUTE + " REAL NULL, "
                + QuizsColumns.SPEED + " REAL NULL, "
                + QuizsColumns.PHOTO_URL + " TEXT NULL,"
                + QuizsColumns.WEIGHT + " INTEGER NULL, "
                + QuizsColumns.CREATE_TIME + " INTEGER NOT NULL, "
                + QuizsColumns.ANSWER_STATE + " INTEGER NOT NULL, "
                + QuizsColumns.ALARM_TYPE + " INTEGER NULL, "
                + QuizsColumns.PASS + " INTEGER NULL, "
                + SyncColumns.SYNC_TYPE + " INTEGER NOT NULL,"
                + SyncColumns.UPDATED + " INTEGER NOT NULL, "
                + "UNIQUE (" + QuizsColumns.QUIZ_ID + ") ON CONFLICT REPLACE)");
        
        db.execSQL("CREATE TABLE " + Tables.CHOICES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ChoicesColumns.CHOICE_ID + " TEXT NOT NULL,"
                + ChoicesColumns.QUIZ_ID + " TEXT NOT NULL,"
                + ChoicesColumns.LAN_CODE + " TEXT NOT NULL,"
                + ChoicesColumns.CHOICE_CONTENT + " TEXT NOT NULL,"
                + ChoicesColumns.File_TYPE + " TEXT NULL,"
                + ChoicesColumns.NOTE + " TEXT NULL,"
                + ChoicesColumns.NUMBER + " INTEGER NULL,"
                + "UNIQUE (" + ChoicesColumns.CHOICE_ID + ") ON CONFLICT REPLACE)");
        
        db.execSQL("CREATE TABLE " + Tables.SETTING + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SettingsColumns.SETTING_ID + " TEXT NOT NULL,"
                + SettingsColumns.AUTHOR_ID + " TEXT NOT NULL,"
                + SettingsColumns.FIELD + " INTEGER NOT NULL,"
                + SettingsColumns.CONTENT + " TEXT NOT NULL,"
                + SettingsColumns.NAME + " TEXT NULL,"
                + SettingsColumns.NUM + " INTEGER NULL,"
                + SyncColumns.UPDATED + " INTEGER NOT NULL, "
                + "UNIQUE (" + SettingsColumns.SETTING_ID + ") ON CONFLICT REPLACE)");
        
        db.execSQL("CREATE TABLE " + Tables.PROFILES + "("   
        		+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ProfilesColumns.PROFILE_ID + " TEXT NOT NULL,"
                + ProfilesColumns.AUTHOR_ID + " TEXT NOT NULL,"
                + ProfilesColumns.FIELD + " INTEGER NOT NULL,"
                + ProfilesColumns.MIN_X1 + " REAL NULL,"
                + ProfilesColumns.MIN_Y1 + " REAL NULL,"
                + ProfilesColumns.MIN_X2 + " REAL NULL,"
                + ProfilesColumns.MIN_Y2 + " REAL NULL,"
                + ProfilesColumns.SUB_TYPE + " TEXT NULL, "
                + SettingsColumns.NUM + " INTEGER NULL,"
                + SyncColumns.UPDATED + " INTEGER NOT NULL, "
                + "UNIQUE (" + ProfilesColumns.PROFILE_ID + ") ON CONFLICT REPLACE)");
        
        db.execSQL("CREATE TABLE " + Tables.NOTIFYS + "("   
        		+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        		+ NotifysColumns.NOTIFY_ID + " TEXT NOT NULL,"
                + NotifysColumns.NOTIFY_TYPE + " INTEGER NOT NULL,"
                + NotifysColumns.CREATE_TIME + " INTEGER NOT NULL, "
                + NotifysColumns.LATITUTE + " REAL NULL, "
                + NotifysColumns.LNGITUTE + " REAL NULL, "
                + NotifysColumns.SPEED + " REAL NULL, "
                + NotifysColumns.Feedback + " INTEGER NOT NULL,"
                + NotifysColumns.UPDATE_TIME + " INTEGER NOT NULL, "
                + SyncColumns.SYNC_TYPE + " INTEGER NOT NULL,"
                + "UNIQUE (" + NotifysColumns.NOTIFY_ID+ ") ON CONFLICT REPLACE)");

//        createSessionsSearch(db);
//        createVendorsSearch(db);

//        db.execSQL("CREATE TABLE " + Tables.SEARCH_SUGGEST + " ("
//                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
//                + SearchManager.SUGGEST_COLUMN_TEXT_1 + " TEXT NOT NULL)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);

        // NOTE: This switch statement is designed to handle cascading database
        // updates, starting at the current version and falling through to all
        // future upgrade cases. Only use "break;" when you want to drop and
        // recreate the entire database.
        int version = oldVersion;
        switch (version) {
            case VER_LAUNCH:
                // Version 19 added column for session hashtags.
                version = VER_SESSION_HASHTAG;
        }

        Log.d(TAG, "after upgrade logic, at version " + version);
        if (version != DATABASE_VERSION) {
            Log.w(TAG, "Destroying old data during upgrade");

            db.execSQL("DROP TABLE IF EXISTS " + Tables.ITEMS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.LANGUAGES);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.ITEMS_TITLES);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.ITEMS_COMMENTS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.ITEMS_TAGS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.CHOICES);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.QUIZS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.SETTING);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.PROFILES);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.NOTIFYS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.ANSWERS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.QUESTIONS);
            onCreate(db);
        }
    }
}
