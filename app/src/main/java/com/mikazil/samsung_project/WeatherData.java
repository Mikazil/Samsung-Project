package com.mikazil.samsung_project;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

    public class WeatherData {
        private double temperature;
        private double feelsLike;
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
            data.temperature = tempKelvin - 273.15;
            double feelsLikeKelvin = main.getDouble("feels_like");
            data.feelsLike = feelsLikeKelvin - 273.15;
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

            for (int i = 0; i < 4 && i < list.length(); i++) {
                JSONObject item = list.getJSONObject(i);
                long dt = item.getLong("dt") * 1000;

                JSONObject main = item.getJSONObject("main");
                double temp = main.getDouble("temp") - 273.15;

                JSONArray weatherArray = item.getJSONArray("weather");
                JSONObject weather = weatherArray.getJSONObject(0);
                String iconCode = weather.getString("icon");

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String time = sdf.format(new Date(dt));

                int iconRes = getIconResource(iconCode);
                forecasts.add(new HourlyForecast(time, (int) Math.round(temp), iconRes, i == 0));
            }
            return forecasts;
        }

    private static int getIconResource(String iconCode) {
        switch (iconCode) {
            case "01d":
                return R.drawable.ic_sunny;
            case "01n":
                return R.drawable.ic_night;
            case "02d":
            case "03d":
            case "04d":
                return R.drawable.ic_partly_cloudy;
            case "02n":
            case "03n":
            case "04n":
                return R.drawable.ic_night_cloudy;
            case "09d":
            case "10d":
            case "09n":
            case "10n":
                return R.drawable.ic_rain;
            case "13d":
            case "13n":
                return R.drawable.ic_snow;
            case "50d":
            case "50n":
                return R.drawable.ic_fog;
            default:
                return R.drawable.ic_sunny;
        }
    }

        public double getTemperature() { return temperature; }
        public double getFeelsLike() { return feelsLike; }
        public int getHumidity() { return humidity; }
        public double getWindSpeed() { return windSpeed; }
        public double getPressure() { return pressure; }
        public int getClouds() { return cloudiness; }
    }