package com.stoonproduction.jobapplicatio.dao;

import com.stoonproduction.jobapplicatio.models.Job;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface JobDao {
    Job save(Job job) throws SQLException;
    Optional<Job> findById(Long id) throws SQLException ;
    List<Job> findAll() throws SQLException ;
    List<Job> findByPostedBy(Long employerId) throws SQLException ;
    List<Job> findByTitleContaining(String title) throws SQLException ;
    List<Job> findByLocation(String location) throws SQLException ;
    List<Job> findBySalary(Double salary) throws SQLException ;
    Optional<Job> findByTitleAndPostedBy(String title, long postedBy) throws SQLException ;
    void deleteById(Long id) throws SQLException ;
}