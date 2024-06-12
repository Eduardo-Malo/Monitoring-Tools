package com.hanse.monitoring.repository;

import com.hanse.monitoring.domain.Analytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsRepository extends JpaRepository<Analytics, Integer> {

    @Query("SELECT a FROM Analytics a WHERE " +
            "( cast(:startDate as localdatetime) IS NULL OR a.createdAt >= :startDate ) AND " +
            "( cast(:endDate as localdatetime) IS NULL OR a.createdAt <= :endDate ) AND " +
            "( :responseCode IS NULL OR a.responseCode = :responseCode ) AND " +
            "( :result IS NULL OR a.result = :result ) AND " +
            "( :jobId IS NULL OR a.jobId = :jobId ) AND " +
            "( :minResponseTime IS NULL OR a.responseTime >= :minResponseTime ) AND " +
            "( :maxResponseTime IS NULL OR a.responseTime <= :maxResponseTime )")
    List<Analytics> findAllAnalytics(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("responseCode") Integer responseCode,
            @Param("result") Boolean result,
            @Param("jobId") Integer jobId,
            @Param("minResponseTime") Double minResponseTime,
            @Param("maxResponseTime") Double maxResponseTime

    );

}
