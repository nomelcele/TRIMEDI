package com.group2.trimedi;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by kimdohyun on 2016-11-30.
 */

public class BandSettingsActivity extends BlunoLibrary {
    private Button buttonScan;
    private Button buttonSerialSend;
    private EditText serialSendText;
    private TextView serialReceivedText;

    int msr_type = 0;
    double msr_value = 0.0;

    private Handler handler;
    private Runnable mrun;

    private final String TAG = "TriMedi";
    private Switch bandConnectSwitch;
    private TextView measurePeriodSet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.band_settings_layout);

        ActionBar actionBar =getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        onCreateProcess();														//onCreate Process by BlunoLibrary


        serialBegin(115200);													//set the Uart Baudrate on BLE chip to 115200

        // 디바이스 연결 스위치
        bandConnectSwitch = (Switch)findViewById(R.id.bandConnectSwitch);
        bandConnectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    // 스위치를 켜면 디바이스 연결 시도
                    buttonScanOnClickProcess(); // 디바이스 스캔
                } else {
                    // 스위치를 끄면 디바이스 연결 해제
                }
            }
        });

        // 측정 주기 설정
        SharedPreferences sp = getSharedPreferences("trimediSharedPreferences",MODE_PRIVATE);
        int measurePeriod = sp.getInt("measurePeriod",0); // 측정 주기 (분 단위로 들어감)
        final int measurePeriodHour = measurePeriod / 60;
        final int measurePeriodMinute = measurePeriod % 60;
        final Calendar measureCal = Calendar.getInstance();
        measurePeriodSet = (TextView)findViewById(R.id.measurePeriodSet);
        measurePeriodSet.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                TimePickerDialog timeDialog = new TimePickerDialog(BandSettingsActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                StringBuffer measurePeriodText = new StringBuffer();
                                if(hourOfDay != 0) {
                                    measurePeriodText.append(hourOfDay).append("시간");
                                }
                                if(minute != 0) {
                                    measurePeriodText.append(" ").append(minute).append("분");
                                }
                                measurePeriodText.append(" 마다");
                                measurePeriodSet.setText(measurePeriodText.toString());

                                measureCal.set(Calendar.HOUR_OF_DAY,hourOfDay);
                                measureCal.set(Calendar.MINUTE,minute);
                            }
                        },
                        measurePeriodHour,measurePeriodMinute,true);
                timeDialog.show();
                TimerService.setTime = (measureCal.get(Calendar.HOUR_OF_DAY)*3600000)+(measureCal.get(Calendar.MINUTE)*60000); // 측정 주기 설정
                Intent intent = new Intent(getApplicationContext(),TimerService.class);
                startService(intent);
                Log.i("BandSetting","측정 주기 설정 완료 " + TimerService.setTime);
                // 이 부분에서 초기 값이 들어감 -> 한번더 선택하면 정상 작동
                return false;
            }
        });
//
//        Button bandTimerBtn = (Button)findViewById(R.id.bandTimerBtn);
//        bandTimerBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getApplicationContext(),TimerService.class);
//                startService(intent);
//                Log.d(TAG,getApplicationContext()+"");
//                Toast.makeText(getApplicationContext(),"Sensing Service Start", Toast.LENGTH_LONG).show();
//            }
//        });

        Switch bandNofiSwitch = (Switch)findViewById(R.id.bandNofiSwitch);
        bandNofiSwitch.setChecked(true);
    }

    protected void onResume(){
        super.onResume();
        System.out.println("BlUNOActivity onResume");
        onResumeProcess();														//onResume Process by BlunoLibrary

        if(connectedFlag){
            // 디바이스 연결이 되어 있으면
            // 디바이스 연결 스위치 켬
            bandConnectSwitch.setChecked(true);
        } else {
            // 연결이 안 되어 있으면 스위치 끔
            bandConnectSwitch.setChecked(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MainActivity","onActivityResult() Call.");
        onActivityResultProcess(requestCode, resultCode, data);					//onActivityResult Process by BlunoLibrary
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        Log.d("MainActivity","onPause() Call.");
        super.onPause();
        onPauseProcess();														//onPause Process by BlunoLibrary
    }

    protected void onStop() {
        Log.d("MainActivity","onStop() Call.");
        super.onStop();
        onStopProcess();														//onStop Process by BlunoLibrary
    }

    @Override
    protected void onDestroy() {
        Log.d("MainActivity","onDestroy() Call.");
        super.onDestroy();
//        onDestroyProcess();														//onDestroy Process by BlunoLibrary
    }

    @Override
    public void onConnectionStateChange(BlunoLibrary.connectionStateEnum theConnectionState) {
        Log.d("MainActivity","onConnectionStateChange() Call.");
        switch (theConnectionState) {
            case isConnected:
                // 연결됐을 때
                serialSend(SENSING_START+"");
                commandSendFlag = true;
                break;
        }
    }

    @Override
    public void onSerialReceived(String theString) {							//Once connection data received, this function will be called
        //theString: 수신한 문자열
        double receivedData = Double.parseDouble(theString); 					// 수신한 문자열을 double형으로 저장
        int inputCommand = ((int)receivedData / 10000);
        inputCommand *= 10000; 													// 입력명령 저장(수신확인, 측정시작, 측정종료)
        switch (inputCommand){
            case NO_DEVICE: noDeviceProcess();
                break;
            case RECEIVE_OK: receiveOkProcess();
                break;
            case SENSING_END: sensingEndProcess(receivedData%10000);
                break;
        }

    }
    @Override
    public void sensingEndProcess(double data){
        commandReceivedFlag = false;
        double dataType = data / 1000;													//수신한 데이터 타입저장(온도, 심박)
        double sensingData = data % 1000;												//측정값 저장
        switch ((int)dataType){
            case TYPE_HEART: Log.d("TestLog","HeartRate: "+sensingData);
                if(sensingData>120.0&&sensingData<60) NotificationSomethings(TYPE_HEART,sensingData); 	// 이상값 측정시 알림 발생
//                heartValue = sensingData;
                break;
            case TYPE_TEMP: Log.d("TestLog","Temperature: "+sensingData);
                if(sensingData>38.0&&sensingData<30) NotificationSomethings(TYPE_TEMP,sensingData); 	// 이상값 측정시 알림 발생
//                temperValue = sensingData;
//                Intent intent = new Intent(getApplicationContext(),MeasureFinishActivity.class);
//                intent.putExtra("value1",heartValue); // 심박
//                intent.putExtra("value2",temperValue); // 체온
//                startActivity(intent);
                finish();
                break;
        }

    }
    @Override
    public void receiveOkProcess(){
        Log.d("TestLog","Wait...");
        commandReceivedFlag = true;
        commandSendFlag = false;
    }
    @Override
    public void noDeviceProcess(){
        Log.d("TestLog","noDevice...");
    }
    public void NotificationSomethings(int type, double data) {							// Notification을 발생시키는 메소드

        String str_type = "";
        switch (type){
            case TYPE_HEART: str_type = "HeartRate";
                break;
            case TYPE_TEMP: str_type = "Temperater";
                break;
        }
        Resources res = getResources();
        //알림을 터치하였을 때 특정 엑티비티를 실행하려면 아래 주석 해제(현재: 아무 동작 안함)
//		Intent notificationIntent = new Intent(this, MainActivity.class);
//		notificationIntent.putExtra("notificationId", data); //전달할 값
//		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentTitle(TAG)													//알림 타이들 설정
                .setContentText(str_type+":"+data)										//알림 내용(서브타이틀) 설정
                .setTicker(data+"")														//알림 내용 설정
                .setSmallIcon(R.mipmap.ic_launcher)										//상단바 아이콘 설정
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))	//상단바를 내렸을 때 아이콘 설정
//				.setContentIntent(contentIntent)										//알림을 터치하였을때 수행할 동작 설정
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())									//시간 표시 설정
                .setDefaults(Notification.DEFAULT_ALL);



        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_DEFAULT)							//우선순위 설정
                    .setVisibility(Notification.VISIBILITY_PUBLIC);						//표시할 범위 설정(잠금화면, 일반 화면 등)
        }

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(type, builder.build()); // notify(int id, Notification notification)
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
