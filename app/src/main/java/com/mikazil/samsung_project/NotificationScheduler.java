package com.mikazil.samsung_project;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.preference.PreferenceManager;

import java.util.Calendar;

public class NotificationScheduler {

    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleDailyNotification(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WeatherAlarmReceiver.class);

        // ИСПРАВЛЕНИЕ: Добавлен FLAG_IMMUTABLE
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Проверка разрешения для Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                requestExactAlarmPermission(context);
                return;
            }
        }

        // Получаем сохраненное время
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int hour = prefs.getInt("notification_hour", 8);
        int minute = prefs.getInt("notification_minute", 0);

        // Устанавливаем время
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // Если время уже прошло сегодня, переносим на завтра
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Устанавливаем повторение ежедневно
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent);
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent);
            }
        }

        Log.d("NotificationScheduler", "Notification scheduled for " + calendar.getTime());
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private static void requestExactAlarmPermission(Context context) {
        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e("NotificationScheduler", "Cannot request exact alarm permission", e);
            Toast.makeText(context, "Please enable exact alarm permission in settings", Toast.LENGTH_LONG).show();
        }
    }

    public static void cancelNotification(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WeatherAlarmReceiver.class);

        // ИСПРАВЛЕНИЕ: Добавлен FLAG_IMMUTABLE
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}