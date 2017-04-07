package com.test.android36http;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;


public class InsertPage extends Activity {

    private EditText edit_id;
    private EditText edit_pw;
    private EditText edit_name;
    private EditText edit_tel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.insert);

        edit_id = (EditText) findViewById(R.id.edit_id);
        edit_pw = (EditText) findViewById(R.id.edit_pw);
        edit_name = (EditText) findViewById(R.id.edit_name);
        edit_tel = (EditText) findViewById(R.id.edit_tel);

        Button insertOKBtn = (Button) findViewById(R.id.insertOKBtn);
        insertOKBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("Insert Log", edit_id.getText().toString());
                Log.i("Insert Log", edit_pw.getText().toString());
                Log.i("Insert Log", edit_name.getText().toString());
                Log.i("Insert Log", edit_tel.getText().toString());

                new Thread() {
                    @Override
                    public void run() {
                        doProcess();
                    }
                }.start();



            }
        });

    }

    private Handler mHandler = new Handler();
    private String insert_result;

    private void doProcess() {
        String id = edit_id.getText().toString();
        String pw = edit_pw.getText().toString();
        String name = edit_name.getText().toString();
        String tel = edit_tel.getText().toString();

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://192.168.0.149:8080/jsp01hello/insertOK.jsp");
        ArrayList<NameValuePair> nameValues =
                new ArrayList<NameValuePair>();
        try {
            nameValues.add(new BasicNameValuePair("id", URLEncoder.encode(id, "UTF-8")));
            nameValues.add(new BasicNameValuePair("pw", URLEncoder.encode(pw, "UTF-8")));
            nameValues.add(new BasicNameValuePair("name", URLEncoder.encode(name, "UTF-8")));
            nameValues.add(new BasicNameValuePair("tel", URLEncoder.encode(tel, "UTF-8")));

            post.setEntity(
                    new UrlEncodedFormEntity(
                            nameValues, "UTF-8"));

        } catch (UnsupportedEncodingException ex) {
            Log.e("Insert Log", ex.toString());
        }



        try {
            HttpResponse response = client.execute(post);


            Log.i("Insert Log", response.toString());
            Log.i("Insert Log", "response.getStatusCode:" +response.getStatusLine().getStatusCode());

            for(Header x:response.getAllHeaders()){
                Log.i("Insert Log", x.toString());
            }
            InputStream is = null;
            if(response.getStatusLine().getStatusCode()>=300 &&
                        response.getStatusLine().getStatusCode()<400){
                Log.i("Insert Log", "response.getFirstHeader(\"Location\"):" +response.getFirstHeader("Location").toString());
                String location = response.getFirstHeader("Location").toString();
                String sPath = location.substring("Location: ".length());
                Log.i("Insert Log", "sPath:"+sPath);
                is = new URL("http://192.168.0.149:8080/jsp01hello/"+sPath).openStream();
            }else{
                is = response.getEntity().getContent();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                //Log.i("Insert Log", "while>>>>>"+line);
                sb.append(line);
            }

            Log.i("Insert Log", sb.toString());

            JSONObject jsonObject = new JSONObject(sb.toString());
            Log.i("Insert Log", jsonObject.getString("result"));

            insert_result = jsonObject.getString("result");

            if(insert_result.equals("insert successed")){
                Intent intent = new Intent(
                        getApplicationContext(),
                        MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(
                            InsertPage.this.getApplicationContext(),
                            insert_result,
                            Toast.LENGTH_SHORT).show();
                }
            });


        } catch (ClientProtocolException ex) {
            Log.e("Insert Log", ex.toString());
        } catch (IOException ex) {
            Log.e("Insert Log", ex.toString());
        }catch (JSONException ex) {
            Log.e("Insert Log", ex.toString());
        }
    }


}
