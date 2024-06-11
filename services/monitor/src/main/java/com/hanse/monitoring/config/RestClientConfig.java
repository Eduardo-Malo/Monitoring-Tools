package com.hanse.monitoring.config;

import com.hanse.monitoring.analytics.client.AnalyticsHttpClient;
import com.hanse.monitoring.configuration.client.ConfigurationHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class RestClientConfig {

    @Bean
    ConfigurationHttpClient configurationHttpClient() {
        RestClient restClient = RestClient.create("http://localhost:8070/api/v1");
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
        return factory.createClient(ConfigurationHttpClient.class);
    }

    @Bean
    AnalyticsHttpClient analyticsHttpClient() {
        RestClient restClient = RestClient.create("http://localhost:8071/api/v1");
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
        return factory.createClient(AnalyticsHttpClient.class);
    }

    @Bean
    public RestClient monitorClient(){
        return RestClient.builder().build();
    }
}
