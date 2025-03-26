package com.stoonproduction.jobapplicatio.dao;

import com.stoonproduction.jobapplicatio.models.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UserDao {
    User save(User user) throws SQLException;

    User update(User user) throws SQLException;

    Optional<User> findById(Long id) throws SQLException;

    Optional<User> findByEmail(String email) throws SQLException;

    List<User> findAll() throws SQLException;

    void deleteById(Long id) throws SQLException;

    boolean existsByEmail(String email) throws SQLException;
}