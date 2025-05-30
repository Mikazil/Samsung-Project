package com.mikazil.samsung_project;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherNotificationService extends Service {

    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "weather_channel";
    private static final int SERVICE_NOTIFICATION_ID = 1002;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        // Для Android 8.0+ создаем уведомление для foreground service
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Weather Service")
                .setContentText("Updating weather data")
                .setSmallIcon(R.drawable.ic_weather)
                .build();

        startForeground(SERVICE_NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        checkWeatherAndNotify();
        return START_STICKY;
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Weather Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("Daily weather forecast notifications");

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private void checkWeatherAndNotify() {
        Log.d("WeatherService", "Checking weather for notification");

        // ИСПРАВЛЕНИЕ: Получаем город из SharedPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String city = preferences.getString("last_city", "Moscow");
        boolean useGeolocation = preferences.getBoolean("use_geolocation", false);

        // В зависимости от режима получаем погоду
        if (useGeolocation) {
            // Для простоты используем город (в реальном приложении здесь нужно использовать координаты)
            fetchWeatherForNotification(city);
        } else {
            fetchWeatherForNotification(city);
        }
    }

    // Новый метод для получения погоды
    private void fetchWeatherForNotification(String city) {
        WeatherAPI.getWeatherDataByCity(city, new WeatherAPI.WeatherCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    WeatherData data = WeatherData.fromJson(response);
                    showWeatherNotification(data);
                } catch (JSONException e) {
                    Log.e("WeatherService", "JSON parsing error", e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("WeatherService", "Failed to fetch weather data", t);
            }
        });
    }

    private void showWeatherNotification(WeatherData data) {
        // Форматируем данные
        String temp = String.format(Locale.getDefault(), "%.0f°C", data.getTemperature());
        String date = new SimpleDateFormat("d MMMM", Locale.getDefault()).format(new Date());

        // Создаем интент для открытия приложения
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("city", data.getCityName());
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Создаем уведомление
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_weather)
                .setContentTitle("Прогноз погоды на " + date)
                .setContentText(data.getCityName() + ": " + temp + ", " + data.getWeatherDescription())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Сейчас в " + data.getCityName() + ": " + temp +
                                "\n" + capitalizeFirstLetter(data.getWeatherDescription()) +
                                "\nОщущается как: " + String.format(Locale.getDefault(), "%.0f°C", data.getFeelsLike())))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        // Показываем уведомление
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}