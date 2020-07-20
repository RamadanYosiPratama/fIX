package com.pjs.pjs_sos;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class AppServices  extends Service {
    MyApp MAPP;
    private final static String TAG = "APPLOGS";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent,final int flags,final int startId) {
        Log.d(TAG, "Service onStartCommand");
        super.onStartCommand(intent, flags, startId);
        MAPP = new MyApp(this);
        startTimer();
        return START_STICKY;
    }
    @Override
    public void onCreate() {
    }
    @Override
    public void onTaskRemoved(Intent rootIntent){
        Log.d(TAG,"ON onTaskRemoved");

        Intent restartServiceTask = new Intent(getApplicationContext(),this.getClass());
        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent =PendingIntent.getService(getApplicationContext(), 1,restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent);

        super.onTaskRemoved(rootIntent);
    }




    public void startTimer()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    if(true){
                        Log.i(TAG, "Service running");
                        MAPP.kirimGPS();
                    }
                    try {
                        Thread.sleep(300000);//60000
                    } catch (Exception e) {

                    }


                }

            }
        }).start();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"ON Destroyed");

        Intent restartServiceTask = new Intent(getApplicationContext(),this.getClass());
        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent =PendingIntent.getService(getApplicationContext(), 1,restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent);

        super.onDestroy();
    }
}
