package com.group2.trimedi;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by mo on 2016-12-01.
 */

public class MeasureItemView extends LinearLayout{
    private TextView measureDateView;
    private TextView measureTypeView;
    private TextView measureValueView;

    public MeasureItemView(Context context,MeasureItem item){
        super(context);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.measure_list_item,this,true);

        measureDateView = (TextView)findViewById(R.id.measureDateView);
        measureDateView.setText(item.getMeasureItem(0));
        measureTypeView = (TextView)findViewById(R.id.measureTypeView);
        measureTypeView.setText(item.getMeasureItem(1));
        measureValueView = (TextView)findViewById(R.id.measureValueView);
        measureValueView.setText(item.getMeasureItem(2));
    }

    public void setText(int idx,String data){
        if(idx == 0){
            measureDateView.setText(data);
        } else if(idx == 1){
            measureTypeView.setText(data);
        } else if(idx == 2){
            measureValueView.setText(data);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public MeasureItemView(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    public MeasureItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MeasureItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
