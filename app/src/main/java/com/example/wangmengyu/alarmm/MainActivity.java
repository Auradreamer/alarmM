package com.example.wangmengyu.alarmm;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;



public class MainActivity extends AppCompatActivity {

    Button start;
    Button stop;
    Button sync;
    String error;
    String e1;
    String e2;
    String err;

    private DatabaseHelper myDB;
    private URL url;
    private HttpURLConnection urlConnection;
    private BufferedWriter writer;
    private InputStream listener;
    private String android_id;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

        start = (Button)findViewById(R.id.startbtn);
        stop = (Button)findViewById(R.id.stopbtn);
        sync = (Button)findViewById(R.id.syncbtn);
        start();
        stop();
        sync();

    }

    public void sendRequest() {

                new Thread((new Runnable() {
                    @Override
                    public void run() {

                        err = send();

                        return;
                    }

                })).start();
    }


    protected String send(){

        //get records and set up json
        myDB = new DatabaseHelper(MainActivity.this);
        ScanRecord[] records = myDB.getAll();
        JSONArray json_records = new JSONArray();
        JSONObject json_r, json_message;

        //convert each record into a json object and put in json array
        for (ScanRecord r : records) {
            try {
                json_r = new JSONObject();
                json_r.put("stamp", URLEncoder.encode(r.timestamp, "UTF-8"));
                json_r.put("ssid", r.ssid);
                json_r.put("strength", r.levelToString());
            } catch (Exception e) {
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
            return e.toString();
        }

        //can catch a variety of wonderful things
        try {
            //constants
            url = new URL("http://db.science.uoit.ca:9001/send_data");
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

            //do somehting with response
            /* Couldn't get this stuff to work
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null)
                response.append(line);

            error = response.toString();

            if (!response.toString().equals("hello")) {
                myDB.deleteAll();
                error = "Deleted all records from local database.";
            }
            */

            myDB.deleteAll();

            //clean up
            os.close();
            //is.close();
            conn.disconnect();
        } catch (Exception e) {
            return e.toString();
        }
        return null;
    }


    public void start() {

        start.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(MainActivity.this, MyService.class);
                        switch (view.getId()) {
                            case R.id.startbtn:
                                //startService(intent);
                                PollingUtils.startPollingService(MainActivity.this, 100 , MyService.class, MyService.ACTION);
                                break;
                            default:
                                break;
                        }


                    }
                }


        );


    }

    public void stop() {

        stop.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(MainActivity.this, MyService.class);
                        switch (view.getId())
                        {
                            case R.id.stopbtn:
                                //stopService(intent);
                                PollingUtils.stopPollingService(MainActivity.this,MyService.class,MyService.ACTION);
                                break;
                            default:
                                break;
                        }


                    }
                }



        );


    }

    public void sync() {

        sync.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {



                        sendRequest();

                        if (error != null) {
                            Toast.makeText(MainActivity.this, "Connection error\n" + error, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Connection success", Toast.LENGTH_LONG).show();
                        }

                        if (err != null) {
                            Toast.makeText(MainActivity.this, "error\n" + err, Toast.LENGTH_LONG).show();
                        } else {

                            Toast.makeText(MainActivity.this, "send success", Toast.LENGTH_LONG).show();

                        }


                    }
                }


        );

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
