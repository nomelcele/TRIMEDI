package com.group2.trimedi;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class TimerService extends Service { // Timer

    public static long setTime;                                        // 측정 시작 신호 전송 주기 설정(60000 -> 1분)
    private final String TAG = "TimerService";

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate(){
        Log.d(TAG,"onCreate() Call.");
        final Timer timer = new Timer(true);
        TimerTask timerTask;

        final long time = setTime;
        final long waitTime = 5000;
        final long lastTime = System.currentTimeMillis();               // 초기 시간 설정

        final int SENSING_START = 10000;


        timerTask = new TimerTask() {
            long tempTime = lastTime;                                   // 비교할 시간 설정
            //            int sendCount = 0;                                          // 한 타이밍에 전송한 횟수
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();          // 현재 시간
                Log.d(TAG,"타이머 작동중 " + "setTime:"+setTime+ "recent received msg:"+BlunoLibrary.inputMsg);
                if((currentTime-tempTime)>setTime) {                       // 현재 시간과 비교할 시간의 차이가 주기 이상 나면
//                    timer.cancel();
                    //심박, 온도 측정
                    Log.d(TAG,"측정시작 신호 전송 to "+BlunoLibrary.mDeviceAddress);                   // 측정 시작 신호 전송
                    tempTime = System.currentTimeMillis();
                    BlunoLibrary.serialSend(SENSING_START+"");
                    BlunoLibrary.commandSendFlag = true;
//                    sendCount++;
                }
//                if((currentTime-tempTime)>waitTime){
//                    if(BlunoLibrary.commandSendFlag){                   // commandReceivedFlag -> false (수신확인 메시지가 안오면)
//                        Log.d(TAG,"측정시작 신호 재전송 to "+BlunoLibrary.mDeviceAddress);               // 측정 시작 신호 재전송
//                        BlunoLibrary.serialSend(SENSING_START+"");
//                        tempTime+=waitTime;
//                        BlunoLibrary.commandSendFlag = true;
//                        sendCount++;
//                    }
//                }
//                if(sendCount>1){
//                    if(BlunoLibrary.commandReceivedFlag){
//                        tempTime -= waitTime*(sendCount-1);            // 기준 시간을 맞춰줌
//                        sendCount = 0;
//                    }
//                }
            }
            @Override
            public boolean cancel() {
                Log.v("","타이머 종료");
                return super.cancel();
            }
        };
        timer.schedule(timerTask, 0, 1000);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy() Call.");

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand() Call.");

        return super.onStartCommand(intent, flags, startId);
    }
//    public void serialSend(String theString){
//        Log.d(TAG,"serialSend() called..");
//        if (BlunoLibrary.mConnectionState == BlunoLibrary.connectionStateEnum.isConnected) {
//            BlunoLibrary.mSCharacteristic.setValue(theString);
//            BlunoLibrary.mBluetoothLeService.writeCharacteristic(BlunoLibrary.mSCharacteristic);
//        }
//    }
}
