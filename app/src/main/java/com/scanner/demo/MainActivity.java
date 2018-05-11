package com.scanner.demo;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.HttpsURLConnection;

import static android.app.PendingIntent.getActivity;

public class MainActivity extends ActionBarActivity {

    private static final int REQUEST_CODE = 99;
    private Button scanButton;
    private Button cameraButton;
    private Button mediaButton;
    private Button sendButton;
    private ImageView scannedImageView;
    private String Test;
    public String filePath;
    public String file_extn;

    final String httpPath = "http://www.edumobile.org/android/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
//        RequestTask rt = new RequestTask();
//        Test= rt.doInBackground();
//        HttpClient httpClient = new DefaultHttpClient();
//        HttpGet httpGet = new HttpGet(httpPath);
//
//        try{
//            HttpEntity httpEntity = httpClient.execute(httpGet).getEntity();
//            if (httpEntity!=null){
//                InputStream inputStream = httpEntity.getContent();
//                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader())
//            }
//        }

    }

    private void init() {
        scanButton = (Button) findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new ScanButtonClickListener());
        cameraButton = (Button) findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_CAMERA));
        mediaButton = (Button) findViewById(R.id.mediaButton);
        mediaButton.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_MEDIA));
        scannedImageView = (ImageView) findViewById(R.id.scannedImage);


        //sendButton = (Button) findViewById(R.id.sendButton);
        //sendButton.setOnClickListener(new ScanButtonClickListener());
    }

    private class ScanButtonClickListener implements View.OnClickListener {

        private int preference;

        public ScanButtonClickListener(int preference) {
            this.preference = preference;
        }

        public ScanButtonClickListener() {
        }

        @Override
        public void onClick(View v) {
            startScan(preference);
        }
    }

    protected void startScan(int preference) {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
        startActivityForResult(intent, REQUEST_CODE);
    }

    public void sendServer(View view)  {
        Log.i("Scan Clicked IG", "Send Server Invoked 1 ");

       /* RequestTask rt = new RequestTask();
        //Test = rt.doInBackground();
        rt.execute();
        Log.i("DIB called on", "Scan Click");
        */

//        RequestTask rt = new RequestTask();
//        Test= rt.doInBackground();
//        Log.i("DIB called on", "Scan Click");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                getContentResolver().delete(uri, null, null);
                scannedImageView.setImageBitmap(bitmap);
                File pictureFile = getOutputMediaFile();
                if (pictureFile == null) {
                    Log.w("TAG", "Error creating media file, check storage permissions: ");// e.getMessage());
                    return;
                }
                FileOutputStream fos = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.w("TAG", "File not found: " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.w("TAG", "Error accessing file: " + e.getMessage());
            }

        }
    }


    private Bitmap convertByteArrayToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
//        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
//                + "/Android/data/"
//                + getApplicationContext().getPackageName()
//                + "/Files");

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/scanSample/After/");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm_ss").format(new Date());
        File mediaFile;
        String mImageName = "MI_" + timeStamp + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);

        filePath = mediaStorageDir.getPath();
        file_extn = "jpg";
//        image_name_tv.setText(filePath);

//        try {
        if (file_extn.equals("img") || file_extn.equals("jpg") || file_extn.equals("jpeg") || file_extn.equals("png")) {
            //FINE
        } else {
            //NOT IN REQUIRED FORMAT
        }
//        }
//        catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        return mediaFile;
    }

    String imageName = filePath + file_extn;


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

    class RequestTask extends AsyncTask<String, String, String> {

        @Override
        public String doInBackground(String... uri) {
            String responseString = null;
            //return uploadFile();


            try {
                URL url = new URL("http://httpbin.org/ip");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                Log.i("conn.toString()", conn.toString());
                if (conn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    Log.i("IN BG w ResponseCode", conn.getResponseMessage());
                    Log.i("Input Stream", conn.getInputStream().toString());

                    //Log.i("Output Stream", conn.getOutputStream().toString());

                    InputStream responseBody = conn.getInputStream();
                    InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                    JsonReader jsonReader = new JsonReader(responseBodyReader);

                    //Log.i("JSON READ?", jsonReader.toString() );

                    jsonReader.beginObject(); // Start processing the JSON object
                    while (jsonReader.hasNext()) { // Loop through all keys
                        String key = jsonReader.nextName(); // Fetch the next key
                        if (key.equals("origin")) { // Check if desired key
                            // Fetch the value as a String
                            String value = jsonReader.nextString(); //*****************************BOOLEAN NOT ALWAYS A STRING
                            Log.i("Placehholder",value);
                                // Do something with the value
                            break; // Break out of the loop
                        } else {
                            jsonReader.skipValue(); // Skip values of other keys
                            //String value = jsonReader.nextString();
                            //Log.i("Placehholder",value);
                        }
                    }
                } else {
                    responseString = "FAILED"; // See documentation for more info on response handling
                    Log.i("BACKGROUND MEIN", "NAHI HUA");
                }
                conn.disconnect();
            } catch (ClientProtocolException e) {
                Log.i("BACKGROUND MEIN", "ClientProtocolException");
                //TODO Handle problems..
            } catch (IOException e) {
                Log.i("BACKGROUND MEIN", "IOException");
                //TODO Handle problems..
            }
            return "Executed";
        }

        private String uploadFile() {
            String responseString = null;
            Log.d("Log", "File path" + opFilePath);
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Config.FILE_UPLOAD_URL);
            try {
                MultiPartBody
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });
                ExifInterface newIntef = new ExifInterface(opFilePath);
                newIntef.setAttribute(ExifInterface.TAG_ORIENTATION,String.valueOf(2));
                File file = new File(opFilePath);
                entity.addPart("pic", new FileBody(file));
                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();


                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                    Log.d("Log", responseString);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode + " -> " + response.getStatusLine().getReasonPhrase();
                    Log.d("Log", responseString);
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }

            return responseString;
        }


        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //Do anything with response..
        }
    }
}

