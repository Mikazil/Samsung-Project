package com.mikazil.samsung_project;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mikazil.samsung_project.WeatherAPI;
import com.google.android.material.color.DynamicColors;

import com.mikazil.samsung_project.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        DynamicColors.applyToActivityIfAvailable(this);
        WeatherAPI.getWeatherDataByCity("London", new WeatherAPI.WeatherCallback() {
            @Override
            public void onSuccess(String response) {
                // Обработка ответа
                Log.d("TAG", response);
                binding.feelsLike.setText(response);
            }

            @Override
            public void onFailure(Throwable t) {
                // Обработка ошибки
                Log.d("TAG", t.toString());
            }
        });


    }

}