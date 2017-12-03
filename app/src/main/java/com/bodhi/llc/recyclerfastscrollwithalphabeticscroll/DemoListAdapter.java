package com.bodhi.llc.recyclerfastscrollwithalphabeticscroll;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Bhaiya on 12/2/2017.
 */

public class DemoListAdapter extends RecyclerView.Adapter<DemoListAdapter.DataModelViewHolder>  implements
        FastScrollRecyclerView.SectionedAdapter
      {
    private Context mcontext;
    private ArrayList<DataModel> dataList;


    public DemoListAdapter(Context mcontext, ArrayList<DataModel> dataList) {
        this.mcontext = mcontext;
        this.dataList = dataList;
    }


    @Override
    public DataModelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DataModelViewHolder(LayoutInflater.from(mcontext).inflate(R.layout.content_list_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(DataModelViewHolder holder, int position) {
        holder.demotexrt.setText(dataList.get(position).getFastName());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }



    @NonNull
    @Override
    public String getSectionName(int position) {
        return dataList.get(position).fastName.substring(0,1);
    }

    class DataModelViewHolder extends RecyclerView.ViewHolder {
        TextView demotexrt;

        public DataModelViewHolder(View itemView) {
            super(itemView);
            demotexrt = (TextView) itemView.findViewById(R.id.demo_text);
        }
    }
}
