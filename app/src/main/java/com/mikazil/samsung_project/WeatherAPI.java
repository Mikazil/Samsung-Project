package com.mikazil.samsung_project;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import java.io.IOException;

abstract class WeatherRequestHandler {
    protected static final String API_KEY = "b37d8af2cceace36578fa5899ad6a9f8";
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

    // Обработчик ответов
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

    interface OpenWeatherMapService {
        @GET("weather")
        Call<ResponseBody> getWeather(
                @Query("q") String city,
                @Query("lat") String lat,
                @Query("lon") String lon,
                @Query("appid") String apiKey,
                @Query("units") String units,
                @Query("lang") String lang
        );
    }
}

// Запрос по городу
class CityWeatherRequest extends WeatherRequestHandler {
    public void execute(String city, WeatherAPI.WeatherCallback callback) {
        Call<ResponseBody> call = service.getWeather(city, null, null, API_KEY, "metric", "ru");
        handleResponse(call, callback);
    }
}

// Запрос по координатам
class CoordinatesWeatherRequest extends WeatherRequestHandler {
    public void execute(String lat, String lon, WeatherAPI.WeatherCallback callback) {
        Call<ResponseBody> call = service.getWeather(null, lat, lon, API_KEY, "metric", "ru");
        handleResponse(call, callback);
    }
}

// Интерфейс и методы для работы с API
public class WeatherAPI {
    public interface WeatherCallback {
        void onSuccess(String response);
        void onFailure(Throwable t);
    }

    public static void getWeatherDataByCity(String city, WeatherCallback callback) {
        new CityWeatherRequest().execute(city, callback);
    }

    public static void getWeatherDataByCoordinates(String lat, String lon, WeatherCallback callback) {
        new CoordinatesWeatherRequest().execute(lat, lon, callback);
    }
}

class WeatherData {
    private double temperature;
    private double feelsLike;
    private double minTemp;
    private double maxTemp;
    private int humidity;
    private double windSpeed;
    private double pressure;
    private int cloudiness;
    private String weatherDescription;
    private String iconCode;
    private String cityName;
    private int timezone;
    private long timestamp;

    public static WeatherData fromJson(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        JSONObject main = root.getJSONObject("main");
        JSONObject wind = root.getJSONObject("wind");
        JSONObject clouds = root.getJSONObject("clouds");
        JSONArray weatherArray = root.getJSONArray("weather");

        WeatherData data = new WeatherData();
        data.temperature = main.getDouble("temp");
        data.feelsLike = main.getDouble("feels_like");
        data.minTemp = main.getDouble("temp_min");
        data.maxTemp = main.getDouble("temp_max");
        data.humidity = main.getInt("humidity");
        data.pressure = main.getDouble("pressure");
        data.windSpeed = wind.getDouble("speed");
        data.cloudiness = clouds.getInt("all");
        data.cityName = root.getString("name");
        data.timestamp = root.getLong("dt") * 1000;
        data.timezone = root.getInt("timezone");

        if (weatherArray.length() > 0) {
            JSONObject weather = weatherArray.getJSONObject(0);
            data.weatherDescription = weather.getString("description");
            data.iconCode = weather.getString("icon"); // Получаем код иконки
        } else {
            data.weatherDescription = "";
            data.iconCode = "01d"; // Значение по умолчанию
        }

        return data;
    }

    public double getTemperature() { return temperature; }
    public double getFeelsLike() { return feelsLike; }
    public double getMinTemp() { return minTemp; }
    public double getMaxTemp() { return maxTemp; }
    public int getHumidity() { return humidity; }
    public double getWindSpeed() { return windSpeed; }
    public double getPressure() { return pressure; }
    public int getClouds() { return cloudiness; }
    public String getWeatherDescription() { return weatherDescription; }
    public String getIconCode() { return iconCode; }
    public String getCityName() { return cityName; }
    public long getTimestamp() { return timestamp; }
    public int getTimezone(){ return timezone; }
}