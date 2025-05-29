package com.mikazil.samsung_project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
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
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean useGeolocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Инициализация сервиса геолокации
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Получение флага из интента
        if (getIntent() != null) {
            useGeolocation = getIntent().getBooleanExtra("use_geolocation", false);
        }

        setupSearchView();
        setupBackButton();

        // Определение источника данных для погоды
        if (useGeolocation) {
            requestLocationPermission();
        } else {
            // По умолчанию показываем Москву
            fetchWeatherData("Moscow");
        }

        DynamicColors.applyToActivityIfAvailable(this);
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // При поиске отключаем геолокацию
                useGeolocation = false;
                fetchWeatherData(query);
                binding.searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
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
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                // Если разрешение не дано, показываем Москву
                Toast.makeText(this, "Разрешение на геолокацию не получено", Toast.LENGTH_SHORT).show();
                fetchWeatherData("Moscow");
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
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Location", "Error getting location", e);
                    fetchWeatherData("Moscow");
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
                    runOnUiThread(() -> updateUI(data));
                    // Запрашиваем прогноз по тем же координатам
                    fetchForecastByCoordinates(lat, lon);
                } catch (JSONException e) {
                    Log.e("TAG", "JSON parsing error", e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("TAG", "API call failed", t);
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Ошибка получения данных", Toast.LENGTH_SHORT).show()
                );
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
                    runOnUiThread(() -> updateUI(data));
                    fetchForecastData(city);
                } catch (JSONException e) {
                    Log.e("TAG", "JSON parsing error", e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("TAG", "API call failed", t);
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Город не найден", Toast.LENGTH_SHORT).show()
                );
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
        binding.tempMin.setText(String.format("%.1f°C", data.getMinTemp()));
        binding.tempMax.setText(String.format("%.1f°C", data.getMaxTemp()));
        binding.weatherIcon.setText(getWeatherEmoji(data.getIconCode()));
        binding.location.setText(data.getCityName());
        binding.currentDate.setText(formatDate(data.getTimestamp(), data.getTimezone()));
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