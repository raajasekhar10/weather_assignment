package com.example.demo.adapter;

import com.example.demo.builder.WeatherServiceUriBuilder;
import com.example.demo.domain.WeatherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class WeatherServiceAdapter {

    private static final Logger log = LoggerFactory.getLogger(WeatherServiceAdapter.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WeatherServiceUriBuilder weatherServiceUriBuilder;

    public WeatherResponse fetchWeatherReport(String zipCode, String product) {

        String url = weatherServiceUriBuilder.buildUrlForPostingOrder(zipCode, product);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<WeatherResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, WeatherResponse.class);

            if (response != null
                    && HttpStatus.OK.equals(response.getStatusCode())) {
                return response.getBody();
            }

        } catch (ResourceAccessException resourceAccessExp) {
            //TODO - log error message here
            log.error("Weather API failed : {}", resourceAccessExp.getMessage());
            return populateErrorMessage(resourceAccessExp.getMessage());
        }
        return null;
    }

    private WeatherResponse populateErrorMessage(String errorMessage) {
        WeatherResponse weatherResponse = new WeatherResponse();
        if (errorMessage.contains("City Information Required")) {
            weatherResponse.setMessage("Invalid Zip Code");
        } else {
            weatherResponse.setMessage(String.format("Weather Service is down and exception: {}", errorMessage));
        }
        return weatherResponse;
    }
}
