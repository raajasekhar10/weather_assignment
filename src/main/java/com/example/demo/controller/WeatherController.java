package com.example.demo.controller;

import com.example.demo.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private static final Logger log = LoggerFactory.getLogger(WeatherController.class);

    @Autowired
    private WeatherService weatherService;

    @GetMapping("/zipCodes/{zipCode}/coolestHour")
    public String generateWeatherReport(@PathVariable(name = "zipCode") String zipCode) {
        log.info("Received zipCode: {}", zipCode);
        return weatherService.findWeatherReportByHourly(zipCode);
    }
}
