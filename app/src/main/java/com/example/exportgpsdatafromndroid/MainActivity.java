
package com.example.exportgpsdatafromndroid;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_VIDEO_PICK = 2;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button selectVideoButton = findViewById(R.id.selectVideoButton);
        selectVideoButton.setOnClickListener(v -> {
            if (checkPermission()) {
                pickVideo();
            } else {
                requestPermission();
            }
        });
    }

    private boolean checkPermission() {
        int result = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_MEDIA_VIDEO);
        }else{
            result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        Log.d(TAG, "checkPermission: " + (result == PackageManager.PERMISSION_GRANTED ? "Permission granted" : "Permission denied"));
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_VIDEO)) {
            Toast.makeText(this, "External storage permission is required to select a video", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "requestPermission: showing rationale");
        }
        Log.d(TAG, "requestPermission: requesting permission");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_VIDEO}, PERMISSION_REQUEST_CODE);


        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: permission granted");
                pickVideo();
            } else {
                Log.d(TAG, "onRequestPermissionsResult: permission denied");
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pickVideo() {
        Log.d(TAG, "pickVideo: starting video pick intent");
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_VIDEO_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VIDEO_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedVideo = data.getData();
            Log.d(TAG, "onActivityResult: video selected: " + selectedVideo);
            extractGpsData(selectedVideo);
        }
    }

    private void extractGpsData(Uri videoUri) {
        Log.d(TAG, "extractGpsData: extracting GPS data from " + videoUri);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, videoUri);

        String location = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION);
        if (location != null) {
            Log.d(TAG, "extractGpsData: GPS data found : " + location );
            sendEmailWithGpsData(location);
        } else {
            Log.d(TAG, "extractGpsData: No GPS data found in this video");
            Toast.makeText(this, "No GPS data found in this video", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmailWithGpsData(String location  ) {
        String subject = "Video GPS Data";
        String message = "Location: " + location  ;
        Log.d(TAG, "sendEmailWithGpsData: sending email with GPS data");

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"your_email@example.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.d(TAG, "sendEmailWithGpsData: no email clients installed");
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
