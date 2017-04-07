package com.group2.trimedi;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by mo on 2016-12-18.
 */

public class CheckTakingRecordBroadcastReceiver extends BroadcastReceiver {
    private Context context;
    private Intent intent;

    private int id;
    private String alarmType;

    @Override
    public void onReceive(Context context, Intent intent) {
        // 파라미터의 intent => 전달된 Intent
        Log.i("CheckTakingRecord","복용 체크 알람");
        this.context = context;
        this.intent = intent;

        try{
            // SharedPreferences에 저장된 처방전 끝나는 시간과 현재 시간을 비교해서
            // 현재 시간이 더 늦으면 알람을 종료
            SharedPreferences sp = context.getSharedPreferences("trimediSharedPreferences", Activity.MODE_PRIVATE);
            String currentPresEndDate = sp.getString("currentPresEndDate","noPres");
            long endDateMilli = 0;
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

            Calendar endCal = Calendar.getInstance();
            Date endDate = dateTimeFormat.parse(currentPresEndDate);
            endCal.setTime(endDate);
            endCal.set(Calendar.HOUR_OF_DAY,23);
            endCal.set(Calendar.MINUTE,59);
            endCal.set(Calendar.SECOND,59);
            endDateMilli = endCal.getTimeInMillis();

            id = intent.getIntExtra("currentId",0);
            alarmType = intent.getStringExtra("alarmType");

            if(System.currentTimeMillis() > endDateMilli){
                Log.i("PillBoxAlarm","알람 종료");
                // 알람 종료
                AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                Intent it = new Intent(context,CheckTakingRecordBroadcastReceiver.class);
                PendingIntent pIntent = PendingIntent.getBroadcast(context,id,it,PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.cancel(pIntent);
            } else {
                // 알림 시간이 되었을 때 onReceive 호출
                // 안드로이드 상태바에 메시지를 보여주기 위한 서비스를 가져옴

                // 복용 여부 확인
                int currentPresNum = sp.getInt("currentPresNum",0); // 현재 적용되는 처방전 번호
                CurrentPresInfoAsyncTask asyncTask = new CurrentPresInfoAsyncTask();
                String requestURL = "http://"+context.getResources().getString(R.string.str_ip_address)+"/prescription/"+currentPresNum;
                asyncTask.execute(requestURL,String.valueOf(currentPresNum));
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private class CurrentPresInfoAsyncTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            try {
                Log.i("CurrentPresInfo","웹 서버 연결 시도");
                URL url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
                conn.setRequestMethod("POST"); // 통신 방식 지정(POST)
                conn.setDoInput(true); // 쓰기 모드 설정
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type","application/json");

                JSONObject params = new JSONObject();
                params.put("presNum",strings[1]); // 상세 정보를 확인할 처방전의 번호를 파라미터로 넘겨줌

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(params.toString().getBytes());
                os.flush();
                os.close();

                Log.i("CurrentPresInfo",String.valueOf(conn.getResponseCode()));
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    Log.i("CurrentPresInfo","통신 성공");
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
            boolean isTaken = true;

            try {
                JSONObject presInfo = new JSONObject(receivedData);
                JSONArray takingrecord = presInfo.getJSONArray("takingrecord");
                JSONObject todayTakingRecord = takingrecord.getJSONObject(takingrecord.length()-1);

                switch(alarmType){
                    case "morning":
                        // 아침 알람 시간에서 1시간 지났으면 아침 복용 여부 확인
                        if(todayTakingRecord.getInt("trCheck1") == 1){
                            // 먹어야 되는데 안 먹었을 경우
                            isTaken = false;
                        }
                        break;
                    case "afternoon":
                        // 점심 알람 시간에서 1시간 지났으면 점심 복용 여부 확인
                        if(todayTakingRecord.getInt("trCheck2") == 1){
                            // 먹어야 되는데 안 먹었을 경우
                            isTaken = false;
                        }
                        break;
                    case "evening":
                        // 저녁 알람 시간에서 1시간 지났으면 저녁 복용 여부 확인
                        if(todayTakingRecord.getInt("trCheck3") == 1){
                            // 먹어야 되는데 안 먹었을 경우
                            isTaken = false;
                        }
                        break;
                }

                if(!isTaken) {
                    // ------------ 복용 기록이 없으면
                    NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    Intent it = new Intent(context, PillBoxAlarmActivity.class);

                    PendingIntent pIntent = PendingIntent.getActivity(context, id, it, PendingIntent.FLAG_UPDATE_CURRENT);
                    // Notification의 각종 속성을 지정하는 데 사용하는 클래스
                    Notification.Builder builder = new Notification.Builder(context);
                    // setWhen : 알림이 표시되는 시간
                    // setDefaults: 알림 시 사운드, 진동, 불빛 등을 설정
                    // setAutoCancel: 알림 터치 시 반응 후 알림을 삭제할지 여부
                    // setContentIntent: 알림 터치 시 반응
                    String contentText = intent.getStringExtra("currentMedicines");
                    builder.setSmallIcon(R.drawable.trimedi_logo).setTicker("TRIMEDI")
                            .setWhen(System.currentTimeMillis()).setContentTitle("복용 알림").setContentText(contentText+"\n 복용해 주세요.")
                            .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE).setContentIntent(pIntent).setAutoCancel(true);

                    // 알림 보냄
                    nManager.notify(id, builder.build());
                }
            } catch(Exception e){

            }
        }
    }
}
