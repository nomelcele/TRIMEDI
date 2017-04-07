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
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by kimdohyun on 2016-11-30.
 */

public class ChangePwdActivity extends Activity {
    private EditText currentMailEdit;
    private EditText currentPwdEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_pwd_layout);

        currentMailEdit = (EditText)findViewById(R.id.currentMailEdit);
        currentPwdEdit = (EditText)findViewById(R.id.currentPwdEdit);

        Button currentMailPwdConfirmBtn = (Button)findViewById(R.id.currentMailPwdConfirmBtn);
        currentMailPwdConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 사용자가 입력한 이메일과 비밀번호가 현재 로그인한 회원의 정보와 일치하는지 확인
                ConfirmMailPwdAsyncTask asyncTask = new ConfirmMailPwdAsyncTask();
                SharedPreferences sp = getSharedPreferences("trimediSharedPreferences",MODE_PRIVATE);
                int memNum = sp.getInt("loggedInMemNum",0); // 현재 로그인한 회원의 번호
                String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/member/"+memNum;
                asyncTask.execute(requestURL,String.valueOf(memNum));
            }
        });

        ActionBar actionBar =getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

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

    private class ConfirmMailPwdAsyncTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            try {
                Log.d("ConfirmMailPwd","웹 서버 연결 시도");
                URL url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
                conn.setRequestMethod("GET"); // 통신 방식 지정
                conn.setDoOutput(true); // 쓰기 모드 설정
                conn.setDoInput(true); // 읽기 모드 설정
                conn.setRequestProperty("Content-Type","application/json; charset=UTF-8");

                // 현재 로그인한 회원의 번호를 파라미터로 보냄
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("memNum",strings[1]);

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(jsonObject.toString().getBytes());
                os.flush();
                os.close();

                Log.d("ConfirmMailPwd",String.valueOf(conn.getResponseCode()));
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Log.i("ConfirmMailPwd", "통신 성공");
                    InputStream is = conn.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));

                    StringBuffer sb = new StringBuffer();
                    String line = "";
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }

                    br.close();
                    is.close();

                    Log.i("Test", "Received Data: " + sb.toString());

                    return sb.toString();

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String receivedData) {
            try {
                JSONObject memInfo = new JSONObject(receivedData);

                // 사용자가 입력한 이메일, 비밀번호와 사용자 정보의 이메일, 비밀번호 비교
                String inputMemMail = currentMailEdit.getText().toString();
                String inputMemPwd = currentPwdEdit.getText().toString();
                String memMail = memInfo.getString("memMail");
                String memPwd = memInfo.getString("memPwd");

                if(inputMemMail.equals(memMail) && inputMemPwd.equals(memPwd)){
                    // 이메일, 비밀번호 바르게 입력한 경우
                    // 새 비밀번호 입력 액티비티로 이동
                    Intent intent = new Intent(ChangePwdActivity.this, NewPwdActivity.class);
                    startActivity(intent);
                } else if(!inputMemMail.equals(memMail)){
                    // 이메일을 잘못 입력한 경우
                    Toast.makeText(ChangePwdActivity.this,"이메일을 잘못 입력하셨습니다.",Toast.LENGTH_SHORT).show();
                } else if(!inputMemPwd.equals(memPwd)){
                    // 비밀번호를 잘못 입력한 경우
                    Toast.makeText(ChangePwdActivity.this,"비밀번호를 잘못 입력하셨습니다.",Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
