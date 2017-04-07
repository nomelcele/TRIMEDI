package com.group2.trimedi;

import android.graphics.drawable.Drawable;

public class PrescriptionListItem {
    private Drawable iconDrawable ;
    private String titleStr ;
    private String descStr ;
    private int itemId;

    public void setIcon(Drawable icon) {
        iconDrawable = icon ;
    }
    public void setTitle(String title) {
        titleStr = title ;
    }
    public void setDesc(String desc) {
        descStr = desc ;
    }
    public void setItemId(int id) {itemId = id;}

    public Drawable getIcon() {
        return this.iconDrawable ;
    }
    public String getTitle() {
        return this.titleStr ;
    }
    public String getDesc() {
        return this.descStr ;
    }
    public int getItemId() { return this.itemId;}
}
