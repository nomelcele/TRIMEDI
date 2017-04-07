package com.group2.trimedi;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by mo on 2016-11-29.
 */

public class MeasureListActivity extends Activity implements View.OnClickListener{
    private LineChart measureChart;
    private ListView measureList;
    private MeasureItemListAdapter adapter;

    private RadioGroup msrRadioGroup;
    private Button dailyBtn;
    private Button weeklyBtn;
    private Button monthlyBtn;
    private Button userPeriodBtn;

    private XAxis xAxis;
    private YAxis leftAxis;
    private YAxis rightAxis;

    private long dailyMinXValue;
    private long weeklyMinXValue;
    private long monthlyMinXValue;
    private long userPeriodMinXValue;
    private long userPeriodMaxXValue;

    private Drawable selectedBtnImg;
    private Drawable notSelectedBtnImg;

    private int memNum;
    private SelectedPeriodMeasuresDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measure_list_layout);

        SharedPreferences sp = getSharedPreferences("trimediSharedPreferences",MODE_PRIVATE);
        memNum = sp.getInt("loggedInMemNum",0);

        // 현재 날짜 텍스트뷰에 세팅
        TextView todayDateView = (TextView)findViewById(R.id.todayDateView);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일");
        todayDateView.setText(sdf.format(Calendar.getInstance().getTime()));

        // 버튼 이벤트 세팅
        dailyBtn = (Button)findViewById(R.id.dailyBtn);
        weeklyBtn = (Button)findViewById(R.id.weeklyBtn);
        monthlyBtn = (Button)findViewById(R.id.monthlyBtn);
        userPeriodBtn = (Button)findViewById(R.id.userPeriodBtn);
        dailyBtn.setOnClickListener(this);
        weeklyBtn.setOnClickListener(this);
        monthlyBtn.setOnClickListener(this);
        userPeriodBtn.setOnClickListener(this);

        // radiogroup 이벤트 리스너 세팅
        msrRadioGroup = (RadioGroup)findViewById(R.id.msrRadioGroup);
        msrRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId){
                    case R.id.temperRadioBtn:
                        // 체온 측정값만 표시
                        Log.i("measureChart","체온 표시");
                        measureChart.getData().getDataSetByIndex(0).setVisible(true);
                        measureChart.getData().getDataSetByIndex(1).setVisible(false);
                        break;
                    case R.id.heartRadioBtn:
                        // 심박 측정값만 표시
                        Log.i("heartChart","심박 표시");
                        measureChart.getData().getDataSetByIndex(0).setVisible(false);
                        measureChart.getData().getDataSetByIndex(1).setVisible(true);
                        break;
                }
                measureChart.invalidate();
            }
        });



        // 그래프 세팅
        measureChart = (LineChart)findViewById(R.id.measureChart);
        measureChart.getDescription().setEnabled(false);
        measureChart.setTouchEnabled(false);
        // x축 세팅
        xAxis = measureChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE); // x축 위치 지정
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        // x축 값 표현 방식 지정
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            private SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                long millis = TimeUnit.HOURS.toMillis((long)value);
                return sdf.format(new Date(millis));
            }
        });

        // y축 세팅
        // 좌측 y축
        leftAxis = measureChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART); // 위치 지정
        leftAxis.setAxisMinimum(0f); // y축 최솟값 지정
        leftAxis.setAxisMaximum(200f); // y축 최댓값 지정
        // 우측 y축
        rightAxis = measureChart.getAxisRight();
        rightAxis.setEnabled(false); // 숨김

        measureList = (ListView)findViewById(R.id.measureList);
        adapter = new MeasureItemListAdapter(this);

        selectedBtnImg = getResources().getDrawable(R.drawable.measure_btn_selected);
        notSelectedBtnImg = getResources().getDrawable(R.drawable.measure_btn_not_selected);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // 차트 부분 데이터 불러옴
        MeasureChartAsyncTask chartAsyncTask = new MeasureChartAsyncTask();
        chartAsyncTask.execute("daily",null,null); // 기본은 일간 기록만 보여줌

        // 리스트(모든 측정값 표시)에 출력할 데이터 불러옴
        MeasureListAsyncTask listAsyncTask = new MeasureListAsyncTask();
        String requestURL = "http://"+getResources().getString(R.string.str_ip_address)+"/measure/user/"+memNum;
        listAsyncTask.execute(requestURL);
    }

    @Override
    public void onClick(View view) {
        String timeCondition = null;
        switch (view.getId()){
            case R.id.dailyBtn:
                timeCondition = "daily";
                weeklyBtn.setBackground(notSelectedBtnImg);
                monthlyBtn.setBackground(notSelectedBtnImg);
                userPeriodBtn.setBackground(notSelectedBtnImg);
                break;
            case R.id.weeklyBtn:
                timeCondition = "weekly";
                dailyBtn.setBackground(notSelectedBtnImg);
                monthlyBtn.setBackground(notSelectedBtnImg);
                userPeriodBtn.setBackground(notSelectedBtnImg);
                break;
            case R.id.monthlyBtn:
                timeCondition = "monthly";
                weeklyBtn.setBackground(notSelectedBtnImg);
                dailyBtn.setBackground(notSelectedBtnImg);
                userPeriodBtn.setBackground(notSelectedBtnImg);
                break;
            case R.id.userPeriodBtn:
                timeCondition = "userPeriod";
                weeklyBtn.setBackground(notSelectedBtnImg);
                monthlyBtn.setBackground(notSelectedBtnImg);
                dailyBtn.setBackground(notSelectedBtnImg);

                // 시작일 ~ 종료일 선택 Dialog 띄움
                dialog = new SelectedPeriodMeasuresDialog(MeasureListActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 다이얼로그 타이틀 숨기기
                dialog.show();

                break;
        }

        view.setBackground(selectedBtnImg);

        MeasureChartAsyncTask asyncTask = new MeasureChartAsyncTask();
        asyncTask.execute(timeCondition,null,null);
    }

    private class MeasureChartAsyncTask extends AsyncTask<String,Void,String[]>{
        @Override
        protected String[] doInBackground(String... strings) {
            try {
//                Socket socket = new Socket("www.google.com", 80);
//                String localAddr = socket.getLocalAddress().getHostAddress();
//                Log.i("DirectMeasure", "local address is : " + localAddr);

                String timeCondition = strings[0]; // 조회하려는 시간 유형(일간/주간/월간/사용자가 설정한 기간)

                StringBuffer requestURL = new StringBuffer();
                requestURL.append("http://").append(getResources().getString(R.string.str_ip_address)).append("/measure/user/").append(memNum);

                // 검색 조건으로 사용할 시작일/종료일
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                Calendar todayCal = Calendar.getInstance(); // 현재 시간 정보가 담긴 Calendar 객체
                long fromDate = 0; // 검색 시작일
                long toDate = todayCal.getTimeInMillis(); // 검색 종료일: 현재 시간
                switch (timeCondition){
                    case "daily":
                        // 일간 측정값을 표시해야 하는 경우
                        // 시작일: 현재 시간 - 1일
                        todayCal.add(Calendar.DATE,-1);
                        fromDate = todayCal.getTimeInMillis();
                        // x축(측정 시간 표시)의 최솟값을 검색 시작일로 설정
                        dailyMinXValue = fromDate;
                        break;
                    case "weekly":
                        // 주간 측정값을 표시해야 하는 경우
                        // 시작일: 현재 시간 - 6일
                        todayCal.add(Calendar.DATE,-6);
                        fromDate = todayCal.getTimeInMillis();
                        weeklyMinXValue = fromDate;
                        break;
                    case "monthly":
                        // 월간 측정값을 표시해야 하는 경우
                        // 시작일: 현재 시간 - 30일
                        todayCal.add(Calendar.DATE,-30);
                        fromDate = todayCal.getTimeInMillis();
                        monthlyMinXValue = fromDate;
                        break;
                    case "userPeriod":
                        // 사용자가 선택한 특정 기간 동안의 측정값을 표시해야 하는 경우
                        fromDate = Long.valueOf(strings[1]);
                        toDate = Long.valueOf(strings[2]);
                        // x축(측정 시간 표시)의 최솟값을 검색 시작일로 설정
                        userPeriodMinXValue = fromDate;
                        // x축(측정 시간 표시)의 최댓값을 검색 종료일로 설정
                        userPeriodMaxXValue = toDate;
                        break;
                }

                requestURL.append("/from/").append(fromDate).append("/to/").append(toDate);
                Log.i("MeasureList","requestURL: "+requestURL);

                Log.i("MeasureList","웹 서버 연결 시도");
                URL url = new URL(requestURL.toString());
                requestURL.setLength(0);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
                conn.setRequestMethod("POST"); // 통신 방식 지정(POST)
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type","application/json; charset=UTF-8");

                // 현재 로그인한 회원의 회원 번호, 검색 시작일, 검색 종료일을 파라미터로 보냄
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("memNum",memNum);
                jsonObject.put("from",fromDate);
                jsonObject.put("to",toDate);

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(jsonObject.toString().getBytes());
                os.flush();
                os.close();

                Log.i("MeasureList",String.valueOf(conn.getResponseCode()));
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    // 서버로부터 받은 측정값 데이터들 읽기
                    Log.i("MeasureList","통신 성공");
                    InputStream is = conn.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));

                    StringBuffer sb = new StringBuffer();
                    String line = "";
                    while((line = br.readLine()) != null){
                        sb.append(line);
                    }

                    br.close();
                    is.close();

                    String[] paramArr = new String[2];
                    paramArr[0] = sb.toString();
                    paramArr[1] = strings[0];

                    return paramArr;

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] paramArr) {
            try{
                // 서버로부터 읽은 측정값 데이터 리스트 JSON 파싱
                String receivedData = paramArr[0];
                String timeCondition = paramArr[1];
                Log.i("MeasureList","Received Data: "+receivedData);
                JSONArray jsonArray = new JSONArray(receivedData);

                // Entry: x축에 표시될 값, y축에 표시될 값으로 구성
                // 각 값들은 float 타입이어야 함
                // Entry들을 저장하는 ArrayList
                ArrayList<Entry> temperChartData = new ArrayList<Entry>(); // 체온 저장
                ArrayList<Entry> heartChartData = new ArrayList<Entry>(); // 심박수 저장

                for(int i=0; i<jsonArray.length(); i++){
                    // 측정값 데이터 리스트에서 각각의 측정값을 추출
                    JSONObject obj = jsonArray.getJSONObject(i);
                    long msrDateMilli = obj.getLong("msrDate"); // 측정 날짜

                    // ArrayList에 측정 날짜와 측정값을 저장하는 Entry를 넣음
                    float dateX = msrDateMilli;
                    float valueY = Float.parseFloat(obj.getString("msrValue"));
                    if(obj.getString("msrType").equals("0")){
                        // 측정값이 체온인 경우 temperChardData에 저장
                        temperChartData.add(new Entry(dateX,valueY));
                    } else {
                        // 측정값이 체온인 경우 heartChardData에 저장
                        heartChartData.add(new Entry(dateX,valueY));
                    }

                }

                // Entry를 담고 있는 ArrayList와 List의 이름(범례)을 저장
                LineDataSet temperDataSet = new LineDataSet(temperChartData,"체온");
                // 그래프의 색상 지정
                temperDataSet.setColor(getResources().getColor(R.color.pillbox_theme_color));
                temperDataSet.setCircleColor(getResources().getColor(R.color.pillbox_theme_color));
                LineDataSet heartDataSet = new LineDataSet(heartChartData,"심박수");
                // 그래프의 색상 지정
                heartDataSet.setColor(getResources().getColor(R.color.settings_theme_color));
                heartDataSet.setCircleColor(getResources().getColor(R.color.settings_theme_color));

                // 차트에 나타날 데이터를 저장할 객체
                ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                // 체온 값이 저장된 LineDataSet과 심박수 값이 저장된 heartDataSet을 추가
                dataSets.add(temperDataSet);
                dataSets.add(heartDataSet);
                LineData lineData = new LineData(dataSets);
                // 차트에 데이터 세팅
                measureChart.setData(lineData);

                // 차트 x축 설정
                switch(timeCondition){
                    case "daily":
                        // 일간 측정값 표시
                        xAxis.setAxisMinimum(dailyMinXValue); // x축 최소값 설정
                        xAxis.setAxisMaximum(Calendar.getInstance().getTimeInMillis()); // x축 최대값 설정
                        // x축 값을 표시할 방식 지정
                        xAxis.setValueFormatter(new IAxisValueFormatter() {
                            private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                            @Override
                            public String getFormattedValue(float value, AxisBase axis) {
                                return sdf.format(new Date((long)value));
                            }
                        });
                        break;
                    case "weekly":
                        // 주간 측정값 표시
                        xAxis.setAxisMinimum(weeklyMinXValue); // x축 최소값 설정
                        xAxis.setAxisMaximum(Calendar.getInstance().getTimeInMillis()); // x축 최대값 설정
                        xAxis.setValueFormatter(new IAxisValueFormatter() {
                            private SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
                            @Override
                            public String getFormattedValue(float value, AxisBase axis) {
                                return sdf.format(new Date((long)value));
                            }
                        });
                        break;
                    case "monthly":
                        // 월간 측정값 표시
                        xAxis.setAxisMinimum(monthlyMinXValue); // x축 최소값 설정
                        xAxis.setAxisMaximum(Calendar.getInstance().getTimeInMillis()); // x축 최대값 설정
                        xAxis.setValueFormatter(new IAxisValueFormatter() {
                            private SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
                            @Override
                            public String getFormattedValue(float value, AxisBase axis) {
                                return sdf.format(new Date((long)value));
                            }
                        });
                        break;
                    case "userPeriod":
                        // 월간 측정값 표시
                        xAxis.setAxisMinimum(userPeriodMinXValue); // x축 최소값 설정
                        xAxis.setAxisMaximum( userPeriodMaxXValue); // x축 최대값 설정
                        xAxis.setValueFormatter(new IAxisValueFormatter() {
                            private SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
                            @Override
                            public String getFormattedValue(float value, AxisBase axis) {
                                return sdf.format(new Date((long)value));
                            }
                        });
                        break;
                }

                measureChart.invalidate(); // 차트 refresh

            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private class MeasureListAsyncTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            try {
                Socket socket = new Socket("www.google.com", 80);
                String localAddr = socket.getLocalAddress().getHostAddress();
                Log.i("MeasureList", "local address is : " + localAddr);

                Log.i("MeasureList","웹 서버 연결 시도");
                URL url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection(); // URL을 연결한 객체 생성
                conn.setRequestMethod("POST"); // 통신 방식 지정(POST)
                conn.setDoInput(true); // 쓰기 모드 설정
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type","application/json");

                // 현재 로그인한 사용자의 회원 번호를 파라미터로 보냄
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("memNum",memNum);

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(jsonObject.toString().getBytes());
                os.flush();
                os.close();

                Log.i("MeasureList",String.valueOf(conn.getResponseCode()));
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    // 서버로부터 받은 측정값 데이터들 읽기
                    Log.i("MeasureList","통신 성공");
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
            // 받은 데이터 리스트에 출력
            try{
                // 읽은 데이터 JSON 파싱
                Log.i("MeasureList","Received Data: "+receivedData);
                JSONArray jsonArray = new JSONArray(receivedData);

                String[] measureItemArr = new String[3];

                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject measure = jsonArray.getJSONObject(i);
                    long msrDateMilli = measure.getLong("msrDate");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    String type;
                    String msrValue = measure.getString("msrValue");
                    if(measure.getString("msrType").equals("0")){
                        type = "체온";
                    } else {
                        type = "심박";
                        msrValue = String.valueOf((int)(Double.parseDouble(msrValue)));
                    }

                    // 리스트에 추가
                    measureItemArr[0] = sdf.format(new Date(msrDateMilli));
                    measureItemArr[1] = type;
                    measureItemArr[2] = msrValue;
                    adapter.addItem(new MeasureItem(measureItemArr));
                    measureItemArr = new String[3];
                }

                measureList.setAdapter(adapter);

            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public class SelectedPeriodMeasuresDialog extends Dialog {
        private DatePicker startDatePicker;
        private DatePicker endDatePicker;

        public SelectedPeriodMeasuresDialog(Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // 다이얼로그 외부 화면 어둡게 표현
            WindowManager.LayoutParams lpWIndow = new WindowManager.LayoutParams();
            lpWIndow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            lpWIndow.dimAmount = 0.8f;
            getWindow().setAttributes(lpWIndow);

            // 다이얼로그의 레이아웃 설정
            setContentView(R.layout.selected_period_measures_dialog);

            // DatePicker
            startDatePicker = (DatePicker)findViewById(R.id.startDatePicker);
            endDatePicker = (DatePicker)findViewById(R.id.endDatePicker);

            // 버튼 세팅
            Button measuresDialogOkBtn = (Button)findViewById(R.id.measuresDialogOkBtn);
            measuresDialogOkBtn.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 조회 버튼 클릭했을 때
                    // 선택한 시작일, 종료일을 서버로 보내서 그 기간 안에 있는 측정값만 가져옴
                    Log.i("SelectedPeriodMeasures","시작일: "+startDatePicker.getYear());
                    Log.i("SelectedPeriodMeasures","시작일: "+(startDatePicker.getMonth()+1));
                    Log.i("SelectedPeriodMeasures","시작일: "+startDatePicker.getDayOfMonth());
                    // 시작일 millisecond
                    Calendar startDateCal = Calendar.getInstance();
                    startDateCal.set(Calendar.YEAR,startDatePicker.getYear());
                    startDateCal.set(Calendar.MONTH,startDatePicker.getMonth());
                    startDateCal.set(Calendar.DAY_OF_MONTH,startDatePicker.getDayOfMonth());
                    Calendar endDateCal = Calendar.getInstance();
                    endDateCal.set(Calendar.YEAR,endDatePicker.getYear());
                    endDateCal.set(Calendar.MONTH,endDatePicker.getMonth());
                    endDateCal.set(Calendar.DAY_OF_MONTH,endDatePicker.getDayOfMonth());

                    long startDateMilli = startDateCal.getTimeInMillis();
                    long endDateMilli = endDateCal.getTimeInMillis();

                    MeasureChartAsyncTask asyncTask = new MeasureChartAsyncTask();
                    asyncTask.execute("userPeriod",String.valueOf(startDateMilli),String.valueOf(endDateMilli));
//                    recreate();
                    dismiss();

                }
            });
            Button measuresDialogCancelBtn = (Button)findViewById(R.id.measuresDialogCancelBtn);
            measuresDialogCancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 취소 버튼 클릭
                    dismiss();
                }
            });
        }
    }
}
