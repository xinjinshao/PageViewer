package com.shaoxinjin.pageviewer.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.shaoxinjin.pageviewer.Util;

public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = Util.PREFIX + DbHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "PageViewer.db";
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_TABLE_NAME = "PageViewerStar";
    static final String FIELD_TYPE = "type";
    static final String FIELD_NAME = "name";
    static final String FIELD_URL = "url";
    static final String FIELD_PICURL = "picUrl";

    static final String DATABASE_WEB_TABLE_NAME = "PageViewWeb";
    static final String FIELD_WEB = "web";
    static final String FIELD_WEBURL = "webUrl";
    static final String FIELD_SECTIONS = "sections";

    DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
        createWebTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createTable(db);
        createWebTable(db);
    }

    private void createTable(SQLiteDatabase db) {
        Log.d(TAG, "createTable");
        StringBuilder buffer = new StringBuilder();
        buffer.append("CREATE TABLE IF NOT EXISTS ");
        buffer.append(DATABASE_TABLE_NAME);
        buffer.append("( ");
        buffer.append("`").append(FIELD_TYPE).append("` VARCHAR(256),");
        buffer.append("`").append(FIELD_NAME).append("` VARCHAR(256),");
        buffer.append("`").append(FIELD_URL).append("` VARCHAR(256),");
        buffer.append("`").append(FIELD_PICURL).append("` VARCHAR(256),");
        buffer.append("PRIMARY KEY(" + FIELD_URL + ")");
        buffer.append(")");
        db.execSQL(buffer.toString());
    }

    private void createWebTable(SQLiteDatabase db) {
        Log.d(TAG, "createWebTable");
        StringBuilder buffer = new StringBuilder();
        buffer.append("CREATE TABLE IF NOT EXISTS ");
        buffer.append(DATABASE_WEB_TABLE_NAME);
        buffer.append("( ");
        buffer.append("`").append(FIELD_WEB).append("` VARCHAR(256),");
        buffer.append("`").append(FIELD_WEBURL).append("` VARCHAR(256),");
        buffer.append("`").append(FIELD_SECTIONS).append("` VARCHAR(256),");
        buffer.append("PRIMARY KEY(" + FIELD_WEB + ")");
        buffer.append(")");
        db.execSQL(buffer.toString());
    }
}