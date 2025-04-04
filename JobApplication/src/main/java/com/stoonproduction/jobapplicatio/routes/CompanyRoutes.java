package com.stoonproduction.jobapplicatio.routes;

import com.stoonproduction.jobapplicatio.controllers.CompanyController;
import com.sun.net.httpserver.HttpServer;

public class CompanyRoutes {

    public static void registerRoutes(HttpServer server) {
        CompanyController companyController = new CompanyController();

        // Register route for GET /api/companies (List all companies)
        server.createContext("/api/companies", companyController::handleListCompanies);

        // Register route for GET /api/companies/{id} (Get a specific company by ID)
        server.createContext("/api/companies/company", companyController::handleGetCompanyById);

        // Register route for POST /api/companies (Create a new company)
        server.createContext("/api/companies/register", companyController::handleCreateCompany);

        // Register route for SEARCH /api/companies/search (Search for companies by name)
        server.createContext("/api/companies/search", companyController::handleSearchCompanies);
    }
}
