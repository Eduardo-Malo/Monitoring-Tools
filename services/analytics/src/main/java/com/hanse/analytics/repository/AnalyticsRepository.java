package com.hanse.analytics.repository;

import com.hanse.analytics.domain.Analytics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalyticsRepository extends JpaRepository<Analytics, Integer> {


}
