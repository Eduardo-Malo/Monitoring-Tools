package com.hanse.monitoring.configuration.client;

import com.hanse.monitoring.configuration.domain.Configuration;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import java.util.List;

public interface ConfigurationHttpClient {

    @GetExchange("/configurations")
    List<Configuration> listConfigurations(@RequestParam("active") boolean active);
}
