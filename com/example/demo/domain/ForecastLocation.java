package com.example.demo.domain;

import java.util.List;

public class ForecastLocation {
    private List<Forecast> forecast;
    private String city;
    private String country;
    private String state;

    public List<Forecast> getForecast ()
    {
        return forecast;
    }

    public void setForecast (List<Forecast> forecast)
    {
        this.forecast = forecast;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "ForecastLocation{" +
                "forecast=" + forecast +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
