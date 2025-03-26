package com.stoonproduction.jobapplicatio.controllers;

import com.stoonproduction.jobapplicatio.config.DatabaseConfig;
import com.stoonproduction.jobapplicatio.dao.UserDao;
import com.stoonproduction.jobapplicatio.models.User;
import com.stoonproduction.jobapplicatio.utils.JwtUtil;
import com.stoonproduction.jobapplicatio.utils.PasswordHasher;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;
import java.util.Optional;

public class UserController implements HttpHandler {

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
                    stmt.setLong(4, user.getId()); // We need to specify the user ID for the update

                    int affectedRows = stmt.executeUpdate();
                    if (affectedRows > 0) {
                        return user; // Return the updated user
                    } else {
                        return null; // No user found or no update was performed
                    }
                }
            }
        }


        @Override
        public Optional<User> findById(Long id) throws SQLException {
            try (Connection connection = DatabaseConfig.getConnection()) {
                String query = "SELECT * FROM users WHERE id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setLong(1, id);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        String email = rs.getString("email");
                        String password = rs.getString("password");
                        User.UserRole role = User.UserRole.valueOf(rs.getString("role"));
                        User user = new User(email, password, role);
                        user.setId(rs.getLong("id"));
                        return Optional.of(user);
                    }
                    return Optional.empty();
                }
            }
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
                    return rs.next();
                }
            }
        }

        @Override
        public List<User> findAll() throws SQLException {
            // Similar logic for finding all users can be added here
            return null;
        }

        @Override
        public void deleteById(Long id) throws SQLException {
            // Similar logic for deleting user by ID can be added here
        }
    };

    // Register User
    public void handleRegister(HttpExchange exchange) throws IOException {
        try {
            JSONObject request = parseRequest(exchange);
            String email = request.getString("email");
            String password = request.getString("password");
            User.UserRole role = User.UserRole.valueOf(request.getString("role").toUpperCase());

            if (userDao.existsByEmail(email)) {
                sendResponse(exchange, 400, new JSONObject().put("error", "Email already registered"));
                return;
            }

            User newUser = new User(email, PasswordHasher.hashPassword(password), role);
            userDao.save(newUser);

            JSONObject response = new JSONObject()
                    .put("status", "success")
                    .put("userId", newUser.getId());
            sendResponse(exchange, 201, response);
        } catch (SQLException e) {
            sendResponse(exchange, 500, new JSONObject().put("error", "Database error: " + e.getMessage()));
        } catch (Exception e) {
            sendResponse(exchange, 500, new JSONObject().put("error", "Internal server error"));
        }
    }

    public void handleLogin(HttpExchange exchange) throws IOException {
        try {
            JSONObject request = parseRequest(exchange);
            String email = request.getString("email");
            String password = request.getString("password");

            // Debugging: log the email and password
            System.out.println("login with email: " + email);

            Optional<User> userOpt = userDao.findByEmail(email);
            if (!userOpt.isPresent()) {
                System.out.println("User not found for email: " + email);
                sendResponse(exchange, 404, new JSONObject().put("error", "User not found"));
                return;
            }

            User user = userOpt.get();
            System.out.println("User found: " + user.getEmail() + ", Role: " + user.getRole());

            // Debugging: Check password hash comparison
            System.out.println("Password verification for: " + email);
            if (!PasswordHasher.verifyPassword(password, user.getPassword())) {
                System.out.println("Password mismatch for email: " + email);
                sendResponse(exchange, 401, new JSONObject().put("error", "Invalid credentials"));
                return;
            }

            // Generate token if password is correct
            String token = JwtUtil.generateToken(user.getEmail(), user.getRole());
            System.out.println("Token generated for user: " + email);

            JSONObject response = new JSONObject()
                    .put("status", "success")
                    .put("token", token)
                    .put("role", user.getRole().toString());
            sendResponse(exchange, 200, response);

        } catch (SQLException e) {
            sendResponse(exchange, 500, new JSONObject().put("error", "Database error: " + e.getMessage()));
        } catch (Exception e) {
            sendResponse(exchange, 500, new JSONObject().put("error", "Internal server error"+e.getMessage()));
        }
    }

    public void handleUpdateUser(HttpExchange exchange) throws IOException {
        try {
            JSONObject request = parseRequest(exchange);
            long userId = request.getLong("userId");
            String newEmail = request.optString("email", null);
            String newPassword = request.optString("password", null);
            String newRoleString = request.optString("role", null);
            User.UserRole newRole = null;
            if (newRoleString != null) {
                newRole = User.UserRole.valueOf(newRoleString.toUpperCase());
            }

            // Fetch the existing user by ID (you need to implement this in UserDao)
            Optional<User> existingUserOpt = userDao.findById(userId);
            if (!existingUserOpt.isPresent()) {
                sendResponse(exchange, 404, new JSONObject().put("error", "User not found"));
                return;
            }

            User existingUser = existingUserOpt.get();

            // Update fields only if they are provided
            if (newEmail != null && !newEmail.equals(existingUser.getEmail())) {
                // Check if email already exists
                if (userDao.existsByEmail(newEmail)) {
                    sendResponse(exchange, 400, new JSONObject().put("error", "Email already in use"));
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

            // Save the updated user using the update method
            userDao.update(existingUser);

            // Respond with success
            JSONObject response = new JSONObject()
                    .put("status", "success")
                    .put("userId", existingUser.getId());
            sendResponse(exchange, 200, response);
        } catch (SQLException e) {
            sendResponse(exchange, 500, new JSONObject().put("error", "Database error: " + e.getMessage()));
        } catch (Exception e) {
            sendResponse(exchange, 500, new JSONObject().put("error", "Internal server error"));
        }
    }




    // Utility function to parse the incoming request
    private JSONObject parseRequest(HttpExchange exchange) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
        return new JSONObject(body);
    }

    // Send a response back to the client
    private void sendResponse(HttpExchange exchange, int statusCode, JSONObject response) throws IOException {
        byte[] responseBytes = response.toString().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // This method can be left empty as routes are handled elsewhere
    }


    public void handleDeleteUser(HttpExchange httpExchange) {
    }

    public void handleGetUser(HttpExchange httpExchange) {
    }
}
