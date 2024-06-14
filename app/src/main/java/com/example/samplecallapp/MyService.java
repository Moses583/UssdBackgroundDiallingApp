package com.example.samplecallapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.util.LinkedList;
import java.util.Queue;

public class MyService extends Service {

    private Handler handler;
    private Runnable runnableCode;
    private Queue<UssdResponse> queue;
    private DBHelper helper;

    TelephonyManager manager;
    TelephonyManager.UssdResponseCallback callback;
    Handler handler2;
    private static final String TAG = "Failed";
    String s = "";



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler = new Handler();
        queue = new LinkedList<>();
        helper = new DBHelper(this);

        manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        queue = checkDatabaseAndRetrieve();
        runnableCode = new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (queue.isEmpty()){
                            Log.d(TAG,"queue is empty");
                            deleteData();
                            stopSelf();
                        }
                        else{
                            UssdResponse response = queue.poll();
                            dialUssdCode(response.getUssdCode(), response.getSubscriptionId());
                            Log.d(TAG,s);
                        }
                    }
                }).start();
                handler.postDelayed(this,3000);
            }
        };
        handler.post(runnableCode);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("ForegroundServiceChannel", "Foreground Service Channel", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "ForegroundServiceChannel")
                .setContentTitle("Database Check Service")
                .setContentText("Running...");

        startForeground(1, notification.build());

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Removes pending code execution
        handler.removeCallbacks(runnableCode);
    }

    public String dialUssdCode(String ussdRequest, int subscriptionId) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            callback = new TelephonyManager.UssdResponseCallback() {
                @Override
                public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                    super.onReceiveUssdResponse(telephonyManager, request, response);
                    insertResponse(response.toString(),"*100*4*1*1#",subscriptionId);
                    s = response.toString();
                }

                @Override
                public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
                    super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode);
                    insertResponse("Failed with the following error: "+String.valueOf(failureCode),"*100*4*1*1#",subscriptionId);
                    s = String.valueOf(failureCode);
                }
            };
            handler2 = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                }
            };
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createForSubscriptionId(subscriptionId).sendUssdRequest(ussdRequest, callback, handler2);

        }
        return s;
    }

    public void insertResponse(String response,String ussdCode, int subscriptionId){
        boolean checkInsertData = helper.insertResponse(response, ussdCode, subscriptionId);
        if (checkInsertData){
            Toast.makeText(this, "response added successfully", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Insertion unsuccessful", Toast.LENGTH_SHORT).show();
        }
    }

    private Queue<UssdResponse> checkDatabaseAndRetrieve() {
        Cursor cursor = helper.getFailedResponses();
        Queue<UssdResponse> myQueue = new LinkedList<>();
        if (cursor.getCount() == 0){
            Toast.makeText(this, "Failed transactions cleared", Toast.LENGTH_SHORT).show();
        }
        else{
            while (cursor.moveToNext()){
                String response = cursor.getString(cursor.getColumnIndexOrThrow("response"));
                String ussdCode = cursor.getString(cursor.getColumnIndexOrThrow("ussdCode"));
                int subscriptionId = cursor.getInt(cursor.getColumnIndexOrThrow("subscriptionId"));
                myQueue.add(new UssdResponse(response,ussdCode,subscriptionId));
            }
        }
        cursor.close();
        return myQueue;
    }
    private void deleteData(){
        boolean deleteData = helper.deleteData();
        if (deleteData){
            Log.d(TAG,"Data deleted");
        }
        else{
            Log.d(TAG, "Data not deleted");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
