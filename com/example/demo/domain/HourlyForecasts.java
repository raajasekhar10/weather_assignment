package com.example.demo.domain;

public class HourlyForecasts {

    private ForecastLocation forecastLocation;

    public ForecastLocation getForecastLocation ()
    {
        return forecastLocation;
    }

    public void setForecastLocation (ForecastLocation forecastLocation)
    {
        this.forecastLocation = forecastLocation;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [forecastLocation = "+forecastLocation+"]";
    }
}
