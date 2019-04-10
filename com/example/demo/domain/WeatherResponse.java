package com.example.demo.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherResponse {

    private HourlyForecasts hourlyForecasts;

    private String message;

    public HourlyForecasts getHourlyForecasts ()
    {
        return hourlyForecasts;
    }

    public void setHourlyForecasts (HourlyForecasts hourlyForecasts)
    {
        this.hourlyForecasts = hourlyForecasts;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "WeatherResponse{" +
                "hourlyForecasts=" + hourlyForecasts +
                ", message=" + message +
                '}';
    }
}
