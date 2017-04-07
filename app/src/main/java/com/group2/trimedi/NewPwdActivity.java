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

public class NewPwdActivity extends Activity {
    private EditText newPwdEdit;
    private EditText newPwdConfirmEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_pwd_layout);
        ActionBar actionBar =getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // 비밀번호 입력 EditText
        newPwdEdit = (EditText)findViewById(R.id.newPwdEdit);
        newPwdConfirmEdit = (EditText)findViewById(R.id.newPwdConfirmEdit);

        // 확인 버튼
        Button newPwdOkBtn = (Button)findViewById(R.id.newPwdOkBtn);
        newPwdOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newPwd = newPwdEdit.getText().toString();
                if(newPwd.equals(newPwdConfirmEdit.getText().toString())){
                    // 비밀번호 입력과 재입력이 일치하면
                    // 비밀번호 변경
                    ChangePwdAsyncTask asyncTask = new ChangePwdAsyncTask();
                    SharedPreferences sp = getSharedPreferences("trimediSharedPreferences",MODE_PRIVATE);
                    int memNum = sp.getInt("loggedInMemNum",0); // 현재 로그인한 회원의 번호
                    int familyNum = sp.getInt("loggedInMemNum",0);
                    String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/member/"+memNum+"/"+newPwd;
                    asyncTask.execute(requestURL,String.valueOf(memNum),newPwd,String.valueOf(familyNum));
                } else {
                    // 비밀번호 입력과 재입력이 일치하지 않으면
                    Toast.makeText(NewPwdActivity.this,"비밀번호가 일치하지 않습니다.\n 다시 입력해 주세요.",Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    private class ChangePwdAsyncTask extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... strings) {
            try {
                Log.d("ChangePwd","웹 서버 연결 시도");
                URL url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
                conn.setRequestMethod("PUT"); // 통신 방식 지정
                conn.setDoOutput(true); // 쓰기 모드 설정
                conn.setDoInput(true); // 읽기 모드 설정
                conn.setRequestProperty("Content-Type","application/json; charset=UTF-8");

                JSONObject params = new JSONObject();
                params.put("memNum",strings[1]); // 현재 로그인한 회원의 번호
                params.put("memPwd",strings[2]); // 새로운 비밀번호
                Log.i("ChangePwd","params: "+params.toString());

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(params.toString().getBytes());
                os.flush();
                os.close();

                Log.d("ChangePwd",String.valueOf(conn.getResponseCode()));

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // 비밀번호 변경 후
            // 로그아웃하고 로그인 액티비티로 이동
            Toast.makeText(NewPwdActivity.this,"비밀번호가 변경되었습니다.\n 다시 로그인해 주세요.",Toast.LENGTH_SHORT).show();
            // 로그아웃
            SharedPreferences sp = getSharedPreferences("trimediSharedPreferences",MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("loggedInMemNum",0); // 현재 로그인한 회원 번호 초기화
            editor.putInt("loggedInFamily",0); // 현재 로그인한 회원의 가족 번호 초기화
            editor.putInt("currentPresNum",0); // 현재 적용되는 처방전 번호 초기화
            editor.putString("currentPresEndDate","noPres"); // 현재 적용되는 처방전 종료일 초기화
            editor.putInt("autoLogin",0); // 자동 로그인 해제
            editor.commit();

            startActivity(new Intent(NewPwdActivity.this,LoginActivity.class));
            finish();
        }
    }
}
