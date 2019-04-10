package com.example.demo.service;

import com.example.demo.adapter.WeatherServiceAdapter;
import com.example.demo.domain.Forecast;
import com.example.demo.domain.WeatherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.demo.constants.WeatherConstants.FORECAST_HOURLY;

@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    @Autowired
    private WeatherServiceAdapter weatherServiceAdapter;

    public String findWeatherReportByHourly(String zipCode) {
        WeatherResponse weatherResponse = weatherServiceAdapter.fetchWeatherReport(zipCode, FORECAST_HOURLY);
        return printCoolestHour(weatherResponse.getHourlyForecasts().getForecastLocation().getForecast(), weatherResponse.getHourlyForecasts().getForecastLocation().getCity());
    }

    public String printCoolestHour(List<Forecast> forecastList, String cityName) {
        ArrayList<Double> temp = new ArrayList<>();
        ArrayList<String> dateTime = new ArrayList<>();
        int i = 0;
        String today = forecastList.stream().findFirst().get().getWeekday();
        for (i=1;today.equals(forecastList.get(i).getWeekday());i++)
        {}
        for (int j=0; j < 24; i++,j++)
        {
            temp.add(Double.parseDouble((forecastList.get(i).getTemperature())));
            dateTime.add(forecastList.get(i).getLocalTime());
        }
        int minIndex = temp.indexOf(Collections.min(temp));
        String time = dateTime.get(minIndex).substring(0,2);
        String date = dateTime.get(minIndex).substring(2, dateTime.get(minIndex).length());
        date = date.substring(0,2) + "-" + date.substring(2,4) + "-" + date.substring(4,8);
        String meridiem = "am";
        if(Integer.parseInt(time) > 11)
        {
            if (time!="12") {
                time = Integer.toString((Integer.parseInt(time) - 12));
            }
            meridiem = "pm";
        } else {
            if(time=="00")
                time = "12";
        }
        return new StringBuilder()
                .append("Coolest hour at ")
                .append(cityName).append(" tomorrow on ")
                .append(date)
                .append(" would be at ")
                .append(time).append(" ")
                .append(meridiem).toString();
    }
}
