package com.group2.trimedi;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONObject;

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
import java.util.Locale;

public class MainActivity extends BlunoLibrary implements
		ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	private SharedPreferences sp;
	private SharedPreferences.Editor spEditor;

	private int memNum;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		FirebaseMessaging.getInstance().subscribeToTopic("news");
//		FirebaseInstanceId.getInstance().getToken();

		// 앱 내부 데이터를 저장하기 위한 SharedPreferences 생성
		sp = getSharedPreferences("trimediSharedPreferences",MODE_PRIVATE);
		spEditor = sp.edit();
		memNum = sp.getInt("loggedInMemNum",0);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		setContentView(R.layout.main_layout);


		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			String currentPresEndDate = sp.getString("currentPresEndDate","noPres");
			long endDateMilli = 0;
			Log.i("Main","currentPresEndDate: "+currentPresEndDate);
			Log.i("Main","currentPresNum: "+sp.getInt("currentPresNum",0));

			if(!currentPresEndDate.equals("noPres")) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				endDateMilli = sdf.parse(currentPresEndDate).getTime();
			}

			if(currentPresEndDate.equals("noPres") || System.currentTimeMillis() > endDateMilli){
				// 현재 적용되는 처방전이 끝나는 날짜를 가져옴
				// 저장된 값이 없으면 or 현재 시간이 처방전 끝나는 날짜보다 늦으면
				// db에서 처방전 정보 조회
				GetPresInfoAsyncTask asyncTask = new GetPresInfoAsyncTask();
				String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/prescription/latest/member/"+memNum;
				asyncTask.execute(requestURL);
			}


		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		Context mContext = getApplicationContext();

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			switch(position) {
			case 0:
				return new BandFragment(mContext);
			case 1:
				return new PillBoxFragment(mContext);
			}
			return null;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
					container, false);
			TextView dummyTextView = (TextView) rootView
					.findViewById(R.id.section_label);
			dummyTextView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return rootView;
		}
	}

	private class GetPresInfoAsyncTask extends AsyncTask<String,Void,Void> {
		@Override
		protected Void doInBackground(String... strings) {
			try {
				Log.i("GetPresInfo", "웹 서버 연결 시도");
				URL url = new URL(strings[0]);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // URL을 연결한 객체 생성
				conn.setRequestMethod("POST"); // 통신 방식 지정(POST)
				conn.setDoInput(true); // 쓰기 모드 설정
				conn.setDoOutput(true);
				conn.setRequestProperty("Content-Type","application/json; charset=UTF-8");

				JSONObject jsonObject = new JSONObject();
				jsonObject.put("memNum",memNum);

				OutputStream os = new BufferedOutputStream(conn.getOutputStream());
				os.write(jsonObject.toString().getBytes());
				os.flush();
				os.close();

				Log.i("GetPresInfo", String.valueOf(conn.getResponseCode()));
				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					// 서버로부터 받은 알람값 데이터들 읽기
					Log.i("GetPresInfo", "통신 성공");
					InputStream is = conn.getInputStream();
					BufferedReader br = new BufferedReader(new InputStreamReader(is));

					StringBuffer sb = new StringBuffer();
					String line = "";
					while ((line = br.readLine()) != null) {
						sb.append(line);
					}

					br.close();
					is.close();

					// 읽은 데이터 JSON 파싱
					JSONObject presInfo = new JSONObject(sb.toString());

					// 현재 적용되는 처방전 번호 저장
					spEditor.putInt("currentPresNum",presInfo.getInt("presNum"));

					String pres_date = presInfo.getString("presDate");
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
					Date date = sdf.parse(pres_date);
					int presDay = Integer.parseInt(presInfo.getString("presDay"));
					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					cal.add(Calendar.DATE,presDay-1);
					spEditor.putString("currentPresEndDate",sdf.format(cal.getTime()));

					spEditor.commit();
					Log.i("GetPresInfo","currentPresNum: "+sp.getInt("currentPresNum",0));
					Log.i("GetPresInfo","currentPresEndDate: "+sp.getString("currentPresEndDate","No Current Pres"));

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}
	}

	@Override
	public void onConnectionStateChange(connectionStateEnum theconnectionStateEnum) {

	}

	@Override
	public void noDeviceProcess() {
		Log.d("MainActivity","noDevice...");
	}

	@Override
	public void receiveOkProcess() {
		commandReceivedFlag = true;
		commandSendFlag = false;
	}

	@Override
	public void sensingEndProcess(double data) {

	}
	@Override
	public void onSerialReceived(String theString) {							//Once connection data received, this function will be called
		//theString: 수신한 문자열
		Log.d("TriMedi",theString);
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
}
