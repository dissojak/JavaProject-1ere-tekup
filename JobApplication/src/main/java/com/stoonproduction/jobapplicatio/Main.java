package com.stoonproduction.jobapplicatio;

import com.stoonproduction.jobapplicatio.routes.CompanyRoutes;
import com.stoonproduction.jobapplicatio.routes.JobOffresRoutes;
import com.stoonproduction.jobapplicatio.routes.UserRoutes;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException, IOException {
        // Create an HTTP server on port 8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // Register routes for user-related requests
        UserRoutes.registerRoutes(server);
        CompanyRoutes.registerRoutes(server);
        JobOffresRoutes.registerRoutes(server);

        server.setExecutor(null);
        server.start();
        System.out.println("✅ Server ready at http://localhost:8000");
    }
}
