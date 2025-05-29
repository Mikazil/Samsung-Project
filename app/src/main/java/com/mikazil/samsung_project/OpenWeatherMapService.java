package com.mikazil.samsung_project;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenWeatherMapService {
    // Метод для текущей погоды (без изменений)
    @GET("weather")
    Call<ResponseBody> getWeather(
            @Query("q") String city,
            @Query("lat") String lat,
            @Query("lon") String lon,
            @Query("appid") String apiKey,
            @Query("units") String units,
            @Query("lang") String lang
    );

    // Обновленный метод для прогноза погоды
    @GET("forecast")
    Call<ResponseBody> getForecast(
            @Query("q") String city,
            @Query("lat") String lat,
            @Query("lon") String lon,
            @Query("appid") String apiKey,
            @Query("units") String units,
            @Query("lang") String lang
    );
}

// Обновленные классы запросов
class CityForecastRequest extends WeatherRequestHandler {
    public void execute(String city, WeatherAPI.WeatherCallback callback) {
        // Добавлены параметры units и lang
        Call<ResponseBody> call = service.getForecast(city, null, null, API_KEY, "metric", "ru");
        handleResponse(call, callback);
    }
}

class CoordinatesForecastRequest extends WeatherRequestHandler {
    public void execute(String lat, String lon, WeatherAPI.WeatherCallback callback) {
        // Добавлены параметры units и lang
        Call<ResponseBody> call = service.getForecast(null, lat, lon, API_KEY, "metric", "ru");
        handleResponse(call, callback);
    }
}