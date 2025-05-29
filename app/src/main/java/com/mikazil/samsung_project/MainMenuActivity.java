package com.mikazil.samsung_project;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient; // Добавлен импорт
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.internal.ApiKey;
import com.google.android.material.button.MaterialButton;
import androidx.appcompat.widget.SearchView;

public class MainMenuActivity extends AppCompatActivity {
    private WebView weatherMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);

        MaterialButton weatherButton = findViewById(R.id.weatherButton);
        SearchView searchView = findViewById(R.id.searchView);
        weatherMap = findViewById(R.id.weatherMap); // Инициализация WebView

        initWeatherMap(); // Инициализация карты

        weatherButton.setOnClickListener(v -> {
            launchWeatherActivityWithGeolocation();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                launchWeatherActivityWithCity(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        // Обработчик кнопки "Показать погоду"
        weatherButton.setOnClickListener(v -> {
            // Запуск с геолокацией
            launchWeatherActivityWithGeolocation();
        });

        // Обработчик поиска
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Запуск с указанным городом
                launchWeatherActivityWithCity(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void initWeatherMap() {
        WebSettings settings = weatherMap.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        String mapUrl = "https://openweathermap.org/weathermap?basemap=map&cities=true&layer=clouds";
        weatherMap.loadUrl(mapUrl);

        weatherMap.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("openweathermap.org/city/")) {
                    extractCityIdFromUrl(url);
                    return true;
                }
                return false;
            }
        });
    }


    private void extractCityIdFromUrl(String url) {
        String[] parts = url.split("/");
        if (parts.length > 0) {
            String cityId = parts[parts.length - 1];
            launchWeatherActivityWithCityId(cityId);
        }
    }

    private void launchWeatherActivityWithCityId(String cityId) {
        Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
        intent.putExtra("city_id", cityId);
        startActivity(intent);
    }

    // Методы должны быть объявлены на уровне класса, а не внутри onCreate!
    private void launchWeatherActivityWithGeolocation() {
        Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
        intent.putExtra("use_geolocation", true);
        startActivity(intent);
    }

    private void launchWeatherActivityWithCity(String city) {
        Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
        intent.putExtra("use_geolocation", false);
        intent.putExtra("city", city);
        startActivity(intent);
    }
}