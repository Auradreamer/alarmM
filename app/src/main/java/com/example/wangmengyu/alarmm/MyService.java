package com.example.wangmengyu.alarmm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import android.text.TextUtils;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.util.Log;


public class MyService extends Service {

    public static final String ACTION = "com.example.wangmengyu.alarmm";
    private static final String TAG = "MyService";

    WifiManager wifi;
    SimpleAdapter adapter;
    List<ScanResult> scanList=new ArrayList<ScanResult>();
    DatabaseHelper myDB;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public MyService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        initDatabase();
        insertion();
        return Service.START_STICKY;
    }

    private void initDatabase() {

        myDB = new DatabaseHelper(this);

        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled() == false)
        {
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }


    }


    private void insertion() {

        scanList = wifi.getScanResults();
        boolean inserted = true;
        String date = sdf.format(new Date()).toString();

        for (ScanResult scan : scanList) {
            inserted = myDB.insert(date, scan.SSID.replace("\"", "").replace("\'", ""), scan.BSSID.replace("\"", "").replace("\'", ""), scan.level);
            if (!inserted) {
                Log.w(TAG, scan.toString());
                Toast.makeText(this, "Broken\n"+scan.toString(), Toast.LENGTH_SHORT).show();
                break;
            }
        }

        if (inserted)
            Toast.makeText(this, "Scan entered into database", Toast.LENGTH_SHORT).show();

    }

    private void insertionAll() {

        scanList = wifi.getScanResults();
        boolean inserted = true;
        String date = sdf.format(new Date()).toString();

        String sql_string = "INSERT INTO " + myDB.TABLE_NAME + " ( "
                + myDB.COL_SSID + ", "
                + myDB.COL_BSSID + ", "
                + myDB.COL_STRENGTH + ", "
                + myDB.COL_TIMESTAMP
                + ") VALUES ";

        List<String> sql_values = new ArrayList<String>();

        for (ScanResult scan : scanList) {
            sql_values.add("("
                    + scan.SSID.replace("\"", "").replace("\'", "") + ", "
                    + scan.BSSID + ", "
                    + scan.level + ", "
                    + date +")");
        }

        sql_string = sql_string + TextUtils.join(", ", sql_values) + ";";

        inserted = myDB.execute(sql_string);
        if (!inserted) {
            Log.w(TAG, sql_string);
            Toast.makeText(this, "Broken\n"+sql_string.toString(), Toast.LENGTH_SHORT).show();
        }

        if (inserted)
            Toast.makeText(this, "Scan entered into database", Toast.LENGTH_SHORT).show();

    }





    @Override
    public void onDestroy() {



    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }
}
