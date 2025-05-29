package com.mikazil.samsung_project;

public class HourlyForecast {
    private final String time;
    private final double temperature; // Изменено с int на double
    private final int iconRes;
    private final boolean isNow;

    public HourlyForecast(String time, double temperature, int iconRes, boolean isNow) {
        this.time = time;
        this.temperature = temperature;
        this.iconRes = iconRes;
        this.isNow = isNow;
    }

    public String getTime() { return time; }
    public double getTemperature() { return temperature; } // Возвращает double
    public int getIconRes() { return iconRes; }
    public boolean isNow() { return isNow; }
}