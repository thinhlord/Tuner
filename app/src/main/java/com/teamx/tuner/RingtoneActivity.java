package com.teamx.tuner;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RingtoneActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    Tone tone = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringtone);

        tone = (Tone) getIntent().getSerializableExtra("Tone");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        findViewById(R.id.download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filename = tone.url.substring(tone.url.lastIndexOf('/') + 1);
                File f = new File(filename);
                if (f.exists()) {
                    Toast.makeText(RingtoneActivity.this, "Ringtone already downloaded", Toast.LENGTH_SHORT).show();
                }
                else new DownloadFileTask().execute(tone.url);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_search) {
//            startActivity(new Intent(this, RingtoneActivity.class));
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private class DownloadFileTask extends AsyncTask<String, Integer, Void> {
        NotificationManager mNotifyManager;
        NotificationCompat.Builder mBuilder;

        protected void onPreExecute() {
            super.onPreExecute();
            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(RingtoneActivity.this);
            mBuilder.setContentTitle("Tuner")
                    .setContentText("Download in progress")
                    .setSmallIcon(R.mipmap.ic_launcher);
            Toast.makeText(RingtoneActivity.this,
                    "Downloading the ringtone... The download progress is on notification bar.", Toast.LENGTH_LONG).show();

        }

        protected Void doInBackground(String... params) {
            URL url;
            int count;
            String storeDir = Environment.getExternalStorageDirectory().toString() + "/Tuner/Ringtone";

            try {
                url = new URL(params[0]);
                try {
                    File f = new File(storeDir);
                    if ((f.mkdirs() || f.isDirectory())) {
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        InputStream is = con.getInputStream();
                        String pathr = url.getPath();
                        String filename = pathr.substring(pathr.lastIndexOf('/') + 1);
                        FileOutputStream fos = new FileOutputStream(storeDir + "/" + filename);
                        int lenghtOfFile = con.getContentLength();
                        byte data[] = new byte[1024];
                        long total = 0;
                        while ((count = is.read(data)) != -1) {
                            total += count;
                            // publishing the progress
                            publishProgress((int) ((total * 100) / lenghtOfFile));
                            // writing data to output file
                            fos.write(data, 0, count);
                        }

                        is.close();
                        fos.flush();
                        fos.close();
                    } else {
                        Log.e("Error", "Not found: " + storeDir);

                    }

                } catch (Exception e) {
                    e.printStackTrace();

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;

        }

        protected void onProgressUpdate(Integer... progress) {
            if (progress[0] % 10 == 0) {
                mBuilder.setProgress(100, progress[0], false);
                mNotifyManager.notify(0, mBuilder.build());
            }
        }

        protected void onPostExecute(Void result) {
            mBuilder.setContentText("Download complete");
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(0, mBuilder.build());
        }

    }
}