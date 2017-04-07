package com.group2.trimedi;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by mo on 2016-11-15.
 */

public class EmailSignUpActivity extends Activity {
    EditText editMemMail, editMemName, editMemPwd, editMemConfirmPwd, editMemBirth;
    Spinner editMemGender;
    String[] items = {"남성","여성"};
    Calendar birthDate = Calendar.getInstance();
    Button signUpBtn;

    String mem_mail,mem_name,mem_pwd,mem_birth;
    int mem_gender;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_signup_layout);

        editMemMail = (EditText)findViewById(R.id.memMail);
        editMemName = (EditText)findViewById(R.id.memName);
        editMemPwd = (EditText)findViewById(R.id.memPwd);
        editMemConfirmPwd = (EditText)findViewById(R.id.memConfirmPwd);
        editMemBirth = (EditText)findViewById(R.id.memBirth);
        editMemGender = (Spinner)findViewById(R.id.memGender);

        // 생년월일 선택(DatePickerDialog)
        editMemBirth.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        DatePickerDialog.OnDateSetListener onDateSet = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                birthDate.set(Calendar.YEAR,year);
                                birthDate.set(Calendar.MONTH,month);
                                birthDate.set(Calendar.DAY_OF_MONTH,day);
                                Log.d("year",String.valueOf(year));
                                Log.d("month",String.valueOf(month));
                                Log.d("day",String.valueOf(day));
                                SimpleDateFormat format = new SimpleDateFormat("yyyy년 MM월 dd일");
//                                memBirth.setText(DateFormat.getInstance().format(birthDate.getTime()));
                                editMemBirth.setText(format.format(birthDate.getTime()));
                            }
                        };
                        DatePickerDialog dialog = new DatePickerDialog(EmailSignUpActivity.this,
                                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                                onDateSet,
                                birthDate.get(Calendar.YEAR),birthDate.get(Calendar.MONTH),birthDate.get(Calendar.DAY_OF_MONTH));
                        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                Log.d("cancel","cancel btn clicked");
                            }
                        });
                        dialog.show();
                        break;
                }
                return false;
            }
        });


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,items);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        editMemGender.setAdapter(adapter);

        // 회원가입 버튼 클릭 리스너
        signUpBtn = (Button)findViewById(R.id.signUpBtn);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 비밀번호 같은지 확인
                if(editMemPwd.getText().toString().equals(editMemConfirmPwd.getText().toString())){
                    // db에 회원 정보 넣기
                    Log.d("EmailSignUp","비밀번호가 같습니다.");

                    String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/member"; // 192.168.0.137
                    EmailSignUpAsyncTask asyncTask = new EmailSignUpAsyncTask();
                    mem_mail = editMemMail.getText().toString();
                    mem_name = editMemName.getText().toString();
                    mem_pwd = editMemPwd.getText().toString();

                    if(editMemGender.getSelectedItem().toString().equals("남성")){
                        mem_gender = 0;
                    } else {
                        mem_gender = 1;
                    }

//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    mem_birth = sdf.format(birthDate.getTime());

                    asyncTask.execute(requestURL);
                } else {
                    Log.d("EmailSignUp","비밀번호가 같지 않습니다.");
                    Toast.makeText(getApplicationContext(),"비밀번호가 같지 않습니다.",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private class EmailSignUpAsyncTask extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... strings) {
            try {
                Log.d("EmailSignUp","웹 서버 연결 시도");
                URL url = new URL(strings[0]); // 요청 URL 설정
                HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
                conn.setRequestMethod("POST"); // 통신 방식 지정(POST)
                conn.setDoOutput(true); // 쓰기 모드 설정
                conn.setRequestProperty("Content-Type","application/json; charset=UTF-8"); // 서버로 보낼 데이터의 타입 지정(JSON)

                // 앱에서 서버로 보낼 데이터들을 JSONObject 객체에 담음
                // 새로 가입할 회원의 정보를 보냄
                // key는 member(회원의 정보를 저장하는 테이블)의 컬럼 명과 일치함
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("memMail",mem_mail);
                jsonObject.put("memName",mem_name);
                jsonObject.put("memPwd",mem_pwd);
                jsonObject.put("memBirth",mem_birth);
                Log.i("Test","생일: "+mem_birth);
                jsonObject.put("memGender",mem_gender);

                // Stream을 통해 서버로 데이터 전송
                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(jsonObject.toString().getBytes());
                os.flush();
                os.close();

                // 서버로부터 온 응답 코드 출력
                Log.d("EmailSignUp",String.valueOf(conn.getResponseCode()));

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        }
    }
}
