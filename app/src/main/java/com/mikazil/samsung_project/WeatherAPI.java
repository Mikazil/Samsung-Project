package com.mikazil.samsung_project;

import android.util.Log;
import com.mikazil.samsung_project.HourlyForecast;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
                @Query("appid") String apiKey
        );
    }
}

// Запрос по городу
class CityWeatherRequest extends WeatherRequestHandler {
    public void execute(String city, WeatherAPI.WeatherCallback callback) {
        Call<ResponseBody> call = service.getWeather(city, null, null, API_KEY);
        handleResponse(call, callback);
    }
}

// Запрос по координатам
class CoordinatesWeatherRequest extends WeatherRequestHandler {
    public void execute(String lat, String lon, WeatherAPI.WeatherCallback callback) {
        Call<ResponseBody> call = service.getWeather(null, lat, lon, API_KEY);
        handleResponse(call, callback);
    }
}

// Интерфейс и методы для работы с API
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
}

class WeatherData {
    private static double temperature;
    private static double feelsLike;
    private int humidity;
    private double windSpeed;
    private double pressure;
    private int cloudiness;


    public static WeatherData fromJson(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        JSONObject main = root.getJSONObject("main");
        JSONObject wind = root.getJSONObject("wind");
        JSONObject clouds = root.getJSONObject("clouds");

        WeatherData data = new WeatherData();
        double tempKelvin = main.getDouble("temp");
        temperature = tempKelvin - 273.15;
        double feelsLikeKelvin = main.getDouble("feels_like");
        feelsLike = feelsLikeKelvin - 273.15;
        data.humidity = main.getInt("humidity");
        data.pressure = main.getDouble("pressure");
        data.windSpeed = wind.getDouble("speed");
        data.cloudiness = clouds.getInt("all");
        return data;
    }

    public static List<HourlyForecast> parseForecastData(String json) throws JSONException {
        List<HourlyForecast> forecasts = new ArrayList<>();
        JSONObject root = new JSONObject(json);
        JSONArray list = root.getJSONArray("list");

        // Берем первые 4 элемента прогноза (текущий + 3 следующих)
        for (int i = 0; i < 4 && i < list.length(); i++) {
            JSONObject item = list.getJSONObject(i);
            long dt = item.getLong("dt") * 1000; // Конвертация в миллисекунды

            JSONObject main = item.getJSONObject("main");
            double temp = main.getDouble("temp") - 273.15; // Конвертация в Цельсии

            JSONArray weatherArray = item.getJSONArray("weather");
            JSONObject weather = weatherArray.getJSONObject(0);
            String iconCode = weather.getString("icon");

            // Форматирование времени
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String time = sdf.format(new Date(dt));

            // Получение ресурса иконки
            int iconRes = getIconResource(iconCode);

            forecasts.add(new HourlyForecast(time, (int) Math.round(temp), iconRes, i == 0));
        }

        return forecasts;
    }
    private static int getIconResource(String iconCode) {
        switch (iconCode) {
            case "01d": return R.drawable.ic_sunny;
            case "01n": return R.drawable.ic_night;
            case "02d": case "03d": case "04d":
                return R.drawable.ic_partly_cloudy;
            case "02n": case "03n": case "04n":
                return R.drawable.ic_night_cloudy;
            case "09d": case "10d": case "09n": case "10n":
                return R.drawable.ic_rain;
            case "13d": case "13n":
                return R.drawable.ic_snow;
            case "50d": case "50n":
                return R.drawable.ic_fog;
            default: return R.drawable.ic_sunny;
        }
    }
    public double getTemperature() { return temperature; }
    public double getFeelsLike() { return feelsLike; }
    public int getHumidity() { return humidity; }
    public double getWindSpeed() { return windSpeed; }
    public double getPressure() { return pressure; }
    public int getClouds() { return cloudiness; }
}