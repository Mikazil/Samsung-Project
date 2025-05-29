package com.mikazil.samsung_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);
        MaterialButton weatherButton = findViewById(R.id.weatherButton);
        weatherButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
            intent.putExtra("use_geolocation", true);
            startActivity(intent);
        });
    }
}