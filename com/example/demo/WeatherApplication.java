package com.example.demo;

import com.example.demo.config.RestTemplateConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(scanBasePackages = {"com.example.demo"})
public class WeatherApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeatherApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder,
									 ClientHttpRequestFactory httpRequestFactory) {
		RestTemplate restTemplate = restTemplateBuilder
				.requestFactory(httpRequestFactory)
				.build();
		RestTemplateConfig.setErrorHandler(restTemplate);
		return restTemplate;
	}
}
