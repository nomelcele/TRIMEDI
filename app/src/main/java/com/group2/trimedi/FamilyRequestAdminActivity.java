package com.group2.trimedi;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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

public class FamilyRequestAdminActivity extends Activity {
    private LinearLayout familyReqNoticeLayout;
    private LinearLayout famReqLayout1;
    private LinearLayout famReqLayout2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.family_request_admin_layout);
//        setContentView(R.layout.family_request_admin_layout_2);

        ActionBar actionBar =getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        familyReqNoticeLayout = (LinearLayout)findViewById(R.id.familyReqNoticeLayout);
        famReqLayout1 = (LinearLayout)findViewById(R.id.famReqLayout1);
        famReqLayout2 = (LinearLayout)findViewById(R.id.famReqLayout2);

        familyReqNoticeLayout.setVisibility(View.GONE);
        famReqLayout1.setVisibility(View.GONE);
        famReqLayout2.setVisibility(View.GONE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // 나에게 가족 등록 요청을 보낸 회원들의 정보 가져옴
        FamilyRequestersAsyncTask asyncTask = new FamilyRequestersAsyncTask();
        SharedPreferences sp = getSharedPreferences("trimediSharedPreferences",MODE_PRIVATE);
        int frTo = sp.getInt("loggedInMemNum",0); // 현재 로그인한 회원의 번호
        String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/familyrequest/frTo/"+frTo;
        asyncTask.execute(requestURL,String.valueOf(frTo));
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

    private class FamilyRequestersAsyncTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            try {
                Log.d("FamilyRequesters","웹 서버 연결 시도");
                URL url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
                conn.setRequestMethod("POST"); // 통신 방식 지정(POST)
                conn.setDoOutput(true); // 쓰기 모드 설정
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type","application/json");

                JSONObject params = new JSONObject();
                params.put("frTo",strings[1]); // 현재 로그인한 회원의 번호

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(params.toString().getBytes());
                os.flush();
                os.close();

                Log.d("FamilyRequesters",String.valueOf(conn.getResponseCode()));
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    // 서버로부터 받은 알람값 데이터들 읽기
                    Log.i("FamilyRequesters","통신 성공");
                    InputStream is = conn.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));

                    StringBuffer sb = new StringBuffer();
                    String line = "";
                    while((line = br.readLine()) != null){
                        sb.append(line);
                    }

                    br.close();
                    is.close();

                    Log.i("FamilyRequesters","데이터: "+sb.toString());

                    return sb.toString();

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String receivedData) {
            Log.i("FamilyRequesters","receivedData: "+receivedData);
            try {
//                LinearLayout familyReqNoticeLayout = (LinearLayout)findViewById(R.id.familyReqNoticeLayout);
//                LinearLayout famReqLayout1 = (LinearLayout)findViewById(R.id.famReqLayout1);

                if(!receivedData.equals("[]")){
                    JSONArray famReqList = new JSONArray(receivedData);

                    for(int i=0; i<famReqList.length(); i++){
                        JSONObject famReq = famReqList.getJSONObject(i);
                        if(famReq.getInt("frAuthtype") != 0){
                            famReqList.remove(i);
                        }
                    }



                    familyReqNoticeLayout.setVisibility(View.GONE);

                    final JSONObject famReq1 = famReqList.getJSONObject(0);
                    JSONObject frFromInfo1 = famReq1.getJSONObject("frFrom");
                    Log.i("FamilyRequest","receivedData: "+famReq1.toString());
                    TextView famReqMemNameView1 = (TextView)findViewById(R.id.famReqMemNameView1);
                    famReqMemNameView1.setText(frFromInfo1.getString("memName"));

                    TextView famReqMemGenderAgeView1 = (TextView)findViewById(R.id.famReqMemGenderAgeView1);
                    StringBuffer genderAgeText = new StringBuffer();
                    if(frFromInfo1.getInt("memGender") == 0){
                        genderAgeText.append("남");
                    } else {
                        genderAgeText.append("여");
                    }
                    Calendar todayCal = Calendar.getInstance();
                    int todayYear = todayCal.get(Calendar.YEAR);
                    Calendar birthCal = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                    birthCal.setTime(dateFormat.parse(frFromInfo1.getString("memBirth")));
                    int birthYear = birthCal.get(Calendar.YEAR);
                    genderAgeText.append("/").append(todayYear-birthYear);
                    famReqMemGenderAgeView1.setText(genderAgeText.toString());

                    TextView famReqMemMailView1 = (TextView)findViewById(R.id.famReqMemMailView1);
                    famReqMemMailView1.setText(frFromInfo1.getString("memMail"));

                    // 승인 버튼에 이벤트 리스너 달기
                    Button famReqOkBtn1 = (Button)findViewById(R.id.famReqOkBtn1);
                    famReqOkBtn1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.i("FamilyRequest","승인 버튼 클릭");
                            AdminFamilyRequestAsyncTask asyncTask = new AdminFamilyRequestAsyncTask();
                            try {
                                int frNum = famReq1.getInt("frNum");
                                // 요청 번호, 업데이트할 승인 여부(1: 승인)
                                asyncTask.execute(frNum,1);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    // 거절 버튼에 이벤트 리스너 달기
                    Button famReqRefuseBtn1 = (Button)findViewById(R.id.famReqRefuseBtn1);
                    famReqRefuseBtn1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.i("FamilyRequest","거절 버튼 클릭");
                            AdminFamilyRequestAsyncTask asyncTask = new AdminFamilyRequestAsyncTask();
                            try {
                                int frNum =  famReq1.getInt("frNum");
                                // 요청 번호, 업데이트할 승인 여부(2: 거절)
                                asyncTask.execute(frNum,2);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    famReqLayout1.setVisibility(View.VISIBLE);

                    if(famReqList.length() == 2){
                        final JSONObject famReq2 = famReqList.getJSONObject(1);
                        JSONObject frFromInfo2 = famReq2.getJSONObject("frFrom");
                        Log.i("FamilyRequest","receivedData: "+famReq2.toString());
                        TextView famReqMemNameView2 = (TextView)findViewById(R.id.famReqMemNameView2);
                        famReqMemNameView2.setText(frFromInfo2.getString("memName"));

                        TextView famReqMemGenderAgeView2 = (TextView)findViewById(R.id.famReqMemGenderAgeView2);
                        StringBuffer genderAgeText2 = new StringBuffer();
                        if(frFromInfo2.getInt("memGender") == 0){
                            genderAgeText2.append("남");
                        } else {
                            genderAgeText2.append("여");
                        }
                        Calendar birthCal2 = Calendar.getInstance();
                        birthCal2.setTime(dateFormat.parse(frFromInfo2.getString("memBirth")));
                        int birthYear2 = birthCal2.get(Calendar.YEAR);
                        genderAgeText2.append("/").append(todayYear-birthYear2);
                        famReqMemGenderAgeView2.setText(genderAgeText2.toString());

                        TextView famReqMemMailView2 = (TextView)findViewById(R.id.famReqMemMailView2);
                        famReqMemMailView2.setText(frFromInfo2.getString("memMail"));

                        // 승인 버튼에 이벤트 리스너 달기
                        Button famReqOkBtn2 = (Button)findViewById(R.id.famReqOkBtn2);
                        famReqOkBtn2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.i("FamilyRequest","승인 버튼 클릭");
                                AdminFamilyRequestAsyncTask asyncTask = new AdminFamilyRequestAsyncTask();
                                try {
                                    int frNum = famReq2.getInt("frNum");
                                    // 요청 번호, 업데이트할 승인 여부(1: 승인)
                                    asyncTask.execute(frNum,1);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        // 거절 버튼에 이벤트 리스너 달기
                        Button famReqRefuseBtn2 = (Button)findViewById(R.id.famReqRefuseBtn2);
                        famReqRefuseBtn2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.i("FamilyRequest","거절 버튼 클릭");
                                AdminFamilyRequestAsyncTask asyncTask = new AdminFamilyRequestAsyncTask();
                                try {
                                    int frNum =  famReq2.getInt("frNum");
                                    // 요청 번호, 업데이트할 승인 여부(2: 거절)
                                    asyncTask.execute(frNum,2);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        famReqLayout2.setVisibility(View.VISIBLE);
                    }

                } else {
                    familyReqNoticeLayout.setVisibility(View.VISIBLE);
                    famReqLayout1.setVisibility(View.GONE);
                    famReqLayout2.setVisibility(View.GONE);
                }

            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private class AdminFamilyRequestAsyncTask extends AsyncTask<Integer,Void,Void>{
        private int reqType;

        @Override
        protected Void doInBackground(Integer... integers) {
            // HTTP Request 유형(0: 승인, 1: 거절), 요청 보낸 사람, 요청 받는 사람(로그인한 회원 번호)
            reqType = integers[1];

            try {
                Log.d("AdminFamilyRequest","웹 서버 연결 시도");
                String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/family/response/frNum/"+integers[0]+"/frAuthType/"+integers[1];

                URL url = new URL(requestURL);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
                conn.setRequestMethod("POST"); // 통신 방식 지정(POST)
                conn.setDoOutput(true); // 쓰기 모드 설정
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type","application/json");

                JSONObject params = new JSONObject();
                params.put("frNum",integers[0]);
                params.put("frAuthtype",integers[1]);
//                params.put("frAuthType",integers[1]);
                Log.i("familyRequestSend","params: "+params.toString());

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(params.toString().getBytes());
                os.flush();
                os.close();

                Log.d("AdminFamilyRequest",String.valueOf(conn.getResponseCode()));

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(reqType == 1){
                Toast.makeText(FamilyRequestAdminActivity.this,"가족 등록 요청을 승인했습니다.",Toast.LENGTH_LONG).show();
            } else if(reqType == 2){
                Toast.makeText(FamilyRequestAdminActivity.this,"가족 등록 요청을 거절했습니다.",Toast.LENGTH_LONG).show();
            }
            recreate();
        }
    }
}
