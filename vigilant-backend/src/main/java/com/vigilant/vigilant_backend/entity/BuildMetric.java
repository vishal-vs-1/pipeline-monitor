package com.vigilant.vigilant_backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "build_metrics")
@Getter
@Setter
@NoArgsConstructor
public class BuildMetric {

    @Id
    @Column(name = "repo_id")
    private Long repoId;

    @Column(name = "avg_duration_last_10", precision = 10, scale = 2)
    private BigDecimal avgDurationLast10;

    @Column(name = "failure_count_last_5")
    private Integer failureCountLast5;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void prePersist() {
        this.updatedAt = LocalDateTime.now();
    }
}
