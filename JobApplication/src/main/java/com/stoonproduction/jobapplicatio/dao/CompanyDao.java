package com.stoonproduction.jobapplicatio.dao;

import com.stoonproduction.jobapplicatio.models.Company;
import java.util.List;
import java.util.Optional;

public interface CompanyDao {
    Company save(Company company);
    Optional<Company> findById(Long id);
    List<Company> findAll();
    List<Company> findByNameContaining(String name);
    void deleteById(Long id);
    boolean existsByName(String name);
}