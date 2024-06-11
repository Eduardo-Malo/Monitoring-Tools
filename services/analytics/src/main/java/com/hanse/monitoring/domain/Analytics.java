package com.hanse.monitoring.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "analytics")
public class Analytics {

    @Id
    @GeneratedValue
    private Integer id;
    @NotNull(message = "jobId should be present")
    private Integer jobId;
    @NotEmpty(message = "jobName should be present")
    private String jobName;
    @NotEmpty(message = "uri should be present")
    private String uri;
    private String errorMessage;
    @NotNull(message = "responseTime should be present")
    private Long responseTime;
    @NotNull(message = "result should be present")
    private Boolean result;
    @NotNull(message = "responseCode should be present")
    private Integer responseCode;
    @PastOrPresent
    @NotNull(message = "createdAt should be present")
    private LocalDateTime createdAt;
}
