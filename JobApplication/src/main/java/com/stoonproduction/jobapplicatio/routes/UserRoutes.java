package com.stoonproduction.jobapplicatio.routes;

import com.stoonproduction.jobapplicatio.controllers.UserController;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class UserRoutes {

    public static void registerRoutes(HttpServer server) {
        UserController userController = new UserController();

        // Register route for POST /api/auth/register
        server.createContext("/api/auth/register", userController::handleRegister);

        // Register route for POST /api/auth/login
        server.createContext("/api/auth/login", userController::handleLogin);

        // Register route for PUT /api/auth/update
        server.createContext("/api/auth/update", userController::handleUpdateUser);

        // Register route for DELETE /api/auth/delete
        server.createContext("/api/auth/delete", userController::handleDeleteUser);

        // Register route for GET /api/auth/user
        server.createContext("/api/auth/user", userController::handleGetUser);
    }
}
