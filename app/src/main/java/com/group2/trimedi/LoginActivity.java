package com.group2.trimedi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by mo on 2016-11-22.
 */

public class LoginActivity extends Activity{
    private String inputMemMail;
    private String inputMemPwd;
    private String encodedMail;

    private EditText memMailEdit;
    private EditText memPwdEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        memMailEdit = (EditText)findViewById(R.id.memMailEdit);
        memPwdEdit = (EditText)findViewById(R.id.memPwdEdit);

        // 로그인 버튼 클릭
        Button loginBtn = (Button)findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginAsyncTask asyncTask = new LoginAsyncTask();
                inputMemMail = memMailEdit.getText().toString();
                encodedMail = inputMemMail.replace("@","-");
                encodedMail = inputMemMail.replace(".","");
                Log.i("Login","encodedMail: "+encodedMail);
                inputMemPwd = memPwdEdit.getText().toString();
                String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/member/mail/"+encodedMail;
                asyncTask.execute(requestURL);

//                SharedPreferences sp = getSharedPreferences("trimediSharedPreferences",MODE_PRIVATE);
//                SharedPreferences.Editor editor = sp.edit();
//                editor.putInt("autoLogin",1);
//                editor.putInt("loggedInMemNum",50);
//                editor.commit();
//                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });

        // 아이디, 비밀번호 찾기
        TextView goToFindIdPwd = (TextView)findViewById(R.id.goToFindIdPwd);
        goToFindIdPwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });

    }

    private class LoginAsyncTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            try {
                Log.d("Login","웹 서버 연결 시도");
                URL url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
                conn.setRequestMethod("POST"); // 통신 방식 지정
                conn.setDoOutput(true); // 쓰기 모드 설정
                conn.setDoInput(true); // 읽기 모드 설정
                conn.setRequestProperty("Content-Type","application/json; charset=UTF-8");
//                conn.setConnectTimeout(40000);

                // 사용자가 입력한 이메일 주소를 파라미터로 보냄
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("memMail",inputMemMail);

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(jsonObject.toString().getBytes());
                os.flush();
                os.close();

                Log.d("Login",String.valueOf(conn.getResponseCode()));
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // 입력한 이메일 주소에 해당하는 회원의 정보를 서버에서 읽음
                    Log.i("Login", "통신 성공");
                    InputStream is = conn.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));

                    StringBuffer sb = new StringBuffer();
                    String line = "";
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }

                    br.close();
                    is.close();

                    return sb.toString();

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String receivedData) {
            try{
                // 서버에서 받은 회원의 정보
                JSONObject jsonObject = new JSONObject(receivedData);
                Log.i("Login","로그인 하려는 회원의 정보: "+jsonObject);
                String memPwd = jsonObject.getString("memPwd"); // 비밀번호
                int memAuthType = jsonObject.getInt("memAuthtype"); // 인증 여부(0: 인증 안 됨, 1: 인증됨)
                int memNum = jsonObject.getInt("memNum");
                String memMail = jsonObject.getString("memMail");

                if(memPwd == null){
                    // 사용자가 입력한 이메일에 해당하는 회원 정보가 없는 경우(이메일을 잘못 입력했을 때)
                    Toast.makeText(LoginActivity.this,"이메일 주소가 틀렸습니다.",Toast.LENGTH_SHORT).show();
                } else {
                    // 이메일을 맞게 입력했을 경우
                    Log.i("Login","회원의 비밀번호: "+memPwd);
                    Log.i("Login","입력한 비밀번호: "+inputMemPwd);
                    if(!memPwd.equals(inputMemPwd)){
                        // 비밀번호가 틀렸을 경우
                        Toast.makeText(LoginActivity.this,"비밀번호가 틀렸습니다.",Toast.LENGTH_SHORT).show();
                    } else {
                        // 이메일, 비밀번호 모두 맞았을 경우(로그인)
                        // 인증 여부 확인
                        if(memAuthType == 1) {
                            // 인증된 회원인 경우 로그인
                            // 로그인한 회원의 멤버 번호 SharedPreferences에 저장
                            SharedPreferences sp = getSharedPreferences("trimediSharedPreferences",MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putInt("loggedInMemNum",memNum);
//                            editor.putString("loggedInMemMail",memMail);
                            editor.putInt("pillBoxAlarm",1);
                            // 회원의 가족 번호 가져오기
//                            String familyStr = jsonObject.getString("family");
                            if(!jsonObject.isNull("family")){
                                JSONObject family = jsonObject.getJSONObject("family");
                                int familyNum = family.getInt("familyNum");
                                editor.putInt("loggedInFamily", familyNum);
                            }
                            editor.commit();
                            Log.i("Login","로그인한 회원 번호: "+sp.getInt("loggedInMemNum",0));
                            Log.i("Login","로그인한 회원의 가족 번호: "+sp.getInt("loggedInFamily",0));
                            // 메인 액티비티로 이동
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else {
                            // 인증이 안 된 상태인 경우
                            // 인증 번호 입력할 Dialog 띄움
                            LoginAuthorizeDialog dialog = new LoginAuthorizeDialog(LoginActivity.this,jsonObject);
                            dialog.show();
                        }
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

}
