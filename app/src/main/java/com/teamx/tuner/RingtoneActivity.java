package com.teamx.tuner;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RingtoneActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final int NOTIFICATION_ID = 342;
    public static final int TYPE_CONTACT = 1975;
    public static final int TYPE_DOWNLOAD = 1945;
    static final int PICK_CONTACT_REQUEST = 1;

    static boolean downloading = false;
    Tone tone = null;
    String storeDir = Environment.getExternalStorageDirectory().toString() + "/Tuner/Ringtone";
    boolean showDownloadDone = true;

    public Intent getOpenFileIntent() {
        Uri uri = Uri.parse(storeDir + File.pathSeparator + tone.filename);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(uri, "file/*");
        return intent;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringtone);

        tone = (Tone) getIntent().getSerializableExtra("Tone");

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        findViewById(R.id.set_ringtone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndSetRingtone(RingtoneManager.TYPE_RINGTONE);
            }
        });

        findViewById(R.id.set_alarm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndSetRingtone(RingtoneManager.TYPE_ALARM);
            }
        });

        findViewById(R.id.set_noti).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndSetRingtone(RingtoneManager.TYPE_NOTIFICATION);
            }
        });

        findViewById(R.id.set_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndSetRingtone(TYPE_CONTACT);
            }
        });

        findViewById(R.id.download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndSetRingtone(TYPE_DOWNLOAD);
            }
        });
    }

    protected void checkAndSetRingtone(final int type) {
        if (downloading) {
            Toast.makeText(RingtoneActivity.this, "Please wait for the download complete first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isNetworkAvailable()) {
            Toast.makeText(RingtoneActivity.this, "No network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        File f = new File(storeDir, tone.filename);
        if (f.exists()) {
            if (type == TYPE_DOWNLOAD)
                Toast.makeText(RingtoneActivity.this, "Ringtone already downloaded", Toast.LENGTH_SHORT).show();
            else setRingtone(tone, type);
        } else if (type != TYPE_DOWNLOAD) {
            showDownloadDone = false;
            DownloadFileTask task = new DownloadFileTask();
            task.callback = new TaskCompleteCallback() {
                @Override
                public void onComplete() {
                    setRingtone(tone, type);
                }
            };
            task.execute(tone);
        } else {
            showDownloadDone = true;
            new DownloadFileTask().execute(tone);
        }
    }

    protected void setRingtone(Tone tone, int type) {
        if (type == TYPE_CONTACT) {
            Toast.makeText(RingtoneActivity.this, "Select your contact", Toast.LENGTH_SHORT).show();
            Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
            pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
            startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
            return;
        }
        File file = new File(storeDir, tone.filename);
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, tone.name);
        values.put(MediaStore.MediaColumns.SIZE, file.length());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
        values.put(MediaStore.Audio.Media.IS_RINGTONE, type == RingtoneManager.TYPE_RINGTONE);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, type == RingtoneManager.TYPE_NOTIFICATION);
        values.put(MediaStore.Audio.Media.IS_ALARM, type == RingtoneManager.TYPE_ALARM);
        values.put(MediaStore.Audio.Media.ARTIST, "Tuner");

        //Insert it into the database
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());
        Uri newUri = getContentResolver().insert(uri, values);

        if (newUri == null) newUri = Uri.withAppendedPath(uri, "" + getMediaIdFromFile(file));

        RingtoneManager.setActualDefaultRingtoneUri(
                this,
                type,
                newUri
        );
        switch (type) {
            case RingtoneManager.TYPE_RINGTONE:
                Toast.makeText(RingtoneActivity.this, String.format("Set %s as default ringtone", tone.name), Toast.LENGTH_SHORT).show();
                break;
            case RingtoneManager.TYPE_ALARM:
                Toast.makeText(RingtoneActivity.this, String.format("Set %s as alarm ringtone", tone.name), Toast.LENGTH_SHORT).show();
                break;
            case RingtoneManager.TYPE_NOTIFICATION:
                Toast.makeText(RingtoneActivity.this, String.format("Set %s as notification ringtone", tone.name), Toast.LENGTH_SHORT).show();
                break;
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri contactUri = data.getData();

                File file = new File(storeDir, tone.filename);

                ContentValues localContentValues = new ContentValues();
                localContentValues.put(ContactsContract.Data.CUSTOM_RINGTONE, file.getAbsolutePath());
                int i = getContentResolver().update(contactUri, localContentValues, null, null);
                Log.d("đmm", i + "");
                Toast.makeText(RingtoneActivity.this, String.format("Set %s as contact ringtone", tone.name), Toast.LENGTH_SHORT).show();

            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private long getMediaIdFromFile(File file) {
        long videoId = -1;

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());
        String[] projection = {MediaStore.Audio.AudioColumns._ID};

        Cursor cursor = getContentResolver().query(
                uri, projection, MediaStore.Audio.AudioColumns.DATA + " LIKE ?", new String[]{file.getAbsolutePath()}, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(projection[0]);
            videoId = cursor.getLong(columnIndex);
            cursor.close();
        }
        return videoId;
    }

    interface TaskCompleteCallback {
        void onComplete();
    }

    private class DownloadFileTask extends AsyncTask<Tone, Integer, Boolean> {
        NotificationManager mNotifyManager;
        NotificationCompat.Builder mBuilder;
        TaskCompleteCallback callback;

        protected void onPreExecute() {
            super.onPreExecute();
            downloading = true;
            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(RingtoneActivity.this);
            mBuilder.setContentTitle("Tuner")
                    .setContentText("Download in progress...")
                    .setSmallIcon(R.mipmap.ic_launcher);
            Toast.makeText(RingtoneActivity.this,
                    "Downloading the ringtone... The download progress is on notification bar.", Toast.LENGTH_SHORT).show();
        }

        protected Boolean doInBackground(Tone... params) {
            URL url;
            int count;

            try {
                url = new URL(tone.url);
                try {
                    File file = new File(storeDir);
                    if ((file.mkdirs() || file.isDirectory())) {
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        InputStream is = con.getInputStream();
                        String path = storeDir + "/" + tone.filename;
                        Log.d("Path", path);
                        FileOutputStream fos = new FileOutputStream(path);
                        int lengthOfFile = con.getContentLength();
                        byte data[] = new byte[1024];
                        long total = 0;
                        int progressDone = -1;
                        while ((count = is.read(data)) != -1) {
                            total += count;
                            // publishing the progress
                            int progress = (int) ((total * 100) / lengthOfFile);
                            if (progress != progressDone && progress % 10 == 0) {
                                progressDone = progress;
                                publishProgress(progress);
                            }
                            // writing data to output file
                            fos.write(data, 0, count);
                        }

                        is.close();
                        fos.flush();
                        fos.close();
                        return true;
                    } else {
                        Log.e("Error", "Not found: " + storeDir);
                        return false;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        protected void onProgressUpdate(Integer... progress) {
            mBuilder.setProgress(100, progress[0], false);
            mBuilder.setContentText("Download in progress..." + progress[0] + "%");
            mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                if (showDownloadDone) {
                    mBuilder.setContentText("Download complete");
                    mBuilder.setProgress(0, 0, false);
                    PendingIntent contentIntent = PendingIntent.getActivity(
                            RingtoneActivity.this, 0, getOpenFileIntent(), PendingIntent.FLAG_CANCEL_CURRENT);
                    mBuilder.setContentIntent(contentIntent);
                    mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
                } else {
                    mNotifyManager.cancel(NOTIFICATION_ID);
                    if (callback != null) callback.onComplete();
                }
            } else {
                mNotifyManager.cancel(NOTIFICATION_ID);
                Toast.makeText(RingtoneActivity.this, "No network connection", Toast.LENGTH_SHORT).show();
            }
            downloading = false;
        }
    }
}