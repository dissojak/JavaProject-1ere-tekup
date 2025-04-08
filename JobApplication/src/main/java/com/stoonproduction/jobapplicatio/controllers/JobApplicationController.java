package com.stoonproduction.jobapplicatio.controllers;

import com.stoonproduction.jobapplicatio.config.DatabaseConfig;
import com.stoonproduction.jobapplicatio.dao.JobApplicationDao;
import com.stoonproduction.jobapplicatio.models.JobApplication;
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

public class JobApplicationController extends BaseController implements HttpHandler {

    private final JobApplicationDao jobApplicationDao = new JobApplicationDao() {
        @Override
        public JobApplication save(JobApplication application) throws SQLException {
            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                conn.setAutoCommit(false);

                String sql = "INSERT INTO job_applications (job_id, job_seeker_id, status, applied_at) " +
                        "VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setLong(1, application.getJobId());
                    stmt.setLong(2, application.getJobSeekerId());
                    stmt.setString(3, application.getStatus().toString());
                    stmt.setTimestamp(4, Timestamp.valueOf(application.getAppliedAt()));
                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            application.setId(rs.getLong(1));
                        }
                    }
                }

                conn.commit();
                return application;
            } catch (SQLException e) {
                if (conn != null) conn.rollback();
                throw e;
            } finally {
                if (conn != null) conn.setAutoCommit(true);
            }
        }

        @Override
        public Optional<JobApplication> findById(Long id) throws SQLException {
            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                String sql = "SELECT * FROM job_applications WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, id);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return Optional.of(mapApplicationFromResultSet(rs));
                        }
                    }
                }
            } finally {
                if (conn != null) conn.close();
            }
            return Optional.empty();
        }

        @Override
        public List<JobApplication> findAll() throws SQLException {
            List<JobApplication> applications = new ArrayList<>();
            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                String sql = "SELECT * FROM job_applications";
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        applications.add(mapApplicationFromResultSet(rs));
                    }
                }
            } finally {
                if (conn != null) conn.close();
            }
            return applications;
        }

        @Override
        public List<JobApplication> findByJobId(Long jobId) throws SQLException {
            List<JobApplication> applications = new ArrayList<>();
            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                String sql = "SELECT * FROM job_applications WHERE job_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, jobId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            applications.add(mapApplicationFromResultSet(rs));
                        }
                    }
                }
            } finally {
                if (conn != null) conn.close();
            }
            return applications;
        }

        @Override
        public List<JobApplication> findByJobSeekerId(Long jobSeekerId) throws SQLException {
            List<JobApplication> applications = new ArrayList<>();
            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                String sql = "SELECT * FROM job_applications WHERE job_seeker_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, jobSeekerId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            applications.add(mapApplicationFromResultSet(rs));
                        }
                    }
                }
            } finally {
                if (conn != null) conn.close();
            }
            return applications;
        }

        @Override
        public List<JobApplication> findByStatus(JobApplication.ApplicationStatus status) throws SQLException {
            List<JobApplication> applications = new ArrayList<>();
            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                String sql = "SELECT * FROM job_applications WHERE status = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, status.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            applications.add(mapApplicationFromResultSet(rs));
                        }
                    }
                }
            } finally {
                if (conn != null) conn.close();
            }
            return applications;
        }

        @Override
        public void deleteById(Long id) throws SQLException {
            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                conn.setAutoCommit(false);

                String sql = "DELETE FROM job_applications WHERE id = ?";
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
        public boolean existsByJobIdAndJobSeekerId(Long jobId, Long jobSeekerId) throws SQLException {
            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                String sql = "SELECT COUNT(*) FROM job_applications WHERE job_id = ? AND job_seeker_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, jobId);
                    stmt.setLong(2, jobSeekerId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt(1) > 0;
                        }
                    }
                }
                return false;
            } finally {
                if (conn != null) conn.close();
            }
        }

        @Override
        public JobApplication update(JobApplication application) throws SQLException {
            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                conn.setAutoCommit(false);

                String sql = "UPDATE job_applications SET status = ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, application.getStatus().toString());
                    stmt.setLong(2, application.getId());
                    stmt.executeUpdate();
                }

                conn.commit();
                return application;
            } catch (SQLException e) {
                if (conn != null) conn.rollback();
                throw e;
            } finally {
                if (conn != null) conn.setAutoCommit(true);
            }
        }

        private JobApplication mapApplicationFromResultSet(ResultSet rs) throws SQLException {
            return new JobApplication(
                    rs.getLong("id"),
                    rs.getLong("job_id"),
                    rs.getLong("job_seeker_id"),
                    JobApplication.ApplicationStatus.valueOf(rs.getString("status")),
                    rs.getTimestamp("applied_at").toLocalDateTime()
            );
        }
    };

    private JSONObject applicationToJson(JobApplication application) {
        return new JSONObject()
                .put("id", application.getId())
                .put("jobId", application.getJobId())
                .put("jobSeekerId", application.getJobSeekerId())
                .put("status", application.getStatus().toString())
                .put("appliedAt", application.getAppliedAt().toString());
    }

    private final AuthMiddleware authMiddleware;

    public JobApplicationController() {
        this.authMiddleware = new AuthMiddleware(UserController.userDao);
    }

    public void handleGetApplicationById(HttpExchange exchange) throws IOException {
        try {
            JSONObject request = parseJsonRequest(exchange);
            if (!request.has("id")) {
                sendJsonResponse(exchange, 400, new JSONObject().put("error", "Missing application ID"));
                return;
            }

            long id = request.getLong("id");
            Optional<JobApplication> applicationOpt = jobApplicationDao.findById(id);
            if (applicationOpt.isEmpty()) {
                sendJsonResponse(exchange, 404, new JSONObject().put("error", "Application not found"));
                return;
            }

            JSONObject response = new JSONObject()
                    .put("status", "success")
                    .put("application", applicationToJson(applicationOpt.get()));

            sendJsonResponse(exchange, 200, response);
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject().put("error", "Failed to fetch application"));
        }
    }

    public void handleListApplications(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, new JSONObject().put("error", "Method Not Allowed"));
                return;
            }

            List<JobApplication> applications = jobApplicationDao.findAll();
            JSONArray applicationsArray = new JSONArray();
            for (JobApplication application : applications) {
                applicationsArray.put(applicationToJson(application));
            }

            sendJsonResponse(exchange, 200, new JSONObject()
                    .put("status", "success")
                    .put("applications", applicationsArray));
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject().put("error", "Failed to list applications"));
        }
    }

    public void handleCreateApplication(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            return;
        }

        User currentUser = authMiddleware.authenticate(exchange);
        if (currentUser == null) return;

        try {
            JSONObject request = parseJsonRequest(exchange);

            // Validate required fields
            if (!request.has("jobId") || !request.has("jobSeekerId")) {
                sendJsonResponse(exchange, 400, new JSONObject()
                        .put("error", "Missing required fields (jobId and jobSeekerId are required)"));
                return;
            }

            long jobSeekerId = request.getLong("jobSeekerId");

            // Verify token matches the jobSeekerId
            if (currentUser.getId() != jobSeekerId) {
                sendJsonResponse(exchange, 403, new JSONObject()
                        .put("error", "You can only create applications for your own account"));
                return;
            }

            long jobId = request.getLong("jobId");

            // Check if application already exists
            if (jobApplicationDao.existsByJobIdAndJobSeekerId(jobId, jobSeekerId)) {
                sendJsonResponse(exchange, 409, new JSONObject()
                        .put("error", "Application for this job already exists"));
                return;
            }

            JobApplication application = new JobApplication(
                    jobId,
                    jobSeekerId
            );

            JobApplication savedApplication = jobApplicationDao.save(application);

            JSONObject response = new JSONObject()
                    .put("status", "success")
                    .put("application", applicationToJson(savedApplication));

            sendJsonResponse(exchange, 201, response);
        } catch (SQLException e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject()
                    .put("error", "Database error: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject()
                    .put("error", "Failed to create application: " + e.getMessage()));
        }
    }

    public void handleDeleteApplication(HttpExchange exchange) throws IOException {
        if (!"DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            return;
        }

        User currentUser = authMiddleware.authenticate(exchange);
        if (currentUser == null) return;

        try {
            JSONObject request = parseJsonRequest(exchange);

            if (!request.has("id")) {
                sendJsonResponse(exchange, 400, new JSONObject()
                        .put("error", "Missing application ID"));
                return;
            }

            long applicationId = request.getLong("id");

            Optional<JobApplication> applicationOpt = jobApplicationDao.findById(applicationId);
            if (applicationOpt.isEmpty()) {
                sendJsonResponse(exchange, 404, new JSONObject()
                        .put("error", "Application not found"));
                return;
            }

            JobApplication application = applicationOpt.get();

            // Verify authorization:
            // User must be either the job seeker who applied OR the job poster
            boolean isJobSeeker = currentUser.getId() == application.getJobSeekerId();
            boolean isJobPoster = isJobPoster(currentUser.getId(), application.getJobId());

            if (!isJobSeeker && !isJobPoster) {
                sendJsonResponse(exchange, 403, new JSONObject()
                        .put("error", "You can only delete your own applications or applications to your jobs"));
                return;
            }

            jobApplicationDao.deleteById(applicationId);

            sendJsonResponse(exchange, 200, new JSONObject()
                    .put("status", "success")
                    .put("message", "Application deleted successfully"));

        } catch (SQLException e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject()
                    .put("error", "Database error: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject()
                    .put("error", "Failed to delete application"));
        }
    }

    private boolean isJobPoster(Long userId, Long jobId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "SELECT COUNT(*) FROM jobs WHERE id = ? AND posted_by = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, jobId);
                stmt.setLong(2, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
            return false;
        } finally {
            if (conn != null) conn.close();
        }
    }

    public void handleGetApplicationsByJob(HttpExchange exchange) throws IOException {
        try {
            JSONObject request = parseJsonRequest(exchange);
            if (!request.has("jobId")) {
                sendJsonResponse(exchange, 400, new JSONObject().put("error", "Missing job ID"));
                return;
            }

            long jobId = request.getLong("jobId");
            List<JobApplication> applications = jobApplicationDao.findByJobId(jobId);

            JSONArray applicationsArray = new JSONArray();
            for (JobApplication application : applications) {
                applicationsArray.put(applicationToJson(application));
            }

            sendJsonResponse(exchange, 200, new JSONObject()
                    .put("status", "success")
                    .put("applications", applicationsArray));
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject().put("error", "Failed to fetch applications by job"));
        }
    }

    public void handleGetApplicationsByJobSeeker(HttpExchange exchange) throws IOException {
        User currentUser = authMiddleware.authenticate(exchange);
        if (currentUser == null) return;

        try {
            List<JobApplication> applications = jobApplicationDao.findByJobSeekerId(currentUser.getId());

            JSONArray applicationsArray = new JSONArray();
            for (JobApplication application : applications) {
                applicationsArray.put(applicationToJson(application));
            }

            sendJsonResponse(exchange, 200, new JSONObject()
                    .put("status", "success")
                    .put("applications", applicationsArray));
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject().put("error", "Failed to fetch applications by job seeker"));
        }
    }

    public void handleUpdateApplicationStatus(HttpExchange exchange) throws IOException {
        if (!"PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            return;
        }

        User currentUser = authMiddleware.authenticate(exchange);
        if (currentUser == null) return;

        try {
            JSONObject request = parseJsonRequest(exchange);

            if (!request.has("id") || !request.has("status")) {
                sendJsonResponse(exchange, 400, new JSONObject()
                        .put("error", "Missing required fields (id and status)"));
                return;
            }

            long applicationId = request.getLong("id");
            String status = request.getString("status");

            Optional<JobApplication> applicationOpt = jobApplicationDao.findById(applicationId);
            if (applicationOpt.isEmpty()) {
                sendJsonResponse(exchange, 404, new JSONObject()
                        .put("error", "Application not found"));
                return;
            }

            JobApplication application = applicationOpt.get();

            // Verify authorization: Only the job poster can update status
            if (!isJobPoster(currentUser.getId(), application.getJobId())) {
                sendJsonResponse(exchange, 403, new JSONObject()
                        .put("error", "Only the job poster can update application status"));
                return;
            }

            // Update the status
            application.setStatus(JobApplication.ApplicationStatus.valueOf(status));
            // Note: You would need to add an update method to your DAO to persist this change

            sendJsonResponse(exchange, 200, new JSONObject()
                    .put("status", "success")
                    .put("application", applicationToJson(application)));

        } catch (IllegalArgumentException e) {
            sendJsonResponse(exchange, 400, new JSONObject()
                    .put("error", "Invalid status value"));
        } catch (SQLException e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject()
                    .put("error", "Database error: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject()
                    .put("error", "Failed to update application status"));
        }
    }

    public void handleAcceptApplication(HttpExchange exchange) throws IOException {
        handleApplicationStatusUpdate(exchange, JobApplication.ApplicationStatus.ACCEPTED);
    }

    public void handleRejectApplication(HttpExchange exchange) throws IOException {
        handleApplicationStatusUpdate(exchange, JobApplication.ApplicationStatus.REJECTED);
    }

    private void handleApplicationStatusUpdate(HttpExchange exchange, JobApplication.ApplicationStatus newStatus) throws IOException {
        if (!"PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            return;
        }

        User currentUser = authMiddleware.authenticate(exchange);
        if (currentUser == null) return;

        try {
            JSONObject request = parseJsonRequest(exchange);

            if (!request.has("applicationId")) {
                sendJsonResponse(exchange, 400, new JSONObject()
                        .put("error", "Missing application ID"));
                return;
            }

            long applicationId = request.getLong("applicationId");

            // Get the application
            Optional<JobApplication> applicationOpt = jobApplicationDao.findById(applicationId);
            if (applicationOpt.isEmpty()) {
                sendJsonResponse(exchange, 404, new JSONObject()
                        .put("error", "Application not found"));
                return;
            }

            JobApplication application = applicationOpt.get();

            // Verify the current user is the job poster or an admin
            if (!isJobPoster(currentUser.getId(), application.getJobId()) && !isAdminEmployer(currentUser.getId())) {
                sendJsonResponse(exchange, 403, new JSONObject()
                        .put("error", "Only the job poster or admin can update application status"));
                return;
            }

            // Update the status
            application.setStatus(newStatus);

            // You'll need to add an update method to your JobApplicationDao interface
            // For now, we'll assume it exists
            jobApplicationDao.update(application);

            JSONObject response = new JSONObject()
                    .put("status", "success")
                    .put("message", "Application " + newStatus.toString().toLowerCase() + " successfully")
                    .put("application", applicationToJson(application));

            sendJsonResponse(exchange, 200, response);

        } catch (SQLException e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject()
                    .put("error", "Database error: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject()
                    .put("error", "Failed to update application status"));
        }
    }

    // Add this helper method to check if user is an admin employer
    private boolean isAdminEmployer(Long userId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "SELECT is_admin FROM employers WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean("is_admin");
                    }
                    return false;
                }
            }
        } finally {
            if (conn != null) conn.close();
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Implement routing logic here based on the request URI and method
        // Similar to how it's done in JobOfferController
    }
}