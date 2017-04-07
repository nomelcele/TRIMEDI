package com.group2.trimedi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressLint("ValidFragment")
public class BandFragment extends Fragment {
	private Context mContext;
	private TextView currentHeartView;
	private TextView currentTemperView;
	private TextView currentHeartNormalView;
	private TextView currentTemperNormalView;

	private int memNum;

	public BandFragment(Context context) {
		mContext = context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.band_main_layout, null);

//			Button btn1 = (Button)view.findViewById(R.id.myhis);
//			btn1.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					Intent intent = new Intent(getActivity(), MeasureListActivity.class);
//					getActivity().startActivity(intent);
//				}
//			});
//
//			Button btn2 = (Button)view.findViewById(R.id.going);
//			btn2.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					Intent intent = new Intent(getActivity(),DirectMeasureActivity.class);
//					getActivity().startActivity(intent);
//				}
//			});

		currentHeartView = (TextView)view.findViewById(R.id.currentHeartView);
		currentTemperView = (TextView)view.findViewById(R.id.currentTemperView);
		currentHeartNormalView = (TextView)view.findViewById(R.id.currentHeartNormalView);
		currentTemperNormalView = (TextView)view.findViewById(R.id.currentTemperNormalView);

		ImageButton measureListBtn = (ImageButton)view.findViewById(R.id.measureListBtn);
		measureListBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(getActivity(),MeasureListActivity.class));
			}
		});
		ImageButton directMeasureBtn = (ImageButton)view.findViewById(R.id.directMeasureBtn);
		directMeasureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(getActivity(),DirectMeasureActivity.class));
			}
		});

		setHasOptionsMenu(true);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		// 현재 심박수와 현재 온도 표시
		// 측정값 테이블 조회해서 가장 최근값 2개(심박수와 온도) 가져옴
		RecentMeasuresAsyncTask asyncTask = new RecentMeasuresAsyncTask();
		SharedPreferences sp = getActivity().getSharedPreferences("trimediSharedPreferences",getActivity().MODE_PRIVATE);
		memNum = sp.getInt("loggedInMemNum",0);
		Log.i("recentMeasures","최근 측정값 보여줄 회원 번호: "+memNum);
		String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/measure/user/"+memNum;
		asyncTask.execute(requestURL);
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

	private class RecentMeasuresAsyncTask extends AsyncTask<String,Void,String>{
		@Override
		protected String doInBackground(String... strings) {
			try {
				Log.d("RecentMeasures","웹 서버 연결 시도");
				URL url = new URL(strings[0]);
				HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
				conn.setRequestMethod("POST"); // 통신 방식 지정
				conn.setDoOutput(true); // 쓰기 모드 설정
				conn.setDoInput(true); // 읽기 모드 설정
				conn.setRequestProperty("Content-Type","application/json; charset=UTF-8");
//                conn.setConnectTimeout(40000);

				// 현재 로그인한 회원의 번호를 파라미터로 보냄
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("memNum",memNum);
				Log.i("RecentMeasures","현재 로그인한 회원 번호: "+jsonObject.getString("memNum"));

				OutputStream os = new BufferedOutputStream(conn.getOutputStream());
				os.write(jsonObject.toString().getBytes());
				os.flush();
				os.close();

				Log.i("RecentMeasures",String.valueOf(conn.getResponseCode()));
				if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					// 현재 로그인한 회원의 측정값 리스트 가져옴
					Log.i("RecentMeasures", "통신 성공");
					InputStream is = conn.getInputStream();
					BufferedReader br = new BufferedReader(new InputStreamReader(is));

					StringBuffer sb = new StringBuffer();
					String line = "";
					while ((line = br.readLine()) != null) {
						sb.append(line);
					}

					br.close();
					is.close();

					Log.i("Test", "Received Data: " + sb.toString());

					return sb.toString();

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String receivedData) {
			try{
//				JSONObject jsonObject = new JSONObject(receivedData);
				JSONArray jsonArray = new JSONArray(receivedData);
				Log.i("RecentMeasures","측정값 리스트: "+jsonArray);

				// 가장 최근에 측정된 값 2개(체온/맥박 각각 1개씩) 가져옴
				JSONObject measure1 = null; // 가장 최근에 측정한 심박 데이터
				JSONObject measure2 = null; // 가장 최근에 측정한 체온 데이터
				for(int i=0; i<jsonArray.length(); i++){
					JSONObject measureObj = jsonArray.getJSONObject(i); // 측정값 데이터
					if(measureObj.getInt("msrType") == 1){
						// 측정값의 타입이 심박인 경우
						measure1 = measureObj;
					} else if(measureObj.getInt("msrType") == 0){
						// 측정값의 타입이 체온인 경우
						measure2 = measureObj;
					}
				}

				//////////////////////////////////////////////////////////////
				Log.i("RecentMeasures","measure1: "+measure1);
				Log.i("RecentMeasures","measure2: "+measure2);

				// 가장 최근에 측정한 심박 측정값 표시
				double msrValue1 = measure1.getDouble("msrValue");
				Log.i("RecentMeasures","msrValue1: "+msrValue1);
				Log.i("RecentMeasures","msrNum1: "+measure1.getString("msrNum"));
				currentHeartView.setText(String.valueOf((int)msrValue1));
				// 정상/비정상 표시
				if(msrValue1 > 50 && msrValue1 < 100){
					currentHeartNormalView.setText("정상");
				} else {
					currentHeartNormalView.setText("비정상");
				}


				// 가장 최근에 측정한 체온 측정값 표시
				double msrValue2 = measure2.getDouble("msrValue");
				Log.i("RecentMeasures","msrValue2: "+msrValue2);
				Log.i("RecentMeasures","msrNum2: "+measure2.getString("msrNum"));
				currentTemperView.setText(String.valueOf(msrValue2));
				// 정상/비정상 표시
				if(msrValue2 > 33 && msrValue2 < 38){
					currentTemperNormalView.setText("정상");
				} else {
					currentTemperNormalView.setText("비정상");
				}

			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
}