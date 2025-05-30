package com.mikazil.samsung_project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.DynamicColors;
import com.mikazil.samsung_project.databinding.ActivityMainBinding;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean useGeolocation = false;
    private SharedPreferences preferences;
    private static final String PREFS_CITY = "last_city";
    private static final String PREFS_USE_GEOLOCATION = "use_geolocation";
    private static final int FOREGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SwitchCompat notificationSwitch = findViewById(R.id.notification_switch);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Инициализация SharedPreferences
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Восстановление настроек из SharedPreferences
        useGeolocation = preferences.getBoolean(PREFS_USE_GEOLOCATION, false);
        String savedCity = preferences.getString(PREFS_CITY, "Moscow");

        // Обработка входящего интента
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("use_geolocation")) {
            useGeolocation = intent.getBooleanExtra("use_geolocation", false);
            String city = intent.getStringExtra("city");

            // Сохраняем настройки из интента
            savePreferences(city != null ? city : savedCity, useGeolocation);

            if (city != null && !city.isEmpty() && !useGeolocation) {
                fetchWeatherData(city);
            } else if (useGeolocation) {
                requestLocationPermission();
            } else {
                fetchWeatherData(savedCity);
            }
        } else {
            // Используем сохраненные настройки
            if (useGeolocation) {
                requestLocationPermission();
            } else {
                fetchWeatherData(savedCity);
            }
        }

        setupSearchView();
        setupBackButton();
        DynamicColors.applyToActivityIfAvailable(this);
    }

    // Сохранение настроек в SharedPreferences
    private void savePreferences(String city, boolean useGeo) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFS_CITY, city);
        editor.putBoolean(PREFS_USE_GEOLOCATION, useGeo);
        editor.apply();
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // При поиске отключаем геолокацию
                useGeolocation = false;
                fetchWeatherData(query);
                savePreferences(query, false); // Сохраняем город
                binding.searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void updateRecommendation(double temperature, String weatherCondition) {
        TextView recommendationText = findViewById(R.id.recommendationText);
        StringBuilder recommendation = new StringBuilder();

        // Рекомендации по температуре
        if (temperature < -10) {
            recommendation.append("❄️ Очень холодно! Наденьте теплую зимнюю одежду, шапку, шарф и варежки.");
        } else if (temperature < 0) {
            recommendation.append("⛄ Холодно. Наденьте теплую куртку, шапку и шарф.");
        } else if (temperature < 10) {
            recommendation.append("🧥 Прохладно. Рекомендуется надеть ветровку или свитер.");
        } else if (temperature < 20) {
            recommendation.append("👕 Тепло! Можно надеть легкую куртку или свитер.");
        } else if (temperature < 30) {
            recommendation.append("🩳 Жарко! Наденьте футболку и шорты.");
        } else {
            recommendation.append("🥵 Очень жарко! Наденьте легкую одежду и используйте солнцезащитные средства.");
        }

        // Добавляем эмодзи для лучшей визуализации
        recommendation.append("\n\n");

        // Рекомендации по осадкам
        if (weatherCondition.contains("дождь") || weatherCondition.contains("ливень")) {
            recommendation.append("☔ Возьмите зонт, ожидается дождь.");
        } else if (weatherCondition.contains("снег")) {
            recommendation.append("⛄ Ожидается снег, оденьтесь теплее.");
        } else if (weatherCondition.contains("гроза")) {
            recommendation.append("⚡ Будет гроза, будьте осторожны.");
        } else if (weatherCondition.contains("солнце") || weatherCondition.contains("ясно")) {
            recommendation.append("😎 Солнечно, не забудьте солнцезащитные очки.");
        } else if (weatherCondition.contains("облач") || weatherCondition.contains("пасмурно")) {
            recommendation.append("⛅ Облачно, но дождь не ожидается.");
        } else {
            recommendation.append("👍 Хорошая погода для прогулки!");
        }

        recommendationText.setText(recommendation.toString());
    }

    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            // Возврат в главное меню
            Intent intent = new Intent(MainActivity.this, MainMenuActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void requestLocationPermission() {
        List<String> permissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }
        }

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissions.toArray(new String[0]),
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                getLocation();
            } else {
                Toast.makeText(this, "Разрешение на геолокацию не получено", Toast.LENGTH_SHORT).show();
                fetchWeatherData("Moscow");
                savePreferences("Moscow", false);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        fetchWeatherByCoordinates(latitude, longitude);
                    } else {
                        // Если местоположение не найдено, показываем Москву
                        Toast.makeText(this, "Местоположение не найдено", Toast.LENGTH_SHORT).show();
                        fetchWeatherData("Moscow");
                        savePreferences("Moscow", false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Location", "Error getting location", e);
                    fetchWeatherData("Moscow");
                    savePreferences("Moscow", false);
                });
    }

    private void fetchWeatherByCoordinates(double latitude, double longitude) {
        String lat = String.valueOf(latitude);
        String lon = String.valueOf(longitude);

        // Запрашиваем текущую погоду по координатам
        WeatherAPI.getWeatherDataByCoordinates(lat, lon, new WeatherAPI.WeatherCallback() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onSuccess(String response) {
                try {
                    WeatherData data = WeatherData.fromJson(response);
                    runOnUiThread(() -> {
                        updateUI(data);
                        // Сохраняем город из API и режим геолокации
                        savePreferences(data.getCityName(), true);
                    });
                    // Запрашиваем прогноз по тем же координатам
                    fetchForecastByCoordinates(lat, lon);
                } catch (JSONException e) {
                    Log.e("TAG", "JSON parsing error", e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("TAG", "API call failed", t);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Ошибка получения данных", Toast.LENGTH_SHORT).show();
                    savePreferences("Moscow", false);
                });
            }
        });
    }

    private void fetchWeatherData(String city) {
        WeatherAPI.getWeatherDataByCity(city, new WeatherAPI.WeatherCallback() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onSuccess(String response) {
                try {
                    WeatherData data = WeatherData.fromJson(response);
                    runOnUiThread(() -> {
                        updateUI(data);
                        savePreferences(city, false);
                    });
                    fetchForecastData(city);
                } catch (JSONException e) {
                    Log.e("TAG", "JSON parsing error", e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("TAG", "API call failed", t);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Город не найден", Toast.LENGTH_SHORT).show();
                    // При ошибке загружаем сохраненный город
                    String savedCity = preferences.getString(PREFS_CITY, "Moscow");
                    fetchWeatherData(savedCity);
                });
            }
        });
    }

    private void fetchForecastData(String city) {
        WeatherAPI.getForecastByCity(city, new WeatherAPI.WeatherCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    List<HourlyForecast> forecasts = WeatherData.parseForecastData(response);
                    runOnUiThread(() -> updateHourlyForecast(forecasts));
                } catch (JSONException e) {
                    Log.e("TAG", "Forecast parsing error", e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("TAG", "Forecast API call failed", t);
            }
        });
    }

    private void fetchForecastByCoordinates(String lat, String lon) {
        WeatherAPI.getForecastByCoordinates(lat, lon, new WeatherAPI.WeatherCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    List<HourlyForecast> forecasts = WeatherData.parseForecastData(response);
                    runOnUiThread(() -> updateHourlyForecast(forecasts));
                } catch (JSONException e) {
                    Log.e("TAG", "Forecast parsing error", e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("TAG", "Forecast API call failed", t);
            }
        });
    }

    private void updateHourlyForecast(List<HourlyForecast> forecasts) {
        LinearLayout container = findViewById(R.id.weatherForecast);

        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);

            if (child instanceof MaterialCardView && i < forecasts.size()) {
                updateForecastCard((MaterialCardView) child, forecasts.get(i));
            }
        }
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void updateUI(WeatherData data) {
        binding.temperature.setText(String.format("%.1f°C", data.getTemperature()));
        binding.feelsLike.setText(String.format("По ощущениям: %.1f°C", data.getFeelsLike()));
        binding.humidity.setText(String.format("%d%%", data.getHumidity()));
        binding.windSpeed.setText(String.format("%.1f м/с", data.getWindSpeed()));
        binding.pressureValue.setText(String.format("%.0f гПа", data.getPressure()));
        binding.weatherCondition.setText(capitalizeFirstLetter(data.getWeatherDescription()));
        binding.weatherIcon.setText(getWeatherEmoji(data.getIconCode()));
        binding.location.setText(data.getCityName());
        binding.currentDate.setText(formatDate(data.getTimestamp(), data.getTimezone()));
        updateRecommendation(data.getTemperature(), data.getWeatherDescription().toLowerCase());
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private String getWeatherEmoji(String iconCode) {
        switch (iconCode) {
            case "01d": return "☀️";
            case "01n": return "🌙";
            case "02d": return "⛅";
            case "02n": return "☁️";
            case "03d": case "03n": return "☁️";
            case "04d": case "04n": return "☁️️";
            case "09d": case "09n": return "🌧️";
            case "10d": return "🌦️";
            case "10n": return "🌧️";
            case "11d": case "11n": return "⛈️";
            case "13d": case "13n": return "❄️";
            case "50d": case "50n": return "🌫️";
            default: return "❓";
        }
    }

    private String formatDate(long timestamp, int timezoneOffset) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM, EEEE", new Locale("ru"));
            TimeZone timeZone = TimeZone.getTimeZone("GMT");
            timeZone.setRawOffset(timezoneOffset * 1000);
            dateFormat.setTimeZone(timeZone);
            return dateFormat.format(new Date(timestamp));
        } catch (Exception e) {
            Log.e("DateFormat", "Error formatting date with timezone", e);
            return "";
        }
    }

    private void updateForecastCard(MaterialCardView card, HourlyForecast forecast) {
        LinearLayout layout = (LinearLayout) card.getChildAt(0);

        TextView timeView = (TextView) layout.getChildAt(0);
        timeView.setText(forecast.isNow() ? "Сейчас" : forecast.getTime());

        ImageView iconView = (ImageView) layout.getChildAt(1);
        iconView.setImageResource(forecast.getIconRes());

        TextView tempView = (TextView) layout.getChildAt(2);
        tempView.setText(String.format(Locale.getDefault(), "%.0f°", forecast.getTemperature()));
    }
}