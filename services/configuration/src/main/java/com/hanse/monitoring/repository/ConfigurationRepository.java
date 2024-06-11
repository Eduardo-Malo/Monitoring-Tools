package com.hanse.monitoring.repository;

import com.hanse.monitoring.domain.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConfigurationRepository extends JpaRepository<Configuration, Integer> {

    List<Configuration> findByActive(Boolean active);
}
