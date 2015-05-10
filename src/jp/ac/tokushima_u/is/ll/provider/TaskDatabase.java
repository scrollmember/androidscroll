package jp.ac.tokushima_u.is.ll.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskDatabase extends SQLiteOpenHelper {
	private final static String DB_TABLE = "TASK_TABLE";
	private final static String DB_NAME = "Tasklearning.db";
	private final static int DB_VERSION = 1;

	public TaskDatabase(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
//		db.execSQL("CREATE TABLE TASK_TABLE (_id INTEGER PRIMARY KEY,id TEXT,create_time TEXT,lat TEXT,lng TEXT,place TEXT,title TEXT,update TEXT,author TEXT,language TEXT,location_base TEXT,level TEXT)");
		db.execSQL("CREATE TABLE TASK_TABLE (_id INTEGER PRIMARY KEY,user_id TEXT,create_time TEXT,lat TEXT,lng TEXT,place TEXT,title TEXT,update_time TEXT,author TEXT,language TEXT,location_base TEXT,level TEXT)");
		db.execSQL("CREATE TABLE script_info (_id INTEGER PRIMARY KEY,task_id TEXT,script_id TEXT,step TEXT,description TEXT)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		db.execSQL("CREATE TABLE TASK_TABLE (_id INTEGER PRIMARY KEY,id TEXT,create_time TEXT,lat TEXT,lng TEXT,place TEXT,title TEXT,update TEXT,author TEXT,language TEXT,location_base TEXT,level TEXT)");
		db.execSQL("CREATE TABLE TASK_TABLE (_id INTEGER PRIMARY KEY,user_id TEXT,create_time TEXT,lat TEXT,lng TEXT,place TEXT,title TEXT,update TEXT,author TEXT,language TEXT,location_base TEXT,level TEXT)");
	}

	interface Tables {
		String ID = "id";
		String CREATE_TIME = "create_time";
		String LAT = "lat";
		String LNG = "lag";
		String PLACE = "place";
		String TITLE = "title";
		String UPDATE_TIME = "update_time";
		String AUTHOR_ID = "author_id";
		String LANGUAGE_ID = "language_id";
		String LOCATION_BASE = "location_base";
		String LEVEL = "level";
	}
}
