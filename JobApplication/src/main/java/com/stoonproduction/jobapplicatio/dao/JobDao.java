package com.stoonproduction.jobapplicatio.dao;

import com.stoonproduction.jobapplicatio.models.Job;
import java.util.List;
import java.util.Optional;

public interface JobDao {
    Job save(Job job);
    Optional<Job> findById(Long id);
    List<Job> findAll();
    List<Job> findByCompanyId(Long companyId);
    List<Job> findByPostedBy(Long employerId);
    List<Job> findByTitleContaining(String title);
    List<Job> findByLocation(String location);
    List<Job> findBySalaryRange(Double minSalary, Double maxSalary);
    void deleteById(Long id);
}