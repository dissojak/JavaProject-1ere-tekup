package com.stoonproduction.jobapplicatio.dao;

import com.stoonproduction.jobapplicatio.models.JobApplication;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface JobApplicationDao {
    JobApplication save(JobApplication application)throws SQLException;
    Optional<JobApplication> findById(Long id)throws SQLException;
    List<JobApplication> findAll()throws SQLException;
    List<JobApplication> findByJobId(Long jobId)throws SQLException;
    List<JobApplication> findByJobSeekerId(Long jobSeekerId)throws SQLException;
    List<JobApplication> findByStatus(JobApplication.ApplicationStatus status)throws SQLException;
    void deleteById(Long id)throws SQLException;
    boolean existsByJobIdAndJobSeekerId(Long jobId, Long jobSeekerId)throws SQLException;
    JobApplication update(JobApplication application) throws SQLException;
}