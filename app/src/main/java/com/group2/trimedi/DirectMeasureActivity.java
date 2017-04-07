package com.group2.trimedi;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

public class DirectMeasureActivity extends BlunoLibrary {
	private Button buttonScan;
	private Button buttonSerialSend;
	private EditText serialSendText;
	private TextView serialReceivedText;

	int msr_type = 0;
	double msr_value = 0.0;

	private Handler handler;
	private Runnable mrun;

	private final String TAG = "DirectMeasureActivity";

	private double temperValue;
	private double heartValue;
	private CustomDialog dialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.direct_measure_ing_layout);

		handler = new Handler();
//		mrun = new Runnable() {
//			@Override
//			public void run() {
//				Intent intent = new Intent(getApplicationContext(),MeasureFinishActivity.class);
//				startActivity(intent);
//				finish();
//
//			}
//		};
//		handler.postDelayed(mrun,3000);

		onCreateProcess();														//onCreate Process by BlunoLibrary


		serialBegin(115200);													//set the Uart Baudrate on BLE chip to 115200



//        serialReceivedText=(TextView) findViewById(R.id.serialReveicedText);	//initial the EditText of the received data
//        serialSendText=(EditText) findViewById(R.id.serialSendText);			//initial the EditText of the sending data
//
//        buttonSerialSend = (Button) findViewById(R.id.buttonSerialSend);		//initial the button for sending the data
//        buttonSerialSend.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//
//				//serialSend(serialSendText.getText().toString());				//send the data to the BLUNO
//				serialSend(SENSING_START+"");
//			}
//		});
//
//        buttonScan = (Button) findViewById(R.id.buttonScan);					//initial the button for scanning the BLE device
//        buttonScan.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//
//				buttonScanOnClickProcess();										//Alert Dialog for selecting the BLE device
//			}
//		});
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		handler.removeCallbacks(mrun);
	}

	protected void onResume(){
		super.onResume();
		System.out.println("BlUNOActivity onResume");
		onResumeProcess();														//onResume Process by BlunoLibrary
		if(!connectedFlag){
			// 디바이스 연결 안 됐을 때
			dialog = new CustomDialog(DirectMeasureActivity.this, "밴드 연결", "밴드가 연결되지 않았습니다.\n 지금 연결하시겠습니까?",
					new OnClickListener() {
						@Override
						public void onClick(View view) {
							// 확인 버튼 클릭 시
							buttonScanOnClickProcess(); // 디바이스 스캔
							dialog.dismiss();
						}
					},"연결");
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.show();
		}
		else serialSend(SENSING_START+"");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG,"onActivityResult() Call.");
		onActivityResultProcess(requestCode, resultCode, data);					//onActivityResult Process by BlunoLibrary
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onPause() {
		Log.d(TAG,"onPause() Call.");
		super.onPause();
		onPauseProcess();														//onPause Process by BlunoLibrary
	}

	protected void onStop() {
		Log.d(TAG,"onStop() Call.");
		super.onStop();
		onStopProcess();														//onStop Process by BlunoLibrary
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG,"onDestroy() Call.");
		super.onDestroy();
		onDestroyProcess();														//onDestroy Process by BlunoLibrary
	}

	@Override
	public void onConnectionStateChange(connectionStateEnum theConnectionState) {
		Log.d(TAG,"onConnectionStateChange() Call.");
		switch (theConnectionState) {
			case isConnected:
				// 연결됐을 때
				serialSend(SENSING_START+"");
				commandSendFlag = true;
				connectedFlag = true;
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
				heartValue = Double.valueOf(String.format("%.2f",sensingData));
				break;
			case TYPE_TEMP: Log.d("TestLog","Temperature: "+sensingData);
				if(sensingData>38.0&&sensingData<30) NotificationSomethings(TYPE_TEMP,sensingData); 	// 이상값 측정시 알림 발생
//				temperValue = sensingData;
				temperValue = Double.valueOf(String.format("%.2f",sensingData));
				Intent intent = new Intent(getApplicationContext(),MeasureFinishActivity.class);
				intent.putExtra("value1",heartValue); // 심박
				intent.putExtra("value2",temperValue); // 체온
				startActivity(intent);
				finish();
				break;
		}
	}

	@Override
	public void onSerialReceived(String theString) {
		Log.d(TAG,theString);
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
	public void receiveOkProcess(){
		Log.d(TAG,"Wait...");
		commandReceivedFlag = true;
		commandSendFlag = false;
	}
	@Override
	public void noDeviceProcess(){
		dialog = new CustomDialog(DirectMeasureActivity.this, "밴드 착용", "밴드를 착용하지 않았습니다.\n 지금 착용하셨습니까?",
				new OnClickListener() {
					@Override
					public void onClick(View view) {
						// 확인 버튼 클릭 시
						serialSend(SENSING_START+"");
						dialog.dismiss();
					}
				},"착용완료");
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.show();
		Log.d(TAG,"noDevice...");
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

}
