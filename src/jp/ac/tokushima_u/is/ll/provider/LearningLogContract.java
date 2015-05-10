/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.ac.tokushima_u.is.ll.provider;


import jp.ac.tokushima_u.is.ll.provider.LearningLogDatabase.Tables;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract class for interacting with {@link LearningLogProvider}. Unless
 * otherwise noted, all time-based fields are milliseconds since epoch and can
 * be compared against {@link System#currentTimeMillis()}.
 * <p>
 * The backing {@link ContentProvider} assumes that {@link Uri} are generated
 * using stronger {@link String} identifiers, instead of {@code int}
 * {@link BaseColumns#_ID} values, which are prone to shuffle during sync.
 */
public class LearningLogContract {

    /**
     * Special value for {@link SyncColumns#UPDATED} indicating that an entry
     * has never been updated, or doesn't exist yet.
     */
    public static final long UPDATED_NEVER = -2;

    /**
     * Special value for {@link SyncColumns#UPDATED} indicating that the last
     * update time is unknown, usually when inserted from a local file source.
     */
    public static final long UPDATED_UNKNOWN = -1;

    public interface SyncColumns {
        /** Last time this entry was updated or synchronized. */
        String UPDATED = "updated";
        String SYNC_TYPE = "sync_type";

        public static final Integer SYNC_TYPE_REQUEST = 1;// Client -> Server, used in synchronizing
    	public static final Integer SYNC_TYPE_PUSH = 2; // server-> Client, used in context-aware learning
    	public static final Integer SYNC_TYPE_CLIENT_INSERT = 3; //  Client, created
    	public static final Integer SYNC_TYPE_CLIENT_UPDATE = 4; //  Client, created
    }

    interface ItemsColumns {
        String ITEM_ID = "item_id";
        String TAG = "tag";
//        String BARCODE = "barcode";
//        String GRCODE = "qrcode";
//        String RFID = "rfid";
        String NOTE = "note";
        String PLACE = "place";
        String RELATE = "relate";
        String LATITUTE = "item_lat";
        String LNGITUTE = "item_lng";
        String SPEED = "speed";
        String PHOTO_URL = "photo_url";
        String FILE_TYPE = "file_type";
        String NICK_NAME = "nickname";
        String UPDATE_TIME = "update_time";
        String AUTHOR_ID = "author_id";
        String CATEGORY = "category";
        String SHARE = "share";
        String LOCATION_BASED = "location_based";
        String QUESTION_TYPES = "question_types";
        String TITLES = "titles";
        String ATTACHED = "attached";
        String DISABLED = "disabled";
    }

    interface LanguagesColumns {
        String LANGUAGE_ID = "language_id";
        String CODE = "code";
        String NAME = "name";
    }

    interface ItemTitleColumns {
    	String ITEMTITLE_ID = "itemtitle_id";
        String LANGUAGE_ID = "language_id";
        String ITEM_ID = "item_id";
        String CONTENT = "content";
    }

    interface ItemTagColumns {
    	String ITEMTAG_ID = "tag_id";
        String ITEM_ID = "item_id";
        String TAG = "tag";
    }

    interface ItemPlaceColumns {
    	String ITEMPLACE_ID = "itemplace_id";
    	String LANGUAGE_ID = "language_id";
    	String ITEM_ID = "item_id";
    	String PLACE = "place";
    }

    interface ItemRelateColumns {
    	String ITEMRELATE_ID = "itemrelate_id";
    	String LANGUAGE_ID = "language_id";
    	String ITEM_ID = "item_id";
    	String RELATE = "relate";
    }
    
    interface QuestionColumns{
    	String QUESTION_ID = "question_id";
    	String ITEM_ID = "item_id";
    	String CONTENT = "content";
    	String LANGUAGE_CODE = "language_code";
    	String STATE = "state";
    	String AUTHOR_ID = "author_id";
    	String NICKNAME = "nickname";
    	String UPDATE_TIME = "update_time";
    }

    interface AnswerColumns{
    	String ANSWER_ID = "ANSWER_id";
    	String ITEM_ID = "item_id";
    	String Question_ID = "question_id";
    	String CONTENT = "content";
    	String AUTHOR_ID = "author_id";
    	String NICKNAME = "nickname";
    	String UPDATE_TIME = "update_time";
    }

    interface ItemCommentColumns {
    	String ITEMCOMMENT_ID = "comment_id";
        String ITEM_ID = "item_id";
        String NICKNAME = "nickname";
        String COMMENT = "comment";
    }


    interface QuizsColumns {
        String QUIZ_ID = "quiz_id";
        String Item_ID = "item_id";
        String QUIZ_CONTENT = "quiz_content";
        String ANSWER = "answer";
        String LAN_CODE = "lan_code";
        String AUTHOR_ID = "author_id";
        String QUIZ_TYPE = "quiz_type";
        String FILE_TYPE = "file_type";
        String PHOTO_URL = "photo_url";
        String LATITUTE = "quiz_lat";
        String LNGITUTE = "quiz_lng";
        String SPEED = "speed";
        String MY_ANSWER = "my_answer";
        String PASS = "pass";
        String CREATE_TIME = "create_time";
        String ALARM_TYPE = "alarm_type";
        String ANSWER_STATE = "answer_state";
        String WEIGHT = "weight";
    }


    interface ChoicesColumns {
        String CHOICE_ID = "choice_id";
        String QUIZ_ID = "quiz_id";
        String LAN_CODE = "lan_code";
        String CHOICE_CONTENT = "choice_content";
        String File_TYPE = "file_type";
        String NOTE = "note";
        String NUMBER = "number";
    }

    interface SettingsColumns {
        String SETTING_ID = "setting_id";
        String AUTHOR_ID = "author_id";
        String FIELD = "field";
        String CONTENT = "content";
        String NAME = "name";
        String NUM = "num";
    }

    interface ProfilesColumns{
    	String PROFILE_ID = "profile_id";
    	String AUTHOR_ID = "author_id";
    	String MIN_X1 = "min_x1";
    	String MIN_X2 = "min_x2";
    	String MIN_Y1 = "min_y1";
    	String MIN_Y2 = "min_y2";
    	String FIELD = "field";
    	String SUB_TYPE = "typ";
    	String NUM = "num";
    }

	interface NotifysColumns {
		String NOTIFY_TYPE = "notify_type";
		String CREATE_TIME = "create_time";
		String UPDATE_TIME = "update_time";
		String LATITUTE = "lat";
		String LNGITUTE = "lng";
		String SPEED = "speed";
		String Feedback = "feedback";
		String NOTIFY_ID = "notify_id";
	}

    public static final String CONTENT_AUTHORITY = "jp.ac.tokushima_u.is.ll";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_USERS = "users";
    private static final String PATH_ITEMS = "items";
    private static final String PATH_PROFILES = "profiles";
    private static final String PATH_LANGUAGES = "languages";
    private static final String PATH_ITEM_TITLES = "itemtitles";
    private static final String PATH_ITEM_TAGS = "itemtags";
    private static final String PATH_ITEM_PLACES = "itemplaces";
    private static final String PATH_ITEM_RELATE = "itemrelate";
    private static final String PATH_ITEM_COMMENTS = "itemcomments";
    private static final String PATH_QUIZS = "quizs";
    private static final String PATH_CHOICES = "choices";
    private static final String PATH_SETTINGS = "settings";
    private static final String PATH_SETTINGS_LANGUAGES = "settings_langauges";
    private static final String PATH_NOTIFYS = "notifys";
    private static final String PATH_QUESTIONS = "questions";
    private static final String PATH_ANSWERS = "answers";


    public static class Users implements BaseColumns {
    	public static final Uri CONTENT_URI =
            BASE_CONTENT_URI.buildUpon().appendPath(PATH_USERS).build();

        public static final String CONTENT_TYPE =
            "vnd.android.cursor.dir/vnd.learninglog.user";
        public static final String CONTENT_ITEM_TYPE =
            "vnd.android.cursor.item/vnd.learninglog.user";

    	 /** Build {@link Uri} for requested {@link #BLOCK_ID}. */
        public static Uri buildUserUri(String userId) {
            return CONTENT_URI.buildUpon().appendPath(userId).build();
        }

        public static Uri buildUsersItemUri(String userId) {
            return CONTENT_URI.buildUpon().appendPath(userId).appendPath(PATH_ITEMS).build();
        }

        public static Uri buildUsersSettingUri(String userId) {
            return CONTENT_URI.buildUpon().appendPath(userId).appendPath(PATH_SETTINGS).build();
        }

        public static Uri buildUsersSettingLanguageUri(String userId) {
            return CONTENT_URI.buildUpon().appendPath(userId).appendPath(PATH_SETTINGS_LANGUAGES).build();
        }
        
        public static Uri buildUsersQuizUri(String userId) {
            return CONTENT_URI.buildUpon().appendPath(userId).appendPath(PATH_QUIZS).build();
        }

        public static String getUserId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }

    public static class Items implements ItemsColumns, SyncColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ITEMS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.learninglog.item";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.learninglog.item";

//        /** Count of {@link Sessions} inside given block. */
//        public static final String SESSIONS_COUNT = "sessions_count";

        /**
         * Flag indicating that at least one {@link Sessions#SESSION_ID} inside
         * this block has {@link Sessions#STARRED} set.
         */
        public static final String CONTAINS_STARRED = "contains_starred";

        public static final Integer PAGE_SIZE = 100;

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = ItemsColumns.UPDATE_TIME + " DESC ";

        public static Uri buildItemUri(String itemId) {
            return CONTENT_URI.buildUpon().appendPath(itemId).build();
        }

        public static Uri buildItemSearchUri(String mQuery) {
            return BASE_CONTENT_URI.buildUpon().appendPath("search").appendPath(mQuery).build();
        }

        public static Uri buildItemQuestionUri(String itemId) {
            return CONTENT_URI.buildUpon().appendPath(itemId).appendPath(PATH_QUESTIONS).build();
        }

        public static Uri buildItemAnswerUri(String itemId) {
            return CONTENT_URI.buildUpon().appendPath(itemId).appendPath(PATH_ANSWERS).build();
        }

        public static Uri buildItemtitlesUri(String itemId) {
            return CONTENT_URI.buildUpon().appendPath(itemId).appendPath(PATH_ITEM_TITLES).build();
        }

        public static Uri buildItemtagsUri(String itemId) {
            return CONTENT_URI.buildUpon().appendPath(itemId).appendPath(PATH_ITEM_TAGS).build();
        }

        public static Uri buildItemcommentsUri(String itemId) {
            return CONTENT_URI.buildUpon().appendPath(itemId).appendPath(PATH_ITEM_COMMENTS).build();
        }

        public static Uri buildItemRelateUri(String itemId) {
        	return CONTENT_URI.buildUpon().appendPath(itemId).appendPath(PATH_ITEM_RELATE).build();
        }
        
        public static Uri buildItemPlaceUri(String itemId) {
        	return CONTENT_URI.buildUpon().appendPath(itemId).appendPath(PATH_ITEM_PLACES).build();
        }


//        /**
//         * Build {@link Uri} that references any {@link Blocks} that occur
//         * between the requested time boundaries.
//         */
//        public static Uri buildBlocksBetweenDirUri(long startTime, long endTime) {
//            return CONTENT_URI.buildUpon().appendPath(PATH_BETWEEN).appendPath(
//                    String.valueOf(startTime)).appendPath(String.valueOf(endTime)).build();
//        }

        /** Read {@link #BLOCK_ID} from {@link Blocks} {@link Uri}. */
        public static String getItemId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getItemSearch(Uri uri) {
            return uri.getPathSegments().get(1);
        }

//        /**
//         * Generate a {@link #BLOCK_ID} that will always match the requested
//         * {@link Blocks} details.
//         */
//        public static String generateBlockId(long startTime, long endTime) {
//            startTime /= DateUtils.SECOND_IN_MILLIS;
//            endTime /= DateUtils.SECOND_IN_MILLIS;
//            return ParserUtils.sanitizeId(startTime + "-" + endTime);
//        }
    }

    public static class Questions implements QuestionColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_QUESTIONS).build();

        public static final int NotAnsweredState = 0;
        public static final int AnsweredState = 1;

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.learninglog.question";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.learninglog.question";

        public static final String DEFAULT_SORT = QuestionColumns.UPDATE_TIME + " DESC ";

        /** Build {@link Uri} for requested {@link #BLOCK_ID}. */
        public static Uri buildQuestionUri(String questionId) {
            return CONTENT_URI.buildUpon().appendPath(questionId).build();
        }

        public static Uri buildQuestionAnswerUri(String questionId) {
            return CONTENT_URI.buildUpon().appendPath(questionId).appendPath(PATH_ANSWERS).build();
        }

        public static String getQuestionId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class Answers implements AnswerColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ANSWERS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.learninglog.answer";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.learninglog.answer";

        public static final String DEFAULT_SORT = Tables.ANSWERS + "." + AnswerColumns.UPDATE_TIME + " ASC ";

        public static Uri buildAnswerUri(String answerId) {
            return CONTENT_URI.buildUpon().appendPath(answerId).build();
        }

        public static String getAnswerId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }


    public static class Languages implements LanguagesColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LANGUAGES).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.learninglog.languge";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.learninglog.language";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = LanguagesColumns.NAME + " ASC ";

        public static Uri buildLanguageUri(String languageId) {
            return CONTENT_URI.buildUpon().appendPath(languageId).build();
        }

        public static String getLanguageId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class Itemtitles implements ItemTitleColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ITEM_TITLES).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.learninglog.itemtitle";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.learninglog.itemtitle";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = ItemTitleColumns.ITEMTITLE_ID + " ASC ";

        public static Uri buildItemtitleUri(String itemtitleid) {
            return CONTENT_URI.buildUpon().appendPath(itemtitleid).build();
        }

        public static String getItemtitleId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class Itemtags implements ItemTagColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ITEM_TAGS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.learninglog.itemtag";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.learninglog.itemtag";

        public static final String DEFAULT_SORT = ItemTagColumns.ITEM_ID + " ASC ";

        public static Uri buildItemTagUri(String tagid) {
            return CONTENT_URI.buildUpon().appendPath(tagid).build();
        }

        public static String getItemTagId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class ItemPlace implements ItemPlaceColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ITEM_PLACES).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.learninglog.itemplace";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.learninglog.itemplace";

        public static final String DEFAULT_SORT = ItemPlaceColumns.ITEM_ID + " ASC ";

		public static final String PLACE = null;

        public static Uri buildItemPlaceUri(String placeid) {
            return CONTENT_URI.buildUpon().appendPath(placeid).build();
        }

        public static String getItemPlaceId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class ItemRelate implements ItemRelateColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ITEM_RELATE).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.learninglog.itemrelate";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.learninglog.itemrelate";

        public static final String DEFAULT_SORT = ItemRelateColumns.ITEM_ID + " ASC ";

		public static final String RELATE = null;

        public static Uri buildItemRelateUri(String relateid) {
            return CONTENT_URI.buildUpon().appendPath(relateid).build();
        }

        public static String getItemRelated(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class Itemcomments implements ItemCommentColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ITEM_COMMENTS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.learninglog.itemcomment";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.learninglog.itemcomment";

        public static final String DEFAULT_SORT = ItemCommentColumns.ITEM_ID + " ASC ";

        public static Uri buildItemCommentUri(String commentid) {
            return CONTENT_URI.buildUpon().appendPath(commentid).build();
        }

        public static String getItemCommentId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class Quizs implements QuizsColumns,SyncColumns, BaseColumns{
    	public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_QUIZS).build();

    	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.learninglog.quiz";
    	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.learninglog.quiz";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = QuizsColumns.WEIGHT + " ASC ";

        public static Uri buildQuizUri(String quizid) {
            return CONTENT_URI.buildUpon().appendPath(quizid).build();
        }

        public static Uri buildQuizChoicesUri(String quizId) {
            return CONTENT_URI.buildUpon().appendPath(quizId).appendPath(PATH_CHOICES).build();
        }

        public static String getQuizId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class Choices implements ChoicesColumns, BaseColumns{
    	public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CHOICES).build();

    	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.learninglog.choice";
    	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.learninglog.choice";

    	/** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = ChoicesColumns.NUMBER + " ASC ";

        public static Uri buildChoiceUri(String choiceid) {
            return CONTENT_URI.buildUpon().appendPath(choiceid).build();
        }

        public static String getChoiceId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class Settings implements SettingsColumns,SyncColumns, BaseColumns{
    	public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SETTINGS).build();

    	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.learninglog.setting";
    	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.learninglog.setting";

    	public static final Integer SETTING_CATEGORY_FIELD_ID = 1;
    	public static final Integer SETTING_MYLAN_FIELD_ID = 2;
    	public static final Integer SETTING_STUDYLAN_FIELD_ID = 3;
    	public static final Integer SETTING_STUDY_TIME_FIELD_ID = 4;
    	public static final Integer SETTING_STUDY_AREA_FIELD_ID = 5;

    	/** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = SettingsColumns.NUM + " ASC ";

        public static Uri buildSettingUri(String settingid) {
            return CONTENT_URI.buildUpon().appendPath(settingid).build();
        }

        public static String getSettingId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class Profiles implements ProfilesColumns,SyncColumns, BaseColumns{
    	public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PROFILES).build();

    	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.learninglog.profile";
    	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.learninglog.profile";

    	public static final Integer PROFILE_AREA_FIELD_ID = 1;
    	public static final Integer PROFILE_TIME_FIELD_ID = 2;
    	public static final Integer PROFILE_SEND_TIME_FIELD_ID = 3;

    	/** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = ProfilesColumns.NUM + " ASC ";

        public static Uri buildProfileUri(String profileid) {
            return CONTENT_URI.buildUpon().appendPath(profileid).build();
        }

        public static String getProfileId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class SettingsLanguages implements SettingsColumns, LanguagesColumns, BaseColumns{
    	public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SETTINGS_LANGUAGES).build();
        public static Uri buildSettingsLanguagesUri(String settingid) {
            return CONTENT_URI.buildUpon().appendPath(settingid).build();
        }
    }

    public static class Notifys implements NotifysColumns, SyncColumns, BaseColumns{
    	public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_NOTIFYS).build();

    	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.learninglog.notify";
    	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.learninglog.notify";

    	public static final Integer NOTIFY_CONTEXT_QUIZ_ID = 1;
    	public static final Integer NOTIFY_ITEM_ID = 2;
    	public static final Integer NOTIFY_LOCATION_TIME_QUIZ_ID = 3;
    	public static final Integer NOTIFY_SENDTIME_QUIZ_ID = 4;
    	public static final Integer NOTIFY_RANDOM_QUIZ_ID = -1;

    	public static final Integer NOTIFY_NOT_FEEDBACK = 0;
    	public static final Integer NOTIFY_FEEDBACK = 1;

    	  public static final String DEFAULT_SORT = NotifysColumns.CREATE_TIME + " DESC ";

        public static Uri buildNotifyUri(String notifyid) {
            return CONTENT_URI.buildUpon().appendPath(notifyid).build();
        }

        public static String getNotifyId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    private LearningLogContract() {
    }
}
