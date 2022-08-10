package com.auc.smsgateway;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    Integer sentMessages = 0;
    Handler handler;
    Runnable runnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn_start = (Button) findViewById(R.id.btn_start);
        Button btn_stop = (Button) findViewById(R.id.btn_stop);
        textView = (TextView) findViewById(R.id.tv_sent_messages);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 5000);
                readSMS();                    }
        };

        if (ContextCompat.checkSelfPermission(MainActivity.this, "android.permission.SEND_SMS") == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { "android.permission.SEND_SMS" },1);
        }
        else {
            Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }



        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_start.setEnabled(false);
                btn_stop.setEnabled(true);


                handler.postDelayed(runnable, 5000);


            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_start.setEnabled(true);
                btn_stop.setEnabled(false);
                handler.removeCallbacks(runnable);


            }
        });



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (!(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(MainActivity.this, "Permission denied to send SMS", Toast.LENGTH_SHORT).show();
            }
        }
        return;
    }

    private class GetSMSTask extends AsyncTask<String,String, String>{
        @Override
        protected void onProgressUpdate(String... values) {
            textView.setText(values[0].toString());
            Toast.makeText(getBaseContext(), values.toString(), Toast.LENGTH_LONG).show();

        }

        @Override
        protected String doInBackground(String... urls) {

            String response = "";
            for(String u: urls) {
                SystemClock.sleep(1000);

                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                try {
                    URL url = new URL(u);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    StringBuffer buffer = new StringBuffer();
                    String line = "";

                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);

                    }

                    return buffer.toString();

                } catch (Exception e) {
                    e.printStackTrace();
                    return "exception";
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            try {
                JSONObject object = new JSONObject(s);
                sendSMS(object.getString("Phone"), object.getString("Body"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void readSMS(){
        GetSMSTask task = new GetSMSTask();
        task.execute(new String[]{"http://10.0.2.2:8080/getSMS"});
    }
    private void sendSMS(String phone, String message){
        sentMessages++;
        textView.setText("Sent SMS Messages: "+sentMessages.toString());
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phone,
                null,
                message,
                null,
                null);


    }

}