package jp.ac.tokushima_u.is.ll.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Answers;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Choices;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemcomments;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Items;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemtags;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemtitles;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Languages;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Notifys;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Profiles;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Questions;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Quizs;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Settings;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Users;
import jp.ac.tokushima_u.is.ll.provider.LearningLogDatabase.Tables;
import jp.ac.tokushima_u.is.ll.service.SyncService;
import jp.ac.tokushima_u.is.ll.util.Constants;
import jp.ac.tokushima_u.is.ll.util.GeoUtils;
import jp.ac.tokushima_u.is.ll.util.SelectionBuilder;
import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Provider that stores {@link LearningLogContract} data. Data is usually inserted
 * by {@link SyncService}, and queried by various {@link Activity} instances.
 */
public class LearningLogProvider extends ContentProvider {
    private static final String TAG = "LearningLogProvider";
    private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE);

    private LearningLogDatabase mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int ITEMS = 100;
    private static final int ITEMS_ID = 102;
    private static final int LANGUAGES = 103;
    private static final int LANGUAGES_ID = 104;
    private static final int ITEM_TITLES = 105;
    private static final int ITEM_TITLES_ID = 106;
    private static final int ITEM_TAGS = 107;
    private static final int ITEM_TAGS_ID = 108;
    private static final int ITEM_COMMENTS = 109;
    private static final int ITEM_COMMENTS_ID = 110;
    private static final int ITEM_ID_ITEM_TITLES = 111;
    private static final int ITEM_ID_ITEM_TAGS= 112;
    private static final int ITEM_ID_ITEM_COMMENTS = 113;
    private static final int USER_ID_QUIZS = 114;
    private static final int USER_ID_ITEMS = 115;
    private static final int USER_ID_SETTINGS = 116;
    private static final int QUIZS = 117;
    private static final int QUIZS_ID = 118;
    private static final int QUIZ_ID_CHOICES = 119;
    private static final int CHOICES = 120;
    private static final int CHOICES_ID = 121;
    private static final int SETTINGS = 122;
    private static final int SETTINGS_ID = 123;
    private static final int SETTINGS_LANGUAGES = 124;
    private static final int USER_ID_SETTINGS_LANGUAGES = 125;
    private static final int PROFILES = 126;
    private static final int PROFILES_ID = 127;
    private static final int NOTIFYS = 128;
    private static final int NOTIFYS_ID = 129;
    private static final int QUESTIONS = 130;
    private static final int QUESTIONS_ID = 131;
    private static final int ANSWERS = 132;
    private static final int ANSWERS_ID = 133;
    private static final int ITEM_ID_QUESTIONS = 134;
    private static final int QUESTION_ID_ANSWERS = 135;
    private static final int ITEM_ID_ANSWERS = 136;
    private static final int ITEM_SEARCH = 99;
    private static final int SEARCH_SUGGEST = 800;

    private static final String MIME_XML = "text/xml";

    /**
     * Build and return a {@link UriMatcher} that catches all {@link Uri}
     * variations supported by this {@link ContentProvider}.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = LearningLogContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "items", ITEMS);
        matcher.addURI(authority, "items/*", ITEMS_ID);
        matcher.addURI(authority, "languages", LANGUAGES);
        matcher.addURI(authority, "languages/*", LANGUAGES_ID);
        matcher.addURI(authority, "itemtitles", ITEM_TITLES);
        matcher.addURI(authority, "itemtitles/*", ITEM_TITLES_ID);
        matcher.addURI(authority, "itemtags", ITEM_TAGS);
        matcher.addURI(authority, "itemtags/*", ITEM_TAGS_ID);
        matcher.addURI(authority, "itemcomments", ITEM_COMMENTS);
        matcher.addURI(authority, "itemcomments/*", ITEM_COMMENTS_ID);
        matcher.addURI(authority, "search/*", ITEM_SEARCH);
        matcher.addURI(authority, "items/*/itemtitles", ITEM_ID_ITEM_TITLES);
        matcher.addURI(authority, "items/*/itemtags", ITEM_ID_ITEM_TAGS);
        matcher.addURI(authority, "items/*/itemcomments", ITEM_ID_ITEM_COMMENTS);
        matcher.addURI(authority, "users/*/items", USER_ID_ITEMS);
        matcher.addURI(authority, "users/*/settings", USER_ID_SETTINGS);
        matcher.addURI(authority, "users/*/quizs", USER_ID_QUIZS);
        matcher.addURI(authority, "quizs", QUIZS);
        matcher.addURI(authority, "quizs/*", QUIZS_ID);
        matcher.addURI(authority, "choices", CHOICES);
        matcher.addURI(authority, "choices/*", CHOICES_ID);
        matcher.addURI(authority, "quizs/*/choices", QUIZ_ID_CHOICES);
        matcher.addURI(authority, "settings", SETTINGS);
        matcher.addURI(authority, "settings/*", SETTINGS_ID);
        matcher.addURI(authority, "settings_langauges/*", SETTINGS_LANGUAGES);
        matcher.addURI(authority, "users/*/settings_langauges", USER_ID_SETTINGS_LANGUAGES);
        matcher.addURI(authority, "profiles", PROFILES);
        matcher.addURI(authority, "profiles/*", PROFILES_ID);
        matcher.addURI(authority, "notifys", NOTIFYS);
        matcher.addURI(authority, "notifys/*", NOTIFYS_ID);
        matcher.addURI(authority, "items/*/questions", ITEM_ID_QUESTIONS);
        matcher.addURI(authority, "items/*/answers", ITEM_ID_ANSWERS);
        matcher.addURI(authority, "questions", QUESTIONS);
        matcher.addURI(authority, "questions/*", QUESTIONS_ID);
        matcher.addURI(authority, "answers", ANSWERS);
        matcher.addURI(authority, "answers/*", ANSWERS_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        mOpenHelper = new LearningLogDatabase(context);
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return Items.CONTENT_TYPE;
            case USER_ID_ITEMS:
                return Items.CONTENT_TYPE;    
            case USER_ID_SETTINGS:
                return Settings.CONTENT_TYPE;        
            case ITEMS_ID:
                return Items.CONTENT_ITEM_TYPE;
            case LANGUAGES:
                return Languages.CONTENT_TYPE;
            case LANGUAGES_ID:
                return Languages.CONTENT_ITEM_TYPE;   
            case ITEM_TITLES:
                return Itemtitles.CONTENT_TYPE;
            case ITEM_TITLES_ID:
                return Itemtitles.CONTENT_ITEM_TYPE;  
            case ITEM_TAGS:
                return Itemtags.CONTENT_TYPE;
            case ITEM_TAGS_ID:
                return Itemtags.CONTENT_ITEM_TYPE;  
            case ITEM_COMMENTS:
                return Itemcomments.CONTENT_TYPE;
            case ITEM_COMMENTS_ID:
                return Itemcomments.CONTENT_ITEM_TYPE;
            case ITEM_ID_ITEM_TITLES:
            	 return Itemtitles.CONTENT_TYPE;
            case ITEM_ID_ITEM_TAGS:
            	 return Itemtags.CONTENT_TYPE;
            case ITEM_ID_ITEM_COMMENTS:
           	 	 return Itemcomments.CONTENT_TYPE; 
            case USER_ID_QUIZS:
            	return Quizs.CONTENT_TYPE;
            case PROFILES:
                return Profiles.CONTENT_TYPE;
            case PROFILES_ID:
                return Profiles.CONTENT_ITEM_TYPE;   
            case QUESTIONS:
                return Questions.CONTENT_ITEM_TYPE;
            case QUESTIONS_ID:
                return Questions.CONTENT_ITEM_TYPE;  
            case ANSWERS:
                return Answers.CONTENT_ITEM_TYPE;
            case ANSWERS_ID:
                return Answers.CONTENT_ITEM_TYPE;      
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        if (LOGV) Log.v(TAG, "query(uri=" + uri + ", proj=" + Arrays.toString(projection) + ")");
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            default: {
                final SelectionBuilder builder = buildExpandedSelection(uri, match);
                return builder.where(selection, selectionArgs).query(db, projection, this.buildGroupby(match), sortOrder);
            }
//            case NOTES_EXPORT: {
//                // Provide query values for file attachments
//                final String[] columns = { OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE };
//                final MatrixCursor cursor = new MatrixCursor(columns, 1);
//                cursor.addRow(new String[] { "notes.xml", null });
//                return cursor;
//            }
//            case SEARCH_SUGGEST: {
//                final SelectionBuilder builder = new SelectionBuilder();
//
//                // Adjust incoming query to become SQL text match
//                selectionArgs[0] = selectionArgs[0] + "%";
//                builder.table(Tables.SEARCH_SUGGEST);
//                builder.where(selection, selectionArgs);
//                builder.map(SearchManager.SUGGEST_COLUMN_QUERY,
//                        SearchManager.SUGGEST_COLUMN_TEXT_1);
//
//                projection = new String[] { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
//                        SearchManager.SUGGEST_COLUMN_QUERY };
//
//                final String limit = uri.getQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT);
//                return builder.query(db, projection, null, null, SearchSuggest.DEFAULT_SORT, limit);
//            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (LOGV) Log.v(TAG, "insert(uri=" + uri + ", values=" + values.toString() + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS: {
                db.insertOrThrow(Tables.ITEMS, null, values);
                return Items.buildItemUri(values.getAsString(Items.ITEM_ID));
            }
            case QUESTIONS:{
            	 db.insertOrThrow(Tables.QUESTIONS, null, values);
                 return Questions.buildQuestionUri(values.getAsString(Questions.QUESTION_ID));
            }
            case ANSWERS:{
           	 db.insertOrThrow(Tables.ANSWERS, null, values);
                return Answers.buildAnswerUri(values.getAsString(Answers.ANSWER_ID));
           }
            case LANGUAGES: {
                db.insertOrThrow(Tables.LANGUAGES, null, values);
                return Languages.buildLanguageUri(values.getAsString(Languages.LANGUAGE_ID));
            }
            case ITEM_TITLES: {
                db.insertOrThrow(Tables.ITEMS_TITLES, null, values);
                return Itemtitles.buildItemtitleUri(values.getAsString(values.getAsString(Itemtitles.ITEMTITLE_ID)));
            }
            case ITEM_TAGS: {
                db.insertOrThrow(Tables.ITEMS_TAGS, null, values);
                return Itemtags.buildItemTagUri(values.getAsString(values.getAsString(Itemtags.ITEMTAG_ID)));
            }
            case ITEM_COMMENTS: {
                db.insertOrThrow(Tables.ITEMS_COMMENTS, null, values);
                return Itemcomments.buildItemCommentUri(values.getAsString(values.getAsString(Itemcomments.ITEMCOMMENT_ID)));
            }
            case SETTINGS: {
                db.insertOrThrow(Tables.SETTING, null, values);
                return Settings.buildSettingUri(values.getAsString(values.getAsString(Settings.SETTING_ID)));
            }
            case QUIZS: {
                db.insertOrThrow(Tables.QUIZS, null, values);
                return Quizs.buildQuizUri(values.getAsString(values.getAsString(Quizs.QUIZ_ID)));
            }
            case CHOICES: {
                db.insertOrThrow(Tables.CHOICES, null, values);
                return Quizs.buildQuizUri(values.getAsString(values.getAsString(Choices.CHOICE_ID)));
            }
            case PROFILES: {
                db.insertOrThrow(Tables.PROFILES, null, values);
                return Profiles.buildProfileUri(values.getAsString(values.getAsString(Profiles.PROFILE_ID)));
            }
            case NOTIFYS: {
                long notifyId = db.insertOrThrow(Tables.NOTIFYS, null, values);
                return Notifys.buildNotifyUri(String.valueOf(notifyId));
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (LOGV) Log.v(TAG, "update(uri=" + uri + ", values=" + values.toString() + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        return builder.where(selection, selectionArgs).update(db, values);
    }

    /** {@inheritDoc} */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (LOGV) Log.v(TAG, "delete(uri=" + uri + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        return builder.where(selection, selectionArgs).delete(db);
    }

    /**
     * Apply the given set of {@link ContentProviderOperation}, executing inside
     * a {@link SQLiteDatabase} transaction. All changes will be rolled back if
     * any single one fails.
     */
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Build a simple {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually enough to support {@link #insert},
     * {@link #update}, and {@link #delete} operations.
     */
    private SelectionBuilder buildSimpleSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS: {
                return builder.table(Tables.ITEMS);
            }
            case ITEMS_ID: {
                final String itemId = Items.getItemId(uri);
                return builder.table(Tables.ITEMS)
                        .where(Items.ITEM_ID + "=?", itemId);
            }
            case QUESTIONS:{
            	return builder.table(Tables.QUESTIONS);
           }
            case QUESTIONS_ID: {
                final String questionId = Questions.getQuestionId(uri);
                return builder.table(Tables.QUESTIONS)
                        .where(Questions.QUESTION_ID + "=?", questionId);
            }
            case ITEM_ID_ANSWERS:{
            	final String itemId = Items.getItemId(uri);
            	return builder.table(Tables.ANSWERS)
            				  .where(Answers.ITEM_ID+"=?", itemId);
            }
            case ITEM_ID_QUESTIONS:{
            	final String itemId = Items.getItemId(uri);
            	return builder.table(Tables.QUESTIONS)
            				  .where(Questions.ITEM_ID+"=?", itemId);
            }
           case ANSWERS:{
        	   return builder.table(Tables.ANSWERS);
           }
           case ANSWERS_ID: {
               final String answerId = Answers.getAnswerId(uri);
               return builder.table(Tables.ANSWERS)
                       .where(Answers.ANSWER_ID + "=?", answerId);
           }
            case LANGUAGES: {
                return builder.table(Tables.LANGUAGES);
            }
            case LANGUAGES_ID: {
                final String languageId = Languages.getLanguageId(uri);
                return builder.table(Tables.LANGUAGES)
                        .where(Languages.LANGUAGE_ID + "=?", languageId);
            }
            case ITEM_TITLES: {
                return builder.table(Tables.ITEMS_TITLES);
            }
            case ITEM_TITLES_ID: {
                final String itemtitleId = Itemtitles.getItemtitleId(uri);
                return builder.table(Tables.ITEMS_TITLES)
                        .where(Itemtitles.ITEMTITLE_ID + "=?", itemtitleId);
            }
            case ITEM_TAGS: {
                return builder.table(Tables.ITEMS_TAGS);
            }
            case ITEM_TAGS_ID: {
                final String itemtagId = Itemtags.getItemTagId(uri);
                return builder.table(Tables.ITEMS_TAGS)
                        .where(Itemtags.ITEMTAG_ID + "=?", itemtagId);
            }
            case ITEM_COMMENTS: {
                return builder.table(Tables.ITEMS_COMMENTS);
            }
            case ITEM_COMMENTS_ID: {
                final String itemcommentId = Itemcomments.getItemCommentId(uri);
                return builder.table(Tables.ITEMS_COMMENTS)
                        .where(Itemcomments.ITEMCOMMENT_ID + "=?", itemcommentId);
            }
            case ITEM_ID_ITEM_TITLES:{
            	final String itemId = Items.getItemId(uri);
            	return builder.table(Tables.ITEMS_TITLES).where(Itemtitles.ITEM_ID + "=?", itemId);
            }
            case ITEM_ID_ITEM_TAGS:{
            	final String itemId = Items.getItemId(uri);
            	return builder.table(Tables.ITEMS_TAGS).where(Itemtags.ITEM_ID + "=?", itemId);
            }
            case ITEM_ID_ITEM_COMMENTS:{
            	final String itemId = Items.getItemId(uri);
            	return builder.table(Tables.ITEMS_COMMENTS).where(Itemcomments.ITEM_ID + "=?", itemId);
            }
            case QUIZS: {
                return builder.table(Tables.QUIZS);
            }
            case QUIZS_ID: {
                final String quizId = Quizs.getQuizId(uri);
                return builder.table(Tables.QUIZS)
                        .where(Quizs.QUIZ_ID + "=?", quizId);
            }
            case CHOICES: {
                return builder.table(Tables.CHOICES);
            }
            case CHOICES_ID: {
                final String choiceId = Choices.getChoiceId(uri);
                return builder.table(Tables.CHOICES)
                        .where(Choices.CHOICE_ID + "=?", choiceId);
            }
            case SETTINGS: {
                return builder.table(Tables.SETTING);
            }
            case SETTINGS_ID: {
                final String settingId = Settings.getSettingId(uri);
                return builder.table(Tables.SETTING)
                        .where(Settings.SETTING_ID + "=?", settingId);
            }
            case PROFILES: {
                return builder.table(Tables.PROFILES);
            }
            case PROFILES_ID: {
                final String profileId = Profiles.getProfileId(uri);
                return builder.table(Tables.PROFILES)
                        .where(Profiles.PROFILE_ID + "=?", profileId);
            }
            case NOTIFYS: {
                return builder.table(Tables.NOTIFYS);
            }
            case NOTIFYS_ID: {
                final String notifyId = Notifys.getNotifyId(uri);
                return builder.table(Tables.NOTIFYS)
                        .where(Notifys._ID + "=?", notifyId);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }
    
    private String buildGroupby(int match) {
    	switch (match) {
	        case ITEMS: {
	        	return " items.item_id ";
	        }
	        case USER_ID_ITEMS: {
	        	return " items.item_id ";
	        }
	        case ITEM_SEARCH: {
	        	return " items.item_id ";
	        }
	        default: {
	        	return null;
	        }
    	}
    }
        
    

    /**
     * Build an advanced {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually only used by {@link #query}, since it
     * performs table joins useful for {@link Cursor} data.
     */
    private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
        final SelectionBuilder builder = new SelectionBuilder();
        switch (match) {
            case ITEMS: {
            	return builder.table(Tables.ITEMS_JOIN_ITEMSTITLES)
          		.mapToTable(BaseColumns._ID, Tables.ITEMS)
          		.mapToTable(Items.ITEM_ID, Tables.ITEMS)
          		.mapToTable(Itemtitles.CONTENT, Tables.ITEMS_TITLES)
          		.map(Items.TITLES, Subquery.ITEM_TITLES)
          		.map(BaseColumns._ID, Subquery.ITEM_ID);
//          		.where(Tables.ITEMS+"."+Items.DISABLED+"!=?", "1");
            }
            case ITEMS_ID: {
                final String itemId = Items.getItemId(uri);
                return builder.table(Tables.ITEMS)
                        .where(Items.ITEM_ID + "=?", itemId);
            }
            case ITEM_ID_QUESTIONS:{
            	 final String itemId = Items.getItemId(uri);
            	return builder.table(Tables.QUESTIONS_JOIN_ITEMS)
            	.mapToTable(BaseColumns._ID, Tables.ITEMS)
            	.mapToTable(Items.ITEM_ID, Tables.ITEMS)
            	.mapToTable(Questions.CONTENT, Tables.QUESTIONS)
            	.where(Tables.ITEMS+"."+Items.ITEM_ID+"=?", itemId);
            }
            case ITEM_ID_ANSWERS:{
           	 final String itemId = Items.getItemId(uri);
           	return builder.table(Tables.ITEMS_JOIN_ANSWERS)
           	.mapToTable(BaseColumns._ID, Tables.ITEMS)
           	.mapToTable(Items.ITEM_ID, Tables.ITEMS)
           	.mapToTable(Answers.CONTENT, Tables.ANSWERS)
           	.mapToTable(Answers.UPDATE_TIME, Tables.ANSWERS)
           	.mapToTable(Answers.NICKNAME, Tables.ANSWERS)
           	.where(Tables.ITEMS+"."+Items.ITEM_ID+"=?", itemId);
           }
            case QUESTIONS:{
            	return builder.table(Tables.QUESTIONS);
            }
            case QUESTIONS_ID: {
                final String questionId = Questions.getQuestionId(uri);
                return builder.table(Tables.QUESTIONS)
                        .where(Questions.QUESTION_ID+ "=?", questionId);
            }
            case ANSWERS:{
            	return builder.table(Tables.ANSWERS);
            }
            case ANSWERS_ID: {
                final String answerId = Answers.getAnswerId(uri);
                return builder.table(Tables.ANSWERS)
                        .where(Answers.ANSWER_ID + "=?", answerId);
            }
            case LANGUAGES: {
                return builder.table(Tables.LANGUAGES);
            }
            case LANGUAGES_ID: {
                final String languageId = Languages.getLanguageId(uri);
                return builder.table(Tables.ITEMS)
                        .where(Languages.LANGUAGE_ID + "=?", languageId);
            }
            case ITEM_ID_ITEM_TITLES:{
            	final String itemId = Items.getItemId(uri);
                return builder.table(Tables.ITEMSTITLES_JOIN_ITEMS_LANGUAGES)
                		.mapToTable(BaseColumns._ID, Tables.ITEMS_TITLES)
                        .mapToTable(Itemtitles.ITEM_ID, Tables.ITEMS_TITLES)
                        .mapToTable(Itemtitles.LANGUAGE_ID, Tables.ITEMS_TITLES)
                        .mapToTable(Itemtitles.CONTENT, Tables.ITEMS_TITLES)
                        .mapToTable(Languages.NAME, Tables.LANGUAGES)
                        .where(Tables.ITEMS_TITLES+"."+Itemtitles.ITEM_ID + "=?", itemId);
            }
            case ITEM_ID_ITEM_TAGS:{
            	final String itemId = Items.getItemId(uri);
                return builder.table(Tables.ITEMS_TAGS)
                        .where(Itemtags.ITEM_ID + "=?", itemId);
            }
            case ITEM_ID_ITEM_COMMENTS:{
            	final String itemId = Items.getItemId(uri);
                return builder.table(Tables.ITEMS_COMMENTS)
                        .where(Itemtags.ITEM_ID + "=?", itemId);
            }
            case ITEM_TITLES: {
                return builder.table(Tables.ITEMSTITLES_JOIN_LANGUAGES)
                	.mapToTable(BaseColumns._ID, Tables.ITEMS_TITLES)
                	.mapToTable(Itemtitles.LANGUAGE_ID, Tables.ITEMS_TITLES)
                	.mapToTable(Itemtitles.CONTENT, Tables.ITEMS_TITLES)
                    .mapToTable(Languages.NAME, Tables.LANGUAGES);
            }
            case ITEM_SEARCH:{
            	final String search = Items.getItemSearch(uri);
            	if(search!=null&&search.contains("geo:")){
            		Map<String,Double> map = GeoUtils.parseGeoUri(search);
            		Double lat = map.get("latitude");
            		Double lng = map.get("longitude");
            		if(lat!=null&&lng!=null){
            			Map<String,Double>range = GeoUtils.getKMRange(lat, lng, 0.25);
            			Double x1 = range.get("x1");
            			Double y1 = range.get("y1");
            			Double x2 = range.get("x2");
            			Double y2 = range.get("y2");
            			if(x1!=null&&x2!=null&&y1!=null&&y2!=null){
            				if(y1>y2){
            					return builder.table(Tables.ITEMS_JOIN_ITEMSTITLES)
            	          		.mapToTable(BaseColumns._ID, Tables.ITEMS)
            	          		.mapToTable(Items.ITEM_ID, Tables.ITEMS)
            	          		.mapToTable(Itemtitles.CONTENT, Tables.ITEMS_TITLES)
            	          		.map(Items.TITLES, Subquery.ITEM_TITLES)
            	          		.map(BaseColumns._ID, Subquery.ITEM_ID)
            	          		.where(Tables.ITEMS+"."+Items.LATITUTE+">=?", x2.toString())
            	          		.where(Tables.ITEMS+"."+Items.LATITUTE+"<=?", x1.toString())
            	          		.where(Tables.ITEMS+"."+Items.LNGITUTE+">=?", y2.toString())
            	          		.where(Tables.ITEMS+"."+Items.LNGITUTE+"<=?", y1.toString());
            				}else{
            					return builder.table(Tables.ITEMS_JOIN_ITEMSTITLES)
            	          		.mapToTable(BaseColumns._ID, Tables.ITEMS)
            	          		.mapToTable(Items.ITEM_ID, Tables.ITEMS)
            	          		.mapToTable(Itemtitles.CONTENT, Tables.ITEMS_TITLES)
            	          		.map(Items.TITLES, Subquery.ITEM_TITLES)
            	          		.map(BaseColumns._ID, Subquery.ITEM_ID)
            	          		.where(Tables.ITEMS+"."+Items.LATITUTE+">=?", x2.toString())
            	          		.where(Tables.ITEMS+"."+Items.LATITUTE+"<=?", x1.toString())
            	          		.where("("+Tables.ITEMS+"."+Items.LNGITUTE+"<="+y1.toString()+" and "+Items.LNGITUTE+">=-180"+") or ("+Items.LNGITUTE+">="+y2.toString()+" and "+Items.LATITUTE+"<=180"+"))", new String[]{});
            				}
            			}
            			
//            			if (form.isMapenabled() && form.getX1() != null && form.getY1() != null
//            					&& form.getX2() != null && form.getY2() != null) {
//            				if (form.getY1() > form.getY2()) {
//            					criteria.add(
//            							Restrictions.and(
//            								Restrictions.between("itemLat", form.getX2(),form.getX1()), 
//            								Restrictions.between("itemLng", form.getY2(), form.getY1())
//            							)
//            						);
//            				} else {
//            					criteria.add(
//            							Restrictions.or(
//            								Restrictions.and(
//            										Restrictions.between("itemLat", form.getX2(),form.getX1()), 
//            										Restrictions.between("itemLng", form.getY2(), 180d)
//            								), 
//            								Restrictions.and(
//            										Restrictions.between("itemLat", form.getX2(), form.getX1()), 
//            										Restrictions.between("itemLng", -180d, form.getY1())
//            								)
//            							)
//            						);
//            				}
//            			}
            		}
            	}	
            	
            	return builder.table(Tables.ITEMS_JOIN_ITEMSTITLES)
          		.mapToTable(BaseColumns._ID, Tables.ITEMS)
          		.mapToTable(Items.ITEM_ID, Tables.ITEMS)
          		.mapToTable(Itemtitles.CONTENT, Tables.ITEMS_TITLES)
          		.map(Items.TITLES, Subquery.ITEM_TITLES)
          		.map(BaseColumns._ID, Subquery.ITEM_ID)
          		.where(" ("+Tables.ITEMS_TITLES+"."+Itemtitles.CONTENT + " like '%"+search+"%' or "+Tables.ITEMS+"."+Items.NICK_NAME+" like '%"+search+"%') ", new String[]{});
//            		return builder.table(Tables.ITEMSTITLES_JOIN_ITEMS_LANGUAGES)
//             		.mapToTable(BaseColumns._ID, Tables.ITEMS_TITLES)
//                     .mapToTable(Itemtitles.ITEM_ID, Tables.ITEMS_TITLES)
//                     .mapToTable(Itemtitles.LANGUAGE_ID, Tables.ITEMS_TITLES)
//                     .mapToTable(Itemtitles.CONTENT, Tables.ITEMS_TITLES)
//                     .mapToTable(Languages.NAME, Tables.LANGUAGES)
//                     .where(Tables.ITEMS_TITLES+"."+Itemtitles.CONTENT + " like %?%", search);
//            	}
            	
            }
            case USER_ID_ITEMS:{
            	final String userId = Users.getUserId(uri);
            	return builder.table(Tables.ITEMS_JOIN_ITEMSTITLES)
          		.mapToTable(BaseColumns._ID, Tables.ITEMS)
          		.mapToTable(Items.ITEM_ID, Tables.ITEMS)
          		.mapToTable(Itemtitles.CONTENT, Tables.ITEMS_TITLES)
          		.map(Items.TITLES, Subquery.ITEM_TITLES)
          		.map(BaseColumns._ID, Subquery.ITEM_ID)
          		.where(Tables.ITEMS+"."+Items.AUTHOR_ID + "=?", userId);
//                return builder.table(Tables.ITEMSTITLES_JOIN_ITEMS_LANGUAGES)
//                		.mapToTable(BaseColumns._ID, Tables.ITEMS_TITLES)
//                        .mapToTable(Itemtitles.ITEM_ID, Tables.ITEMS_TITLES)
//                        .mapToTable(Itemtitles.LANGUAGE_ID, Tables.ITEMS_TITLES)
//                        .mapToTable(Itemtitles.CONTENT, Tables.ITEMS_TITLES)
//                        .mapToTable(Languages.NAME, Tables.LANGUAGES)
//                        .where(Tables.ITEMS_TITLES+"."+Itemtitles.ITEM_ID + "=?", itemId);
            }
            case USER_ID_SETTINGS:{
            	final String userId = Users.getUserId(uri);
            	return builder.table(Tables.SETTING)
          		.where(Tables.SETTING+"."+Settings.AUTHOR_ID + "=?", userId);
            }
            case USER_ID_QUIZS:{
            	final String userId = Users.getUserId(uri);
            	return builder.table(Tables.QUIZS)
          		.where(Tables.QUIZS +"."+Quizs.AUTHOR_ID + "=?", userId)
          		.where(Tables.QUIZS +"."+Quizs.ANSWER_STATE + "=?", Constants.NotAnsweredState.toString());
            }
            case QUIZ_ID_CHOICES:{
            	final String quizId = Quizs.getQuizId(uri);
            	return builder.table(Tables.CHOICES).where(Choices.QUIZ_ID+" =?", quizId);
            }
            case QUIZS: {
                return builder.table(Tables.QUIZS);
            }
            case QUIZS_ID: {
                final String quizId = Quizs.getQuizId(uri);
                return builder.table(Tables.QUIZS)
                        .where(Quizs.QUIZ_ID + "=?", quizId);
            }
            case CHOICES: {
                return builder.table(Tables.CHOICES);
            }
            case CHOICES_ID: {
                final String choiceId = Choices.getChoiceId(uri);
                return builder.table(Tables.CHOICES)
                        .where(Choices.CHOICE_ID + "=?", choiceId);
            }
            case USER_ID_SETTINGS_LANGUAGES:{
            	final String userId = Users.getUserId(uri);
            	return builder.table(Tables.SETTING_JOIN_LANGUAGES)
	      		  .mapToTable(BaseColumns._ID, Tables.SETTING)
	      		  .mapToTable(Settings.NAME, Tables.SETTING)
	      		  .where(Tables.SETTING+"."+Settings.AUTHOR_ID+"=?", userId)
	      		  .where(Tables.SETTING+"."+Settings.FIELD+" in (?, ?)", new String[]{Settings.SETTING_MYLAN_FIELD_ID.toString(), Settings.SETTING_STUDYLAN_FIELD_ID.toString()});
            }
            case SETTINGS_LANGUAGES:{
            	return builder.table(Tables.SETTING_JOIN_LANGUAGES)
            		  .mapToTable(BaseColumns._ID, Tables.SETTING)
            		  .mapToTable(Settings.NAME, Tables.SETTING)
            		  .where(Tables.SETTING+"."+Settings.FIELD+" in (?, ?)", new String[]{Settings.SETTING_MYLAN_FIELD_ID.toString(), Settings.SETTING_STUDYLAN_FIELD_ID.toString()});
            }
            case PROFILES: {
                return builder.table(Tables.PROFILES);
            }
            case PROFILES_ID: {
                final String profileId = Profiles.getProfileId(uri);
                return builder.table(Tables.PROFILES)
                        .where(Profiles.PROFILE_ID + "=?", profileId);
            }
            case NOTIFYS: {
                return builder.table(Tables.NOTIFYS);
            }
            case NOTIFYS_ID: {
                final String notifyId = Notifys.getNotifyId(uri);
                return builder.table(Tables.NOTIFYS)
                        .where(Notifys._ID + "=?", notifyId);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }
    
    private interface Subquery {
        String ITEM_TITLES = "group_concat(content,', ')";
        String ITEM_ID = Tables.ITEMS+"."+BaseColumns._ID;
        
//        String BLOCK_CONTAINS_STARRED = "(SELECT MAX(" + Qualified.SESSIONS_STARRED + ") FROM "
//                + Tables.SESSIONS + " WHERE " + Qualified.SESSIONS_BLOCK_ID + "="
//                + Qualified.BLOCKS_BLOCK_ID + ")";
//
//        String TRACK_SESSIONS_COUNT = "(SELECT COUNT(" + Qualified.SESSIONS_TRACKS_SESSION_ID
//                + ") FROM " + Tables.SESSIONS_TRACKS + " WHERE "
//                + Qualified.SESSIONS_TRACKS_TRACK_ID + "=" + Qualified.TRACKS_TRACK_ID + ")";
//
//        String TRACK_VENDORS_COUNT = "(SELECT COUNT(" + Qualified.VENDORS_VENDOR_ID + ") FROM "
//                + Tables.VENDORS + " WHERE " + Qualified.VENDORS_TRACK_ID + "="
//                + Qualified.TRACKS_TRACK_ID + ")";
//
//        String SESSIONS_SNIPPET = "snippet(" + Tables.SESSIONS_SEARCH + ",'{','}','\u2026')";
//        String VENDORS_SNIPPET = "snippet(" + Tables.VENDORS_SEARCH + ",'{','}','\u2026')";
    }


//    @Override
//    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
//        final int match = sUriMatcher.match(uri);
//        switch (match) {
//            case NOTES_EXPORT: {
//                try {
//                    final File notesFile = NotesExporter.writeExportedNotes(getContext());
//                    return ParcelFileDescriptor
//                            .open(notesFile, ParcelFileDescriptor.MODE_READ_ONLY);
//                } catch (IOException e) {
//                    throw new FileNotFoundException("Unable to export notes: " + e.toString());
//                }
//            }
//            default: {
//                throw new UnsupportedOperationException("Unknown uri: " + uri);
//            }
//        }
//    }


}
