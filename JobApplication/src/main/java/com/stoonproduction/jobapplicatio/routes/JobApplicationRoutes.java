package com.stoonproduction.jobapplicatio.routes;

import com.stoonproduction.jobapplicatio.controllers.JobApplicationController;
import com.sun.net.httpserver.HttpServer;

public class JobApplicationRoutes {

    public static void registerRoutes(HttpServer server) {
        JobApplicationController applicationController = new JobApplicationController();

        // Register route for GET /api/applications (List all applications - typically admin only)
        server.createContext("/api/applications", applicationController::handleListApplications);

        // Register route for GET /api/applications/{id} (Get a specific application by ID)
        server.createContext("/api/applications/application", applicationController::handleGetApplicationById);

        // Register route for POST /api/applications (Create a new application)
        server.createContext("/api/applications/register", applicationController::handleCreateApplication);

        // Register route for DELETE /api/applications/{id} (Delete an application)
        server.createContext("/api/applications/delete", applicationController::handleDeleteApplication);

        // Register route for GET /api/applications/job/{jobId} (Get applications for a specific job)
        server.createContext("/api/applications/job", applicationController::handleGetApplicationsByJob);

        // Register route for GET /api/applications/job-seeker (Get applications by current job seeker)
        server.createContext("/api/applications/job-seeker", applicationController::handleGetApplicationsByJobSeeker);

        // Register route for PUT /api/applications/status (Update application status)
        server.createContext("/api/applications/status", applicationController::handleUpdateApplicationStatus);
    }
}