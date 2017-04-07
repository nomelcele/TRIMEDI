package com.group2.trimedi;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by mo on 2016-12-05.
 */

public class PillBoxAlarmSettingActivity2 extends Activity {
    private TextView morningAlarmTime;
    private TextView afternoonAlarmTime;
    private TextView eveningAlarmTime;

    private String currentMorningAlarm;
    private String currentAfternoonAlarm;
    private String currentEveningAlarm;
    private String morningMedi;
    private String afternoonMedi;
    private String eveningMedi;

    private Calendar morningCal;
    private Calendar afternoonCal;
    private Calendar eveningCal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pillbox_alarm_setting_layout);

        Intent intent = getIntent();
        currentMorningAlarm = intent.getStringExtra("morningAlarm");
        currentAfternoonAlarm = intent.getStringExtra("afternoonAlarm");
        currentEveningAlarm = intent.getStringExtra("eveningAlarm");
        morningMedi = intent.getStringExtra("morningMedi");
        afternoonMedi = intent.getStringExtra("afternoonMedi");
        eveningMedi = intent.getStringExtra("eveningMedi");

        morningAlarmTime = (TextView)findViewById(R.id.morningAlarmTime);
        morningAlarmTime.setText(currentMorningAlarm);
        afternoonAlarmTime = (TextView)findViewById(R.id.afternoonAlarmTime);
        afternoonAlarmTime.setText(currentAfternoonAlarm);
        eveningAlarmTime = (TextView)findViewById(R.id.eveningAlarmTime);
        eveningAlarmTime.setText(currentEveningAlarm);

        // 현재 미리 설정된 알람 시간이 있는 경우, 그 시간을 화면에 세팅
        morningCal = Calendar.getInstance();
        morningCal.set(Calendar.HOUR_OF_DAY,Integer.parseInt(currentMorningAlarm.substring(0,2)));
        morningCal.set(Calendar.MINUTE,Integer.parseInt(currentMorningAlarm.substring(3,5)));
        afternoonCal = Calendar.getInstance();
        afternoonCal.set(Calendar.HOUR_OF_DAY,Integer.parseInt(currentAfternoonAlarm.substring(0,2)));
        afternoonCal.set(Calendar.MINUTE,Integer.parseInt(currentAfternoonAlarm.substring(3,5)));
        eveningCal = Calendar.getInstance();
        eveningCal.set(Calendar.HOUR_OF_DAY,Integer.parseInt(currentEveningAlarm.substring(0,2)));
        eveningCal.set(Calendar.MINUTE,Integer.parseInt(currentEveningAlarm.substring(3,5)));

        // 알람 설정 완료 버튼
        Button alarmSettingBtn = (Button)findViewById(R.id.alarmSettingBtn);
        alarmSettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("SetAlarmTime","알람 설정 완료 버튼");
                // 알람 시간 설정(update)
                SharedPreferences sp = getSharedPreferences("trimediSharedPreferences",MODE_PRIVATE);
                int presNum = sp.getInt("currentPresNum",0);
                SetAlarmAsyncTask asyncTask = new SetAlarmAsyncTask();
                String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/prescription/alarm/"+presNum;
                // 현재 적용되는 처방전의 번호를 파라미터로 전달
                asyncTask.execute(requestURL,String.valueOf(presNum));
            }
        });

        // 시간 선택 버튼
        ImageView morningAlarmPicker = (ImageView)findViewById(R.id.morningAlarmPicker);
        morningAlarmPicker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.i("PillBoxAlarmSetting","아침 알람 시간 선택");
                int currentHour = 5;
                int currentMinute = 0;
                if(!currentMorningAlarm.equals("알람 시간을 설정해 주세요.")){
                    String[] currentMorningAlarmArr = currentMorningAlarm.split(":");
                    currentHour = Integer.parseInt(currentMorningAlarmArr[0]);
                    currentMinute = Integer.parseInt(currentMorningAlarmArr[1]);
                }

                // 선택한 알람 시간을 Calendar 객체에 저장
                TimePickerDialog timeDialog = new TimePickerDialog(PillBoxAlarmSettingActivity2.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                StringBuffer morningTimeText = new StringBuffer();
                                if(hourOfDay<10) {
                                    morningTimeText.append("0");
                                }
                                morningTimeText.append(hourOfDay).append(":");
                                if(minute<10){
                                    morningTimeText.append("0");
                                }
                                morningTimeText.append(minute);
                                morningAlarmTime.setText(morningTimeText.toString());

                                morningCal.set(Calendar.HOUR_OF_DAY,hourOfDay);
                                morningCal.set(Calendar.MINUTE,minute);
                                morningCal.set(Calendar.SECOND,0);
                            }
                        },
                        currentHour,currentMinute,false);
                timeDialog.show();

                return false;
            }
        });

        ImageView afternoonAlarmPicker = (ImageView)findViewById(R.id.afternoonAlarmPicker);
        afternoonAlarmPicker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.i("PillBoxAlarmSetting","점심 알람 시간 선택");
                int currentHour = 11;
                int currentMinute = 0;
                if(!currentAfternoonAlarm.equals("알람 시간을 설정해 주세요.")){
                    String[] currentAfternoonAlarmArr = currentAfternoonAlarm.split(":");
                    currentHour = Integer.parseInt(currentAfternoonAlarmArr[0]);
                    currentMinute = Integer.parseInt(currentAfternoonAlarmArr[1]);
                }

                TimePickerDialog timeDialog = new TimePickerDialog(PillBoxAlarmSettingActivity2.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                StringBuffer afternoonTimeText = new StringBuffer();
                                if(hourOfDay<10) {
                                    afternoonTimeText.append("0");
                                }
                                afternoonTimeText.append(hourOfDay).append(":");
                                if(minute<10){
                                    afternoonTimeText.append("0");
                                }
                                afternoonTimeText.append(minute);
                                afternoonAlarmTime.setText(afternoonTimeText.toString());

                                afternoonCal.set(Calendar.HOUR_OF_DAY,hourOfDay);
                                afternoonCal.set(Calendar.MINUTE,minute);
                                afternoonCal.set(Calendar.SECOND,0);
                            }
                        },
                        currentHour,currentMinute,false);
                timeDialog.show();

                return false;
            }
        });

        ImageView eveningAlarmPicker = (ImageView)findViewById(R.id.eveningAlarmPicker);
        eveningAlarmPicker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.i("PillBoxAlarmSetting","저녁 알람 시간 선택");
                int currentHour = 17;
                int currentMinute = 0;
                if(!currentEveningAlarm.equals("알람 시간을 설정해 주세요.")){
                    String[] currentEveningAlarmArr = currentEveningAlarm.split(":");
                    currentHour = Integer.parseInt(currentEveningAlarmArr[0]);
                    currentMinute = Integer.parseInt(currentEveningAlarmArr[1]);
                }
                TimePickerDialog timeDialog = new TimePickerDialog(PillBoxAlarmSettingActivity2.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                StringBuffer eveningTimeText = new StringBuffer();
                                if(hourOfDay<10) {
                                    eveningTimeText.append("0");
                                }
                                eveningTimeText.append(hourOfDay).append(":");
                                if(minute<10){
                                    eveningTimeText.append("0");
                                }
                                eveningTimeText.append(minute);
                                eveningAlarmTime.setText(eveningTimeText.toString());

                                eveningCal.set(Calendar.HOUR_OF_DAY,hourOfDay);
                                eveningCal.set(Calendar.MINUTE,minute);
                                eveningCal.set(Calendar.SECOND,0);

                            }
                        },
                        currentHour,currentMinute,false);
                timeDialog.show();

                return false;
            }
        });

    }


    private class SetAlarmAsyncTask extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... strings) {
            try{
                // 알람 시간 설정
                Log.i("SetAlarmTime","웹 서버 연결 시도");
                URL url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
                conn.setRequestMethod("PUT"); // 통신 방식 지정(POST)
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type","application/json");

                // 서버에 파라미터로 전달할 JSONObject
                JSONObject params = new JSONObject();
                params.put("presNum",strings[1]); // 알람 시간을 바꿀 처방전 번호

                // Calendar 객체에 저장한 알람 시간을 String(HHmm) 형태로 변환하여 JSONObject에 저장
                StringBuffer newPresAlarm1 = new StringBuffer();
                StringBuffer newPresAlarm2 = new StringBuffer();
                StringBuffer newPresAlarm3 = new StringBuffer();
                if(morningCal.get(Calendar.HOUR_OF_DAY) < 10){
                    newPresAlarm1.append("0");
                }
                newPresAlarm1.append(morningCal.get(Calendar.HOUR_OF_DAY));
                if(morningCal.get(Calendar.MINUTE) < 10){
                    newPresAlarm1.append("0");
                }
                newPresAlarm1.append(morningCal.get(Calendar.MINUTE));

                if(afternoonCal.get(Calendar.HOUR_OF_DAY) < 10){
                    newPresAlarm2.append("0");
                }
                newPresAlarm2.append(afternoonCal.get(Calendar.HOUR_OF_DAY));
                if(afternoonCal.get(Calendar.MINUTE) < 10){
                    newPresAlarm2.append("0");
                }
                newPresAlarm2.append(afternoonCal.get(Calendar.MINUTE));

                if(eveningCal.get(Calendar.HOUR_OF_DAY) < 10){
                    newPresAlarm3.append("0");
                }
                newPresAlarm3.append(eveningCal.get(Calendar.HOUR_OF_DAY));
                if(eveningCal.get(Calendar.MINUTE) < 10){
                    newPresAlarm3.append("0");
                }
                newPresAlarm3.append(eveningCal.get(Calendar.MINUTE));

                params.put("presAlarm1",newPresAlarm1.toString()); // 바꿀 아침 알람 시간 (HHmm)
                params.put("presAlarm2",newPresAlarm2.toString()); // 바꿀 점심 알람 시간
                params.put("presAlarm3",newPresAlarm3.toString()); // 바꿀 저녁 알람 시간
                Log.i("SetAlarmTime","Params: "+params.toString());

                // 서버로 데이터 전송
                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(params.toString().getBytes());
                os.flush();
                os.close();

                Log.i("SetAlarmTime",String.valueOf(conn.getResponseCode()));

                // 설정한 알람 시작
                Context context = getApplicationContext();

                Log.i("morningCal",morningCal.get(Calendar.HOUR_OF_DAY)+":"+morningCal.get(Calendar.MINUTE));
                Log.i("afternoonCal",afternoonCal.get(Calendar.HOUR_OF_DAY)+":"+afternoonCal.get(Calendar.MINUTE));
                Log.i("eveningCal",eveningCal.get(Calendar.HOUR_OF_DAY)+":"+eveningCal.get(Calendar.MINUTE));

                // ------------------------ 알람 만들기 ------------------------
                // 설정하는 알람 시간이 현재 시간보다 이른 경우
                // AlarmManager에 세팅한 알람을 시작하면 바로 알람이 울리는 현상이 있기 때문에
                // 1일 뒤부터 실행되게 함
                Calendar todayCal = Calendar.getInstance();
                if(todayCal.getTimeInMillis() > morningCal.getTimeInMillis()){
                    morningCal.add(Calendar.DATE,1);
                }
                if(todayCal.getTimeInMillis() > afternoonCal.getTimeInMillis()){
                    afternoonCal.add(Calendar.DATE,1);
                }
                if(todayCal.getTimeInMillis() > eveningCal.getTimeInMillis()){
                    eveningCal.add(Calendar.DATE,1);
                }

                // 안드로이드에서 알람을 관리할 수 있는 AlarmManager 객체
                // ------ 아침 알람
                AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                // 설정한 알람 시간이 되면 BroadcastReceiver를 호출한다.
                Intent intent1 = new Intent(context,PillBoxAlarmBroadcastReceiver.class);
                // Notification의 내용으로 보여줄 텍스트(복용해야 하는 약의 종류와 복용량)
                intent1.putExtra("currentMedicines",morningMedi);
                intent1.putExtra("currentId",0);
                // PendingIntent 두번째 파라미터(requestCode->각 알람을 구분하기 위한 ID로 사용): 0(아침), 1(점심), 2(저녁)
                PendingIntent pIntent1 = PendingIntent.getBroadcast(context,0,intent1,PendingIntent.FLAG_UPDATE_CURRENT);
                Log.i("SetAlarmTime","morningCal: "+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(morningCal.getTime()));
                // 파라미터: 알람 유형, 알람을 시작할 시간, 알람이 반복되는 주기(24시간), 알람이 울릴 시간이 되면 실행할 PendingIntent
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,morningCal.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pIntent1);
                // --- 아침 복용 확인 알람: 알람 시간으로부터 1시간이 지난 후에 복용 기록이 있는지를 확인하고 복용 기록이 없으면 notification 띄움
                Intent checkIntent1 = new Intent(context,CheckTakingRecordBroadcastReceiver.class);
                checkIntent1.putExtra("currentMedicines",morningMedi);
                checkIntent1.putExtra("currentId",3);
                checkIntent1.putExtra("alarmType","morning");
                PendingIntent checkPIntent1 = PendingIntent.getBroadcast(context,3,checkIntent1,PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,morningCal.getTimeInMillis()+3600000,AlarmManager.INTERVAL_DAY,checkPIntent1);


                // ------ 점심 알람
                Intent intent2 = new Intent(context,PillBoxAlarmBroadcastReceiver.class);
                intent2.putExtra("currentMedicines",afternoonMedi);
                intent2.putExtra("currentId",1);
                PendingIntent pIntent2 = PendingIntent.getBroadcast(context,1,intent2,PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,afternoonCal.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pIntent2);
                // --- 점심 복용 확인 알람: 알람 시간으로부터 1시간이 지난 후에 복용 기록이 있는지를 확인하고 복용 기록이 없으면 notification 띄움
                Intent checkIntent2 = new Intent(context,CheckTakingRecordBroadcastReceiver.class);
                checkIntent2.putExtra("currentMedicines",afternoonMedi);
                checkIntent2.putExtra("currentId",4);
                checkIntent2.putExtra("alarmType","afternoon");
                PendingIntent checkPIntent2 = PendingIntent.getBroadcast(context,4,checkIntent2,PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,afternoonCal.getTimeInMillis()+3600000,AlarmManager.INTERVAL_DAY,checkPIntent2);

                // ------ 저녁 알람
                Intent intent3 = new Intent(context,PillBoxAlarmBroadcastReceiver.class);
                intent3.putExtra("currentMedicines",eveningMedi);
                intent3.putExtra("currentId",2);
                PendingIntent pIntent3 = PendingIntent.getBroadcast(context,2,intent3,PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,eveningCal.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pIntent3);
                // --- 저녁 복용 확인 알람: 알람 시간으로부터 1시간이 지난 후에 복용 기록이 있는지를 확인하고 복용 기록이 없으면 notification 띄움
                Intent checkIntent3 = new Intent(context,CheckTakingRecordBroadcastReceiver.class);
                checkIntent3.putExtra("currentMedicines",eveningMedi);
                checkIntent3.putExtra("currentId",5);
                checkIntent3.putExtra("alarmType","evening");
                PendingIntent checkPIntent3 = PendingIntent.getBroadcast(context,5,checkIntent3,PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,eveningCal.getTimeInMillis()+3600000,AlarmManager.INTERVAL_DAY,checkPIntent3);
            }catch(Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // 약통 알람 액티비티로 이동
            Log.i("SetAlarmTime","뒤로 가기");
            finish();
        }
    }
}
