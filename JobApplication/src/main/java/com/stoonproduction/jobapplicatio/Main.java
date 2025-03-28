package com.stoonproduction.jobapplicatio;

import com.stoonproduction.jobapplicatio.routes.UserRoutes;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException, IOException {
        // Create an HTTP server on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // Register routes for user-related requests
        UserRoutes.registerRoutes(server);
        server.start();
        System.out.println("✅ Server ready at http://localhost:8000");
    }
}
