package com.vigilant.vigilant_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tracked_repos")
@Getter
@Setter
@NoArgsConstructor
public class TrackedRepo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repo_name", nullable = false)
    private String repoName;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "branch")
    private String branch = "main";

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "anomaly_multiplier")
    private Double anomalyMultiplier = 1.5;

    @Column(name = "anomaly_window_size")
    private Integer anomalyWindowSize = 10;
}
