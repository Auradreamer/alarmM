package com.example.wangmengyu.alarmm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    Button start;
    Button stop;
    Button sync;




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

        start = (Button)findViewById(R.id.startbtn);
        stop = (Button)findViewById(R.id.stopbtn);
        sync = (Button)findViewById(R.id.syncbtn);
        start();
        stop();
        sync();









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
                                PollingUtils.startPollingService(MainActivity.this, 60, MyService.class, MyService.ACTION);
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
                        UploadTask task = new UploadTask();
                        task.execute();
                        /*String error = myHttpHelper.createConnection();
                        if (error != null) {
                            Toast.makeText(MainActivity.this, "Connection error\n" + error, Toast.LENGTH_SHORT).show();
                        }


                        myHttpHelper.removeConnection();
                        */


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


    private class UploadTask extends AsyncTask<Void, Void, Void> {

        private DatabaseHelper myDB;
        private URL url;
        private HttpURLConnection urlConnection;
        private OutputStream writer;
        private InputStream listener;
        private ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        private String android_id = Secure.getString(MainActivity.this.getContentResolver(), Secure.ANDROID_ID);


        public void onPreExecute() {
            myDB = new DatabaseHelper(MainActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            try {
                url = new URL("http://db.science.uoit.ca:9000");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                writer = new BufferedOutputStream(urlConnection.getOutputStream());
                listener = new BufferedInputStream(urlConnection.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {

            ScanRecord[] records = myDB.getAll();
            int totalRecords = records.length;
            String postData = "";
            String response = "";
            byte[] contents = new byte[1024];
            int bytesRead = 0;
            int currentRecord = 1;


            //Kill task if there are no records
            if (totalRecords == 0) {
                Toast.makeText(getApplicationContext(), "Nothing in the local database to send.", Toast.LENGTH_LONG).show();
                return null;
            }

            //send each record to server
            for (ScanRecord r : records){

                //create the post data in a string
                try {
                    postData = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(r.idToString(), "UTF-8")
                            + "&" + URLEncoder.encode("timestamp", "UTF-8") + "=" + URLEncoder.encode(r.timestamp, "UTF-8")
                            + "&" + URLEncoder.encode("ssid", "UTF-8") + "=" + URLEncoder.encode(r.ssid, "UTF-8")
                            + "&" + URLEncoder.encode("level", "UTF-8") + "=" + URLEncoder.encode(r.levelToString(), "UTF-8")
                            + "&" + URLEncoder.encode("android_id", "UTF-8") + "=" + URLEncoder.encode(android_id, "UTF-8");
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Encoding error: "+e.toString(), Toast.LENGTH_LONG).show();
                    return null;
                }


                //send data
                try {
                    writer.write(postData.getBytes());
                    writer.flush();

                    while( (bytesRead = listener.read(contents)) != -1) {
                        response+= new String(contents, 0, bytesRead);
                    }

                    if (!response.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Problem sending data, received response from server: "
                                +response, Toast.LENGTH_LONG).show();
                        return null;
                    }

                    //remove record from local database
                    myDB.delete(r.id);


                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Problem sending data: "+e.toString(), Toast.LENGTH_LONG).show();
                    return null;
                } //end try to send data

                //update progress bar
                dialog.setProgress((int)Math.ceil(currentRecord/totalRecords));
                currentRecord++;

            } //for each r in record

            return null;
        }

        protected void onProgressUpdate(String... progress) {

        }

        protected void onPostExecute(Void unused) {
            dialog.dismiss();
        }

    } //end UploadTask
}
