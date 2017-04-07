package com.group2.trimedi;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

public class TakingRecordActivity extends Activity {
    private ListView trListView;
    private TakingRecordListAdapter adapter;

    private TextView morningDoseTextView;
    private TextView afternoonDoseTextView;
    private TextView eveningDoseTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taking_record_layout);

        ActionBar actionBar =getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // 측정 기록 보여줄 리스트뷰
        trListView = (ListView)findViewById(R.id.trListView);

        // 아침/점심/저녁 복용량 보여줄 텍스트뷰
        morningDoseTextView = (TextView)findViewById(R.id.morningDoseTextView);
        afternoonDoseTextView = (TextView)findViewById(R.id.afternoonDoseTextView);
        eveningDoseTextView = (TextView)findViewById(R.id.eveningDoseTextView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Adapter 생성
        adapter = new TakingRecordListAdapter();
        // 복용 기록 데이터 받아오기
//        GetTrListAsyncTask asyncTask = new GetTrListAsyncTask();
//        String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+":80/trimedi/getprestr";
//        asyncTask.execute(requestURL);

        try {
            // 아침/점심/저녁에 복용해야 하는 약 정보 출력
            morningDoseTextView.setText(getIntent().getStringExtra("morningDose"));
            afternoonDoseTextView.setText(getIntent().getStringExtra("afternoonDose"));
            eveningDoseTextView.setText(getIntent().getStringExtra("eveningDose"));

            // 복용 기록 출력
            String takingRecordStr = getIntent().getStringExtra("takingRecord");
            Log.i("TakingRecord","복용 기록: "+takingRecordStr);
            JSONArray takingRecord = new JSONArray(takingRecordStr); // 복용 기록

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat itemFormat = new SimpleDateFormat("yyyy.MM.dd.");

            for(int i=0; i<takingRecord.length(); i++){
                JSONObject dailyTakingRecord = takingRecord.getJSONObject(i);
                Date originalTrDate = dateFormat.parse(dailyTakingRecord.getString("trDate"));
                String trDate = itemFormat.format(originalTrDate);

                // 어댑터에 아이템 추가
                adapter.addItem(trDate,
                        getIconType(dailyTakingRecord.getInt("trCheck1")),
                        getIconType(dailyTakingRecord.getInt("trCheck2")),
                        getIconType(dailyTakingRecord.getInt("trCheck3")));
            }

            trListView.setAdapter(adapter);

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public Drawable getIconType(int isTaken){
        Drawable icon = null;
        switch (isTaken){
            case 0:
                // 원래 안 먹는 시간: 공백으로 표시
                break;
            case 1:
                // 먹어야 되는데 안 먹음: 엑스 표시
                icon = getResources().getDrawable(R.drawable.pillbox_not_taken);
                break;
            case 2:
                // 먹음: 체크 표시
                icon = getResources().getDrawable(R.drawable.pillbox_taken);
                break;
        }
        return icon;
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
            Intent intent = new Intent(TakingRecordActivity.this, SettingsActivity.class);
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

//    private class GetTrListAsyncTask extends AsyncTask<String,Void,String>{
//        @Override
//        protected String doInBackground(String... strings) {
//            try {
//                Log.i("GetTrList","웹 서버 연결 시도");
//                URL url = new URL(strings[0]);
//                HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
//                conn.setRequestMethod("POST"); // 통신 방식 지정(POST)
//                conn.setDoInput(true); // 쓰기 모드 설정
//                conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
//
//                StringBuffer params = new StringBuffer();
//                params.append("pres_num").append("=").append(getIntent().getIntExtra("pres_num",0));
//
//                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
//                os.write(params.toString().getBytes());
//                os.flush();
//                os.close();
//
//                Log.i("GetTrList",String.valueOf(conn.getResponseCode()));
//                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
//                    // 서버로부터 받은 복용 기록 데이터들 읽기
//                    Log.i("GetTrList","통신 성공");
//                    InputStream is = conn.getInputStream();
//                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
//
//                    StringBuffer sb = new StringBuffer();
//                    String line = "";
//                    while((line = br.readLine()) != null){
//                        sb.append(line);
//                    }
//
//                    br.close();
//                    is.close();
//
//                    return sb.toString();
//
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String receivedData) {
//            Log.i("GetTrLIst","받은 데이터: "+receivedData);
//            try{
//                // 읽은 데이터 JSON 파싱
//                Log.i("GetMyPres","Received Data: "+receivedData);
//                JSONObject jsonObject = new JSONObject(receivedData);
//                JSONArray jsonArray = new JSONArray(jsonObject.getString("presTrList"));
//
//                SimpleDateFormat dateTimeFormat  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.");
//
//                String presDate = jsonObject.getString("presDate");
//                Calendar trDateCal = Calendar.getInstance();
//                trDateCal.setTime(dateTimeFormat.parse(presDate));
//
//                for(int i=0; i<jsonArray.length(); i++){
//                    JSONArray trArr = jsonArray.getJSONArray(i);
//
//                    // 복용 기록 날짜 세팅
//                    if(i > 0){
//                        trDateCal.add(Calendar.DATE,1);
//                    }
//
//                    String trDate = dateFormat.format(trDateCal.getTime());
//                    Drawable[] icons = new Drawable[3];
//                    for(int j=0; j<icons.length; j++){
//                        if(trArr.getInt(j) == 0){
//                            // 약 안 먹음
//                            icons[j] = getResources().getDrawable(R.drawable.pillbox_not_taken);
//                        } else {
//                            icons[j] = getResources().getDrawable(R.drawable.pillbox_taken);
//                        }
//                    }
//                    // 어댑터에 아이템 추가
//                    adapter.addItem(trDate,icons[0],icons[1],icons[2]);
//                }
//
//                trListView.setAdapter(adapter);
//
//                morningDoseTextView.setText(getIntent().getStringExtra("morningDose"));
//                afternoonDoseTextView.setText(getIntent().getStringExtra("afternoonDose"));
//                eveningDoseTextView.setText(getIntent().getStringExtra("eveningDose"));
//
//            } catch(Exception e){
//                e.printStackTrace();
//            }
//        }
//    }
}
