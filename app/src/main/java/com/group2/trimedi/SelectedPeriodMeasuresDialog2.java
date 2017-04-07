package com.group2.trimedi;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;

/**
 * Created by mo on 2016-12-14.
 */

public class SelectedPeriodMeasuresDialog2 extends Dialog {
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;

    public SelectedPeriodMeasuresDialog2(Context context) {
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
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
                Log.i("SelectedPeriodMeasures","시작일: "+startDatePicker.getMonth());
                Log.i("SelectedPeriodMeasures","시작일: "+startDatePicker.getDayOfMonth());
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
