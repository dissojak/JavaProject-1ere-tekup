package com.stoonproduction.jobapplicatio.controllers;

import com.stoonproduction.jobapplicatio.config.DatabaseConfig;
import com.stoonproduction.jobapplicatio.dao.EmployerDao;
import com.stoonproduction.jobapplicatio.dao.JobSeekerDao;
import com.stoonproduction.jobapplicatio.dao.UserDao;
import com.stoonproduction.jobapplicatio.models.Employer;
import com.stoonproduction.jobapplicatio.models.JobSeeker;
import com.stoonproduction.jobapplicatio.models.User;
import com.stoonproduction.jobapplicatio.utils.JwtUtil;
import com.stoonproduction.jobapplicatio.utils.PasswordHasher;
import com.stoonproduction.jobapplicatio.utils.AuthMiddleware;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserController extends BaseController implements HttpHandler {

    private final UserDao userDao = new UserDao() {

        @Override
        public User save(User user) throws SQLException {
            try (Connection connection = DatabaseConfig.getConnection()) {
                String query = "INSERT INTO users (email, password, role) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, user.getEmail());
                    stmt.setString(2, user.getPassword());
                    stmt.setString(3, user.getRole().name());

                    int affectedRows = stmt.executeUpdate();
                    if (affectedRows > 0) {
                        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                user.setId(generatedKeys.getLong(1));
                            }
                        }
                    }
                    return user;
                }
            }
        }

        @Override
        public User update(User user) throws SQLException {
            try (Connection connection = DatabaseConfig.getConnection()) {
                String query = "UPDATE users SET email = ?, password = ?, role = ? WHERE id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setString(1, user.getEmail());
                    stmt.setString(2, user.getPassword());
                    stmt.setString(3, user.getRole().name());
                    stmt.setLong(4, user.getId());

                    int affectedRows = stmt.executeUpdate();
                    if (affectedRows > 0) {
                        return user;
                    } else {
                        return null;
                    }
                }
            }
        }

        @Override
        public Optional<User> findById(Long id) throws SQLException {
            try (Connection conn = DatabaseConfig.getConnection()) {
                // First get base user data
                String baseQuery = "SELECT * FROM users WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(baseQuery)) {
                    stmt.setLong(1, id);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        // Get user role
                        User.UserRole role = User.UserRole.valueOf(rs.getString("role"));

                        // Handle different user types
                        if (role == User.UserRole.EMPLOYER) {
                            return employerDao.findEmployerById(id).map(e -> (User) e);
                        } else if (role == User.UserRole.JOB_SEEKER) {
                            return jobSeekerDao.findJobSeekerById(id).map(js -> (User) js);
                        } else {
                            // Handle other roles or default case
                            return Optional.of(createBaseUser(rs));
                        }
                    }
                    return Optional.empty();
                }
            }
        }

        private User createBaseUser(ResultSet rs) throws SQLException {
            User user = new User(
                    rs.getString("email"),
                    rs.getString("password"),
                    User.UserRole.valueOf(rs.getString("role"))
            );
            user.setId(rs.getLong("id"));
            return user;
        }

        @Override
        public Optional<User> findByEmail(String email) throws SQLException {
            try (Connection connection = DatabaseConfig.getConnection()) {
                String query = "SELECT * FROM users WHERE email = ?";
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setString(1, email);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        String foundEmail = rs.getString("email");
                        String password = rs.getString("password");
                        User.UserRole role = User.UserRole.valueOf(rs.getString("role"));
                        User user = new User(foundEmail, password, role);
                        user.setId(rs.getLong("id"));
                        return Optional.of(user);
                    }
                    return Optional.empty();
                }
            }
        }

        @Override
        public boolean existsByEmail(String email) throws SQLException {
            try (Connection connection = DatabaseConfig.getConnection()) {
                String query = "SELECT 1 FROM users WHERE email = ?";
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setString(1, email);
                    ResultSet rs = stmt.executeQuery();
                    System.out.println("[DEBUG] Executed query");
                    boolean exists = rs.next();
                    return exists;
                }
            } catch (SQLException e) {
                System.err.println("[ERROR] Database error in existsByEmail: " + e.getMessage());
                throw e;
            }
        }

        @Override
        public List<User> findAll() throws SQLException {
            return null;
        }

        @Override
        public void deleteById(Long id) throws SQLException {
        }
    };

    private final EmployerDao employerDao = new EmployerDao() {
        // SAVE - Returns Employer instead of User
        @Override
        public Employer save(User user) throws SQLException {
            if (!(user instanceof Employer)) {
                throw new IllegalArgumentException("Must provide Employer instance");
            }
            Employer employer = (Employer) user;

            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                conn.setAutoCommit(false);

                // 1. Save base User data
                String userSql = "INSERT INTO users (email, password, role) VALUES (?, ?, ?)";
                try (PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                    userStmt.setString(1, employer.getEmail());
                    userStmt.setString(2, employer.getPassword());
                    userStmt.setString(3, employer.getRole().name());
                    userStmt.executeUpdate();

                    try (ResultSet rs = userStmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            employer.setId(rs.getLong(1));
                        }
                    }
                }

                // 2. Save Employer-specific data
                String employerSql = "INSERT INTO employers (user_id, name, company_id, is_admin) VALUES (?, ?, ?, ?)";
                try (PreparedStatement employerStmt = conn.prepareStatement(employerSql)) {
                    employerStmt.setLong(1, employer.getId());
                    employerStmt.setString(2, employer.getName());
                    employerStmt.setLong(3, employer.getCompanyId());
                    employerStmt.setBoolean(4, employer.isAdmin());
                    employerStmt.executeUpdate();
                }

                conn.commit();
                return employer;
            } catch (SQLException e) {
                if (conn != null) conn.rollback();
                throw e;
            } finally {
                if (conn != null) conn.setAutoCommit(true);
            }
        }

        // UPDATE - Handles Employer-specific fields
        @Override
        public Employer update(User user) throws SQLException {
            if (!(user instanceof Employer)) {
                throw new IllegalArgumentException("Must provide Employer instance");
            }
            Employer employer = (Employer) user;

            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                conn.setAutoCommit(false);

                // 1. Update base User data
                String userSql = "UPDATE users SET email = ?, password = ? WHERE id = ?";
                try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                    userStmt.setString(1, employer.getEmail());
                    userStmt.setString(2, employer.getPassword());
                    userStmt.setLong(3, employer.getId());
                    userStmt.executeUpdate();
                }

                // 2. Update Employer-specific data
                String employerSql = "UPDATE employers SET name = ?, company_id = ?, is_admin = ? WHERE user_id = ?";
                try (PreparedStatement employerStmt = conn.prepareStatement(employerSql)) {
                    employerStmt.setLong(2, employer.getCompanyId());
                    employerStmt.setBoolean(3, employer.isAdmin());
                    employerStmt.setLong(4, employer.getId());
                    employerStmt.executeUpdate();
                }

                conn.commit();
                return employer;
            } catch (SQLException e) {
                if (conn != null) conn.rollback();
                throw e;
            } finally {
                if (conn != null) conn.setAutoCommit(true);
            }
        }

        // FIND BY ID - Returns Employer with all fields
        @Override
        public Optional<User> findById(Long id) throws SQLException {
            try (Connection conn = DatabaseConfig.getConnection()) {
                // Join users and employers tables
                String sql = "SELECT u.*, e.name, e.company_id, e.is_admin " +
                        "FROM users u JOIN employers e ON u.id = e.user_id " +
                        "WHERE u.id = ?";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, id);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        return Optional.of(new Employer(
                                rs.getLong("id"),
                                rs.getString("email"),
                                rs.getString("password"),
                                User.UserRole.valueOf(rs.getString("role")),
                                rs.getTimestamp("created_at").toLocalDateTime(),
                                rs.getString("name"),
                                rs.getLong("company_id"),
                                rs.getBoolean("is_admin")
                        ));
                    }
                }
            }
            return Optional.empty();
        }

        @Override
        public Optional<User> findByEmail(String email) throws SQLException {
            return Optional.empty();
        }

        @Override
        public List<User> findAll() throws SQLException {
            return List.of();
        }

        @Override
        public void deleteById(Long id) throws SQLException {

        }

        @Override
        public boolean existsByEmail(String email) throws SQLException {
            return false;
        }

        // Other methods implementing EmployerDao interface
        @Override
        public Optional<Employer> findEmployerById(Long userId) throws SQLException {
            try (Connection conn = DatabaseConfig.getConnection()) {
                String sql = "SELECT u.*, e.* FROM users u JOIN employers e ON u.id = e.user_id WHERE u.id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, userId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        return Optional.of(mapToEmployer(rs));
                    }
                    return Optional.empty();
                }
            }
        }

        private Employer mapToEmployer(ResultSet rs) throws SQLException {
            Employer employer = new Employer(
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("name"),
                    rs.getLong("company_id"),
                    rs.getBoolean("is_admin")
            );
            employer.setId(rs.getLong("id"));
            employer.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return employer;
        }

        @Override
        public List<Employer> findByCompanyId(Long companyId) throws SQLException {
            List<Employer> employers = new ArrayList<>();
            try (Connection conn = DatabaseConfig.getConnection()) {
                String sql = "SELECT u.*, e.name, e.company_id, e.is_admin " +
                        "FROM users u JOIN employers e ON u.id = e.user_id " +
                        "WHERE e.company_id = ?";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, companyId);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        employers.add(new Employer(
                                rs.getLong("id"),
                                rs.getString("email"),
                                rs.getString("password"),
                                User.UserRole.valueOf(rs.getString("role")),
                                rs.getTimestamp("created_at").toLocalDateTime(),
                                rs.getString("name"),
                                rs.getLong("company_id"),
                                rs.getBoolean("is_admin")
                        ));
                    }
                }
            }
            return employers;
        }

        @Override
        public List<Employer> findAdminsByCompanyId(Long companyId) {
            return List.of();
        }

        // ... [implement other required methods] ...
    };

    private final JobSeekerDao jobSeekerDao = new JobSeekerDao() {
        // SAVE - Handles both User and JobSeeker data
        @Override
        public JobSeeker save(User user) throws SQLException {
            if (!(user instanceof JobSeeker)) {
                throw new IllegalArgumentException("Must provide JobSeeker instance");
            }
            JobSeeker seeker = (JobSeeker) user;

            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                conn.setAutoCommit(false);

                // 1. Save base User data
                String userSql = "INSERT INTO users (email, password, role) VALUES (?, ?, ?)";
                try (PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                    userStmt.setString(1, seeker.getEmail());
                    userStmt.setString(2, seeker.getPassword());
                    userStmt.setString(3, seeker.getRole().name());
                    userStmt.executeUpdate();

                    try (ResultSet rs = userStmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            seeker.setId(rs.getLong(1));
                        }
                    }
                }

                // 2. Save JobSeeker-specific data
                String seekerSql = "INSERT INTO job_seekers (user_id, name, resume_url, skills) VALUES (?, ?, ?, ?)";
                try (PreparedStatement seekerStmt = conn.prepareStatement(seekerSql)) {
                    seekerStmt.setLong(1, seeker.getId());
                    seekerStmt.setString(2, seeker.getName());
                    seekerStmt.setString(3, seeker.getResumeUrl());
                    seekerStmt.setString(4, seeker.getSkills());
                    seekerStmt.executeUpdate();
                }

                conn.commit();
                return seeker;
            } catch (SQLException e) {
                if (conn != null) conn.rollback();
                throw e;
            } finally {
                if (conn != null) conn.setAutoCommit(true);
            }
        }

        // UPDATE - Updates both User and JobSeeker data
        @Override
        public JobSeeker update(User user) throws SQLException {
            if (!(user instanceof JobSeeker)) {
                throw new IllegalArgumentException("Must provide JobSeeker instance");
            }
            JobSeeker seeker = (JobSeeker) user;

            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                conn.setAutoCommit(false);

                // 1. Update base User data
                String userSql = "UPDATE users SET email = ?, password = ? WHERE id = ?";
                try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                    userStmt.setString(1, seeker.getEmail());
                    userStmt.setString(2, seeker.getPassword());
                    userStmt.setLong(3, seeker.getId());
                    userStmt.executeUpdate();
                }

                // 2. Update JobSeeker-specific data
                String seekerSql = "UPDATE job_seekers SET name = ?, resume_url = ?, skills = ? WHERE user_id = ?";
                try (PreparedStatement seekerStmt = conn.prepareStatement(seekerSql)) {
                    seekerStmt.setString(1, seeker.getName());
                    seekerStmt.setString(2, seeker.getResumeUrl());
                    seekerStmt.setString(3, seeker.getSkills());
                    seekerStmt.setLong(4, seeker.getId());
                    seekerStmt.executeUpdate();
                }

                conn.commit();
                return seeker;
            } catch (SQLException e) {
                if (conn != null) conn.rollback();
                throw e;
            } finally {
                if (conn != null) conn.setAutoCommit(true);
            }
        }

        // FIND BY ID - Returns complete JobSeeker
        @Override
        public Optional<User> findById(Long id) throws SQLException {
            try (Connection conn = DatabaseConfig.getConnection()) {
                String sql = "SELECT u.*, js.name, js.resume_url, js.skills " +
                        "FROM users u JOIN job_seekers js ON u.id = js.user_id " +
                        "WHERE u.id = ?";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, id);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        return Optional.of(new JobSeeker(
                                rs.getLong("id"),
                                rs.getString("email"),
                                rs.getString("password"),
                                User.UserRole.valueOf(rs.getString("role")),
                                rs.getTimestamp("created_at").toLocalDateTime(),
                                rs.getLong("id"),
                                rs.getString("name"),
                                rs.getString("resume_url"),
                                rs.getString("skills")
                        ));
                    }
                }
            }
            return Optional.empty();
        }

        // FIND BY EMAIL
        @Override
        public Optional<User> findByEmail(String email) throws SQLException {
            try (Connection conn = DatabaseConfig.getConnection()) {
                String sql = "SELECT u.*, js.name, js.resume_url, js.skills " +
                        "FROM users u JOIN job_seekers js ON u.id = js.user_id " +
                        "WHERE u.email = ?";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, email);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        return Optional.of(new JobSeeker(
                                rs.getLong("id"),
                                rs.getString("email"),
                                rs.getString("password"),
                                User.UserRole.valueOf(rs.getString("role")),
                                rs.getTimestamp("created_at").toLocalDateTime(),
                                rs.getLong("id"),
                                rs.getString("name"),
                                rs.getString("resume_url"),
                                rs.getString("skills")
                        ));
                    }
                }
            }
            return Optional.empty();
        }

        // JOB SEEKER SPECIFIC METHODS
        @Override
        public Optional<JobSeeker> findJobSeekerById(Long userId) throws SQLException {
            return findById(userId).map(js -> (JobSeeker) js);
        }

        @Override
        public List<JobSeeker> findBySkillsContaining(String skill) throws SQLException {
            List<JobSeeker> seekers = new ArrayList<>();
            try (Connection conn = DatabaseConfig.getConnection()) {
                String sql = "SELECT u.*, js.name, js.resume_url, js.skills " +
                        "FROM users u JOIN job_seekers js ON u.id = js.user_id " +
                        "WHERE js.skills LIKE ?";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, "%" + skill + "%");
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        seekers.add(new JobSeeker(
                                rs.getLong("id"),
                                rs.getString("email"),
                                rs.getString("password"),
                                User.UserRole.valueOf(rs.getString("role")),
                                rs.getTimestamp("created_at").toLocalDateTime(),
                                rs.getLong("id"),
                                rs.getString("name"),
                                rs.getString("resume_url"),
                                rs.getString("skills")
                        ));
                    }
                }
            }
            return seekers;
        }

        @Override
        public List<JobSeeker> findByNameContaining(String name) throws SQLException {
            List<JobSeeker> seekers = new ArrayList<>();
            try (Connection conn = DatabaseConfig.getConnection()) {
                String sql = "SELECT u.*, js.name, js.resume_url, js.skills " +
                        "FROM users u JOIN job_seekers js ON u.id = js.user_id " +
                        "WHERE js.name LIKE ?";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, "%" + name + "%");
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        seekers.add(new JobSeeker(
                                rs.getLong("id"),
                                rs.getString("email"),
                                rs.getString("password"),
                                User.UserRole.valueOf(rs.getString("role")),
                                rs.getTimestamp("created_at").toLocalDateTime(),
                                rs.getLong("id"),
                                rs.getString("name"),
                                rs.getString("resume_url"),
                                rs.getString("skills")
                        ));
                    }
                }
            }
            return seekers;
        }

        // OTHER REQUIRED METHODS
        @Override
        public List<User> findAll() throws SQLException {
            List<User> users = new ArrayList<>();
            try (Connection conn = DatabaseConfig.getConnection()) {
                String sql = "SELECT u.*, js.name, js.resume_url, js.skills " +
                        "FROM users u JOIN job_seekers js ON u.id = js.user_id";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        users.add(new JobSeeker(
                                rs.getLong("id"),
                                rs.getString("email"),
                                rs.getString("password"),
                                User.UserRole.valueOf(rs.getString("role")),
                                rs.getTimestamp("created_at").toLocalDateTime(),
                                rs.getLong("id"),
                                rs.getString("name"),
                                rs.getString("resume_url"),
                                rs.getString("skills")
                        ));
                    }
                }
            }
            return users;
        }

        @Override
        public void deleteById(Long id) throws SQLException {
            try (Connection conn = DatabaseConfig.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // Delete from job_seekers first due to foreign key
                    String seekerSql = "DELETE FROM job_seekers WHERE user_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(seekerSql)) {
                        stmt.setLong(1, id);
                        stmt.executeUpdate();
                    }

                    // Then delete from users
                    String userSql = "DELETE FROM users WHERE id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(userSql)) {
                        stmt.setLong(1, id);
                        stmt.executeUpdate();
                    }

                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            }
        }

        @Override
        public boolean existsByEmail(String email) throws SQLException {
            try (Connection conn = DatabaseConfig.getConnection()) {
                String sql = "SELECT 1 FROM users WHERE email = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, email);
                    return stmt.executeQuery().next();
                }
            }
        }
    };

    private final AuthMiddleware authMiddleware;

    public UserController() {
        this.authMiddleware = new AuthMiddleware(userDao);
    }

    // Register User
    public void handleRegister(HttpExchange exchange) throws IOException {
        // Debugging
        System.out.println("Request method: " + exchange.getRequestMethod());
        System.out.println("Request URI: " + exchange.getRequestURI());
        System.out.println("Request headers: " + exchange.getRequestHeaders());


        try {
            JSONObject request = parseJsonRequest(exchange);
            String email = request.getString("email");

            /*System.out.println("this been handled");// Debugging*/
            //Email Validate Input
            if (email == null || email.isEmpty()) {
                authMiddleware.sendErrorResponse(exchange, 402, "Email is required");
                return;
            }

            /*System.out.println("handeling the check of email existence");// Debugging*/
            // Email existence check
            if (userDao.existsByEmail(email)) {
                System.out.println("Duplicate email detected");
                authMiddleware.sendErrorResponse(exchange, 400, "Email already registered");
                return;
            }

            System.out.println("handeling the registration");
            String userType = request.getString("userType");
            switch (userType.toLowerCase()) {
                case "job-seeker":
                    registerJobSeeker(exchange, request);
                    break;
                case "employer":
                    registerEmployer(exchange, request);
                    break;
                default:
                    authMiddleware.sendErrorResponse(exchange, 400, "Invalid user type");
            }
        } catch (Exception e) {
            authMiddleware.sendErrorResponse(exchange, 400, "Registration failed: " + e.getMessage());
        }
    }

    private void registerJobSeeker(HttpExchange exchange, JSONObject request) throws IOException {
        System.out.println("Registering JobSeeker with data: " + request.toString());

        try {
            // Common user data
            String email = request.getString("email");
            String password = request.getString("password");

            // Job seeker specific data
            String name = request.getString("name");
            String resumeUrl = request.optString("resumeUrl", null);
            String skills = request.optString("skills", null);

            // Create and save using JobSeekerDao directly
            JobSeeker jobSeeker = new JobSeeker(
                    email,
                    PasswordHasher.hashPassword(password),
                    name,
                    resumeUrl,
                    skills
            );

            // Save through JobSeekerDao (handles both tables)
            JobSeeker savedJobSeeker = jobSeekerDao.save(jobSeeker);

            System.out.println("Successfully saved jobSeeker with ID: " + savedJobSeeker.getId());
            sendRegistrationResponse(exchange, savedJobSeeker, savedJobSeeker);
        } catch (SQLException e) {
            System.err.println("Error saving job seeker: " + e.getMessage());
            e.printStackTrace();
            authMiddleware.sendErrorResponse(exchange, 500, "Registration failed");
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            authMiddleware.sendErrorResponse(exchange, 500, "Registration failed");
        }
    }

    private void registerEmployer(HttpExchange exchange, JSONObject request) throws SQLException, IOException {

        System.out.println("Registering employer with data: " + request.toString());
        // Common user data
        String email = request.getString("email");
        String password = request.getString("password");

        // Employer specific data
        String name = request.getString("name");
        Long companyId = request.optLong("companyId", 0);
        boolean isAdmin = request.optBoolean("isAdmin", false);

        // Create and save using EmployerDao directly
        Employer employer = new Employer(
                email,
                PasswordHasher.hashPassword(password),
                name,
                companyId,
                isAdmin
        );

        // Save through EmployerDao (handles both tables)
        try {
            Employer savedEmployer = employerDao.save(employer);
            System.out.println("Successfully saved employer with ID: " + savedEmployer.getId());
            sendRegistrationResponse(exchange, savedEmployer, savedEmployer);
        } catch (SQLException e) {
            System.err.println("Error saving employer: " + e.getMessage());
            e.printStackTrace();
            authMiddleware.sendErrorResponse(exchange, 500, "Registration failed");
        }
    }

    private void sendRegistrationResponse(HttpExchange exchange, User user, Object profile) throws IOException {
        String token = JwtUtil.generateToken(user.getEmail(), user.getRole());

        JSONObject response = new JSONObject()
                .put("status", "success")
                .put("token", token)
                .put("userType", user.getRole().toString().toLowerCase())
                .put("userId", user.getId());

        sendJsonResponse(exchange, 201, response);
    }

    public void handleLogin(HttpExchange exchange) throws IOException {
        try {
            JSONObject request = parseJsonRequest(exchange);
            String email = request.getString("email");
            String password = request.getString("password");

            System.out.println("Login attempt for email: " + email);

            // Get base user info
            Optional<User> userOpt = userDao.findByEmail(email);
            if (!userOpt.isPresent()) {
                System.out.println("User not found for email: " + email);
                sendJsonResponse(exchange, 404, new JSONObject().put("error", "User not found"));
                return;
            }

            User baseUser = userOpt.get();
            System.out.println("Base user found: " + baseUser.getEmail() + ", Role: " + baseUser.getRole());

            // Verify password
            if (!PasswordHasher.verifyPassword(password, baseUser.getPassword())) {
                System.out.println("Password mismatch for email: " + email);
                sendJsonResponse(exchange, 401, new JSONObject().put("error", "Invalid credentials"));
                return;
            }

            // Get full user info based on role
            JSONObject userInfo = new JSONObject()
                    .put("id", baseUser.getId())
                    .put("email", baseUser.getEmail())
                    .put("role", baseUser.getRole().toString());

            if (baseUser.getRole() == User.UserRole.EMPLOYER) {
                Optional<Employer> employerOpt = employerDao.findEmployerById(baseUser.getId());
                if (employerOpt.isPresent()) {
                    Employer employer = employerOpt.get();
                    userInfo
                            .put("name", employer.getName())
                            .put("companyId", employer.getCompanyId())
                            .put("isAdmin", employer.isAdmin());
                }
            } else if (baseUser.getRole() == User.UserRole.JOB_SEEKER) {
                Optional<JobSeeker> seekerOpt = jobSeekerDao.findJobSeekerById(baseUser.getId());
                if (seekerOpt.isPresent()) {
                    JobSeeker seeker = seekerOpt.get();
                    userInfo
                            .put("name", seeker.getName())
                            .put("resumeUrl", seeker.getResumeUrl())
                            .put("skills", seeker.getSkills());
                }
            }

            // Generate token
            String token = JwtUtil.generateToken(baseUser.getEmail(), baseUser.getRole());
            System.out.println("Token generated for user: " + email);

            // Prepare response
            JSONObject response = new JSONObject()
                    .put("status", "success")
                    .put("token", token)
                    .put("user", userInfo);

            sendJsonResponse(exchange, 200, response);

        } catch (SQLException e) {
            System.err.println("Database error during login: " + e.getMessage());
            sendJsonResponse(exchange, 500, new JSONObject().put("error", "Database error"));
        } catch (Exception e) {
            System.err.println("Unexpected error during login: " + e.getMessage());
            sendJsonResponse(exchange, 500, new JSONObject().put("error", "Internal server error"));
        }
    }

    public void handleUpdateUser(HttpExchange exchange) throws IOException {
        try {
            // Changed this line to use instance authMiddleware
            User currentUser = authMiddleware.authenticate(exchange);
            if (currentUser == null) return;

            JSONObject request = parseJsonRequest(exchange);
            long userId = request.getLong("userId");

            if (currentUser.getId() != userId && currentUser.getRole() != User.UserRole.SUPER_ADMIN) {
                sendJsonResponse(exchange, 403, new JSONObject().put("error", "You can only update your own account"));
                return;
            }

            String newEmail = request.optString("email", null);
            String newPassword = request.optString("password", null);
            String newRoleString = request.optString("role", null);
            User.UserRole newRole = null;

            if (newRoleString != null) {
                System.out.println("Current user role: " + currentUser.getRole()); // Debug log
                if (currentUser.getRole() != User.UserRole.SUPER_ADMIN) {
                    System.out.println("BLOCKED: User is not admin!"); // Debug log
                    sendJsonResponse(exchange, 403, new JSONObject().put("error", "Only admins can change roles"));
                    return; // This should stop execution
                }
                newRole = User.UserRole.valueOf(newRoleString.toUpperCase());
            }

            Optional<User> existingUserOpt = userDao.findById(userId);
            if (!existingUserOpt.isPresent()) {
                sendJsonResponse(exchange, 404, new JSONObject().put("error", "User not found"));
                return;
            }

            User existingUser = existingUserOpt.get();

            if (newEmail != null && !newEmail.equals(existingUser.getEmail())) {
                if (userDao.existsByEmail(newEmail)) {
                    sendJsonResponse(exchange, 400, new JSONObject().put("error", "Email already in use"));
                    return;
                }
                existingUser.setEmail(newEmail);
            }

            if (newPassword != null && !newPassword.isEmpty()) {
                existingUser.setPassword(PasswordHasher.hashPassword(newPassword));
            }

            if (newRole != null) {
                existingUser.setRole(newRole);
            }

            userDao.update(existingUser);

            JSONObject response = new JSONObject()
                    .put("status", "success")
                    .put("userId", existingUser.getId());
            sendJsonResponse(exchange, 200, response);

        } catch (SQLException e) {
            sendJsonResponse(exchange, 500, new JSONObject().put("error", "Database error: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            sendJsonResponse(exchange, 400, new JSONObject().put("error", "Invalid role specified"));
        } catch (Exception e) {
            sendJsonResponse(exchange, 500, new JSONObject().put("error", "Internal server error"));
        }
    }

    public void handleDeleteUser(HttpExchange exchange) throws IOException {
        try {
            // Added authentication check
            User currentUser = authMiddleware.authenticate(exchange);
            if (currentUser == null) return;

            JSONObject request = parseJsonRequest(exchange);
            long userId = request.getLong("userId");

            if (currentUser.getId() != userId && currentUser.getRole() != User.UserRole.SUPER_ADMIN) {
                sendJsonResponse(exchange, 403, new JSONObject().put("error", "You can only delete your own account"));
                return;
            }

            userDao.deleteById(userId);
            sendJsonResponse(exchange, 200, new JSONObject().put("status", "success"));

        } catch (SQLException e) {
            sendJsonResponse(exchange, 500, new JSONObject().put("error", "Database error: " + e.getMessage()));
        } catch (Exception e) {
            sendJsonResponse(exchange, 500, new JSONObject().put("error", "Internal server error"));
        }
    }

    public void handleGetUser(HttpExchange exchange) throws IOException {
        try {
            User currentUser = authMiddleware.authenticate(exchange);
            if (currentUser == null) return;

            long userId = currentUser.getId();

            Optional<User> userOpt = userDao.findById(userId);
            if (!userOpt.isPresent()) {
                sendJsonResponse(exchange, 404, new JSONObject().put("error", "User not found"));
                return;
            }

            JSONObject response = new JSONObject()
                    .put("status", "success")
                    .put("user", new JSONObject()
                            .put("id", userOpt.get().getId())
                            .put("email", userOpt.get().getEmail())
                            .put("role", userOpt.get().getRole().toString()));

            sendJsonResponse(exchange, 200, response);

        } catch (SQLException e) {
            sendJsonResponse(exchange, 500, new JSONObject().put("error", "Database error: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // this will help you debug what exactly is failing
            sendJsonResponse(exchange, 500, new JSONObject().put("error", "Internal server error"));
        }
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {}
}