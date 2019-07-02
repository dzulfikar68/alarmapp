package com.digitcreativestudio.alarmapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private final static String URL = "http://ashim.pythonanywhere.com/alarmapp";
    private final static String URL_ADD = "http://ashim.pythonanywhere.com/alarmapp_add";
    private final static String URL_DELETE = "http://ashim.pythonanywhere.com/alarmapp_delete";

    TimePicker myTimePicker;
    Button buttonstartSetDialog, btnSave, btnLoad;
//    TextView textAlarmPrompt;

    TimePickerDialog timePickerDialog;

    final static int RQS_1 = 1;

    RecyclerView rv_alarm;
    ArrayList<Model> dataSet;
    AlarmAdapter adapter;
    AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        textAlarmPrompt = (TextView) findViewById(R.id.alarmprompt);
        btnLoad = findViewById(R.id.btn_load);
        btnSave = findViewById(R.id.btn_save);

        dataSet = new ArrayList<>();
        rv_alarm = findViewById(R.id.rv_alarm);
        rv_alarm.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rv_alarm.setLayoutManager(layoutManager);

        adapter = new AlarmAdapter(dataSet);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        adapter.setInitial(getBaseContext(), alarmManager);
        rv_alarm.setAdapter(adapter);

        buttonstartSetDialog = (Button) findViewById(R.id.startSetDialog);
        buttonstartSetDialog.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                textAlarmPrompt.setText("");
                openTimePickerDialog(false);

            }
        });

        final Context context = this;

        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Download Data");
                builder.setMessage("Download data akan menghapus data alarm ponsel ini, dan diganti oleh data alarm dari server.");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loadFromServer();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Upload Data");
//                Upload data akan me-replace data alarm server sesuai data alarm ponsel ini.
                builder.setMessage("Upload data akan menghapus data alarm server, dan diganti oleh data alarm dari ponsel ini.");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteSaveFromToServer();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void deleteAllAlarm(){
        for(int x=0; x<dataSet.size(); x++){
            cancelAlarm(dataSet.get(x).getKey());
        }
    }

    private void cancelAlarm(int key){
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), RQS_1, intent, key);
        alarmManager.cancel(pendingIntent);
    }

    private void deleteSaveFromToServer(){
        StringRequest srblogList = new StringRequest(Request.Method.POST, URL_DELETE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("?", response);
                        if(response.equals("TRUE")){
                            saveToServer();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        //request handler volley
        RequestHandler.getInstance(getApplicationContext()).addToRequestQueue(srblogList);
    }

    private void saveToServer(){
        StringRequest srblogList = new StringRequest(Request.Method.POST, URL_ADD,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("?", response);
                        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<>();
                        return params;
                    }
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
//                        how to datetime to string
//                        json = make list array of datetime + key
//                        params.put("json", json);

                        List<Map<String, String>> list_map_alarm = new ArrayList<Map<String, String>>();
                        for(Model alarm : dataSet){
                            Map<String, String> map = new HashMap<>();
                            map.put("id", ""+alarm.getKey());
                            map.put("content", alarm.getValue());
                            list_map_alarm.add(map);
                        }
                        List<JSONObject> alarm_json = new ArrayList<JSONObject>();
                        for(Map<String, String> data : list_map_alarm) {
                            JSONObject obj = new JSONObject(data);
                            alarm_json.add(obj);
                        }
                        JSONArray alarm_array = new JSONArray(alarm_json);
                        Log.e("json", alarm_array.toString());
                        params.put("json", alarm_array.toString());

                        return params;
                    }
//                    public void onErrorResponse(VolleyError error) {
//                        // As of f605da3 the following should work
//                        NetworkResponse response = error.networkResponse;
//                        if (error instanceof ServerError && response != null) {
//                            try {
//                                String res = new String(response.data,
//                                        HttpHeaderParser.parseCharset(response.headers, "utf-8"));
//                                // Now you can use any deserializer to make sense of data
//                                JSONObject obj = new JSONObject(res);
//                            } catch (UnsupportedEncodingException e1) {
//                                // Couldn't properly decode data to string
//                                e1.printStackTrace();
//                            } catch (JSONException e2) {
//                                // returned data is not JSONObject?
//                                e2.printStackTrace();
//                            }
//                        }
//                    }
                };

        //request handler volley
        RequestHandler.getInstance(getApplicationContext()).addToRequestQueue(srblogList);
    }

    private void loadFromServer(){
        StringRequest srblogList = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        final ArrayList<Model> dataSementara = new ArrayList<Model>();
                        try {
//                            JSONObject jsonObject = new JSONObject(response);

                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONArray js = (JSONArray) jsonArray.get(i);
                                Model obj = new Model((int)js.get(0), (String) js.get(1));
                                Log.e("XXX", obj.getValue());
                                dataSementara.add(obj);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //hapus alarm manager semua (alarm)
                        deleteAllAlarm();

                        //replace alarm dari server ke lokal (alarm)
//                        dataSementara -> alarm lokal;
                        saveAllAlarm(dataSementara);

                        //hapus dan replace alarm dari server ke lokal (list)
                        dataSet.clear();

//                        for data set datasementara -> list lokal
                        for (int i=0; i<dataSementara.size(); i++){
                            dataSet.add(dataSementara.get(i));
                        }

                        adapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(), "Download Data Sukses", Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        //request handler volley
        RequestHandler.getInstance(getApplicationContext()).addToRequestQueue(srblogList);
    }

    private void saveAllAlarm(ArrayList<Model> listDataSementara){
        for(int i=0; i<listDataSementara.size(); i++){
            try{
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                cal.setTime(sdf.parse(listDataSementara.get(i).getValue()));// all done
                setAlarm(listDataSementara.get(i).getKey(), cal);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void openTimePickerDialog(boolean is24r) {
        Calendar calendar = Calendar.getInstance();

        timePickerDialog = new TimePickerDialog(MainActivity.this,
                onTimeSetListener, calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), true);
        timePickerDialog.setTitle("Set Alarm Time");

        timePickerDialog.show();
    }

    TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            Calendar calNow = Calendar.getInstance();
            Calendar calSet = (Calendar) calNow.clone();

            calSet.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calSet.set(Calendar.MINUTE, minute);
            calSet.set(Calendar.SECOND, 0);
            calSet.set(Calendar.MILLISECOND, 0);

            //pengecekan jika waktu sekarang ....
            if (calSet.compareTo(calNow) <= 0) {
                // Today Set time passed, count to tomorrow
                calSet.add(Calendar.DATE, 1);
                Log.i("hasil", " =<0");
            } else if (calSet.compareTo(calNow) > 0) {
                Log.i("hasil", " > 0");
            } else {
                Log.i("hasil", " else ");
            }

            setAlarm(calSet);
        }
    };

    private void setAlarm(Calendar targetCal) {

//        String info = "***\n" + "Alarm set on " + targetCal.getTime() + "\n***";
//        textAlarmPrompt.setText(info);

        int key = (int)(Math.random() * 1000 + 1);

        Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getBaseContext(), RQS_1, intent, key);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(),
                pendingIntent);

        dataSet.add(new Model(key, targetCal.getTime().toString()));
        adapter.notifyDataSetChanged();
    }

    private void setAlarm(int key, Calendar targetCal) {

//        String info = "***\n" + "Alarm set on " + targetCal.getTime() + "\n***";
//        textAlarmPrompt.setText(info);

        Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getBaseContext(), RQS_1, intent, key);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(),
                pendingIntent);

        dataSet.add(new Model(key, targetCal.getTime().toString()));
        adapter.notifyDataSetChanged();
    }
}
