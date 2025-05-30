package com.mikazil.samsung_project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private static final int REQUEST_LOCATION_PERMISSIONS = 102;
    private static final int REQUEST_EXACT_ALARM_PERMISSION = 101;
    private SwitchCompat notificationSwitch;
    private SharedPreferences prefs;

    @SuppressLint("ScheduleExactAlarm")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Log.d(TAG, "Activity created");

        try {
            // Инициализация элементов UI
            notificationSwitch = findViewById(R.id.notification_switch);
            Button timePickerButton = findViewById(R.id.timePickerButton);

            // Получаем SharedPreferences
            prefs = PreferenceManager.getDefaultSharedPreferences(this);

            // Устанавливаем текущее состояние переключателя
            boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", false);
            notificationSwitch.setChecked(notificationsEnabled);

            // Обработчик изменений для переключателя уведомлений
            notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Для Android 12+ проверяем разрешение только при включении
                if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                        requestExactAlarmPermission();
                        return;
                    }
                }

                // Сохраняем настройки
                saveNotificationPreference(isChecked);
            });

            // Обработчик для кнопки выбора времени
            timePickerButton.setOnClickListener(v -> showTimePickerDialog());

            // Обновляем текст кнопки времени
            updateTimeButtonText();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void requestExactAlarmPermission() {
        try {
            // Запрашиваем необходимые разрешения
            List<String> permissions = new ArrayList<>();

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }

            if (!permissions.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        permissions.toArray(new String[0]),
                        REQUEST_LOCATION_PERMISSIONS);
                return;
            }

            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivityForResult(intent, REQUEST_EXACT_ALARM_PERMISSION);
        } catch (Exception e) {
            Log.e(TAG, "Error requesting exact alarm permission", e);
            Toast.makeText(this, "Ошибка при запросе разрешения", Toast.LENGTH_SHORT).show();
            notificationSwitch.setChecked(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_EXACT_ALARM_PERMISSION) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && alarmManager.canScheduleExactAlarms()) {
                // Если разрешение получено, включаем уведомления
                saveNotificationPreference(true);
            } else {
                // Если разрешение не дано, отключаем переключатель
                notificationSwitch.setChecked(false);
                Toast.makeText(this, "Разрешение не предоставлено. Уведомления отключены", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveNotificationPreference(boolean isEnabled) {
        prefs.edit().putBoolean("notifications_enabled", isEnabled).apply();

        if (isEnabled) {
            NotificationScheduler.scheduleDailyNotification(this);
            Toast.makeText(this, "Уведомления включены", Toast.LENGTH_SHORT).show();
        } else {
            NotificationScheduler.cancelNotification(this);
            Toast.makeText(this, "Уведомления отключены", Toast.LENGTH_SHORT).show();
        }
    }

    private void showTimePickerDialog() {
        try {
            int hour = prefs.getInt("notification_hour", 8);
            int minute = prefs.getInt("notification_minute", 0);

            TimePickerDialog timePicker = new TimePickerDialog(
                    this,
                    (view, selectedHour, selectedMinute) -> {
                        // Сохраняем выбранное время
                        prefs.edit()
                                .putInt("notification_hour", selectedHour)
                                .putInt("notification_minute", selectedMinute)
                                .apply();

                        // Обновляем текст кнопки
                        updateTimeButtonText();

                        // Перезапускаем расписание, если уведомления включены
                        if (prefs.getBoolean("notifications_enabled", false)) {
                            NotificationScheduler.scheduleDailyNotification(this);
                            Toast.makeText(this, "Время уведомления обновлено", Toast.LENGTH_SHORT).show();
                        }
                    },
                    hour, minute, true);

            timePicker.setTitle("Выберите время уведомления");
            timePicker.show();

        } catch (Exception e) {
            Log.e(TAG, "Error in time picker", e);
            Toast.makeText(this, "Ошибка выбора времени", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTimeButtonText() {
        try {
            int hour = prefs.getInt("notification_hour", 8);
            int minute = prefs.getInt("notification_minute", 0);

            String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            Button timePickerButton = findViewById(R.id.timePickerButton);
            timePickerButton.setText(time);

        } catch (Exception e) {
            Log.e(TAG, "Error updating time button", e);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Добавляем анимацию перехода
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}