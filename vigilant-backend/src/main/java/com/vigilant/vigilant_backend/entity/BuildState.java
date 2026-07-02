package com.vigilant.vigilant_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "build_states", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"repo_id", "run_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class BuildState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repo_id")
    private TrackedRepo repo;

    @Column(name = "run_id", nullable = false)
    private Long runId;

    @Column(name = "status")
    private String status;

    @Column(name = "conclusion")
    private String conclusion;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_anomaly")
    private Boolean isAnomaly = false;

    @PrePersist
    @PreUpdate
    public void prePersist() {
        this.updatedAt = LocalDateTime.now();
    }
}
