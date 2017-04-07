package com.group2.trimedi;

/**
 * Created by mo on 2016-12-01.
 */

public class MeasureItem { // ListView에 데이터가 들어갈 공간을 정의
    private String[] measureItems; // 데이터를 넣을 배열

    public MeasureItem(String[] measureItems){
        this.measureItems = measureItems;
    }

    public String[] getMeasureItems(){
        return measureItems;
    }

    public String getMeasureItem(int idx){
        if(measureItems == null || idx >= measureItems.length){
            return null;
        } else {
            return measureItems[idx];
        }
    }
}
