package com.stoonproduction.jobapplicatio.dao;

import com.stoonproduction.jobapplicatio.models.JobApplication;
import java.util.List;
import java.util.Optional;

public interface JobApplicationDao {
    JobApplication save(JobApplication application);
    Optional<JobApplication> findById(Long id);
    List<JobApplication> findAll();
    List<JobApplication> findByJobId(Long jobId);
    List<JobApplication> findByJobSeekerId(Long jobSeekerId);
    List<JobApplication> findByStatus(JobApplication.ApplicationStatus status);
    void deleteById(Long id);
    boolean existsByJobIdAndJobSeekerId(Long jobId, Long jobSeekerId);
}