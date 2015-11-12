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
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

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


                        //error = onPreExecute();
                        err = send();
                        //onPostExecute();

                        return;
                    }

                })).start();
    }
/*
    public String onPreExecute () {
        myDB = new DatabaseHelper(MainActivity.this);
        //dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);


        try {
            url = new URL("http://db.science.uoit.ca:9000/send_data");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            // writer = new BufferedOutputStream(urlConnection.getOutputStream());
            writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
            listener = new BufferedInputStream(urlConnection.getInputStream());

        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
        return "";
    }


    protected String send(){

        ScanRecord[] records = myDB.getAll();
        int totalRecords = records.length;
        String postData = "";
        String response = "";
        byte[] contents = new byte[1024];
        int bytesRead = 0;
        int currentRecord = 1;


        //Kill task if there are no records
        if (totalRecords == 0) {
            e1 = "Nothing in the local database to send";
            return e1;
           // Toast.makeText(getApplicationContext(), "Nothing in the local database to send.", Toast.LENGTH_LONG).show();
           // return null;
        }

        //send each record to server
        for (ScanRecord r : records) {

            //create the post data in a string
            try {
                postData = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(r.idToString(), "UTF-8")
                        + "&" + URLEncoder.encode("stamp", "UTF-8") + "=" + URLEncoder.encode(r.timestamp, "UTF-8")
                        + "&" + URLEncoder.encode("ssid", "UTF-8") + "=" + URLEncoder.encode(r.ssid, "UTF-8")
                        + "&" + URLEncoder.encode("strength", "UTF-8") + "=" + URLEncoder.encode(r.levelToString(), "UTF-8")
                        + "&" + URLEncoder.encode("android_id", "UTF-8") + "=" + URLEncoder.encode(android_id, "UTF-8");
            } catch (Exception e) {
                //oast.makeText(getApplicationContext(), "Encoding error: " + e.toString(), Toast.LENGTH_LONG).show();
                return e.toString();
            }

            //send data
            try {
                writer.write(postData);
                writer.flush();

                while ((bytesRead = listener.read(contents)) != -1) {
                    response += new String(contents, 0, bytesRead);
                }

                if (!response.isEmpty()) {
                   // Toast.makeText(getApplicationContext(), "Problem sending data, received response from server: "
                     //       + response, Toast.LENGTH_LONG).show();
                    if (!response.equals("good")) {
                        e2 = "Problem sending data, received response from server: " + response;
                        return e2;
                    }
                }

                //remove record from local database
                myDB.delete(r.id);


            } catch (Exception e) {
                //Toast.makeText(getApplicationContext(), "Problem sending data: " + e.toString(), Toast.LENGTH_LONG).show();
                return e.toString();
               // return null;
            } //end try to send data

            //update progress bar
           // dialog.setProgress((int) Math.ceil(currentRecord / totalRecords));
            currentRecord++;

        } //for each r in record

        return null;
    }

    */

    protected String send(){

        //get records and set up json
        myDB = new DatabaseHelper(MainActivity.this);
        ScanRecord[] records = myDB.getAll();
        JSONArray json_records = new JSONArray();
        JSONObject json_r;

        //convert each record into a json object and put in json array
        for (ScanRecord r : records) {
            try {
                json_r = new JSONObject();
                json_r.put("stamp", r.timestamp);
                json_r.put("ssid", r.ssid);
                json_r.put("strength", r.levelToString());
                json_r.put("android_id", android_id);
            } catch (Exception e) {
                return e.toString();
            }
            json_records.put(json_r);
        } //end for each r in record

        //can catch a variety of wonderful things
        try {
            //constants
            url = new URL("http://db.science.uoit.ca:9001/send_data");
            String message = json_records.toString();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout( 10000 /*milliseconds*/ );
            conn.setConnectTimeout( 15000 /* milliseconds */ );
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
            InputStream is = conn.getInputStream();
            //String contentAsString = readIt(is,len);

            //clean up
            os.close();
            is.close();
            conn.disconnect();
        } catch (Exception e) {
            return e.toString();
        }
        return null;
    }


    protected void onProgressUpdate(String... progress) {

    }

    protected void onPostExecute() {
        urlConnection.disconnect();

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
                                PollingUtils.startPollingService(MainActivity.this, 10 , MyService.class, MyService.ACTION);
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
