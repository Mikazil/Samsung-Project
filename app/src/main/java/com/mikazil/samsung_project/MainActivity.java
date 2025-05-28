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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
        binding.weatherCondition.setText(getCloudinessDescription(data.getClouds()));
    }

    private String getCloudinessDescription(int cloudiness) {
        if (cloudiness <= 10) return "Ясно ☀️";
        else if (cloudiness <= 30) return "Малооблачно 🌤";
        else if (cloudiness <= 70) return "Переменная облачность ⛅";
        else return "Пасмурно ☁️";
    }
}