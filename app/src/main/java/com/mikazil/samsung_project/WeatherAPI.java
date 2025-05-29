package com.mikazil.samsung_project;

import android.util.Log;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;

abstract class WeatherRequestHandler {
    protected static final String API_KEY = "a753173d5d3360f2b0bc53817b17d4e9";
    protected static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    protected static final String LOG_TAG = "WeatherAPI";

    protected final OpenWeatherMapService service;

    public WeatherRequestHandler() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(OpenWeatherMapService.class);
    }

    protected void handleResponse(Call<ResponseBody> call, WeatherAPI.WeatherCallback callback) {
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String result = response.body().string();
                        callback.onSuccess(result);
                    } catch (IOException e) {
                        handleError("Response parsing error: " + e.getMessage(), callback, e);
                    }
                } else {
                    handleError("HTTP error: " + response.code(), callback,
                            new IOException("Request failed"));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleError("Request failed: " + t.getMessage(), callback, t);
            }
        });
    }

    private void handleError(String message, WeatherAPI.WeatherCallback callback, Throwable t) {
        Log.e(LOG_TAG, message);
        callback.onFailure(t);
    }
}

// Запрос текущей погоды по городу
class CityWeatherRequest extends WeatherRequestHandler {
    public void execute(String city, WeatherAPI.WeatherCallback callback) {
        Call<ResponseBody> call = service.getWeather(city, null, null, API_KEY, "metric", "ru");
        handleResponse(call, callback);
    }
}

// Запрос текущей погоды по координатам
class CoordinatesWeatherRequest extends WeatherRequestHandler {
    public void execute(String lat, String lon, WeatherAPI.WeatherCallback callback) {
        Call<ResponseBody> call = service.getWeather(null, lat, lon, API_KEY, "metric", "ru");
        handleResponse(call, callback);
    }
}

// Запрос прогноза по городу
class CityForecastRequest extends WeatherRequestHandler {
    public void execute(String city, WeatherAPI.WeatherCallback callback) {
        Call<ResponseBody> call = service.getForecast(city, null, null, API_KEY, "metric", "ru");
        handleResponse(call, callback);
    }
}

// Запрос прогноза по координатам
class CoordinatesForecastRequest extends WeatherRequestHandler {
    public void execute(String lat, String lon, WeatherAPI.WeatherCallback callback) {
        Call<ResponseBody> call = service.getForecast(null, lat, lon, API_KEY, "metric", "ru");
        handleResponse(call, callback);
    }
}

// Запрос по ID города
class CityIdWeatherRequest extends WeatherRequestHandler {
    public void execute(String cityId, WeatherAPI.WeatherCallback callback) {
        Call<ResponseBody> call = service.getWeatherById(cityId, API_KEY, "metric", "ru");
        handleResponse(call, callback);
    }
}

public class WeatherAPI {
    public interface WeatherCallback {
        void onSuccess(String response);
        void onFailure(Throwable t);
    }

    public static void getForecastByCity(String city, WeatherCallback callback) {
        new CityForecastRequest().execute(city, callback);
    }

    public static void getForecastByCoordinates(String lat, String lon, WeatherCallback callback) {
        new CoordinatesForecastRequest().execute(lat, lon, callback);
    }

    public static void getWeatherDataByCity(String city, WeatherCallback callback) {
        new CityWeatherRequest().execute(city, callback);
    }

    public static void getWeatherDataByCoordinates(String lat, String lon, WeatherCallback callback) {
        new CoordinatesWeatherRequest().execute(lat, lon, callback);
    }

    public static void getWeatherDataById(String cityId, WeatherCallback callback) {
        new CityIdWeatherRequest().execute(cityId, callback);
    }
}