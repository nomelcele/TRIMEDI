package com.group2.trimedi;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by kimdohyun on 2016-12-08.
 */

public class FamilyRequestSendActivity extends Activity {
    private EditText receiverEmailEdit;
    private String receiverName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.family_request_send_layout);

        ActionBar actionBar =getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        receiverEmailEdit = (EditText)findViewById(R.id.receiverEmailEdit);

        Button sendFrBtn = (Button)findViewById(R.id.sendFrBtn);
        sendFrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckValidReceiverEmailAsyncTask asyncTask = new CheckValidReceiverEmailAsyncTask();
                String encodedMail = receiverEmailEdit.getText().toString().replace("@","-").replace(".","");
                String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/member/mail/"+encodedMail;
                asyncTask.execute(requestURL,encodedMail);
//                FamilyRequestSendAsyncTask asyncTask = new FamilyRequestSendAsyncTask();
//                String requestURL = "http://" + getResources().getString(R.string.str_ip_address) + "/family/request";
//                SharedPreferences sp = getSharedPreferences("trimediSharedPreferences",MODE_PRIVATE);
//                String frFrom = sp.getString("loggedInMemMail","0");
//                String frToMail = receiverEmailEdit.getText().toString();
//                asyncTask.execute(requestURL,frFrom,frToMail);
            }
        });
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

    private class CheckValidReceiverEmailAsyncTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            try {
                Log.d("CheckValidReceiverEmail","웹 서버 연결 시도");
                URL url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
                conn.setRequestMethod("POST"); // 통신 방식 지정(POST)
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type","application/json");

                JSONObject params = new JSONObject();
                params.put("memMail",strings[1]); // 입력한 메일을 파라미터로 보냄

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(params.toString().getBytes());
                os.flush();
                os.close();

                Log.d("CheckValidReceiverEmail",String.valueOf(conn.getResponseCode()));
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    // 서버로부터 받은 멤버 데이터 읽음
                    Log.i("CheckValidReceiverEmail","통신 성공");
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
            try {
                if(!receivedData.equals("null")) {
                    // 해당하는 멤버 정보가 있으면(이메일 주소를 올바르게 입력했을 경우)
                    // 등록 요청 받을 사람의 회원 정보
                    JSONObject memInfo = new JSONObject(receivedData);
                    FamilyRequestSendAsyncTask asyncTask = new FamilyRequestSendAsyncTask();
                    SharedPreferences sp = getSharedPreferences("trimediSharedPreferences",MODE_PRIVATE);
//                    String frFrom = sp.getString("loggedInMemMail","0");
//                    String frTo = receiverEmailEdit.getText().toString();
                    int frFrom = sp.getInt("loggedInMemNum",0);
                    int frTo = memInfo.getInt("memNum");
                    receiverName = memInfo.getString("memName");
                    String requestURL = "http://" + getResources().getString(R.string.str_ip_address) + "/familyrequest/frFrom/"+frFrom+"/frTo/"+frTo;
                    asyncTask.execute(requestURL,String.valueOf(frFrom),String.valueOf(frTo));
                } else {
                    // 이메일 주소를 잘못 입력했을 경우
                    Toast.makeText(FamilyRequestSendActivity.this,"이메일 주소가 잘못 되었습니다. \n 다시 입력해 주세요.",Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private class FamilyRequestSendAsyncTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            try {
                Log.d("FamilyRequestSend","웹 서버 연결 시도");
                URL url = new URL(strings[0]);
                Log.i("FamilyRequestSend","URL: "+strings[0]);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
                conn.setRequestMethod("POST"); // 통신 방식 지정(POST)
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type","application/json");

                JSONObject params = new JSONObject();
                params.put("frFrom",strings[1]); // 가족 등록 요청을 보내는 회원의 번호
                params.put("frTo",strings[2]); // 가족 등록 요청을 받을 회원의 번호
                Log.i("FamilyRequestSend","params: "+params.toString());

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(params.toString().getBytes());
                os.flush();
                os.close();

                Log.d("FamilyRequestSend",String.valueOf(conn.getResponseCode()));
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    Log.i("FamilyRequestSend","통신 성공");
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
                JSONObject familyRequest = null;
                JSONObject frToInfo = null;
                if(receivedData != null) {
                    familyRequest = new JSONObject(receivedData);
                    frToInfo = familyRequest.getJSONObject("frTo");
                    Toast.makeText(FamilyRequestSendActivity.this,frToInfo.getString("memName")+" 님께 가족 등록 요청을 보냈습니다.",Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(FamilyRequestSendActivity.this,"이메일 주소가 잘못되었습니다.\n다시 입력해 주세요.",Toast.LENGTH_LONG).show();
                }

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
