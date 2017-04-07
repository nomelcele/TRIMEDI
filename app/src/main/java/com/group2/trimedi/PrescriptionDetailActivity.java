package com.group2.trimedi;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
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
import java.util.Date;

/**
 * Created by kimdohyun on 2016-12-01.
 */

public class PrescriptionDetailActivity extends Activity {
    private int presNum;
    private Button trListBtn;

    private TextView presDetailDateView;
    private TextView presDetailPharmacyView;
    private TextView presDetailWarnView;

    private ListView presMediList;
    private PrescriptionMediListAdapter adapter;

    private StringBuffer morningDose;
    private StringBuffer afternoonDose;
    private StringBuffer eveningDose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prescription_detail_layout);

        presNum = getIntent().getIntExtra("presNum",0);

        ActionBar actionBar =getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        presDetailDateView = (TextView)findViewById(R.id.presDetailDateView);
        presDetailPharmacyView = (TextView)findViewById(R.id.presDetailPharmacyView);
        presDetailWarnView = (TextView)findViewById(R.id.presDetailWarnView);

        presMediList = (ListView)findViewById(R.id.presMediList);

        trListBtn = (Button)findViewById(R.id.trListBtn);

    }

    @Override
    protected void onResume() {
        // 선택한 처방전의 정보 얻기
        super.onResume();
        // 어댑터 생성
        adapter = new PrescriptionMediListAdapter();
        Log.i("GetPresDetail","처방전 정보 얻기");
        GetPresDetailAsyncTask asyncTask = new GetPresDetailAsyncTask();
        String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/prescription/"+presNum;
        asyncTask.execute(requestURL,String.valueOf(presNum));
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
        if (id == R.id.action_settings) {
            Intent intent = new Intent(PrescriptionDetailActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    private class GetPresDetailAsyncTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            try {
                Log.i("GetPresDetail","웹 서버 연결 시도");
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

                Log.i("GetPresDetail",String.valueOf(conn.getResponseCode()));
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    Log.i("GetPresDetail","통신 성공");
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
            // 읽은 데이터 JSON 파싱
            try{
                JSONObject presDetail = new JSONObject(receivedData); // 처방전 정보
                Log.i("GetPresDetail","presDetail: "+presDetail);
                JSONArray presMedicine = presDetail.getJSONArray("medicine"); // 처방전에 포함된 약 정보
                final JSONArray takingRecord = presDetail.getJSONArray("takingrecord"); // 처방전의 복용 정보

                // 처방전 시작 날짜, 종료 날짜 세팅
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.");
                SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyyMMdd");

                String presDate = presDetail.getString("presDate");
                Date date = dbDateFormat.parse(presDate);

                int presDay = Integer.parseInt(presDetail.getString("presDay"));
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.add(Calendar.DATE,presDay-1);

                StringBuffer presDayText = new StringBuffer();
                presDayText.append(dateFormat.format(date)).append(" ~ ");
                presDayText.append(dateFormat.format(cal.getTime()));

                presDetailDateView.setText(presDayText.toString());
                presDetailPharmacyView.setText(presDetail.getString("presPharmacy"));
                presDetailWarnView.setText(presDetail.getString("presWarn"));

                // 복용 기록 액티비티에서 출력할 아침/점심/저녁 복용량(intent로 보냄)
                morningDose = new StringBuffer();
                afternoonDose = new StringBuffer();
                eveningDose = new StringBuffer();

                // requiredAlarmArr[0]의 값이 0이면: 아침에 복용하는 약 없음
                // requiredAlarmArr[1]의 값이 0이면: 점심에 복용하는 약 없음
                // requiredAlarmArr[2]의 값이 0이면: 저녁에 복용하는 약 없음
                int[] requiredAlarmArr = new int[3];

                for(int i=0; i<presMedicine.length(); i++){
                    JSONObject mediInfo = presMedicine.getJSONObject(i);
                    String mediName = mediInfo.getString("mediName");
                    int mediDose1 = mediInfo.getInt("mediDose1");
                    int mediDose2 = mediInfo.getInt("mediDose2");
                    int mediDose3 = mediInfo.getInt("mediDose3");

                    requiredAlarmArr[0] += mediDose1; // 아침 복용량
                    requiredAlarmArr[1] += mediDose2; // 점심 복용량
                    requiredAlarmArr[2] += mediDose3; // 저녁 복용량

                    StringBuffer doseAmount = new StringBuffer();
                    doseAmount.append(mediDose1).append("/").append(mediDose2).append("/").append(mediDose3);
                    morningDose.append(mediName).append(" ").append(mediDose1).append("/");
                    afternoonDose.append(mediName).append(" ").append(mediDose2).append("/");
                    eveningDose.append(mediName).append(" ").append(mediDose3).append("/");

                    // 어댑터에 아이템 추가
                    adapter.addItem(mediName,doseAmount.toString());
                }

                presMediList.setAdapter(adapter);

                // 각 시간대별 복용해야 하는 약의 이름과 복용량을 표시하는 문자열
                // 끝에 /가 있으면 공백으로 대체
                if(morningDose.substring(morningDose.length()-1).equals("/")){
                    morningDose.setCharAt(morningDose.length()-1,' ');
                }
                if(afternoonDose.substring(afternoonDose.length()-1).equals("/")){
                    afternoonDose.setCharAt(afternoonDose.length()-1,' ');
                }
                if(eveningDose.substring(eveningDose.length()-1).equals("/")){
                    eveningDose.setCharAt(eveningDose.length()-1,' ');
                }

                trListBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(PrescriptionDetailActivity.this,TakingRecordActivity.class);
                        intent.putExtra("takingRecord",takingRecord.toString());
                        intent.putExtra("morningDose",morningDose.toString());
                        intent.putExtra("afternoonDose",afternoonDose.toString());
                        intent.putExtra("eveningDose",eveningDose.toString());
                        startActivity(intent);
                    }
                });

            } catch (Exception e){
                e.printStackTrace();
            }

        }
    }

}
