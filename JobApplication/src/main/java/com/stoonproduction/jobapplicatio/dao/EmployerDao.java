package com.stoonproduction.jobapplicatio.dao;

import com.stoonproduction.jobapplicatio.models.Employer;
import java.util.List;
import java.util.Optional;

public interface EmployerDao extends UserDao {
    List<Employer> findByCompanyId(Long companyId);
    List<Employer> findAdminsByCompanyId(Long companyId);
    Optional<Employer> findEmployerById(Long userId);
}