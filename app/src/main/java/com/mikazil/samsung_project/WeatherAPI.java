package com.mikazil.samsung_project;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
public class WeatherAPI {
    private static final String API_KEY = "b37d8af2cceace36578fa5899ad6a9f8";
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather?q=London&appid=" + API_KEY;
    private static final String LOG_TAG = "WeatherAPI";
    private String res;

     public String getWeatherData() {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(API_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            res = result.toString();
            //return result.toString();
        } catch (IOException e) {
            Log.d(LOG_TAG, "IOException: " + e);

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.d(LOG_TAG, "inputStream.close IOException: " + e);
                }
            }
        }
        return res;
    }
}
