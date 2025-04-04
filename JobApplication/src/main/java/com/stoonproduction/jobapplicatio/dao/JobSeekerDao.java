package com.stoonproduction.jobapplicatio.dao;

import com.stoonproduction.jobapplicatio.models.Employer;
import com.stoonproduction.jobapplicatio.models.JobSeeker;
import com.stoonproduction.jobapplicatio.models.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface JobSeekerDao extends UserDao {
    Optional<JobSeeker> findJobSeekerById(Long userId) throws SQLException;
    List<JobSeeker> findBySkillsContaining(String skill) throws SQLException;
    List<JobSeeker> findByNameContaining(String name) throws SQLException;
    @Override
    JobSeeker save(User user) throws SQLException;
}