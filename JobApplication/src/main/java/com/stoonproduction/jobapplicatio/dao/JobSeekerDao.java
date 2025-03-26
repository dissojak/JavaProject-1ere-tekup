package com.stoonproduction.jobapplicatio.dao;

import com.stoonproduction.jobapplicatio.models.JobSeeker;
import java.util.List;
import java.util.Optional;

public interface JobSeekerDao extends UserDao {
    Optional<JobSeeker> findJobSeekerById(Long userId);
    List<JobSeeker> findBySkillsContaining(String skill);
    List<JobSeeker> findByNameContaining(String name);
}