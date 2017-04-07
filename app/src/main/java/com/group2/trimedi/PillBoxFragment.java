package com.group2.trimedi;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@SuppressLint("ValidFragment")
public class PillBoxFragment extends Fragment {
	private Context mContext;
	private String[] presContents; // QR 코드에서 읽은 처방전 정보

	private TextView remainingAlarmTimeView; // 다음 알람까지 남은 시간 표시
 	private TextView recentAlarmTimeView; // 다음 알람 시간 표시

	private int currentPresNum;
	private int memNum;
	private SharedPreferences sp;

	public PillBoxFragment(Context context) {
		mContext = context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.pillbox_main_layout, null);
		Log.i("PillBoxFragment","Context: "+mContext);
		ImageButton goToAlarm = (ImageButton)view.findViewById(R.id.goToAlarm);
		goToAlarm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),PillBoxAlarmActivity.class);
				getActivity().startActivity(intent);
			}
		});

		ImageButton goToQR = (ImageButton)view.findViewById(R.id.goToQR);
		goToQR.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// MainActivity에서 '처방전 등록' 이미지 버튼을 클릭했을 때
				// QR 코드 인식 액티비티로 이동
				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
				intent.putExtra("SCAN_MODE", "ALL");
				startActivityForResult(intent, 0);
			}
		});

		ImageButton goToPresList = (ImageButton)view.findViewById(R.id.goToPresList);
		goToPresList.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getActivity(),PrescriptionListActivity.class);
				getActivity().startActivity(intent);
			}
		});

		ImageButton goToFamilyMedi = (ImageButton)view.findViewById(R.id.goToFamilyMedi);
		goToFamilyMedi.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getActivity(),FamilyMediActivity.class);
				getActivity().startActivity(intent);
			}
		});

		remainingAlarmTimeView = (TextView)view.findViewById(R.id.remainingAlarmTimeView);
		recentAlarmTimeView = (TextView)view.findViewById(R.id.recentAlarmTimeView);

		sp = getActivity().getSharedPreferences("trimediSharedPreferences",getActivity().MODE_PRIVATE);

		setHasOptionsMenu(true);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		// 다음 알람 시간 가져오기
		NextAlarmTimeAsyncTask asyncTask = new NextAlarmTimeAsyncTask();
		SharedPreferences sp = getActivity().getSharedPreferences("trimediSharedPreferences",getActivity().MODE_PRIVATE);
//		currentPresNum = sp.getInt("currentPresNum",0);
//		Log.i("currentPresNum",String.valueOf(currentPresNum));
		memNum = sp.getInt("loggedInMemNum",0);;
//		String requestURL = "http://"+getActivity().getResources().getString(R.string.str_ip_address)+"/prescription/"+currentPresNum;
		String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/prescription/latest/member/"+memNum;
//		asyncTask.execute(requestURL);
		asyncTask.execute(requestURL,String.valueOf(memNum));
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.settings_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if(id == R.id.action_settings) {
			Intent intent = new Intent(getActivity(),SettingsActivity.class);
			getActivity().startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			if (resultCode == LoginActivity.RESULT_OK) {
				String contents = data.getStringExtra("SCAN_RESULT");
				//위의 contents 값에 scan result가 들어온다.
				Log.i("QR 코드 인식", contents);

				// split
				// 14/복약 주의 사항입니다./판교약국/타이레놀:3:4:3/아스피린:2:2:3
				presContents = contents.split("/");

				String requestURL = "http://" + getResources().getString(R.string.str_ip_address) + "/prescription";
				AddPresAsyncTask asyncTask = new AddPresAsyncTask();
				asyncTask.execute(requestURL);

			}
//			super.onActivityResult(requestCode, resultCode, data);
		}
	}


	private class AddPresAsyncTask extends AsyncTask<String,Void,String> {
		@Override
		protected String doInBackground(String... strings) {
			try {
				Socket socket = new Socket("www.google.com", 80);
				String localAddr = socket.getLocalAddress().getHostAddress();
				Log.i("AddPrescription", "local address is : " + localAddr);

				Log.i("AddPrescription", "웹 서버 연결 시도");
				URL url = new URL(strings[0]);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // URL을 연결한 객체 생성
				conn.setRequestMethod("POST"); // 통신 방식 지정(POST)
				conn.setDoOutput(true); // 쓰기 모드 설정
				conn.setDoInput(true);
				conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

				SharedPreferences sp = getActivity().getSharedPreferences("trimediSharedPreferences",getActivity().MODE_PRIVATE);
				JSONObject jsonObject = new JSONObject();
				JSONArray medicines = new JSONArray();
				// 14/복약 주의 사항입니다./판교약국/타이레놀:3:4:3/아스피린:2:2:3

				for (int i = 3; i < presContents.length; i++) {
					JSONObject mediInfo = new JSONObject();
					String[] mediArr = presContents[i].split(":");
					mediInfo.put("mediName",mediArr[0]);
					mediInfo.put("mediDose1",mediArr[1]);
					mediInfo.put("mediDose2",mediArr[2]);
					mediInfo.put("mediDose3",mediArr[3]);
					medicines.put(mediInfo);
				}

				jsonObject.put("medicine",medicines);
				jsonObject.put("memNum",sp.getInt("loggedInMemNum",50));
				// 현재 시간: yyyyMMdd 형태(20161215)
				Calendar todayCal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				jsonObject.put("presDate",sdf.format(todayCal.getTime()));
				jsonObject.put("presDay",presContents[0]);
				jsonObject.put("presPharmacy",presContents[2]);
				jsonObject.put("presWarn",presContents[1]);

				Log.i("QR 코드 인식",medicines.toString());
				Log.i("QR 코드 인식","파라미터: "+jsonObject.toString());

				OutputStream os = new BufferedOutputStream(conn.getOutputStream());
				os.write(jsonObject.toString().getBytes());
				os.flush();
				os.close();

				Log.i("QR 코드 인식",String.valueOf(conn.getResponseCode()));
				if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
					// 서버로부터 받은 측정값 데이터들 읽기
					Log.i("QR 코드 인식","통신 성공");
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
			Log.i("처방전 등록","receivedData: "+receivedData);
			try {
				JSONObject newPres = new JSONObject(receivedData);
				SharedPreferences.Editor editor = sp.edit();
//				editor.putString("currentPresEndDate", ); // 처방전 끝나는 날짜 저장
				editor.putInt("currentPresNum", newPres.getInt("presNum"));
				Log.i("처방전 등록","추가된 처방전 번호: "+newPres.getInt("presNum"));
				editor.commit();
				Toast.makeText(getActivity(), "처방전이 등록되었습니다.", Toast.LENGTH_SHORT).show();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	private class NextAlarmTimeAsyncTask extends AsyncTask<String,Void,String>{
		@Override
		protected String doInBackground(String... strings) {
			try {
				Log.i("NextAlarmTime", "웹 서버 연결 시도");
				URL url = new URL(strings[0]);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // URL을 연결한 객체 생성
				conn.setRequestMethod("POST"); // 통신 방식 지정(POST)
				conn.setDoOutput(true); // 쓰기 모드 설정
				conn.setDoInput(true);
				conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

				JSONObject jsonObject = new JSONObject();
//				jsonObject.put("presNum",currentPresNum);
				jsonObject.put("memNum",strings[1]);
				Log.i("NextAlarmTime","파라미터: "+jsonObject.toString());

				OutputStream os = new BufferedOutputStream(conn.getOutputStream());
				os.write(jsonObject.toString().getBytes());
				os.flush();
				os.close();

				Log.i("NextAlarmTime",String.valueOf(conn.getResponseCode()));
				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					// 서버로부터 받은 알람값 데이터들 읽기
					Log.i("NextAlarmTime", "통신 성공");
					InputStream is = conn.getInputStream();
					BufferedReader br = new BufferedReader(new InputStreamReader(is));

					StringBuffer sb = new StringBuffer();
					String line = "";
					while ((line = br.readLine()) != null) {
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
			Log.i("NextAlarmTime","receivedData: "+receivedData);
			try {
				JSONObject presInfo = new JSONObject(receivedData);
				String[] alarms = new String[3];
				alarms[0] = presInfo.getString("presAlarm1"); // HHmm
				alarms[1] = presInfo.getString("presAlarm2");
				alarms[2] = presInfo.getString("presAlarm3");

				int minDiffer = Integer.MIN_VALUE;
				int nextAlarmIdx = 0;
				String[] trCheckDefaultValues = new String[3];

				// 바로 다음 알람이 뭔지 구하기
				Calendar currentCal = Calendar.getInstance();
				int currentHour = currentCal.get(Calendar.HOUR_OF_DAY);
				int currentMinute = currentCal.get(Calendar.MINUTE);
				int currentMinutes = (currentHour*60)+currentMinute;

				int alarmHour, alarmMinute, alarmMinutes;
				int[] alarmMinutesArr = new int[3];

				for(int i=0; i<3; i++) {
					if(!alarms[i].equals("0")) {
						if(i == 0 && alarms[i].length() == 3){
							// 아침 알람 시간 앞에 0 빠졌을 경우
							alarmHour = Integer.parseInt(alarms[i].substring(0,1));
							alarmMinute = Integer.parseInt(alarms[i].substring(1,3));
						} else {
							alarmHour = Integer.parseInt(alarms[i].substring(0, 2));
							alarmMinute = Integer.parseInt(alarms[i].substring(2, 4));
						}
						alarmMinutesArr[i] = (alarmHour * 60) + alarmMinute;

						// 현재 시간 - 알람은 음수여야 함
						if(currentMinutes - alarmMinutesArr[i] < 0 && currentMinutes - alarmMinutesArr[i] > minDiffer){
							minDiffer = currentMinutes - alarmMinutesArr[i];
							nextAlarmIdx = i;
						}

						trCheckDefaultValues[i] = "1";

					} else {
						// 처방 기록 저장할 때 해당 시간은 원래 안 먹는 시간임을 표시
						trCheckDefaultValues[i] = "0";
					}
				}

				// 현재 시간이 저녁 알람 시간보다 늦으면 다음 알람은 아침 알람
				if(currentMinutes > alarmMinutesArr[2]){
					nextAlarmIdx = 0;
					minDiffer = ((24*60)-currentMinutes)+alarmMinutesArr[0];
				}


				Log.i("NextAlarmTime","NextAlarmIdx: "+nextAlarmIdx);
				Log.i("NextAlarmTime","minDiffer: "+minDiffer);

				if(alarms[0].equals("0") && alarms[1].equals("0") && alarms[2].equals("0")){
					// 설정한 알람 시간이 없을 경우
					remainingAlarmTimeView.setText("00:00"); // 다음 알람까지 남은 시간
					recentAlarmTimeView.setText("알람을 설정해 주세요."); // 복용할 시간
				} else {
					StringBuffer remainingHour = new StringBuffer();
					StringBuffer remainingMinute = new StringBuffer();
					int hour = -1 * (minDiffer / 60);
					int minutes = ((-1 * minDiffer) % 60);
					if (hour < 10) {
						remainingHour.append(0);
					}
					remainingHour.append(hour);
					if (minutes < 10) {
						remainingMinute.append(0);
					}
					remainingMinute.append(minutes);
					remainingAlarmTimeView.setText(remainingHour.toString() + ":" + remainingMinute.toString()); // 다음 알람까지 남은 시간

					if (nextAlarmIdx == 0 && alarms[nextAlarmIdx].length() == 3) {
						// 아침 알람 시간 앞에 0 빠졌을 경우
						recentAlarmTimeView.setText(alarms[nextAlarmIdx].substring(0, 1)
								+ ":" + alarms[nextAlarmIdx].substring(1, 3) + "에 복용"); // 복용할 시간
					} else {
						recentAlarmTimeView.setText(alarms[nextAlarmIdx].substring(0, 2)
								+ ":" + alarms[nextAlarmIdx].substring(2, 4) + "에 복용"); // 복용할 시간
					}
				}

				// 오늘의 복용 기록 데이터가 없는 경우
				JSONArray takingrecord = null;
				SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyyMMdd");
				String todayDate = dbDateFormat.format(Calendar.getInstance().getTime());
				if(presInfo.isNull("takingrecord")){
					// 복용 기록이 없으면
					// 오늘의 복용 기록을 저장할 데이터 INSERT
					Log.i("NextAlarmTime","복용 기록 없음");
					InsertTodayTrAsyncTask asyncTask = new InsertTodayTrAsyncTask();
					String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/prescription/takingrecord/presNum/"+currentPresNum;
					Log.i("InsertTodayTr","todayDate: "+todayDate);
					asyncTask.execute(requestURL,String.valueOf(currentPresNum),todayDate,trCheckDefaultValues[0],trCheckDefaultValues[1],trCheckDefaultValues[2]);

				} else {
					takingrecord = presInfo.getJSONArray("takingrecord");
					JSONObject todayTakingRecord = takingrecord.getJSONObject(takingrecord.length()-1);
					Log.i("InsertTodayTr","마지막 복용 기록 날짜: "+todayTakingRecord.getString("trDate"));
					if(!todayTakingRecord.getString("trDate").equals(todayDate)){
						// 오늘의 복용 기록이 없을 경우 오늘 복용 기록을 저장할 데이터 INSERT
						Log.i("NextAlarmTime","오늘의 복용 기록 없음");
						InsertTodayTrAsyncTask asyncTask = new InsertTodayTrAsyncTask();
						String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/prescription/takingrecord/presNum/"+currentPresNum;
						Log.i("InsertTodayTr","todayDate: "+todayDate);
						asyncTask.execute(requestURL,String.valueOf(currentPresNum),todayDate,trCheckDefaultValues[0],trCheckDefaultValues[1],trCheckDefaultValues[2]);
					}
				}


			} catch(Exception e){
				e.printStackTrace();
			}
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
				params.put("trDate",strings[2]);
				params.put("trCheck1",strings[3]);
				params.put("trCheck2",strings[4]);
				params.put("trCheck3",strings[5]);
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