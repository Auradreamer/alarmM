package com.example.wangmengyu.alarmm;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Scan.db";
    public static final String TABLE_NAME = "scan_table";
    public static final String COL_ID = "ID";
    public static final String COL_TIMESTAMP = "TIMESTAMP";
    public static final String COL_SSID = "SSID";
    public static final String COL_BSSID = "BSSID";
    public static final String COL_STRENGTH = "STRENGTH";

    public static final String TAG = "DatabaseHelper";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_NAME
                + " (" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TIMESTAMP + " DATETIME,"
                + COL_SSID + " TEXT, "
                + COL_BSSID + " TEXT, "
                + COL_STRENGTH + " TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean insert(String t, String ssid, String bssid, int strength) {

        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TIMESTAMP, t);
        cv.put(COL_SSID, ssid);
        cv.put(COL_BSSID, bssid);
        cv.put(COL_STRENGTH, strength);
        db.insert(TABLE_NAME, null, cv);
        db.close();
        return true;
    }

    public boolean execute(String sql_string) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            db.execSQL(sql_string);
        } catch (Exception e) {
            Log.w(TAG, e.toString());
            db.close();
            return false;
        }

        Log.w(TAG, "Executed string.");
        db.close();
        return true;
    }

    public void delete(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_NAME, COL_ID + "=" + String.valueOf(id), null);
        db.close();
    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + ";");
        //db.execSQL("CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, TIMESTAMP DATETIME, SSID TEXT, STRENGTH TEXT);");

        Log.w(TAG, "Deleted all scans in local database.");
        db.close();
    }

    public void deleteThousand() {
        SQLiteDatabase db = this.getWritableDatabase();
        //"delete from " + MYDATABASE_TABLE +
        //" where "+KEY_ID+" in (select "+ KEY_ID +" from "+ MYDATABASE_TABLE+" order by _id LIMIT 3);";
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COL_ID +" IN ( "
                + "SELECT " + COL_ID + " FROM " + TABLE_NAME
                +" ORDER BY " + COL_ID + " ASC LIMIT 1000);");
        //db.execSQL("CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, TIMESTAMP DATETIME, SSID TEXT, STRENGTH TEXT);");

        Log.w(TAG, "Deleted up to one thousand scans in local database.");
        db.close();
    }

    public ScanRecord[] getAll() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery(
                "SELECT "
                + COL_ID + ", "
                + COL_TIMESTAMP + ", "
                + COL_SSID + ", "
                        + COL_BSSID + ", "
                + COL_STRENGTH
                + " FROM " + TABLE_NAME + ";", null);
        ScanRecord[] scanRecords = new ScanRecord[result.getCount()];

        int index = 0;
        while (result.moveToNext()) {
            scanRecords[index] = new ScanRecord(
                    result.getInt(0),
                    result.getString(1),
                    result.getString(2),
                    result.getString(3),
                    result.getInt(4));
            index++;
        } //end while result.moveToNext

        db.close();

        Log.w(TAG, "Found " + scanRecords.length + " scans in table.");
        return scanRecords;
    }//end getAll

    public ScanRecord[] getThousand() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery(
                "SELECT "
                        + COL_ID + ", "
                        + COL_TIMESTAMP + ", "
                        + COL_SSID + ", "
                        + COL_BSSID + ", "
                        + COL_STRENGTH
                        + " FROM " + TABLE_NAME
                        + " ORDER BY " + COL_ID + " ASC LIMIT 1000;", null);
        ScanRecord[] scanRecords = new ScanRecord[result.getCount()];

        int index = 0;
        while (result.moveToNext()) {
            scanRecords[index] = new ScanRecord(
                    result.getInt(0),
                    result.getString(1),
                    result.getString(2),
                    result.getString(3),
                    result.getInt(4));
            index++;
        } //end while result.moveToNext

        db.close();

        Log.w(TAG, "Found " + scanRecords.length + " scans in table.");
        return scanRecords;
    }//end getAll

}
