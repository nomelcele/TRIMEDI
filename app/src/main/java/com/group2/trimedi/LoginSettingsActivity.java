package com.group2.trimedi;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * Created by kimdohyun on 2016-11-29.
 */

public class LoginSettingsActivity extends Activity {
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_settings_layout);

        ActionBar actionBar =getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        sp = getSharedPreferences("trimediSharedPreferences",MODE_PRIVATE);
        editor = sp.edit();

        // 자동 로그인 설정 스위치
        Switch autoLoginSwitch = (Switch)findViewById(R.id.autoLoginSwitch);
        if(sp.getInt("autoLogin",0) == 1){
            // 자동 로그인 설정이 되어 있으면 스위치 켬
            autoLoginSwitch.setChecked(true);
        } else {
            autoLoginSwitch.setChecked(false);
        }
        autoLoginSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    // 스위치 켜졌을 때
                    editor.putInt("autoLogin",1); // 자동 로그인 설정
                } else {
                    // 스위치 꺼졌을 때
                    editor.putInt("autoLogin",0); // 자동 로그인 해제
                }
                editor.commit();
            }
        });


        // 로그아웃 버튼
        Button logoutBtn = (Button)findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 로그아웃
                editor.putInt("loggedInMemNum",0); // 현재 로그인한 회원 번호 초기화
                editor.putInt("loggedInFamily",0); // 현재 로그인한 회원의 가족 번호 초기화
                editor.putInt("currentPresNum",0); // 현재 적용되는 처방전 번호 초기화
                editor.putString("currentPresEndDate","noPres"); // 현재 적용되는 처방전 종료일 초기화
                editor.putInt("autoLogin",0); // 자동 로그인 해제
                editor.commit();

                // 앱 최초 화면으로 이동
                startActivity(new Intent(LoginSettingsActivity.this,StartActivity.class));
                finish();
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
}
