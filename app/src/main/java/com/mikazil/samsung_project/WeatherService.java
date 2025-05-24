package com.mikazil.samsung_project;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherService extends Service {
    private static final String TAG = "WeatherService";
    private static final String API_KEY = "YOUR_API_KEY";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Make a request to the OpenWeatherMap API to retrieve the current weather data
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApi weatherApi = retrofit.create(WeatherApi.class);
        Call<WeatherData> call = weatherApi.getCurrentWeather("London", API_KEY);

        call.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                if (response.isSuccessful()) {
                    WeatherData weatherData = response.body();
                    Log.d(TAG, "Current weather data: " + weatherData.toString());
                } else {
                    Log.e(TAG, "Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }
}