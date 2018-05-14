package com.example.android.udacitychallenge11;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

        ProgressBar progressBar;
        TextView textView;
        Button NotifBtn;
        NotificationCompat.Builder notification;
        EditText editText;
        private static final int NotificationID = 1111;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            progressBar = (ProgressBar)findViewById(R.id.progressbar1);
            //textView = (textView)findViewById(R.id.textview1)
            editText = (EditText)findViewById(R.id.urlbox);
            progressBar.setVisibility(View.INVISIBLE);


        }

        public void startForegroundService(View view){
            String URLImage = null;
            progressBar.setVisibility(View.VISIBLE);
            if(isPermissionGranted())
                new DownloadImage().execute(URLImage);
            else
                requestPermission();
        }
        private boolean isPermissionGranted()
        {
            return   ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;
        }

        private void requestPermission()
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
        }

        public void Notificationbuttonclick(View view) {
            if(view.getId() == R.id.Notificationbtn) {

                notification.setWhen(System.currentTimeMillis());                     //Time which shows in the notification
                notification.setDefaults(Notification.DEFAULT_ALL);
                notification.setPriority(NotificationCompat.PRIORITY_HIGH);           //PRIORITY_HIGH pops up a message, vibrates, sound, etc.

                Intent intent = new Intent(MainActivity.this, MainActivity.class);

                PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                notification.setContentIntent(pendingIntent);

                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                notificationManager.notify(NotificationID, notification.build());
            }
        }

    private class DownloadImage extends AsyncTask<String, Integer, String> {
        ProgressDialog mProgressDialog;
        private final int MAX_INT =100;
        private final String FILE_NAME ="lesson_11_task.jpg";
        String filepath = Environment.getExternalStorageDirectory().getPath();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setMax(MAX_INT);
        }

        @Override
        protected String doInBackground(String... Url) {
            try {
                URL url = new URL(Url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                int fileLength = connection.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream());

                OutputStream output = new FileOutputStream(filepath + "/" + FILE_NAME);

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    // Publish the progress
                    publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }

                // Close connection
                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                // Error Log
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            progressBar.setProgress(progress[0]);

            Log.e("Progress",progress[0].toString());
            if(progress[0]==MAX_INT)
            {
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View dialogView=inflater.inflate(R.layout.alert_layout, null);
                ImageView imageView = (ImageView)dialogView.findViewById(R.id.imageView);
                File file = new File(filepath + "/" + FILE_NAME);
                Glide.with(MainActivity.this).load(file).into(imageView);
                progressBar.setVisibility(View.INVISIBLE);

                final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Download Complete!")
                        .setMessage("Image Downloaded at:" + filepath + "/" + FILE_NAME)
                        .setView(dialogView).create();
                alertDialog.show();
                notification();
                notification = new NotificationCompat.Builder(getApplicationContext());
                notification.setAutoCancel(true);
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==2 && isPermissionGranted())
        {
            new DownloadImage().execute("null");
        }
        else
        {

            boolean showRationale = shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (! showRationale) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Permission Not Granted")
                        .setMessage("Grant Permissions from settings")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
            }
            else {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Permission Not Granted")
                        .setMessage("Grant Permission to continue")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermission();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                                Toast.makeText(getApplicationContext(), "Closing App", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }
        }
    }

    void notification(){


        notification.setTicker("Task Completed");   //For use with Android versions before Lollipop/5.0
        notification.setContentTitle("Image Downloaded!");
        notification.setContentText("Udacity Scholarship Program is amazing...");
        notification.setWhen(System.currentTimeMillis());                  
        notification.setDefaults(Notification.DEFAULT_ALL);                   //Default sound, vibrate pattern, etc.
        notification.setPriority(NotificationCompat.PRIORITY_HIGH);           //PRIORITY_HIGH pops up a message, vibrates, sound, etc. LOW doesn't pop up a message

        Intent intent = new Intent(MainActivity.this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(NotificationID, notification.build());
    }
}



