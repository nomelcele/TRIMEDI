package com.group2.trimedi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);

        Button emailSignUpBtn = (Button)findViewById(R.id.emailSignUpBtn);
        emailSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),EmailSignUpActivity.class));
            }
        });

        Button goToLoginBtn = (Button)findViewById(R.id.goToLoginBtn);
        goToLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            }
        });

        // 자동 로그인 여부 확인
        SharedPreferences sp = getSharedPreferences("trimediSharedPreferences",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if(sp.getInt("autoLogin",0) == 1){
            // 자동 로그인 설정이 되어 있으면
            // 바로 메인 액티비티로 넘어감
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        } else {
            // 자동 로그인 설정이 되어 있지 않으면
            // SharedPreferences에 저장되어 있는 로그인한 회원의 번호를 지움
            editor.putInt("loggedInMemNum",0);
            editor.commit();
        }

    }
}
