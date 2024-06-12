package com.hanse.monitoring.config;

import com.hanse.monitoring.repository.ConfigurationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class DataLoader {

    private final ConfigurationRepository configurationRepository;

    public DataLoader(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    @Bean
    public CommandLineRunner preloadData() {
        return args -> {
            if (configurationRepository.count() == 0) {
                configurationRepository.saveAll(List.of(
                        com.hanse.monitoring.domain.Configuration.builder()
                                                                 .name("configGoogle")
                                                                 .uri("http://www.google.pt")
                                                                 .interval(1)
                                                                 .active(true)
                                                                 .createdAt(java.time.LocalDateTime.now())
                                                                 .build(),
                        com.hanse.monitoring.domain.Configuration.builder()
                                                                 .name("configConfigurations")
                                                                 .uri("http://localhost:8070/api/v1/configurations")
                                                                 .interval(5)
                                                                 .active(true)
                                                                 .createdAt(java.time.LocalDateTime.now())
                                                                 .build(),
                        com.hanse.monitoring.domain.Configuration.builder()
                                                                 .name("configNotUrl")
                                                                 .uri("ItsNotAnUrl")
                                                                 .interval(10)
                                                                 .active(true)
                                                                 .createdAt(java.time.LocalDateTime.now())
                                                                 .build(),
                        com.hanse.monitoring.domain.Configuration.builder()
                                                                 .name("configFacebook")
                                                                 .uri("http://www.facebook.com")
                                                                 .interval(10)
                                                                 .active(true)
                                                                 .createdAt(java.time.LocalDateTime.now())
                                                                 .build(),
                        com.hanse.monitoring.domain.Configuration.builder()
                                                                 .name("configBadUrl")
                                                                 .uri("http://www.badurl.chw")
                                                                 .interval(10)
                                                                 .active(true)
                                                                 .createdAt(java.time.LocalDateTime.now())
                                                                 .build()
                ));
            }
        };
    }
}

