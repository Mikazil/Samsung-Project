package com.mikazil.samsung_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;

import com.google.android.material.button.MaterialButton;

public class MainMenuActivity extends AppCompatActivity {

    private SearchView searchView;
    private MaterialButton weatherButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);

        searchView = findViewById(R.id.searchView);
        weatherButton = findViewById(R.id.weatherButton);

        // Обработчик для кнопки "Показать погоду" - теперь использует геолокацию
        weatherButton.setOnClickListener(v -> {
            openWeatherActivityWithGeolocation();
        });

        // Обработчик для SearchView (при отправке запроса)
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    openWeatherActivityWithCity(query);
                } else {
                    Toast.makeText(MainMenuActivity.this, "Введите название города", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void openWeatherActivityWithCity(String city) {
        Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
        intent.putExtra("city", city);
        intent.putExtra("use_geolocation", false);
        startActivity(intent);
        finish();
    }

    private void openWeatherActivityWithGeolocation() {
        Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
        intent.putExtra("use_geolocation", true);
        startActivity(intent);
        finish();
    }
}