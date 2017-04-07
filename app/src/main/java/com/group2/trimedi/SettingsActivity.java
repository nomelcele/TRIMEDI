package com.group2.trimedi;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * Created by kimdohyun on 2016-11-29.
 */

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        ActionBar actionBar =getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // 개인 정보 확인 및 변경 액티비티로 이동하는 버튼 세팅
        Button goToMemberInfo = (Button)findViewById(R.id.goToMemberInfo);
        goToMemberInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this,MemberInfoActivity.class);
                startActivity(intent);
            }
        });

        // 로그인 설정 액티비티로 이동하는 버튼 세팅
        Button goToLoginSet = (Button)findViewById(R.id.goToLoginSet);
        goToLoginSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this,LoginSettingsActivity.class);
                startActivity(intent);
            }
        });


        // 가족 등록 요청 액티비티로 이동하는 버튼 세팅
        Button goToSendFr = (Button)findViewById(R.id.goToSendFr);
        goToSendFr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this,FamilyRequestSendActivity.class);
                startActivity(intent);
            }
        });

        // 가족 등록 요청 관리 액티비티로 이동하는 버튼 세팅
        Button goToFrAdmin = (Button)findViewById(R.id.goToFrAdmin);
        goToFrAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this,FamilyRequestAdminActivity.class);
                startActivity(intent);
            }
        });

        // 가족 확인 액티비티로 이동하는 버튼 세팅
        Button goToFamilyList = (Button)findViewById(R.id.goToFamilyList);
        goToFamilyList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this,FamilyListActivity.class);
                startActivity(intent);
            }
        });

        // 밴드 설정 액티비티로 이동하는 버튼 세팅
        Button goToBandSettings = (Button)findViewById(R.id.goToBandSettings);
        goToBandSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this,BandSettingsActivity.class);
                startActivity(intent);
            }
        });

        // 약통 설정 액티비티로 이동하는 버튼 세팅
        Button goToPillBoxSettings = (Button)findViewById(R.id.goToPillBoxSettings);
        goToPillBoxSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this,PillBoxSettingsActivity.class);
                startActivity(intent);
            }
        });

        // 버전 확인 액티비티로 이동하는 버튼 세팅
        Button goToCurrentVersion = (Button)findViewById(R.id.goToCurrentVersion);
        goToCurrentVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this,CurrentVersionActivity.class);
                startActivity(intent);
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
