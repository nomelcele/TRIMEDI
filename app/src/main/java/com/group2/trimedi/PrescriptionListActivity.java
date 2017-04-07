package com.group2.trimedi;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by kimdohyun on 2016-12-01.
 */

public class PrescriptionListActivity extends Activity {
    ListView presListView;
    PrescriptionListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prescription_list);

        // 리스트뷰 참조
        presListView = (ListView) findViewById(R.id.presList);


        ActionBar actionBar =getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Adapter 생성
        adapter = new PrescriptionListAdapter();
        // 처방전 목록 서버에서 가져오기
        GetMyPresAsyncTask asyncTask = new GetMyPresAsyncTask();
        SharedPreferences sp = getSharedPreferences("trimediSharedPreferences",MODE_PRIVATE);
        int memNum = sp.getInt("loggedInMemNum",0);
        String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/prescription/member/"+memNum;
        asyncTask.execute(requestURL,String.valueOf(memNum));
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
            Intent intent = new Intent(PrescriptionListActivity.this, SettingsActivity.class);
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

    private class GetMyPresAsyncTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            try {
                Log.i("GetMyPres","웹 서버 연결 시도");
                URL url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
                conn.setRequestMethod("POST"); // 통신 방식 지정(POST)
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type","application/json");

                JSONObject params = new JSONObject();
                params.put("memNum",strings[1]); // 현재 로그인한 회원 번호를 파라미터로 전달
                Log.i("GetMyPres","memNum: "+strings[1]);

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(params.toString().getBytes());
                os.flush();
                os.close();

                Log.i("GetMyPres",String.valueOf(conn.getResponseCode()));
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    // 서버로부터 받은 측정값 데이터들 읽기
                    Log.i("GetMyPres","통신 성공");
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
            try{
                // 읽은 데이터 JSON 파싱
                Log.i("GetMyPres","Received Data: "+receivedData);
                JSONArray myPres = new JSONArray(receivedData);

                SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyyMMdd");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.");

                for(int i=0; i<myPres.length(); i++){
                    JSONObject presInfo = myPres.getJSONObject(i);

                    // 처방전 시작 날짜, 종료 날짜 세팅
                    String presDate = presInfo.getString("presDate");
                    Date date = dbDateFormat.parse(presDate);

                    int presDay = Integer.parseInt(presInfo.getString("presDay"));
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    cal.add(Calendar.DATE,presDay-1);

                    StringBuffer presDayText = new StringBuffer();
                    presDayText.append(dateFormat.format(date)).append(" ~ ");
                    presDayText.append(dateFormat.format(cal.getTime()));

                    // 어댑터에 아이템 추가
                    adapter.addItem(ContextCompat.getDrawable(PrescriptionListActivity.this, R.drawable.pres_label),
                            presDayText.toString(), presInfo.getString("presPharmacy"), Integer.parseInt(presInfo.getString("presNum")));
                }

                presListView.setAdapter(adapter);
                presListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView parent, View v, int position, long id) {
                        Intent intent = new Intent(getApplicationContext(),PrescriptionDetailActivity.class);
                        PrescriptionListItem item = (PrescriptionListItem)parent.getItemAtPosition(position);
                        intent.putExtra("presNum",item.getItemId());
                        Log.i("GetMyPres","확인할 처방전 번호: "+intent.getIntExtra("presMum",0));
                        startActivity(intent); }
                }) ;


            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
