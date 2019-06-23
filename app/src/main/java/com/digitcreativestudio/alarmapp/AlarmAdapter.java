package com.digitcreativestudio.alarmapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import static com.digitcreativestudio.alarmapp.MainActivity.RQS_1;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder>{
    private ArrayList<Model> rv_alarm;
    private Context context;
    private AlarmManager alarmManager;

    AlarmAdapter(ArrayList<Model> rv_alarm) {
        this.rv_alarm = rv_alarm;
    }

    public void setInitial(Context ctx, AlarmManager alm){
        this.context = ctx;
        this.alarmManager = alm;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.alarm_adapter, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final String info = rv_alarm.get(i).getValue();
        viewHolder.tv_info.setText(info);
        final int position = i;
        viewHolder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAlarm(rv_alarm.get(position).getKey());
                rv_alarm.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return rv_alarm.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_info;
        Button btn_delete;
        ViewHolder(View v) {
            super(v);
            tv_info = (TextView) v.findViewById(R.id.tv_info);
            btn_delete = (Button) v.findViewById(R.id.btn_delete);
        }
    }

    private void cancelAlarm(int key){
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, RQS_1, intent, key);
        alarmManager.cancel(pendingIntent);
    }

}
