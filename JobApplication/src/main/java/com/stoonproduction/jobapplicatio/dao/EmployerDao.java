package com.stoonproduction.jobapplicatio.dao;

import com.stoonproduction.jobapplicatio.models.Employer;
import com.stoonproduction.jobapplicatio.models.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface EmployerDao extends UserDao {
    List<Employer> findByCompanyId(Long companyId) throws SQLException;
    List<Employer> findAdminsByCompanyId(Long companyId);
    Optional<Employer> findEmployerById(Long userId) throws SQLException;
    @Override
    Employer save(User user) throws SQLException;
}