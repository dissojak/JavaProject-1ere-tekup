package com.stoonproduction.jobapplicatio.utils;

import com.stoonproduction.jobapplicatio.dao.UserDao;
import com.stoonproduction.jobapplicatio.models.User;
import com.sun.net.httpserver.HttpExchange;
import io.jsonwebtoken.Claims;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class AuthMiddleware {
    private final UserDao userDao;

    public AuthMiddleware(UserDao userDao) {
        this.userDao = userDao;
    }

    public User authenticate(HttpExchange exchange) throws IOException {
        try {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendErrorResponse(exchange, 401, "Missing or invalid token");
                return null;
            }

            String token = authHeader.substring(7);
            Claims claims = JwtUtil.validateToken(token);

            Optional<User> userOpt = userDao.findByEmail(claims.getSubject());
            if (!userOpt.isPresent()) {
                sendErrorResponse(exchange, 404, "User not found");
                return null;
            }

            return userOpt.get();
        } catch (Exception e) {
            sendErrorResponse(exchange, 401, "Invalid token: " + e.getMessage());
            return null;
        }
    }

    /*    public void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
            JSONObject response = new JSONObject().put("error", message);
            byte[] bytes = response.toString().getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            exchange.getResponseBody().write(bytes);
        }*/
    public void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        try {
            JSONObject response = new JSONObject().put("error", message);
            byte[] bytes = response.toString().getBytes(StandardCharsets.UTF_8);

            // Clear any existing headers
            exchange.getResponseHeaders().clear();
            exchange.getResponseHeaders().set("Content-Type", "application/json");

            // Send headers
            exchange.sendResponseHeaders(statusCode, bytes.length);

            // Write with proper resource handling
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
                os.flush();
            }
        } catch (Exception e) {
            System.err.println("Failed to send error response: " + e.getMessage());
            throw e;
        }
    }
}