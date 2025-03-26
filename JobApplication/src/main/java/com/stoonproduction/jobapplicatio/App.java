package com.stoonproduction.jobapplicatio;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONObject;

public class App {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/hello", new HelloHandler());
        server.createContext("/user", new UserHandler());
        server.start();
        System.out.println("Server started on port 8000...");
    }

    static class HelloHandler implements HttpHandler {
        @Override
        public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            JSONObject response = new JSONObject();
            response.put("message", "Hello World!");
            response.put("status", "success");

            sendJsonResponse(exchange, HttpURLConnection.HTTP_OK, response);
        }
    }

    static class UserHandler implements HttpHandler {
        @Override
        public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            try {
                if ("GET".equals(exchange.getRequestMethod())) {
                    String query = exchange.getRequestURI().getQuery();
                    if (query == null || !query.startsWith("id=")) {
                        JSONObject error = new JSONObject();
                        error.put("error", "Missing or invalid user ID");
                        error.put("status", "error");
                        sendJsonResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, error);
                        return;
                    }

                    long userId = Long.parseLong(query.split("=")[1]);
                    JSONObject user = getUserFromDatabase(userId);

                    if (user.has("error")) {
                        sendJsonResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, user);
                    } else {
                        sendJsonResponse(exchange, HttpURLConnection.HTTP_OK, user);
                    }
                } else {
                    JSONObject error = new JSONObject();
                    error.put("error", "Method not allowed");
                    error.put("status", "error");
                    sendJsonResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD, error);
                }
            } catch (Exception e) {
                JSONObject error = new JSONObject();
                error.put("error", e.getMessage());
                error.put("status", "error");
                sendJsonResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, error);
            }
        }

        private JSONObject getUserFromDatabase(Long userId) throws SQLException {
            JSONObject response = new JSONObject();

            String dbUrl = "jdbc:mysql://127.0.0.1:3306/JobApp";
            String dbUsername = "root";
            String dbPassword = "";

            try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
                String sql = "SELECT * FROM users WHERE id = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setLong(1, userId);
                ResultSet resultSet = stmt.executeQuery();

                if (resultSet.next()) {
                    response.put("id", resultSet.getLong("id"));
                    response.put("email", resultSet.getString("email"));
                    response.put("role", resultSet.getString("role"));
                    response.put("status", "success");
                } else {
                    response.put("error", "User not found");
                    response.put("status", "error");
                }
            }
            return response;
        }
    }

    private static void sendJsonResponse(com.sun.net.httpserver.HttpExchange exchange,
                                         int statusCode, JSONObject response) throws IOException {
        byte[] responseBytes = response.toString().getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}