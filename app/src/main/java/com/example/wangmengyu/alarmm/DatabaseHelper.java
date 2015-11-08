package com.example.wangmengyu.alarmm;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Scan.db";
    public static final String TABLE_NAME = "scan_table";
    public static final String COL_ID = "ID";
    public static final String COL_TIMESTAMP = "TIMESTAMP";
    public static final String COL_SSID = "SSID";
    public static final String COL_STRENGTH = "STRENGTH";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, TIMESTAMP DATETIME, SSID TEXT, STRENGTH TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean insert(String t, String ssid, int strength) {

        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TIMESTAMP, t);
        cv.put(COL_SSID, ssid);
        cv.put(COL_STRENGTH, strength);
        db.insert(TABLE_NAME, null, cv);
        db.close();
        return true;
    }

    public void delete(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_NAME, COL_ID+"="+String.valueOf(id), null);
        db.close();
    }

    public ScanRecord[] getAll() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery(
                "SELECT "
                + COL_ID + ", "
                + COL_TIMESTAMP + ", "
                + COL_SSID + ", "
                + COL_STRENGTH
                + " FROM " + TABLE_NAME + ";", null);
        ScanRecord[] scanRecords = new ScanRecord[result.getCount()];

        int index = 0;
        while (result.moveToNext()) {
            scanRecords[index] = new ScanRecord(
                    result.getInt(0),
                    result.getString(1),
                    result.getString(2),
                    result.getInt(3));
            index++;
        } //end while result.moveToNext

        db.close();

        return scanRecords;
    }//end getAll

}
