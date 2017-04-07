package com.group2.trimedi;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by kimdohyun on 2016-11-29.
 */

public class MemberInfoActivity extends Activity {
    private TextView currentMemName;
    private TextView currentMemGenderAge;
    private TextView currentMemMail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.member_info_layout);
        ActionBar actionBar =getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Button goToChangePwd = (Button)findViewById(R.id.goToChangePwd);
        goToChangePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MemberInfoActivity.this,ChangePwdActivity.class);
                startActivity(intent);
            }
        });

        currentMemName = (TextView)findViewById(R.id.currentMemName);
        currentMemGenderAge = (TextView)findViewById(R.id.currentMemGenderAge);
        currentMemMail = (TextView)findViewById(R.id.currentMemMail);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MemberInfoAsyncTask asyncTask = new MemberInfoAsyncTask();
        SharedPreferences sp = getSharedPreferences("trimediSharedPreferences",MODE_PRIVATE);
        int memNum = sp.getInt("loggedInMemNum",0);
        String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/member/"+memNum;
        asyncTask.execute(requestURL,String.valueOf(memNum));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MemberInfoAsyncTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            try {
                Log.i("MemberInfo","웹 서버 연결 시도");
                URL url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
                conn.setRequestMethod("POST"); // 통신 방식 지정(POST)
                conn.setDoInput(true); // 쓰기 모드 설정
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type","application/json");

                JSONObject params = new JSONObject();
                params.put("memNum",strings[1]); // 현재 로그인한 회원 번호

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(params.toString().getBytes());
                os.flush();
                os.close();

                Log.i("MemberInfo",String.valueOf(conn.getResponseCode()));
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    // 서버로부터 받은 현재 처방전의 정보 읽기
                    Log.i("MemberInfo","통신 성공");
                    InputStream is = conn.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));

                    StringBuffer sb = new StringBuffer();
                    String line = "";
                    while((line = br.readLine()) != null){
                        sb.append(line);
                    }

                    br.close();
                    is.close();

                    return sb.toString();

                }

            } catch(Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String receivedData) {
            try {
                JSONObject memInfo = new JSONObject(receivedData);
                currentMemName.setText(memInfo.getString("memName"));

                StringBuffer memGenderAge = new StringBuffer();
                Calendar todayCal = Calendar.getInstance();
                Calendar birthCal = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                birthCal.setTime(dateFormat.parse(memInfo.getString("memBirth")));
                String gender = null;
                if(memInfo.getInt("memGender") == 0){
                    gender = "남";
                } else {
                    gender = "여";
                }
                memGenderAge.append(gender).append("/").append((todayCal.get(Calendar.YEAR)-birthCal.get(Calendar.YEAR)));

                currentMemGenderAge.setText(memGenderAge.toString());
                currentMemMail.setText(memInfo.getString("memMail"));
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
