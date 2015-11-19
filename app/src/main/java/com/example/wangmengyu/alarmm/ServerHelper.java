package com.example.wangmengyu.alarmm;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class ServerHelper {

    private Context context;
    //private HttpURLConnection urlConnection;
    //private BufferedWriter writer;
    //private InputStream listener;
    private String android_id;
    private String err;
    private String TAG;

    public ServerHelper(Context context) {
        this.context = context;
        android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        TAG = "ServerHelper";
    }

    public String sendRequest() {

        new Thread((new Runnable() {
            @Override
            public void run() {

                err = send();

                //return;
            }

        })).start();

        return err;
    }


    private String send(){

        //get records and set up json
        DatabaseHelper myDB = new DatabaseHelper(context);
        ScanRecord[] records = myDB.getThousand();
        JSONArray json_records = new JSONArray();
        JSONObject json_r, json_message;

        //convert each record into a json object and put in json array
        for (ScanRecord r : records) {
            try {
                json_r = new JSONObject();
                json_r.put("stamp", URLEncoder.encode(r.timestamp, "UTF-8"));
                json_r.put("ssid", r.ssid);
                json_r.put("bssid", r.bssid);
                json_r.put("strength", r.levelToString());
            } catch (Exception e) {
                Log.w(TAG, e.toString());
                return e.toString();
            }
            json_records.put(json_r);
        } //end for each r in record

        //package the android_id and the records together in one object
        try {
            json_message = new JSONObject();
            json_message.put("android_id", android_id);
            json_message.put("records", json_records);
        } catch (Exception e) {
            Log.w(TAG, e.toString());
            return e.toString();
        }

        //can catch a variety of wonderful things
        try {
            //constants
            URL url = new URL("http://db.science.uoit.ca:9001/send_data");
            String message = json_message.toString();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout( 50000 /*milliseconds*/ );
            conn.setConnectTimeout( 50000 /* milliseconds */ );
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(message.getBytes().length);

            //make some HTTP header nicety
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");

            //open
            conn.connect();

            //setup send
            OutputStream os = new BufferedOutputStream(conn.getOutputStream());
            os.write(message.getBytes());
            //clean up
            os.flush();

            myDB.deleteThousand();

            //clean up
            os.close();
            //is.close();
            conn.disconnect();
        } catch (Exception e) {
            Log.w(TAG, e.toString());
            return e.toString();
        }
        return null;
    }
}
