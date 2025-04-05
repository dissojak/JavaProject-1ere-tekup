package com.stoonproduction.jobapplicatio.controllers;

import com.stoonproduction.jobapplicatio.config.DatabaseConfig;
import com.stoonproduction.jobapplicatio.dao.JobDao;
import com.stoonproduction.jobapplicatio.models.Job;
import com.stoonproduction.jobapplicatio.models.User;
import com.stoonproduction.jobapplicatio.utils.AuthMiddleware;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JobOfferController extends BaseController implements HttpHandler {

    private final JobDao jobDao = new JobDao() {
        @Override
        public Job save(Job job) throws SQLException {
            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                conn.setAutoCommit(false);

                String sql = "INSERT INTO jobs (title, description, location, salary, posted_by, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, job.getTitle());
                    stmt.setString(2, job.getDescription());
                    stmt.setString(3, job.getLocation());
                    stmt.setDouble(4, job.getSalary());
                    stmt.setLong(5, job.getPostedBy());
                    stmt.setTimestamp(6, Timestamp.valueOf(job.getCreatedAt()));
                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            job.setId(rs.getLong(1));
                        }
                    }
                }

                conn.commit();
                return job;
            } catch (SQLException e) {
                if (conn != null) conn.rollback();
                throw e;
            } finally {
                if (conn != null) conn.setAutoCommit(true);
            }
        }

        @Override
        public Optional<Job> findById(Long id) throws SQLException {
            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                String sql = "SELECT * FROM jobs WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, id);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return Optional.of(mapJobFromResultSet(rs));
                        }
                    }
                }
            } finally {
                if (conn != null) conn.close();
            }
            return Optional.empty();
        }

        @Override
        public List<Job> findAll() throws SQLException {
            List<Job> jobs = new ArrayList<>();
            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                String sql = "SELECT * FROM jobs";
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        jobs.add(mapJobFromResultSet(rs));
                    }
                }
            } finally {
                if (conn != null) conn.close();
            }
            return jobs;
        }

        @Override
        public List<Job> findByPostedBy(Long employerId) throws SQLException {
            List<Job> jobs = new ArrayList<>();
            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                String sql = "SELECT * FROM jobs WHERE posted_by = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, employerId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            jobs.add(mapJobFromResultSet(rs));
                        }
                    }
                }
            } finally {
                if (conn != null) conn.close();
            }
            return jobs;
        }

        @Override
        public List<Job> findByTitleContaining(String title) throws SQLException {
            List<Job> jobs = new ArrayList<>();
            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                String sql = "SELECT * FROM jobs WHERE title LIKE ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, "%" + title + "%");
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            jobs.add(mapJobFromResultSet(rs));
                        }
                    }
                }
            } finally {
                if (conn != null) conn.close();
            }
            return jobs;
        }

        @Override
        public List<Job> findByLocation(String location) throws SQLException {
            List<Job> jobs = new ArrayList<>();
            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                String sql = "SELECT * FROM jobs WHERE location = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, location);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            jobs.add(mapJobFromResultSet(rs));
                        }
                    }
                }
            } finally {
                if (conn != null) conn.close();
            }
            return jobs;
        }

        @Override
        public List<Job> findBySalary(Double salary) throws SQLException {
            List<Job> jobs = new ArrayList<>();
            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                String sql = "SELECT * FROM jobs WHERE salary >= ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setDouble(1, salary);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            jobs.add(mapJobFromResultSet(rs));
                        }
                    }
                }
            } finally {
                if (conn != null) conn.close();
            }
            return jobs;
        }

        @Override
        public void deleteById(Long id) throws SQLException {
            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                conn.setAutoCommit(false);

                String sql = "DELETE FROM jobs WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, id);
                    stmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                if (conn != null) conn.rollback();
                throw e;
            } finally {
                if (conn != null) conn.setAutoCommit(true);
            }
        }

        @Override
        public Optional<Job> findByTitleAndPostedBy(String title, long postedBy) throws SQLException {
            String query = "SELECT * FROM jobs WHERE title = ? AND posted_by = ?";

            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, title);
                stmt.setLong(2, postedBy);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return Optional.of(mapJobFromResultSet(rs));
                } else {
                    return Optional.empty();
                }
            }
        }

        private Job mapJobFromResultSet(ResultSet rs) throws SQLException {
            Job job = new Job(
                    rs.getLong("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("location"),
                    rs.getDouble("salary"),
                    rs.getLong("posted_by"),
                    rs.getTimestamp("created_at").toLocalDateTime()
            );
            return job;
        }
    };

    private JSONObject jobToJson(Job job) {
        return new JSONObject()
                .put("id", job.getId())
                .put("title", job.getTitle())
                .put("description", job.getDescription())
                .put("location", job.getLocation())
                .put("salary", job.getSalary())
                .put("postedBy", job.getPostedBy())
                .put("createdAt", job.getCreatedAt().toString());
    }

    private final AuthMiddleware authMiddleware;

    public JobOfferController() {
        this.authMiddleware = new AuthMiddleware(UserController.userDao);
    }

    public void handleGetJobById(HttpExchange exchange) throws IOException {
        try {
            JSONObject request = parseJsonRequest(exchange);
            if (!request.has("id")) {
                sendJsonResponse(exchange, 400, new JSONObject().put("error", "Missing job ID"));
                return;
            }

            long id = request.getLong("id");
            Optional<Job> jobOpt = jobDao.findById(id);
            if (jobOpt.isEmpty()) {
                sendJsonResponse(exchange, 404, new JSONObject().put("error", "Job not found"));
                return;
            }

            JSONObject response = new JSONObject()
                    .put("status", "success")
                    .put("job", jobToJson(jobOpt.get()));

            sendJsonResponse(exchange, 200, response);
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject().put("error", "Failed to fetch job"));
        }
    }

    public void handleListJobs(HttpExchange exchange) throws IOException {
        try {

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 500, new JSONObject().put("error", "Failed to list jobs , Wrong Methode Used In request !"));
                return;
            }

            List<Job> jobs = jobDao.findAll();
            JSONArray jobsArray = new JSONArray();
            for (Job job : jobs) {
                jobsArray.put(jobToJson(job));
            }

            sendJsonResponse(exchange, 200, new JSONObject()
                    .put("status", "success")
                    .put("jobs", jobsArray));
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject().put("error", "Failed to list jobs"));
        }
    }

    public void handleSearchJobsByTitle(HttpExchange exchange) throws IOException {
        try {
            JSONObject request = parseJsonRequest(exchange);
            if (!request.has("title")) {
                sendJsonResponse(exchange, 400, new JSONObject().put("error", "Missing title parameter"));
                return;
            }

            String title = request.getString("title");
            List<Job> jobs = jobDao.findByTitleContaining(title);

            JSONArray jobsArray = new JSONArray();
            for (Job job : jobs) {
                jobsArray.put(jobToJson(job));
            }

            sendJsonResponse(exchange, 200, new JSONObject()
                    .put("status", "success")
                    .put("jobs", jobsArray));
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject().put("error", "Search failed"));
        }
    }

    public void handleSearchJobsByLocation(HttpExchange exchange) throws IOException {
        try {
            JSONObject request = parseJsonRequest(exchange);
            if (!request.has("location")) {
                sendJsonResponse(exchange, 400, new JSONObject().put("error", "Missing location parameter"));
                return;
            }

            String location = request.getString("location");
            List<Job> jobs = jobDao.findByLocation(location);

            JSONArray jobsArray = new JSONArray();
            for (Job job : jobs) {
                jobsArray.put(jobToJson(job));
            }

            sendJsonResponse(exchange, 200, new JSONObject()
                    .put("status", "success")
                    .put("jobs", jobsArray));
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject().put("error", "Search failed"));
        }
    }

    public void handleSearchJobsBySalary(HttpExchange exchange) throws IOException {
        try {
            JSONObject request = parseJsonRequest(exchange);
            if (!request.has("salary")) {
                sendJsonResponse(exchange, 400, new JSONObject().put("error", "Missing salary parameter"));
                return;
            }

            Double salary = request.getDouble("salary");
            List<Job> jobs = jobDao.findBySalary(salary);

            JSONArray jobsArray = new JSONArray();
            for (Job job : jobs) {
                jobsArray.put(jobToJson(job));
            }

            sendJsonResponse(exchange, 200, new JSONObject()
                    .put("status", "success")
                    .put("jobs", jobsArray));
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject().put("error", "Search failed"));
        }
    }

    public void handleCreateJob(HttpExchange exchange) throws IOException {

        if (!"POST_JOBAPP".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            return;
        }

        User currentUser = authMiddleware.authenticate(exchange);
        if (currentUser == null) return;

        try {
            JSONObject request = parseJsonRequest(exchange);

            // Validate required fields
            if (!request.has("title") || !request.has("postedBy")) {
                sendJsonResponse(exchange, 400, new JSONObject()
                        .put("error", "Missing required fields (title and postedBy are required)"));
                return;
            }

            long postedBy = request.getLong("postedBy");

            // Verify token matches the postedBy user
            if (currentUser.getId() != postedBy) {
                sendJsonResponse(exchange, 403, new JSONObject()
                        .put("error", "You can only create jobs for your own account"));
                return;
            }

            // Check if user is an admin employer
            if (!isAdminEmployer(postedBy)) {
                sendJsonResponse(exchange, 403, new JSONObject()
                        .put("error", "Only admin employers can create job offers"));
                return;
            }

            String title = request.getString("title");

            // Check if job already exists
            Optional<Job> existingJob = jobDao.findByTitleAndPostedBy(title, postedBy);
            if (existingJob.isPresent()) {
                sendJsonResponse(exchange, 409, new JSONObject()
                        .put("error", "Job with same title already exists for this employer"));
                return;
            }

            Job job = new Job(
                    request.getString("title"),
                    request.optString("description", ""),
                    request.optString("location", ""),
                    request.optDouble("salary", 0.0),
                    postedBy
            );

            Job savedJob = jobDao.save(job);

            JSONObject response = new JSONObject()
                    .put("status", "success")
                    .put("job", jobToJson(savedJob));

            sendJsonResponse(exchange, 201, response);
        } catch (SQLException e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject()
                    .put("error", "Database error: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject()
                    .put("error", "Failed to create job: " + e.getMessage()));
        }
    }

    private boolean isAdminEmployer(Long userId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "SELECT is_admin FROM employers WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean("is_admin"); // Returns true if is_admin = 1
                    }
                    return false; // User not found in employers table
                }
            }
        } finally {
            if (conn != null) conn.close();
        }
    }

    public void handleDeleteJob(HttpExchange exchange) throws IOException {
        // Verify HTTP method (should be DELETE for RESTful consistency)
        if (!"DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            return;
        }

        // Authenticate user
        User currentUser = authMiddleware.authenticate(exchange);
        if (currentUser == null) return; // Error response already sent by authMiddleware

        try {
            JSONObject request = parseJsonRequest(exchange);

            // Validate required field
            if (!request.has("id")) {
                sendJsonResponse(exchange, 400, new JSONObject()
                        .put("error", "Missing job ID"));
                return;
            }

            long jobId = request.getLong("id");

            // Check if job exists
            Optional<Job> jobOpt = jobDao.findById(jobId);
            if (jobOpt.isEmpty()) {
                sendJsonResponse(exchange, 404, new JSONObject()
                        .put("error", "Job not found"));
                return;
            }

            Job job = jobOpt.get();

            // Verify authorization:
            // 1. User must be either the job poster OR an admin
            boolean isOwner = currentUser.getId() == job.getPostedBy();
            boolean isAdmin = isAdminEmployer(currentUser.getId());

            /*
            //Debuging
            if (true) {
                System.out.println(currentUser.getId());
                System.out.println(job.getPostedBy());
                System.out.println(isOwner);
                System.out.println(isAdmin);
                return;
            }*/


            if (!isOwner || !isAdmin) {
                sendJsonResponse(exchange, 403, new JSONObject()
                        .put("error", "You can only delete your own jobs"));
                return;
            }

            // Delete the job
            jobDao.deleteById(jobId);

            // Success response
            sendJsonResponse(exchange, 200, new JSONObject()
                    .put("status", "success")
                    .put("message", "Job deleted successfully"));

        } catch (SQLException e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject()
                    .put("error", "Database error: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject()
                    .put("error", "Failed to delete job"));
        }
    }

    public void handleGetJobsByEmployer(HttpExchange exchange) throws IOException {

        // Authenticate user
        User currentUser = authMiddleware.authenticate(exchange);
        if (currentUser == null) return; // Error response already sent by authMiddleware

        try {

            long employerId = currentUser.getId();
            List<Job> jobs = jobDao.findByPostedBy(employerId);

            JSONArray jobsArray = new JSONArray();
            for (Job job : jobs) {
                jobsArray.put(jobToJson(job));
            }

            sendJsonResponse(exchange, 200, new JSONObject()
                    .put("status", "success")
                    .put("jobs", jobsArray));
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject().put("error", "Failed to fetch jobs by employer"));
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
    }
}