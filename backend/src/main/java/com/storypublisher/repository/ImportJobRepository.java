package com.storypublisher.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.storypublisher.model.ImportJob;
import com.storypublisher.model.User;

@Repository
public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {

    List<ImportJob> findByUserOrderByCreatedAtDesc(User user);

    List<ImportJob> findByUserAndStatusOrderByCreatedAtDesc(User user, ImportJob.ImportStatus status);

    @Query("SELECT ij FROM ImportJob ij WHERE ij.user = :user AND ij.status IN ('PENDING', 'RUNNING')")
    List<ImportJob> findActiveJobsByUser(@Param("user") User user);

    Optional<ImportJob> findByIdAndUser(Long id, User user);

    @Query("SELECT COUNT(ij) FROM ImportJob ij WHERE ij.user = :user AND ij.status = 'RUNNING'")
    long countRunningJobsByUser(@Param("user") User user);

    @Query("SELECT ij FROM ImportJob ij WHERE ij.status = 'PENDING' ORDER BY ij.createdAt ASC")
    List<ImportJob> findPendingJobs();
}
