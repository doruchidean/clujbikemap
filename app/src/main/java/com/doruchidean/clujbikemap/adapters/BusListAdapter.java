package com.doruchidean.clujbikemap.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.doruchidean.clujbikemap.R;

import java.util.List;

/**
 * Created by Doru on 09/02/16.
 *
 */
public class BusListAdapter extends BaseAdapter {

    private List<String> mList;
    private int mSelectedPosition=-1;
    private Context mContext;

    public BusListAdapter(Context context, List<String> list){
        this.mContext=context;
        mList=list;
    }

    public void setSelectedBus(int position){
        mSelectedPosition = position;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public String getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = View.inflate(mContext, R.layout.cell_bus, null);
        }
        TextView tv= (TextView) convertView.findViewById(R.id.tv_cell_bus);
        tv.setText(mList.get(position));
        if(position == mSelectedPosition){
            convertView.setBackgroundColor(Color.GRAY);
        }else{
            convertView.setBackgroundColor(Color.WHITE);
        }
        return convertView;
    }
}
