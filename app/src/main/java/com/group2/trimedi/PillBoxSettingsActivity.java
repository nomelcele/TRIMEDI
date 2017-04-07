package com.group2.trimedi;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * Created by kimdohyun on 2016-12-08.
 */

public class PillBoxSettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pillbox_settings_layout);

        ActionBar actionBar =getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // 약통 알림 스위치
        Switch pillBoxAlarmSwitch = (Switch)findViewById(R.id.pillBoxAlarmSwitch);
        SharedPreferences sp = getSharedPreferences("trimediSharedPreferences",MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();

        if(sp.getInt("pillBoxAlarm",0) == 1){
            // 약통 알림 설정이 되어 있으면 스위치 켬
            pillBoxAlarmSwitch.setChecked(true);
        } else {
            pillBoxAlarmSwitch.setChecked(false);
        }
        pillBoxAlarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    // 스위치 켜졌을 때
                    // 약통 알림 켜기
                    // 현재 알람 시간 갖고 와서 alarmManager에 현재 알람 시간 + 1시간마다 돌아가는 알람 만듦
                } else {
                    // 스위치 꺼졌을 때
                    // 약통 알림 끄기
                    editor.putInt("pillBoxAlarm",0);
                    editor.commit();
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
}
