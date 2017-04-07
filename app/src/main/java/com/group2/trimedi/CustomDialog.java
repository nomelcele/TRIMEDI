package com.group2.trimedi;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by mo on 2016-12-07.
 */

public class CustomDialog extends Dialog {
    private TextView customDialogTitleView;
    private TextView customDialogContentView;
    private Button customDialogOkBtn;
    private Button customDialogCancelBtn;

    private String customDialogTitle;
    private String customDialogContent;

    private View.OnClickListener okBtnListener;
//    private View.OnClickListener cancelBtnListener;
    private String okBtnText;

    public CustomDialog(Context context, String customDialogTitle, String customDialogContent,
                        View.OnClickListener okBtnListener,String okBtnText) {
        super(context);
        this.customDialogTitle = customDialogTitle;
        this.customDialogContent = customDialogContent;
        this.customDialogContent = customDialogContent;
        this.okBtnListener = okBtnListener;
//        this.cancelBtnListener = cancelBtnListener;
        this.okBtnText = okBtnText;
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
        setContentView(R.layout.custom_dialog_layout);

        // 위젯 초기화
        customDialogTitleView = (TextView)findViewById(R.id.customDialogTitleView);
        customDialogContentView = (TextView)findViewById(R.id.customDialogContentView);
        customDialogOkBtn = (Button)findViewById(R.id.customDialogOkBtn);
        customDialogCancelBtn = (Button)findViewById(R.id.customDialogCancelBtn);

        // 제목과 내용 설정
        customDialogTitleView.setText(customDialogTitle);
        customDialogContentView.setText(customDialogContent);

        // 버튼 클릭 리스너 설정
        customDialogOkBtn.setOnClickListener(okBtnListener);
        customDialogOkBtn.setText(okBtnText);
        customDialogCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
