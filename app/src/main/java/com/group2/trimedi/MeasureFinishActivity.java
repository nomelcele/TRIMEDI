package com.group2.trimedi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

/**
 * Created by mo on 2016-12-15.
 */

public class MeasureFinishActivity extends Activity {
    private double heartValue;
    private double temperValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.direct_measure_finish_layout);

        Intent intent = getIntent();
        heartValue = intent.getDoubleExtra("value1",0); // 심박
        temperValue = intent.getDoubleExtra("value2",0); // 체온

        TextView measureHeartView = (TextView)findViewById(R.id.measureHeartView);
        TextView measureTemperView = (TextView)findViewById(R.id.measureTemperView);
        measureHeartView.setText(String.valueOf((int)heartValue)+" bpm");
        measureTemperView.setText(String.valueOf(temperValue)+" \u00b0"+"C");
    }

    @Override
    protected void onResume() {
        super.onResume();
        NewMeasureAsyncTask heartAsyncTask = new NewMeasureAsyncTask();
        NewMeasureAsyncTask temperAsyncTask = new NewMeasureAsyncTask();
        String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/measure";
        SharedPreferences sp = getSharedPreferences("trimediSharedPreferences",MODE_PRIVATE);
        int memNum = sp.getInt("loggedInMemNum",0);
        heartAsyncTask.execute(requestURL,String.valueOf(1),String.valueOf(heartValue),String.valueOf(memNum));
        temperAsyncTask.execute(requestURL,String.valueOf(0),String.valueOf(temperValue),String.valueOf(memNum));
    }

    @Override
    public void onBackPressed() {
        // 뒤로 가기 클릭하면 메인 액티비티로 이동
        super.onBackPressed();
        startActivity(new Intent(MeasureFinishActivity.this,MainActivity.class));
        finish();
    }

    private class NewMeasureAsyncTask extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... params) {
            try {
                Log.i("NewMeasure", "웹 서버 연결 시도");
                String requestURL = params[0];
                String msrType = params[1];
                String msrValue = params[2];
                int memNum = Integer.parseInt(params[3]);

                URL url = new URL(requestURL);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type","application/json");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("msrDate", Calendar.getInstance().getTimeInMillis()); // 현재 시간
                jsonObject.put("msrType",msrType);
                jsonObject.put("msrValue",msrValue);
                jsonObject.put("memNum",memNum);
                Log.i("MeasureFinish","params: "+jsonObject.toString());

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(jsonObject.toString().getBytes());
                os.flush();
                os.close();

                Log.d("DirectMeasure","conn.getResponseCode(): "+conn.getResponseCode());

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
