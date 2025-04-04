package com.stoonproduction.jobapplicatio.dao;

import com.stoonproduction.jobapplicatio.models.Company;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CompanyDao {
    Company save(Company company) throws SQLException;
    Optional<Company> findById(Long id) throws SQLException;
    List<Company> findAll() throws SQLException;
    List<Company> findByNameContaining(String name) throws SQLException;
}