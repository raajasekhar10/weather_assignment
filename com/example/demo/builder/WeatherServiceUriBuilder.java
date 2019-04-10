package com.example.demo.builder;

import com.example.demo.config.URLConfig;
import com.example.demo.constants.WeatherConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class WeatherServiceUriBuilder {
    @Autowired
    private URLConfig urlConfig;

    public String buildUrlForPostingOrder(String zipCode, String product) {
        String uri = urlConfig.getWeatherService().getUrl();
        StringBuilder weatherServiceUrl = new StringBuilder(uri);


        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(weatherServiceUrl.toString())
                .queryParam(WeatherConstants.ZIPCODE, zipCode)
                .queryParam(WeatherConstants.APP_CODE, urlConfig.getWeatherService().getAppCode())
                .queryParam(WeatherConstants.APP_ID, urlConfig.getWeatherService().getAppId())
                .queryParam(WeatherConstants.PRODUCT, product);

        return uriBuilder.toUriString();
    }
}
