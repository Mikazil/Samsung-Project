package com.mikazil.samsung_project;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.color.DynamicColors;
import com.mikazil.samsung_project.databinding.ActivityMainBinding;

import org.json.JSONException;

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

        // реализовать города
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
        WeatherAPI.getWeatherDataByCity(city, new WeatherAPI.WeatherCallback() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onSuccess(String response) {
                try {
                    WeatherData data = WeatherData.fromJson(response);

                    runOnUiThread(() -> updateUI(data));

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

    @SuppressLint("DefaultLocale")
    private void updateUI(WeatherData data) {
        binding.temperature.setText(String.format("%.1f°C", data.getTemperature()));
        binding.feelsLike.setText(String.format("По ощущениям: %.1f°C", data.getFeelsLike()));
        binding.humidity.setText(String.format("%d%%", data.getHumidity()));
        binding.windSpeed.setText(String.format("%.1f m/s", data.getWindSpeed()));
        binding.pressureValue.setText(String.format("%.0f hPa", data.getPressure()));
        //binding.weatherCondition.setText(getCloudinessDescription(data.getClouds()));
        binding.weatherCondition.setText(data.getWeatherDescription());
        binding.tempMin.setText(String.format("%.1f°C", data.getMinTemp()));
        binding.tempMax.setText(String.format("%.1f°C", data.getMaxTemp()));
        binding.weatherIcon.setText(getWeatherEmoji(data.getIconCode()));
    }

    private String getWeatherEmoji(String iconCode) {
        switch (iconCode) {
            case "01d": return "☀️"; // Ясно (день)
            case "01n": return "🌙"; // Ясно (ночь)
            case "02d": return "⛅"; // Малооблачно (день)
            case "02n": return "☁️"; // Малооблачно (ночь)
            case "03d": case "03n": return "☁️"; // Облачно
            case "04d": case "04n": return "☁️️"; // Пасмурно
            case "09d": case "09n": return "🌧️"; // Ливень
            case "10d": return "🌦️"; // Дождь (день)
            case "10n": return "🌧️"; // Дождь (ночь)
            case "11d": case "11n": return "⛈️"; // Гроза
            case "13d": case "13n": return "❄️"; // Снег
            case "50d": case "50n": return "🌫️"; // Туман
            default: return "❓";
        }
    }
}