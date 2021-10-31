package com.example.queuesystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;
    ArrayList<Report> reportArrayList;

    public MyAdapter(Context context, ArrayList<Report> reportArrayList) {
        this.context = context;
        this.reportArrayList = reportArrayList;
    }

    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.activity_report_item,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {
        Report report = reportArrayList.get(position);
        holder.datetime.setText(report.DateTime);
        holder.totalQueue.setText(String.valueOf(report.QueueSize));
    }

    @Override
    public int getItemCount() {
        return reportArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView datetime,totalQueue;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            datetime = itemView.findViewById(R.id.datetime);
            totalQueue = itemView.findViewById(R.id.totalQueue);
        }
    }
}
