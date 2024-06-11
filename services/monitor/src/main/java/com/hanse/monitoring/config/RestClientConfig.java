package com.hanse.monitoring.config;

import com.hanse.monitoring.analytics.client.AnalyticsHttpClient;
import com.hanse.monitoring.configuration.client.ConfigurationHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class RestClientConfig {

    @Value("${client.url.configuration-server}")
    private String configurationServiceUrl;
    @Value("${client.url.analytics-server}")
    private String analyticsServiceUrl;

    @Bean
    ConfigurationHttpClient configurationHttpClient() {
        RestClient restClient = RestClient.create(configurationServiceUrl);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
        return factory.createClient(ConfigurationHttpClient.class);
    }

    @Bean
    AnalyticsHttpClient analyticsHttpClient() {
        RestClient restClient = RestClient.create(analyticsServiceUrl);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
        return factory.createClient(AnalyticsHttpClient.class);
    }

    @Bean
    public RestClient monitorClient(){
        return RestClient.builder().build();
    }
}
