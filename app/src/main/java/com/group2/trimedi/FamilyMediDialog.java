package com.group2.trimedi;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;

/**
 * Created by mo on 2016-12-09.
 */

public class FamilyMediDialog extends Dialog {
    private String mediAmount;
    private int dialogType;

    private NumberPicker numberPicker;
    private View.OnClickListener clickListener;

    public FamilyMediDialog(Context context,String mediAmount,int dialogType,View.OnClickListener clickListener) {
        super(context);
        this.mediAmount = mediAmount;
        this.dialogType = dialogType;
        this.clickListener = clickListener;
        Log.i("FamilyMediDialog","DialogType: "+dialogType);
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
        setContentView(R.layout.family_medi_dialog_layout);

        // 위젯 초기화
        Button familyMediOkBtn = (Button)findViewById(R.id.customDialogOkBtn);
        Button familyMediCancelBtn = (Button)findViewById(R.id.customDialogCancelBtn);

        // NumberPicker 설정
        if(dialogType == 0){
            Log.i("FamilyMediDialog","일반 상비약");
            numberPicker = (NumberPicker)findViewById(R.id.familyMediPicker);
            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(Integer.parseInt(mediAmount));
            numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
            setDividerColor(numberPicker,R.color.pillbox_theme_color);
            numberPicker.setWrapSelectorWheel(false);
            numberPicker.setValue(numberPicker.getMinValue());
            numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker numberPicker, int i, int i1) {

                }
            });

        } else {
            // 붕대 등의 상비약
            Log.i("FamilyMediDialog","소독약/붕대 등");
        }


        // 버튼 클릭 리스너 설정
        familyMediOkBtn.setOnClickListener(clickListener);
        familyMediCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

    }

    private void setDividerColor(NumberPicker picker, int color) {
        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public int getNumberPickerNum(){
        return numberPicker.getValue();
    }

}
