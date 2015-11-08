package com.example.wangmengyu.alarmm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import android.os.StrictMode;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    Button start;
    Button stop;
    Button sync;
    HttpHelper myHttpHelper;
    DatabaseHelper myDB;
    String error;



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

        // This also works (by using strickmode)

        /*
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());
                */

        sync();
    }

    // Create a new Thread to send request

    public void sendRequest() {
        new Thread((new Runnable() {
            @Override
            public void run() {

                myDB = new DatabaseHelper(MainActivity.this);
                myHttpHelper = new HttpHelper(myDB);
                error = myHttpHelper.createConnection();


            }
        })).start();
    }


    public void start() {

        start.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(MainActivity.this, MyService.class);
                        switch (view.getId()) {
                            case R.id.startbtn:
                                //startService(intent); every x minutes
                                PollingUtils.startPollingService(MainActivity.this, 300, MyService.class, MyService.ACTION);
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


                       // myHttpHelper.removeConnection();

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
