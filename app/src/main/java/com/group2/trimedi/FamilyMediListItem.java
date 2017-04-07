package com.group2.trimedi;

import android.view.View;
import android.widget.Button;

public class FamilyMediListItem {
    private String titleStr ;
    private String number ;
    private String btnText;
    private View.OnClickListener btnClickListener;


    public void setTitle(String title) {
        titleStr = title ;
    }
    public void setNumber(String num) {
        number = num;
    }
    public void setBtnClickListener(View.OnClickListener listener) {
        btnClickListener = listener;
    }


    public String getTitle() {
        return this.titleStr ;
    }
    public String getNumber() {
        return this.number;
    }
    public View.OnClickListener getBtnClickListener() {
        return this.btnClickListener;
    }


    public String getBtnText() {
        return btnText;
    }

    public void setBtnText(String btnText) {
        this.btnText = btnText;
    }
}
