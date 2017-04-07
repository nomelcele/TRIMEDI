package com.group2.trimedi;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.NumberPicker;

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
import java.util.ArrayList;

/**
 * Created by kimdohyun on 2016-12-02.
 */

public class FamilyMediActivity extends Activity {
    private ListView familyListView;
    private FamilyMediListAdapter adapter;

    private FamilyMediDialog amountDialog;
    private CustomDialog useDialog;

    private String btnText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.family_medi_layout);
        ActionBar actionBar =getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // 리스트뷰 참조
        familyListView = (ListView) findViewById(R.id.familyListView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Adapter 생성
        adapter = new FamilyMediListAdapter();
        // 상비약 리스트 서버에서 가져오기
        GetFamilyMediListAsyncTask asyncTask = new GetFamilyMediListAsyncTask();
        SharedPreferences sp = getSharedPreferences("trimediSharedPreferences",MODE_PRIVATE);
        int familyNum = sp.getInt("loggedInFamily",0); // 현재 로그인한 회원의 가족 번호
        String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/family/member/"+familyNum;
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
        if (id == R.id.action_settings) {
            Intent intent = new Intent(FamilyMediActivity.this, SettingsActivity.class);
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

    private class GetFamilyMediListAsyncTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            try {
                Log.i("FamilyMediList","웹 서버 연결 시도");
                URL url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
                conn.setRequestMethod("POST"); // 통신 방식 지정(POST)
                conn.setDoInput(true); // 쓰기 모드 설정
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type","application/json");

                JSONObject params = new JSONObject();
                params.put("famNum",strings[1]); // 현재 로그인한 회원의 가족 번호 파라미터로 넘겨줌

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(params.toString().getBytes());
                os.flush();
                os.close();

                Log.i("FamilyMediList",String.valueOf(conn.getResponseCode()));
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    // 서버로부터 받은 측정값 데이터들 읽기
                    Log.i("FamilyMediList","통신 성공");
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
                Log.i("FamilyMediList","Received Data: "+receivedData);
                JSONArray familyMedicine = new JSONArray(receivedData); // 상비 약품 목록

                for(int i=0; i<familyMedicine.length(); i++){
                    JSONObject mediInfo = familyMedicine.getJSONObject(i);

                    final String mediName = mediInfo.getString("mediName");
                    final String mediNum = mediInfo.getString("mediNum");
                    String mediAmount = mediInfo.getString("mediAmount");
                    btnText = "복용";
                    final int mediType = mediInfo.getInt("mediType");
                    if(mediType == 1){
                        // 붕대 등의 상비약은 남은 양을 있음/없음으로 표시
                        if(mediAmount.equals("1")){
                            mediAmount = "있음";
                        } else {
                            mediAmount = "없음";
                        }
                        btnText = "사용";
                        adapter.addItem(mediName, mediAmount, btnText,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        useDialog = new CustomDialog(FamilyMediActivity.this, "상비약 사용", mediName + "을 사용하시겠습니까?",
                                                new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        TakeFamilyMedicineAsyncTask asyncTask = new TakeFamilyMedicineAsyncTask();
                                                        String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+":/family/fam_medi/"+mediNum;
                                                        asyncTask.execute(requestURL,mediNum,String.valueOf(1),"2");
                                                    }
                                                },btnText);
                                        useDialog.show();
                                    }
                                });
                    } else {
                        // 일반 상비약
                        final String currentAmount = mediAmount;
                        adapter.addItem(mediName,mediAmount,btnText,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // 복용 버튼 클릭 시
                                        // NumberPicker Dialog 띄움
                                        amountDialog = new FamilyMediDialog(FamilyMediActivity.this, currentAmount, mediType,
                                                new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        TakeFamilyMedicineAsyncTask asyncTask = new TakeFamilyMedicineAsyncTask();
                                                        String selectedValue = String.valueOf(amountDialog.getNumberPickerNum());
                                                        String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/family/fam_medi/"+mediNum+"/medi_amount/"+selectedValue;
                                                        asyncTask.execute(requestURL,mediNum,selectedValue,"1");
                                                    }
                                                });
                                        amountDialog.show();
                                    }
                                });
                    }

                }

                familyListView.setAdapter(adapter);

            } catch(Exception e){
                e.printStackTrace();
            }

        }


    }


    private class TakeFamilyMedicineAsyncTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            try {
                Log.i("TakeFamilyMedicine","웹 서버 연결 시도");
                URL url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
                conn.setRequestMethod("PUT"); // 통신 방식 지정(POST)
                conn.setDoInput(true); // 쓰기 모드 설정
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type","application/json");

                JSONObject params = new JSONObject();
                params.put("mediNum",strings[1]); // 약의 번호
                if(strings[3].equals("1")){
                    // 일반 상비약의 경우
                    params.put("mediAmount",strings[2]); // 사용할 양
                }

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(params.toString().getBytes());
                os.flush();
                os.close();

                Log.i("TakeFamilyMedicine",String.valueOf(conn.getResponseCode()));
            } catch(Exception e){
                e.printStackTrace();
            }

            return strings[3];
        }

        @Override
        protected void onPostExecute(String mediType) {
            // 상비약 복용 후 다이얼로그 닫기
            if(mediType.equals("1")) {
                amountDialog.dismiss();
            } else if(mediType.equals("2")) {
                useDialog.dismiss();
            }
            // 액티비티 새로고침
            recreate();
        }
    }
}
