/*
package com.stoonproduction.jobapplicatio.controllers;

import com.stoonproduction.jobapplicatio.dao.JobSeekerDao;
import com.stoonproduction.jobapplicatio.dao.UserDao;
import com.stoonproduction.jobapplicatio.models.JobSeeker;
import com.stoonproduction.jobapplicatio.models.User;
import com.stoonproduction.jobapplicatio.utils.JwtUtil;
import com.stoonproduction.jobapplicatio.utils.PasswordHasher;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class JobSeekerController extends BaseController {
    private final JobSeekerDao jobSeekerDao;
    private final UserDao userDao;

    public JobSeekerController(JobSeekerDao jobSeekerDao, UserDao userDao) {
        this.jobSeekerDao = jobSeekerDao;
        this.userDao = userDao;
    }

    // Unified Job Seeker Registration
    public void handleRegister(HttpExchange exchange) throws IOException {
        try {
            JSONObject request = parseJsonRequest(exchange);

            // Validate required fields
            String email = request.getString("email");
            String password = request.getString("password");
            String name = request.getString("name");

            // Optional fields
            String resumeUrl = request.optString("resumeUrl", null);
            String skills = request.optString("skills", null);

            // Check if email already exists
            if (userDao.existsByEmail(email)) {
                sendErrorResponse(exchange, 400, "Email already registered");
                return;
            }

            // 1. Create base User
            User newUser = new User(
                    email,
                    PasswordHasher.hashPassword(password),
                    User.UserRole.JOB_SEEKER
            );
            userDao.save(newUser);

            // 2. Create JobSeeker profile
            JobSeeker jobSeeker = new JobSeeker(
                    newUser.getId(),
                    newUser.getEmail(),
                    newUser.getPassword(),
                    newUser.getRole(),
                    LocalDateTime.now(),
                    newUser.getId(),  // userId
                    name,
                    resumeUrl,
                    skills
            );
            jobSeekerDao.save(jobSeeker);

            // 3. Generate JWT token
            String token = JwtUtil.generateToken(email, User.UserRole.JOB_SEEKER);

            // 4. Prepare response
            JSONObject response = new JSONObject()
                    .put("status", "success")
                    .put("token", token)
                    .put("profile", new JSONObject()
                            .put("id", jobSeeker.getId())
                            .put("name", jobSeeker.getName())
                            .put("email", jobSeeker.getEmail())
                            .put("resumeUrl", jobSeeker.getResumeUrl())
                            .put("skills", jobSeeker.getSkills())
                    );

            sendJsonResponse(exchange, 201, response);

        } catch (SQLException e) {
            sendErrorResponse(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(exchange, 400, "Invalid request: " + e.getMessage());
        }
    }

    // ... (keep other methods from previous implementation)
}*/
