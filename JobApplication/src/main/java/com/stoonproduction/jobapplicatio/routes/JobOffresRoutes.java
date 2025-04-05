package com.stoonproduction.jobapplicatio.routes;

import com.stoonproduction.jobapplicatio.controllers.JobOfferController;
import com.sun.net.httpserver.HttpServer;

public class JobOffresRoutes {

    public static void registerRoutes(HttpServer server) {
        JobOfferController jobController = new JobOfferController();

        // Register route for GET /api/jobs (List all jobs)
        server.createContext("/api/jobs", jobController::handleListJobs);

        // Register route for GET /api/jobs/{id} (Get a specific job by ID)
        server.createContext("/api/jobs/job", jobController::handleGetJobById);

        // Register route for POST /api/jobs (Create a new job)
        server.createContext("/api/jobs/register", jobController::handleCreateJob);

        // Register route for DELETE /api/jobs/{id} (Delete a job)
        server.createContext("/api/jobs/delete", jobController::handleDeleteJob);

        // Register route for SEARCH /api/jobs/search/title (Search jobs by title)
        server.createContext("/api/jobs/search/title", jobController::handleSearchJobsByTitle);

        // Register route for SEARCH /api/jobs/search/location (Search jobs by location)
        server.createContext("/api/jobs/search/location", jobController::handleSearchJobsByLocation);

        // Register route for SEARCH /api/jobs/search/salary (Search jobs by salary)
        server.createContext("/api/jobs/search/salary", jobController::handleSearchJobsBySalary);

        // Register route for GET /api/jobs/employer (Get jobs by employer ID)
        server.createContext("/api/jobs/employer", jobController::handleGetJobsByEmployer);
    }
}