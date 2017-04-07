package com.group2.trimedi;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by kimdohyun on 2016-11-30.
 */

public class FamilyListActivity extends Activity {
    private TextView familyListNoticeView;
    private LinearLayout familyListLayout;
    private int memNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.family_list_layout);
        ActionBar actionBar =getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        familyListNoticeView = (TextView)findViewById(R.id.familyListNoticeView);
        familyListNoticeView.setVisibility(View.GONE);
        familyListLayout = (LinearLayout)findViewById(R.id.familyListLayout);
        familyListLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 나의 가족 목록 불러오기
        MyFamilyListAsyncTask asyncTask = new MyFamilyListAsyncTask();
        SharedPreferences sp = getSharedPreferences("trimediSharedPreferences",MODE_PRIVATE);
        int familyNum = sp.getInt("loggedInFamily",0);
        memNum = sp.getInt("loggedInMemNum",0);
        String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/family/"+familyNum;
        asyncTask.execute(requestURL,String.valueOf(familyNum));
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

    private class MyFamilyListAsyncTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            try {
                Log.d("MyFamilyList","웹 서버 연결 시도");
                URL url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
                conn.setRequestMethod("POST"); // 통신 방식 지정
                conn.setDoOutput(true); // 쓰기 모드 설정
                conn.setDoInput(true); // 읽기 모드 설정
                conn.setRequestProperty("Content-Type","application/json");

                // 현재 로그인한 회원의 가족 번호를 파라미터로 보냄
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("famNum",strings[1]);

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(jsonObject.toString().getBytes());
                os.flush();
                os.close();

                Log.d("MyFamilyList",String.valueOf(conn.getResponseCode()));
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Log.i("MyFamilyList", "통신 성공");
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
            try {
                JSONArray family = new JSONArray(receivedData);

                if(family.isNull(0)){
                    // 가족 정보가 없으면
                    Log.i("MyFamilyList","가족 정보 없음");
                    familyListNoticeView.setVisibility(View.VISIBLE);
                } else {
                    // 가족 정보가 있으면
//                    familyListNoticeView.setVisibility(View.GONE);
                    familyListLayout.setVisibility(View.VISIBLE);
                    LinearLayout familyListNoticeLayout = (LinearLayout)findViewById(R.id.familyListNoticeLayout);
                    familyListNoticeLayout.setVisibility(View.GONE);

                    for(int i=0; i<family.length(); i++){
                        JSONObject famMem = family.getJSONObject(i);
                        if(famMem.getInt("memNum") == memNum){
                            // 현재 로그인한 회원(본인)인 경우
                            family.remove(i);
                        }
                    }

                    JSONObject famMem1 = family.getJSONObject(0); // 첫번째 가족 회원 정보

                    // 첫번째 가족 회원 정보 표시
                    // 이름 표시
                    TextView famMemNameView1 = (TextView)findViewById(R.id.famMemNameView1);
                    famMemNameView1.setText(famMem1.getString("memName"));
                    // 성별/나이 표시
                    TextView famMemGenderAgeView1 = (TextView)findViewById(R.id.famMemGenderAgeView1);
                    StringBuffer genderAgeText = new StringBuffer();
                    if(famMem1.getInt("memGender") == 0){
                        genderAgeText.append("남");
                    } else {
                        genderAgeText.append("여");
                    }
                    Calendar todayCal = Calendar.getInstance();
                    int todayYear = todayCal.get(Calendar.YEAR);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                    Calendar birthCal = Calendar.getInstance();
                    birthCal.setTime(dateFormat.parse(famMem1.getString("memBirth")));
                    int birthYear = birthCal.get(Calendar.YEAR);
                    genderAgeText.append("/").append(todayYear-birthYear);
                    famMemGenderAgeView1.setText(genderAgeText.toString());
                    // 계정 이메일 표시
                    TextView famMemMailView1 = (TextView)findViewById(R.id.famMemMailView1);
                    famMemMailView1.setText(famMem1.getString("memMail"));
                    // 알림 켜고 끄는 스위치 세팅
                    // 삭제 버튼 세팅

                    if(family.length() == 2){
                        // 두번째 가족 회원이 있으면
                        JSONObject famMem2 = family.getJSONObject(1); // 두번째 가족 회원 정보
                        // 두번째 가족 회원 정보 표시
                        // 이름 표시
                        TextView famMemNameView2 = (TextView)findViewById(R.id.famMemNameView2);
                        famMemNameView2.setText(famMem2.getString("memName"));
                        // 성별/나이 표시
                        TextView famMemGenderAgeView2 = (TextView)findViewById(R.id.famMemGenderAgeView2);
                        StringBuffer genderAgeText2 = new StringBuffer();
                        if(famMem2.getInt("memGender") == 0){
                            genderAgeText2.append("남");
                        } else {
                            genderAgeText2.append("여");
                        }
                        birthCal.setTime(dateFormat.parse(famMem2.getString("memBirth")));
                        int birthYear2 = birthCal.get(Calendar.YEAR);
                        genderAgeText2.append("/").append(todayYear-birthYear2);
                        famMemGenderAgeView2.setText(genderAgeText2.toString());
                        // 계정 이메일 표시
                        TextView famMemMailView2 = (TextView)findViewById(R.id.famMemMailView2);
                        famMemMailView2.setText(famMem2.getString("memMail"));
                        // 알림 켜고 끄는 스위치 세팅
                        // 삭제 버튼 세팅
                    } else {
                        // 두번째 가족 정보가 없으면
                        // 레이아웃 숨기기
                        LinearLayout famMem2InfoLayout = (LinearLayout)findViewById(R.id.famMem2InfoLayout);
                        famMem2InfoLayout.setVisibility(View.GONE);
                    }

                }

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
