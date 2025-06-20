package com.mikazil.samsung_project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class WeatherAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("WeatherAlarmReceiver", "Alarm received, starting service");

        Intent serviceIntent = new Intent(context, WeatherNotificationService.class);

        context.startForegroundService(serviceIntent);
    }
}