package com.mikazil.samsung_project;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.DynamicColors;
import com.mikazil.samsung_project.databinding.ActivityMainBinding;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupSearchView();
        fetchWeatherData("Moscow");
        DynamicColors.applyToActivityIfAvailable(this);
        DynamicColors.applyToActivityIfAvailable(this);
    }


    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
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

    private void fetchWeatherData(String city) {
        // Запрос текущей погоды
        WeatherAPI.getWeatherDataByCity(city, new WeatherAPI.WeatherCallback() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onSuccess(String response) {
                try {
                    WeatherData data = WeatherData.fromJson(response);

                    runOnUiThread(() -> updateUI(data));

                    // После получения текущей погоды запрашиваем прогноз
                    fetchForecastData(city);

                } catch (JSONException e) {
                    Log.e("TAG", "JSON parsing error", e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("TAG", "API call failed", t);
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

    private void updateHourlyForecast(List<HourlyForecast> forecasts) {
        LinearLayout container = findViewById(R.id.weatherForecast);

        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);

            if (child instanceof MaterialCardView && i < forecasts.size()) {
                updateForecastCard((MaterialCardView) child, forecasts.get(i));
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private void updateUI(WeatherData data) {
        binding.temperature.setText(String.format("%.1f°C", data.getTemperature()));
        binding.feelsLike.setText(String.format("По ощущениям: %.1f°C", data.getFeelsLike()));
        binding.humidity.setText(String.format("%d%%", data.getHumidity()));
        binding.windSpeed.setText(String.format("%.1f m/s", data.getWindSpeed()));
        binding.pressureValue.setText(String.format("%.0f hPa", data.getPressure()));
        binding.weatherCondition.setText(getCloudinessDescription(data.getClouds()));
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

    private String getCloudinessDescription(int cloudiness) {
        if (cloudiness <= 10) return "Ясно ☀️";
        else if (cloudiness <= 30) return "Малооблачно 🌤";
        else if (cloudiness <= 70) return "Переменная облачность ⛅";
        else return "Пасмурно ☁️";
    }
}