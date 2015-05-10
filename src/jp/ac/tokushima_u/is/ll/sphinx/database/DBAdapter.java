package jp.ac.tokushima_u.is.ll.sphinx.database;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * データベースのアダプター
 * 
 * データ構造について。
 * _id : ID(勝手に増えるし触らない)
 * llo1-4 : lloの画像ID(LLOのIDだとうまくいかんとおもう)
 * name1−4: lloの名前
 * author : クイズ製作者の名前
 * createdAt: クイズ作成,受信日をエポックタイムで
 * direction:クイズの方向(1:自分から相手へ、2:相手から自分へ)
 */
public class DBAdapter {
    @SuppressWarnings("unused")
    private static final String TAG = DBAdapter.class.getSimpleName();
    private final DBAdapter self = this;
    
    private static final String DATABASE_NAME = "sphinx.db";
    private static final int DATABASE_VERSION = 2;
    
    public static final String TABLE_NAME = "myquiz";
    public static final String COL_ID = "_id";
    public static final String COL_LLO1 = "llo1";
    public static final String COL_LLO2 = "llo2";
    public static final String COL_LLO3 = "llo3";
    public static final String COL_LLO4 = "llo4";
    
    public static final String COL_NAME1 = "name1";
    public static final String COL_NAME2 = "name2";
    public static final String COL_NAME3 = "name3";
    public static final String COL_NAME4 = "name4";
    
    public static final String COL_ANSWER = "answer";
    public static final String COL_AUTHOR = "author";
    public static final String COL_CREATEDAT = "createdAt";
    public static final String COL_DIRECTION = "direction";
    public static final String COL_TIME = "time";
    
    public static final int DIRECTION_OUTGOING = 1;
    public static final int DIRECTION_INCOMING = 2;
    
    private final Context context;
    protected DatabaseHelper dbHelper;
    protected SQLiteDatabase db;
    
    public DBAdapter(Context context) {
        this.context = context;
        dbHelper = new DatabaseHelper(this.context);
    }
    
    /**
     * SQLiteOpenHelper
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // 半角スペースも重要なので注意して確認すること
            db.execSQL(
                    "create table " + TABLE_NAME + " ("
                    + COL_ID + " integer primary key autoincrement,"
                    + COL_LLO1 + " text not null,"
                    + COL_LLO2 + " text not null,"
                    + COL_LLO3 + " text not null,"
                    + COL_LLO4 + " text not null,"
                    + COL_DIRECTION + " integer not null,"
                    + COL_NAME1 + " text not null,"
                    + COL_NAME2 + " text not null,"
                    + COL_NAME3 + " text not null,"
                    + COL_NAME4 + " text not null,"
                    + COL_AUTHOR + " text not null,"
                    + COL_ANSWER + " integer not null,"
                    + COL_TIME + " integer,"
                    + COL_CREATEDAT + " integer not null);"
                    );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("drop table if exists " + TABLE_NAME);
            onCreate(db);
        }
    }
    
    /**
     * Adapter Methods
     */
    public DBAdapter open() {
        db = dbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        dbHelper.close();
    }
    
    /*
     * App Methods
     */
    
    /**
     * すべての行を消去する
     * @return
     */
    public boolean deleteAll() {
        return db.delete(TABLE_NAME, null, null) > 0;
    }
    
    /**
     * 指定した行を消去する
     * @param id
     * @return
     */
    public boolean delete(int id) {
        return db.delete(TABLE_NAME, COL_ID + "=" + id, null) > 0;
    }
    
    /**
     * すべての行を取得する
     * @return
     */
    public Cursor getAll() {
        return db.query(TABLE_NAME, null, null, null, null, null, null);
    }
    
    /**
     * 指定した方向のみを取得する
     * @param direction
     * @return
     */
    public Cursor getSameDirection(int direction) {
        return db.query(TABLE_NAME, null, COL_DIRECTION + " = " + direction, null, null, null, "_id desc");
    }
    
    
    
    /**
     * 行を追加する
     * @param llo1
     * @param llo2
     * @param llo3
     * @param llo4
     * @param direction
     * @param name
     * @param author
     * @return long データベースID
     */
    public long create(
            String llo1,
            String llo2,
            String llo3,
            String llo4,
            String name1,
            String name2,
            String name3,
            String name4,
            int answer,
            int direction,
            String author
            ) {
        Date now = new Date();
        ContentValues values = new ContentValues();
        
        values.put(COL_LLO1, llo1);
        values.put(COL_LLO2, llo2);
        values.put(COL_LLO3, llo3);
        values.put(COL_LLO4, llo4);
        
        values.put(COL_NAME1, name1);
        values.put(COL_NAME2, name2);
        values.put(COL_NAME3, name3);
        values.put(COL_NAME4, name4);
        
        values.put(COL_DIRECTION, direction);
        values.put(COL_ANSWER, answer);
        values.put(COL_AUTHOR, author);
        values.put(COL_CREATEDAT, now.getTime());
        
        return db.insertOrThrow(TABLE_NAME, null, values);
    }
    
    /**
     * 行を指定して書き換える
     * @param llo1
     * @param llo2
     * @param llo3
     * @param llo4
     * @param direction
     * @param name
     * @param author
     * @return 
     */
    public long rewrite(
            int id,
            String llo1,
            String llo2,
            String llo3,
            String llo4,
            String name1,
            String name2,
            String name3,
            String name4,
            int answer,
            int direction,
            String author
            ) {
        Date now = new Date();
        ContentValues values = new ContentValues();
        
        values.put(COL_LLO1, llo1);
        values.put(COL_LLO2, llo2);
        values.put(COL_LLO3, llo3);
        values.put(COL_LLO4, llo4);
        
        values.put(COL_NAME1, name1);
        values.put(COL_NAME2, name2);
        values.put(COL_NAME3, name3);
        values.put(COL_NAME4, name4);
        
        values.put(COL_DIRECTION, direction);
        values.put(COL_ANSWER, answer);
        values.put(COL_AUTHOR, author);
        values.put(COL_CREATEDAT, now.getTime());
        
        return db.update(TABLE_NAME, values, COL_ID + "=" + id, null);
    }
    
    /**
     * カラムとIDを指定してデータを更新します
     * @param id
     * @param columnName
     * @param data
     * @return
     */
    public long pickupRewrite(long id, String columnName, String data) {
        ContentValues value = new ContentValues();
        value.put(columnName, data);
        
        return db.update(TABLE_NAME, value, COL_ID + "=" + id, null);
    }
    
    /**
     * カラムとIDを指定してデータを更新します
     * @param id
     * @param columnName
     * @param data
     * @return
     */
    public long pickupRewrite(long id, String columnName, long data) {
        ContentValues value = new ContentValues();
        value.put(columnName, data);
        
        return db.update(TABLE_NAME, value, COL_ID + "=" + id, null);
    }
}
