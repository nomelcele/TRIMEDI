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
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by mo on 2016-12-03.
 */

public class PillBoxAlarmBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // 파라미터의 intent => 전달된 Intent
        Log.i("PillBoxAlarm","알람");

        try{
            // SharedPreferences에 저장된 처방전 끝나는 시간과 현재 시간을 비교해서
            // 현재 시간이 더 늦으면 알람을 종료
            SharedPreferences sp = context.getSharedPreferences("trimediSharedPreferences", Activity.MODE_PRIVATE);
            String currentPresEndDate = sp.getString("currentPresEndDate","noPres");
//            long endDateMilli = 0;
//            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
//
//            Calendar endCal = Calendar.getInstance();
//            Date endDate = dateTimeFormat.parse(currentPresEndDate);
//            endCal.setTime(endDate);
//            endCal.set(Calendar.HOUR_OF_DAY,23);
//            endCal.set(Calendar.MINUTE,59);
//            endCal.set(Calendar.SECOND,59);
//            endDateMilli = endCal.getTimeInMillis();

            int id = intent.getIntExtra("currentId",0);

//            if(System.currentTimeMillis() > endDateMilli){
//                Log.i("PillBoxAlarm","알람 종료");
//                // 알람 종료
//                AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//                Intent it = new Intent(context,PillBoxAlarmBroadcastReceiver.class);
//                PendingIntent pIntent = PendingIntent.getBroadcast(context,id,it,PendingIntent.FLAG_UPDATE_CURRENT);
//                alarmManager.cancel(pIntent);
//            } else {
                // 알림 시간이 되었을 때 onReceive 호출
                // 안드로이드 상태바에 메시지를 보여주기 위한 서비스를 가져옴
                NotificationManager nManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                Intent it = new Intent(context,PillBoxAlarmActivity.class);

                PendingIntent pIntent = PendingIntent.getActivity(context,id,it,PendingIntent.FLAG_UPDATE_CURRENT);
                // Notification의 각종 속성을 지정하는 데 사용하는 클래스
                Notification.Builder builder = new Notification.Builder(context);
                // setWhen : 알림이 표시되는 시간
                // setDefaults: 알림 시 사운드, 진동, 불빛 등을 설정
                // setAutoCancel: 알림 터치 시 반응 후 알림을 삭제할지 여부
                // setContentIntent: 알림 터치 시 반응
                String contentText = intent.getStringExtra("currentMedicines");
                builder.setSmallIcon(R.drawable.trimedi_logo).setTicker("TRIMEDI")
                        .setWhen(System.currentTimeMillis()).setContentTitle("복용 알림").setContentText(contentText)
                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE).setContentIntent(pIntent).setAutoCancel(true);

                // 알림 보냄
                nManager.notify(id,builder.build());
//            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
