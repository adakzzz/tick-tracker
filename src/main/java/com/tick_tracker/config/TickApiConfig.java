package com.tick_tracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class TickApiConfig {

    @Bean
    public RestClient tickApiRestClient(@Value("${tick.api.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((Duration.ofSeconds(5)));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        return RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
    }
}
