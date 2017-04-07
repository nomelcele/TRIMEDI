package com.group2.trimedi;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mo on 2016-12-01.
 */

public class MeasureItemListAdapter extends BaseAdapter {
    private Context mContext;
    private List<MeasureItem> mItems = new ArrayList<MeasureItem>();

    MeasureItemListAdapter(Context context){
        mContext = context;
    }

    void addItem(MeasureItem item){
        mItems.add(item);
    }

    private void setListItems(List<MeasureItem> list){
        mItems = list;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int i) {
        return mItems.get(i);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // view 보여주는 역할
        MeasureItemView itemView;
        if(convertView == null){
            itemView = new MeasureItemView(mContext,mItems.get(position));
        } else {
            itemView = (MeasureItemView)convertView;
            itemView.setText(0,mItems.get(position).getMeasureItem(0));
            itemView.setText(1,mItems.get(position).getMeasureItem(1));
            itemView.setText(2,mItems.get(position).getMeasureItem(2));
        }

        return itemView;
    }
}
