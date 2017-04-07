package com.group2.trimedi;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by kimdohyun on 2016-11-28.
 */

public class PillBoxAlarmActivity extends Activity implements View.OnClickListener{
    private TextView presDayView; // 처방전 시작일~종료일 표시
    private TextView presPharmacyView; // 처방전 약국 이름 표시
    private TextView morningAlarmView; // 아침 알람 시간 표시
    private TextView afternoonAlarmView; // 점심 알람 시간 표시
    private TextView eveningAlarmView; // 저녁 알람 시간 표시
    private TextView morningMediView; // 아침에 복용할 약과 복용량
    private TextView afternoonMediView; // 아침에 복용할 약과 복용량
    private TextView eveningMediView; // 아침에 복용할 약과 복용량

    private Button morningAlarmBtn;
    private Button afternoonAlarmBtn;
    private Button eveningAlarmBtn;

    // 복용 다이얼로그
    private String[] alarmContentArr;
    private CustomDialog dialog;

    private int currentPresNum;
    private int checkType;
    private int todayTrNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pillbox_alarm_layout);

        presDayView = (TextView)findViewById(R.id.presDayView);
        presPharmacyView = (TextView)findViewById(R.id.presPharmacyView);
        morningAlarmView = (TextView)findViewById(R.id.morningAlarmView);
        afternoonAlarmView = (TextView)findViewById(R.id.afternoonAlarmView);
        eveningAlarmView = (TextView)findViewById(R.id.eveningAlarmView);
        morningMediView = (TextView)findViewById(R.id.morningMediView);
        afternoonMediView = (TextView)findViewById(R.id.afternoonMediView);
        eveningMediView = (TextView)findViewById(R.id.eveningMediView);


        // 알람 시간 설정 버튼
        Button setAlarmBtn = (Button)findViewById(R.id.setAlarmBtn);
        setAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),PillBoxAlarmSettingActivity.class);
                intent.putExtra("morningAlarm",morningAlarmView.getText());
                intent.putExtra("afternoonAlarm",afternoonAlarmView.getText());
                intent.putExtra("eveningAlarm",eveningAlarmView.getText());
                intent.putExtra("morningMedi",morningMediView.getText());
                intent.putExtra("afternoonMedi",afternoonMediView.getText());
                intent.putExtra("eveningMedi",eveningMediView.getText());
                startActivity(intent);
            }
        });

//        ActionBar actionBar =getActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);

        // 복용 여부 or 복용 버튼 위젯
        morningAlarmBtn = (Button)findViewById(R.id.morningAlarmBtn);
        morningAlarmBtn.setOnClickListener(this);
        afternoonAlarmBtn = (Button)findViewById(R.id.afternoonAlarmBtn);
        afternoonAlarmBtn.setOnClickListener(this);
        eveningAlarmBtn = (Button)findViewById(R.id.eveningAlarmBtn);
        eveningAlarmBtn.setOnClickListener(this);

        alarmContentArr = new String[3];
    }

    @Override
    protected void onResume() {
        // 현재 적용되는 최신 처방전 정보 얻기
        super.onResume();
        Log.i("GetLatestPresInfo","처방전 정보 얻기");
        GetLatestPresInfoAsyncTask asyncTask = new GetLatestPresInfoAsyncTask();
        SharedPreferences sp = getSharedPreferences("trimediSharedPreferences",MODE_PRIVATE);
        int memNum = sp.getInt("loggedInMemNum",0);
        // 현재 로그인한 회원 번호를 파라미터로 넘겨줌
        String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/prescription/latest/member/"+memNum;
        asyncTask.execute(requestURL,String.valueOf(memNum));
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
        if (id == R.id.action_settings) {
            Intent intent = new Intent(PillBoxAlarmActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private class GetLatestPresInfoAsyncTask extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                Log.i("GetLatestPresInfo","웹 서버 연결 시도");
                URL url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
                conn.setRequestMethod("POST"); // 통신 방식 지정(POST)
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type","application/json");

                // 서버에 전달할 파라미터 세팅
                JSONObject params = new JSONObject();
                params.put("memNum",strings[1]); // 현재 로그인한 회원 번호

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(params.toString().getBytes());
                os.flush();
                os.close();

                Log.i("GetLatestPresInfo",String.valueOf(conn.getResponseCode()));
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    // 서버로부터 받은 현재 처방전의 정보 읽기
                    Log.i("GetLatestPresInfo","통신 성공");
                    InputStream is = conn.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));

                    StringBuffer sb = new StringBuffer();
                    String line = "";
                    while((line = br.readLine()) != null){
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
            try {
                // 읽은 데이터 JSON 파싱
                // 가장 최신 처방전 정보
                JSONObject presInfo = new JSONObject(receivedData);
                currentPresNum = presInfo.getInt("presNum");

                Log.i("GetLatestPresInfo","아침 알람: "+presInfo.getString("presAlarm1"));
                Log.i("GetLatestPresInfo","점심 알람: "+presInfo.getString("presAlarm2"));
                Log.i("GetLatestPresInfo","저녁 알람: "+presInfo.getString("presAlarm3"));
                // 아침/점심/저녁 알람 시간 배열에 저장
                String[] alarmTimeArr = new String[3];
                alarmTimeArr[0] = presInfo.getString("presAlarm1");
                alarmTimeArr[1] = presInfo.getString("presAlarm2");
                alarmTimeArr[2] = presInfo.getString("presAlarm3");

                // ---- 서버에서 가져온 처방전 정보 UI에 표시
                // 1. 처방전 시작 날짜 ~ 종료 날짜 표시
                SimpleDateFormat originalFormat, dateFormat, timeFormat;
                Date date;
                StringBuffer presDayText = new StringBuffer();

                // 처방전 시작 날짜
                originalFormat = new SimpleDateFormat("yyyyMMdd");
                date = originalFormat.parse(presInfo.getString("presDate"));

                // 처방전 시작 날짜 + 처방 기간으로 처방전 종료 날짜 구하기
                int presDay = Integer.parseInt(presInfo.getString("presDay"));
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.add(Calendar.DATE, presDay - 1);

                dateFormat = new SimpleDateFormat("yyyy.MM.dd.");
                presDayText.append(dateFormat.format(date)).append(" ~ ");
                presDayText.append(dateFormat.format(cal.getTime()));
                presDayView.setText(presDayText.toString());

                // 2. 약국 이름 표시
                presPharmacyView.setText(presInfo.getString("presPharmacy"));

                // 처방전에 포함된 약 정보
                Log.i("GetLatestPresInfo","receivedData: "+presInfo);
                JSONArray medicineInfo = presInfo.getJSONArray("medicine");
                // requiredAlarmArr[0]의 값이 0이면: 아침에 복용하는 약 없음
                // requiredAlarmArr[1]의 값이 0이면: 점심에 복용하는 약 없음
                // requiredAlarmArr[2]의 값이 0이면: 저녁에 복용하는 약 없음
                int[] requiredAlarmArr = new int[3];
                StringBuffer alarmContent1 = new StringBuffer();
                StringBuffer alarmContent2 = new StringBuffer();
                StringBuffer alarmContent3 = new StringBuffer();
                for(int i=0; i<medicineInfo.length(); i++){
                    JSONObject medi = medicineInfo.getJSONObject(i);
                    String mediName = medi.getString("mediName");
                    int mediDose1 = medi.getInt("mediDose1");
                    int mediDose2 = medi.getInt("mediDose2");
                    int mediDose3 = medi.getInt("mediDose3");

                    requiredAlarmArr[0] += mediDose1; // 아침 복용량
                    requiredAlarmArr[1] += mediDose2; // 점심 복용량
                    requiredAlarmArr[2] += mediDose3; // 저녁 복용량

                    // 각 시간대별로 복용할 약 이름과 복용량을 보여주는 문자열 세팅
                    alarmContent1.append(mediName).append(" ").append(mediDose1).append("/");
                    alarmContent2.append(mediName).append(" ").append(mediDose2).append("/");
                    alarmContent3.append(mediName).append(" ").append(mediDose3).append("/");
                }

                // 3. 아침, 점심, 저녁 알람 시간 표시
                timeFormat = new SimpleDateFormat("HHmm");
                for (int i = 0; i < alarmTimeArr.length; i++) {
                    TextView currentView = null;
                    switch (i) {
                        case 0:
                            currentView = morningAlarmView;
                            break;
                        case 1:
                            currentView = afternoonAlarmView;
                            break;
                        case 2:
                            currentView = eveningAlarmView;
                            break;
                    }

                    if (!alarmTimeArr[i].equals("0")) {
                        // 데이터를 HH:mm (24시간제) 형태로 변환 후 텍스트 세팅
                        if(i == 0 && alarmTimeArr[i].length() == 3){
                            // 아침 알람 시간이 05:00~09:59 사이이면
                            alarmTimeArr[i] = "0"+alarmTimeArr[i];
                        }
                        date = timeFormat.parse(alarmTimeArr[i]);
                        String currentTime = timeFormat.format(date);
                        currentView.setText(currentTime.substring(0,2)+":"+currentTime.substring(2,4));
                    } else if(requiredAlarmArr[i] == 0){
                        // 원래 알람 설정이 필요 없는 경우(ex-점심에 복용하는 약이 없는 경우)
                        // 그 시간대의 알람 시간 표시 X, 약 정보 X, 복용 버튼 X
                        currentView.setText("");
                    } else {
                        // 알람 설정을 하지 않았을 경우 '알람 시간을 설정해 주세요' 표시
                        currentView.setText("알람 시간을 설정해 주세요.");
                    }
                }

                // 4. 약 종류와 복용량 표시 => 알람 내용에도 표시
                // 알람 내용 끝에 /가 있으면 공백으로 대체
                if(alarmContent1.substring(alarmContent1.length()-1).equals("/")){
                    alarmContent1.setCharAt(alarmContent1.length()-1,' ');
                }
                if(alarmContent2.substring(alarmContent2.length()-1).equals("/")){
                    alarmContent2.setCharAt(alarmContent2.length()-1,' ');
                }
                if(alarmContent3.substring(alarmContent3.length()-1).equals("/")){
                    alarmContent3.setCharAt(alarmContent3.length()-1,' ');
                }

                // 텍스트뷰에 세팅(원래 안 먹는 시간이면 공백으로 세팅)
                if(requiredAlarmArr[0] != 0){
                    morningMediView.setText(alarmContent1.toString());
                    alarmContentArr[0] = alarmContent1.toString();
                } else {
                    morningMediView.setText("복용할 약이 없습니다.");
                }

                if(requiredAlarmArr[1] != 0) {
                    afternoonMediView.setText(alarmContent2.toString());
                    alarmContentArr[1] = alarmContent2.toString();
                } else {
                    afternoonMediView.setText("복용할 약이 없습니다.");
                }

                if(requiredAlarmArr[2] != 0){
                    eveningMediView.setText(alarmContent3.toString());
                    alarmContentArr[2] = alarmContent3.toString();
                } else {
                    eveningMediView.setText("복용할 약이 없습니다.");
                }

                // 5. 복용 여부 or 복용 버튼 표시
                // ---------------------------------------------------------------
                // ---------------------------------------------------------------
                // 0: 복용 버튼 비활성화, 1: 복용 버튼 활성화, 2: 약 먹음, 3: 약 안 먹음
                int[] iconArr = new int[3];

                Calendar currentCal = Calendar.getInstance();
                int currentHour = currentCal.get(Calendar.HOUR_OF_DAY);
                int currentMinute = currentCal.get(Calendar.MINUTE);
                int currentMinutes = (currentHour*60)+currentMinute;

                int alarmHour, alarmMinute, alarmMinutes;

                // 복용 기록 정보
                JSONArray takingrecord = presInfo.getJSONArray("takingrecord");
                SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyyMMdd");
                String todayDate = dbDateFormat.format(Calendar.getInstance().getTime());

                // 오늘의 복용 기록이 있으면
                JSONObject todayRecord = takingrecord.getJSONObject(takingrecord.length()-1);
                todayTrNum = todayRecord.getInt("trNum");

                // 오늘의 각 시간대별 복용 여부 표시
                int[] takingRecordArr = new int[3];
                takingRecordArr[0] = todayRecord.getInt("trCheck1");
                takingRecordArr[1] = todayRecord.getInt("trCheck2");
                takingRecordArr[2] = todayRecord.getInt("trCheck3");

                if(!alarmTimeArr[0].equals("0")){
                    // 아침 알람 아이콘 표시
                    // 현재 시간 - 알람 시간
                    int morningDiffer;
                    alarmHour = Integer.parseInt(alarmTimeArr[0].substring(0, 2));
                    alarmMinute = Integer.parseInt(alarmTimeArr[0].substring(2, 4));
                    alarmMinutes = (alarmHour * 60) + alarmMinute;

                    morningDiffer = currentMinutes - alarmMinutes;
                    iconArr[0] = getTrType(morningDiffer,takingRecordArr[0]);
                }

                if(!alarmTimeArr[1].equals("0")){
                    // 점심 알람 아이콘 표시
                    // 현재 시간 - 알람 시간
                    int afternoonDiffer;
                    alarmHour = Integer.parseInt(alarmTimeArr[1].substring(0, 2));
                    alarmMinute = Integer.parseInt(alarmTimeArr[1].substring(2, 4));
                    alarmMinutes = (alarmHour * 60) + alarmMinute;

                    afternoonDiffer = currentMinutes - alarmMinutes;
                    iconArr[1] = getTrType(afternoonDiffer,takingRecordArr[1]);
                }

                if(!alarmTimeArr[2].equals("0")){
                    // 저녁 알람 아이콘 표시
                    // 현재 시간 - 알람 시간
                    int eveningDiffer;
                    alarmHour = Integer.parseInt(alarmTimeArr[2].substring(0, 2));
                    alarmMinute = Integer.parseInt(alarmTimeArr[2].substring(2, 4));
                    alarmMinutes = (alarmHour * 60) + alarmMinute;

                    eveningDiffer = currentMinutes - alarmMinutes;
                    iconArr[2] = getTrType(eveningDiffer,takingRecordArr[2]);
                }

                changeIconStyle(morningAlarmBtn,iconArr[0]);
                changeIconStyle(afternoonAlarmBtn,iconArr[1]);
                changeIconStyle(eveningAlarmBtn,iconArr[2]);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public int getTrType(int compareVal,int isTaken){
        // 음수: 현재 시간이 알람 시간보다 이름 (복용 비활성화)
        // 0~60 : 현재 시간이 알람 시간과 같음~1시간 늦음 (복용 활성화)
        // 61 ~ : 현재 시간이 알람 시간보다 1시간 이상 늦음 (약 안 먹음)
        // 0: 복용 버튼 비활성화, 1: 복용 버튼 활성화, 2: 약 먹음, 3: 약 안 먹음
        switch(isTaken){
            case 0:
                // 원래 안 먹는 시간
                return 4;
            case 1:
                // 먹어야 하는데 안 먹음
                if(compareVal <0){
                    // 현재 시간이 알람 시간보다 이른 경우 -> 복용 비활성화
                    return 0;
                } else if(compareVal >= 0 && compareVal <= 60){
                    // 현재 시간이 알람 시간과 같음 ~ 1시간 늦음
                    // 복용 버튼 활성화
                    return 1;
                } else {
                    // 현재 시간이 알람 시간보다 1시간 이상 늦음
                    // 약 안 먹음
                    return 3;
                }
            case 2:
                // 먹음
                return 2;
        }

        return 0;

    }

    public void changeIconStyle(Button btn,int iconType){
        switch(iconType){
            case 0:
                // 복용 버튼 비활성화
                btn.setText("복용");
                btn.setBackgroundColor(getResources().getColor(R.color.background_color));
                btn.setTextColor(Color.BLACK);
                btn.setClickable(false);
                break;
            case 1:
                // 복용 버튼 활성화
                btn.setText("복용");
                btn.setBackgroundColor(getResources().getColor(R.color.pillbox_theme_color));
                btn.setTextColor(Color.WHITE);
                btn.setClickable(true);
                break;
            case 2:
                // 약 먹음
                btn.setText("");
                btn.setBackgroundResource(R.drawable.pillbox_taken);
                btn.setClickable(false);
                break;
            case 3:
                // 약 안 먹음
                btn.setText("");
                btn.setBackgroundResource(R.drawable.pillbox_not_taken);
                btn.setClickable(false);
                break;
            case 4:
                // 원래 안 먹는 시간(아무것도 표시 안 함)
                btn.setText("");
                btn.setVisibility(View.INVISIBLE);
                btn.setClickable(false);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        // 복용 버튼 클릭했을 때
        Log.i("GetAlarmTime","복용 버튼 클릭");
        // Dialog에 보여줄 내용
        String dialogTitle = null;
        String dialogContent = null;
        checkType = 0;

        switch(view.getId()){
            case R.id.morningAlarmBtn:
                Log.i("GetAlarmTime","아침 복용 버튼");
                dialogTitle = "아침 복용";
                dialogContent = alarmContentArr[0];
                checkType = 1;
                break;
            case R.id.afternoonAlarmBtn:
                Log.i("GetAlarmTime","점심 복용 버튼");
                dialogTitle = "점심 복용";
                dialogContent = alarmContentArr[1];
                checkType = 2;
                break;
            case R.id.eveningAlarmBtn:
                Log.i("GetAlarmTime","저녁 복용 버튼");
                dialogTitle = "저녁 복용";
                dialogContent = alarmContentArr[2];
                checkType = 3;
                break;
        }

        // Dialog 띄우기
        dialog = new CustomDialog(this,
                dialogTitle, dialogContent, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 복용 기록 보내기
                AddTrAsyncTask asyncTask = new AddTrAsyncTask();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/prescription/takingrecord";
                asyncTask.execute(requestURL,String.valueOf(currentPresNum),String.valueOf(checkType));
            }
        },"복용");
        dialog.show();
    }

    private class AddTrAsyncTask extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... strings) {
            try {
                String requestURL = strings[0];
                int presNum = Integer.parseInt(strings[1]);
                int checkType = Integer.parseInt(strings[2]);

                Log.i("AddTakingRecord","웹 서버 연결 시도");
                URL url = new URL(requestURL);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
                conn.setRequestMethod("PUT");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type","application/json");

                JSONObject params = new JSONObject();
                JSONObject pres = new JSONObject();
                pres.put("presNum",presNum);
                params.put("prescription",pres);
                params.put("trNum",todayTrNum);
                switch(checkType){
                    case 1:
                        // 아침 복용 여부 업데이트(2로) => 약 먹음
                        params.put("trCheck1",2);
                        params.put("trCheck2",1);
                        params.put("trCheck3",1);
                        break;
                    case 2:
                        // 점심 복용 여부 업데이트
                        params.put("trCheck1",1);
                        params.put("trCheck2",2);
                        params.put("trCheck3",1);
                        break;
                    case 3:
                        // 저녁 복용 여부 업데이트
                        params.put("trCheck1",1);
                        params.put("trCheck2",1);
                        params.put("trCheck3",2);
                        break;
                }

                Log.i("AddTr","파라미터: "+params.toString());

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(params.toString().getBytes());
                os.flush();
                os.close();

                Log.i("AddTakingRecord",String.valueOf(conn.getResponseCode()));
            } catch(Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // 복용 기록 추가 후 다이얼로그 닫기
            dialog.dismiss();
            // 액티비티 새로고침
            recreate();
        }
    }

    private class InsertTodayTrAsyncTask extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... strings) {
            // 새로운 복용 기록 추가(INSERT)
            try {
                Log.d("InsertTodayTr","웹 서버 연결 시도");
                URL url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
                conn.setRequestMethod("POST"); // 통신 방식 지정(POST)
                conn.setDoOutput(true); // 쓰기 모드 설정
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type","application/json");

                JSONObject params = new JSONObject();
                params.put("presNum",strings[1]); // 현재 처방전 번호
                Log.i("InsertTodayTr","params: "+params.toString());

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(params.toString().getBytes());
                os.flush();
                os.close();

                Log.d("InsertTodayTr",String.valueOf(conn.getResponseCode()));

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

    }
}
