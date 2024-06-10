package com.hanse.monitoring.repository;

import com.hanse.monitoring.domain.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigurationRepository extends JpaRepository<Configuration, Integer> {

}
