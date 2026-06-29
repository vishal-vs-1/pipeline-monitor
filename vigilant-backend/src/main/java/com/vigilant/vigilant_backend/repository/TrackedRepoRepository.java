package com.vigilant.vigilant_backend.repository;

import com.vigilant.vigilant_backend.entity.TrackedRepo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackedRepoRepository extends JpaRepository<TrackedRepo, Long> {
    List<TrackedRepo> findByIsActiveTrue();
}
