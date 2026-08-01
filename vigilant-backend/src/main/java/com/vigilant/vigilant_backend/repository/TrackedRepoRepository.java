package com.vigilant.vigilant_backend.repository;

import com.vigilant.vigilant_backend.entity.TrackedRepo;
import com.vigilant.vigilant_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackedRepoRepository extends JpaRepository<TrackedRepo, Long> {
    List<TrackedRepo> findByIsActiveTrue();
    List<TrackedRepo> findByIsActiveTrueOrderByIdAsc();
    List<TrackedRepo> findByUserOrderByIdAsc(User user);
    List<TrackedRepo> findByIsActiveTrueAndUserEmailIn(java.util.Set<String> emails);
    Optional<TrackedRepo> findByIdAndUser(Long id, User user);
}
