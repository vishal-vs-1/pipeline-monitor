package com.vigilant.vigilant_backend.repository;

import com.vigilant.vigilant_backend.entity.BuildMetric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildMetricRepository extends JpaRepository<BuildMetric, Long> {
}
