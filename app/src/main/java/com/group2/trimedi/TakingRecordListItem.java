package com.group2.trimedi;

import android.graphics.drawable.Drawable;

public class TakingRecordListItem {
    private String titleStr ;
    private Drawable icon1 ;
    private Drawable icon2 ;
    private Drawable icon3 ;


    public void setTitle(String title) {
        titleStr = title ;
    }
    public void setIcon1(Drawable Icon1) {
        icon1 = Icon1 ;
    }
    public void setIcon2(Drawable Icon2) {
        icon2 = Icon2 ;
    }
    public void setIcon3(Drawable Icon3) {
        icon3 = Icon3 ;
    }

    public String getTitle() {
        return this.titleStr ;
    }
    public Drawable getIcon1() {
        return  this.icon1 ;
    }
    public Drawable getIcon2() {
        return  this.icon2 ;
    }
    public Drawable getIcon3() {
        return  this.icon3 ;
    }

}
