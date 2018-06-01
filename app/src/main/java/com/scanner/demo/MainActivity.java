package com.scanner.demo;
//ye wala latest hai. Add text view
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;


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

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.mime.MultipartEntity;
//import org.apache.http.entity.mime.content.ContentBody;
//import org.apache.http.entity.mime.content.FileBody;
//import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

import static android.app.PendingIntent.getActivity;

public class MainActivity extends ActionBarActivity{

    private static final int REQUEST_CODE = 99;
    private Button scanButton;
    private Button cameraButton;
    private Button mediaButton;
    private Button sendButton;
    private ImageView scannedImageView;
    private String Test;
    public String filePath;
    public String file_extn;
    public String imageName;
//    TextView test;
    EditText test2;
    Service service;
//    final String httpPath = "http://www.edumobile.org/android/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        getSupportActionBar().setIcon(R.drawable.premium);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        // Change base URL to your upload server URL.
        service = new Retrofit.Builder().baseUrl("http://35.200.202.208:5000/").client(client).build().create(Service.class);

    }

    private void init() {
        scanButton = (Button) findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new ScanButtonClickListener());
        cameraButton = (Button) findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_CAMERA));
        mediaButton = (Button) findViewById(R.id.mediaButton);
        mediaButton.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_MEDIA));
        scannedImageView = (ImageView) findViewById(R.id.scannedImage);


//        sendButton = (Button) findViewById(R.id.sendButton);
//        sendButton.setOnClickListener(new ScanButtonClickListener());
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
        //Log.i("Img Name",mImageName);

        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);

        //filePath = mediaStorageDir.getPath();
        filePath = mediaStorageDir.getPath() + File.separator + mImageName;
        //Log.i("*****FE****",filePath);
        file_extn = "jpg";
        imageName = filePath;
        //Log.i("*****FE****",filePath+"."+file_extn);
        return mediaFile;
    }

        //String imageName = filePath + file_extn;
        //public String imageName = filePath;
        //Log.i("*****imageNAme****",imageName);

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void sendServer(View view)  {
        File file = new File(imageName);
        Log.i("Scan Clicked IG", "Send Server Invoked 1 ");
        Log.i("***FILE NAME", imageName);

        final ProgressDialog progressDoalog;
        progressDoalog = new ProgressDialog(MainActivity.this);
        progressDoalog.setMax(100);
        progressDoalog.setMessage("Reading");
//        progressDoalog.setTitle("ProgressDialog bar example");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("upload", file.getName(), reqFile);
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload_test");

        retrofit2.Call<okhttp3.ResponseBody> req = service.postImage(body, name);
//        Log.i("*****1","1111111");

        progressDoalog.show();

//        call.enqueue(new Callback < JSONResponse > ()

        req.enqueue(new Callback<ResponseBody>(){
//            Log.i("*****1","2");
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                ResponseBody rBody = response.body();
                if(response.code()==200)
                {
                    progressDoalog.dismiss();
//                String temp = response.toString();
//                Log.i("Ye aya",temp);
                String result="bakwaaaaaaaas";
                try {
                    result= response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Convert String to json object
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(result);


                    // get LL json object
                    //JSONObject json_LL = json.getJSONObject("transcription");
                    Log.i("***transcription***", jsonResponse.getString("transcription"));
                    result = jsonResponse.getString("transcription");




                    // get value from LL Json Object
                    //String str_value=json_LL.getString("transcr"); //<< get value here*/

                } catch (JSONException e) {
                    Log.i("JSON Exception",e.toString());
                    e.printStackTrace();
                }
                test2=(EditText)findViewById(R.id.editText);
                test2.setText(result);
//                test = (TextView)findViewById(R.id.textView);
//                test.setText(result);
                Log.i("HO GYA", "Coming from onRespCall");
                if(result!="")
                {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("transcription",result);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getApplicationContext(),"Text has been copied to clipboard",Toast.LENGTH_SHORT).show();
                }
                else
                    {
                        Toast.makeText(getApplicationContext(),"Server has problems reading data :-(",Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Server temporarily down (Error Maintenance)",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                progressDoalog.dismiss();
                if(isNetworkAvailable()==false)
                    Toast.makeText(getApplicationContext(),"Please connect to the internet",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(),"Please check your connection to the server (Server temporarily down)",Toast.LENGTH_SHORT).show();

                Log.i("NAHI HUA", "Coming from onFailiure");
            }
        });
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

    class RequestTask extends AsyncTask<String, String, String> {

        @Override
        public String doInBackground(String... uri) {
            String responseString = null;

//            File file = new File(imageName);
//            Log.i("FILE NAME", "PASS HUA");
//
//            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
//            MultipartBody.Part body = MultipartBody.Part.createFormData("upload", file.getName(), reqFile);
//            RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload_test");
//
//            retrofit2.Call<okhttp3.ResponseBody> req = service.postImage(body, name);
//            req.enqueue(new Callback<ResponseBody>() {
//                @Override
//                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                    // Do Something
//                    Log.i("HO GYA", "Coming from onRespCall");
//                }
//
//                @Override
//                public void onFailure(Call<ResponseBody> call, Throwable t) {
//                    t.printStackTrace();
//                    Log.i("NAHI HUA", "Coming from onFailiure");
//                }
//            });

            //return uploadFile();


//            try {
//                URL url = new URL("http://httpbin.org/ip");
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//                Log.i("conn.toString()", conn.toString());
//                if (conn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
//                    Log.i("IN BG w ResponseCode", conn.getResponseMessage());
//                    Log.i("Input Stream", conn.getInputStream().toString());
//
//                    //Log.i("Output Stream", conn.getOutputStream().toString());
//
//                    InputStream responseBody = conn.getInputStream();
//                    InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
//                    JsonReader jsonReader = new JsonReader(responseBodyReader);
//
//                    //Log.i("JSON READ?", jsonReader.toString() );
//
//                    jsonReader.beginObject(); // Start processing the JSON object
//                    while (jsonReader.hasNext()) { // Loop through all keys
//                        String key = jsonReader.nextName(); // Fetch the next key
//                        if (key.equals("origin")) { // Check if desired key
//                            // Fetch the value as a String
//                            String value = jsonReader.nextString(); //*****************************BOOLEAN NOT ALWAYS A STRING
//                            Log.i("Placehholder",value);
//                                // Do something with the value
//                            break; // Break out of the loop
//                        } else {
//                            jsonReader.skipValue(); // Skip values of other keys
//
//                        }
//                    }
//                } else {
//                    responseString = "FAILED"; // See documentation for more info on response handling
//                    Log.i("BACKGROUND MEIN", "NAHI HUA");
//                }
//                conn.disconnect();
//            } catch (ClientProtocolException e) {
//                Log.i("BACKGROUND MEIN", "ClientProtocolException");
//                //TODO Handle problems..
//            } catch (IOException e) {
//                Log.i("BACKGROUND MEIN", "IOException");
//                //TODO Handle problems..
//            }
            return "Executed";
        }

//        private String uploadFile() {
//            String responseString = null;
//            Log.d("Log", "File path" + opFilePath);
//            HttpClient httpclient = new DefaultHttpClient();
//            HttpPost httppost = new HttpPost(Config.FILE_UPLOAD_URL);
//            try {
//                MultiPartBody
//                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
//                        new AndroidMultiPartEntity.ProgressListener() {
//
//                            @Override
//                            public void transferred(long num) {
//                                publishProgress((int) ((num / (float) totalSize) * 100));
//                            }
//                        });
//                ExifInterface newIntef = new ExifInterface(opFilePath);
//                newIntef.setAttribute(ExifInterface.TAG_ORIENTATION,String.valueOf(2));
//                File file = new File(opFilePath);
//                entity.addPart("pic", new FileBody(file));
//                totalSize = entity.getContentLength();
//                httppost.setEntity(entity);
//
//                // Making server call
//                HttpResponse response = httpclient.execute(httppost);
//                HttpEntity r_entity = response.getEntity();
//
//
//                int statusCode = response.getStatusLine().getStatusCode();
//                if (statusCode == 200) {
//                    // Server response
//                    responseString = EntityUtils.toString(r_entity);
//                    Log.d("Log", responseString);
//                } else {
//                    responseString = "Error occurred! Http Status Code: "
//                            + statusCode + " -> " + response.getStatusLine().getReasonPhrase();
//                    Log.d("Log", responseString);
//                }
//
//            } catch (ClientProtocolException e) {
//                responseString = e.toString();
//            } catch (IOException e) {
//                responseString = e.toString();
//            }
//
//            return responseString;
//        }


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


