package com.hanse.monitoring.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Table(name = "configuration")
public class Configuration {

    @Id
    @GeneratedValue
    private Integer id;
    @NotEmpty(message = "name should be present")
    private String name;
    @NotEmpty(message = "uri should be present")
    private String uri;
    @NotNull(message = "interval should be present")
    @Min(value = 0)
    @Max(value = 86400)
    private Integer interval;
    @PastOrPresent
    @NotNull(message = "createdAt should be present")
    private LocalDateTime createdAt;
    @PastOrPresent
    private LocalDateTime modifiedAt;
}
