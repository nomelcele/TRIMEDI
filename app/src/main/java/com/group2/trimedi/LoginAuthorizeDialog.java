package com.group2.trimedi;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by mo on 2016-12-13.
 */

public class LoginAuthorizeDialog extends Dialog {
    private Context context;
    private JSONObject memInfo;
    private EditText authNumEdit;
    private TextView loginDialogContentView;

    public LoginAuthorizeDialog(Context context, JSONObject memInfo) {
        super(context);
        this.context = context;
        this.memInfo = memInfo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 다이얼로그 외부 화면 어둡게 표현
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

        // 다이얼로그의 레이아웃 설정
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login_authorize_dialog);

        // 위젯 초기화
        Button loginDialogOkBtn = (Button)findViewById(R.id.loginDialogOkBtn);
        Button loginDialogCancelBtn = (Button)findViewById(R.id.loginDialogCancelBtn);
        authNumEdit = (EditText)findViewById(R.id.authNumEdit);
        loginDialogContentView = (TextView)findViewById(R.id.loginDialogContentView);

        // 버튼 클릭 리스너 설정
        loginDialogOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 인증 버튼 클릭했을 때
                // 부여된 인증번호와 맞게 입력했는지 확인
                try{
                    String memAuthnum = memInfo.getString("memAuthnum");
                    String memNum = memInfo.getString("memNum");
                    if(memInfo.getString("memAuthnum").equals(authNumEdit.getText().toString())){
                        // 인증번호가 맞으면 member 테이블의 memAuthType을 1로 변경(인증)
                        AuthorizeMemberAsyncTask asyncTask = new AuthorizeMemberAsyncTask();
                        String requestURL = "http://"+context.getResources().getString(R.string.str_ip_address)+"/member/"+memNum;
                        asyncTask.execute(requestURL,memNum);
                    } else {
                        // 인증번호를 잘못 입력했을 경우
                        loginDialogContentView.setText("인증 번호가 틀렸습니다. \n 다시 입력해 주세요.");
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }

            }
        });
        loginDialogCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    private class AuthorizeMemberAsyncTask extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... strings) {
            try {
                Log.d("AuthorizeMember","웹 서버 연결 시도");
                URL url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
                conn.setRequestMethod("PUT"); // 통신 방식 지정
                conn.setDoOutput(true); // 쓰기 모드 설정
                conn.setRequestProperty("Content-Type","application/json");

                // 인증여부 업데이트 해줄 사용자의 번호를 파라미터로 보냄
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("memNum",strings[1]);

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(jsonObject.toString().getBytes());
                os.flush();
                os.close();

                Log.d("AuthorizeMember",String.valueOf(conn.getResponseCode()));

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dismiss();
            Toast.makeText(context,"인증이 완료되었습니다.",Toast.LENGTH_SHORT).show();
        }
    }
}
