package com.shaoxinjin.pageviewer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.shaoxinjin.pageviewer.Util;

import java.util.ArrayList;

public class DbManager {
    private static final String TAG = Util.PREFIX + DbManager.class.getSimpleName();
    private SQLiteDatabase db;
    private static DbManager instance;

    private DbManager(Context context) {
        DbHelper dbHelper = new DbHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public static synchronized DbManager getInstance(Context context) {
        if (instance == null) {
            instance = new DbManager(context);
        }
        return instance;
    }

    @Override
    protected void finalize() throws Throwable {
        if (instance != null) {
            instance.close();
            instance = null;
        }
        super.finalize();
    }

    private void close() {
        db.close();
    }

    public void insertRecord(String type, String name, String url, String picUrl) {
        ContentValues c = new ContentValues();
        c.put(DbHelper.FIELD_TYPE, type);
        c.put(DbHelper.FIELD_NAME, name);
        c.put(DbHelper.FIELD_URL, url);
        c.put(DbHelper.FIELD_PICURL, picUrl);
        db.insert(DbHelper.DATABASE_TABLE_NAME, "", c);
    }

    public void deleteRecord(String url) {
        db.delete(DbHelper.DATABASE_TABLE_NAME, DbHelper.FIELD_URL + "=?", new String[]{url});
    }

    public ArrayList<DbStarRecord> queryRecords() {
        ArrayList<DbStarRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM " + DbHelper.DATABASE_TABLE_NAME;
        Cursor cursor;
        try {
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                DbStarRecord item = new DbStarRecord();
                item.type = cursor.getString(0);
                item.name = cursor.getString(1);
                item.url = cursor.getString(2);
                item.picUrl = cursor.getString(3);
                list.add(item);
            }
            cursor.close();
        } catch (Exception e) {
            Log.d(TAG, "exception in queryRecords is " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public void insertWebRecord(DbWebRecord dbWebRecord) {
        db.delete(DbHelper.DATABASE_WEB_TABLE_NAME, DbHelper.FIELD_WEB + "=?", new String[]{dbWebRecord.website});

        ContentValues c = new ContentValues();
        c.put(DbHelper.FIELD_WEB, dbWebRecord.website);
        c.put(DbHelper.FIELD_WEBURL, dbWebRecord.url);
        c.put(DbHelper.FIELD_SECTIONS, dbWebRecord.sections);
        db.insert(DbHelper.DATABASE_WEB_TABLE_NAME, "", c);
    }

    public DbWebRecord queryWebRecord(String web) {
        String sql = "SELECT * FROM " + DbHelper.DATABASE_WEB_TABLE_NAME;
        Cursor cursor;
        try {
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                DbWebRecord item = new DbWebRecord();
                item.website = cursor.getString(0);
                item.url = cursor.getString(1);
                item.sections = cursor.getString(2);
                if (item.website.equals(web)) {
                    return item;
                }
            }
            cursor.close();
        } catch (Exception e) {
            Log.d(TAG, "exception in queryRecords is " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}