package com.stoonproduction.jobapplicatio.controllers;

import com.stoonproduction.jobapplicatio.config.DatabaseConfig;
import com.stoonproduction.jobapplicatio.dao.CompanyDao;
import com.stoonproduction.jobapplicatio.models.Company;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CompanyController extends BaseController implements HttpHandler {

    private final CompanyDao companyDao = new CompanyDao() {
        @Override
        public Company save(Company company) throws SQLException {
            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                conn.setAutoCommit(false);

                // Updated query to insert all fields
                String sql = "INSERT INTO companies (name, description, website) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, company.getName());
                    stmt.setString(2, company.getDescription());
                    stmt.setString(3, company.getWebsite());
                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            company.setId(rs.getLong(1));
                        }
                    }
                }

                conn.commit();
                return company;
            } catch (SQLException e) {
                if (conn != null) conn.rollback();
                throw e;
            } finally {
                if (conn != null) conn.setAutoCommit(true);
            }
        }

        @Override
        public Optional<Company> findById(Long id) throws SQLException {
            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                String sql = "SELECT * FROM companies WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, id);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            Company company = new Company();
                            company.setId(rs.getLong("id"));
                            company.setName(rs.getString("name"));
                            company.setDescription(rs.getString("description"));
                            company.setWebsite(rs.getString("website"));
                            return Optional.of(company);
                        }
                    }
                }
            } catch (SQLException e) {
                throw e;
            } finally {
                if (conn != null) conn.close();
            }
            return Optional.empty();
        }

        @Override
        public List<Company> findAll() throws SQLException {
            List<Company> companies = new ArrayList<>();
            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                String sql = "SELECT * FROM companies";
                try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Company company = new Company();
                        company.setId(rs.getLong("id"));
                        company.setName(rs.getString("name"));
                        company.setDescription(rs.getString("description"));
                        company.setWebsite(rs.getString("website"));
                        companies.add(company);
                    }
                }
            } catch (SQLException e) {
                throw e;
            } finally {
                if (conn != null) conn.close();
            }
            return companies;
        }

        @Override
        public List<Company> findByNameContaining(String name) throws SQLException {
            List<Company> companies = new ArrayList<>();
            Connection conn = null;
            try {
                conn = DatabaseConfig.getConnection();
                String sql = "SELECT * FROM companies WHERE name LIKE ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, "%" + name + "%");
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            Company company = new Company();
                            company.setId(rs.getLong("id"));
                            company.setName(rs.getString("name"));
                            company.setDescription(rs.getString("description"));
                            company.setWebsite(rs.getString("website"));
                            companies.add(company);
                        }
                    }
                }
            } catch (SQLException e) {
                throw e;
            } finally {
                if (conn != null) conn.close();
            }
            return companies;
        }
    }; // Initialize companyDao

    private long extractIdFromPath(String path) {
        String[] pathParts = path.split("/");
        return Long.parseLong(pathParts[pathParts.length - 1]);
    }

    public void handleGetCompanyById(HttpExchange exchange) throws IOException {
        try {
            JSONObject request = parseJsonRequest(exchange);
            if (!request.has("id")) {
                sendJsonResponse(exchange, 400, new JSONObject().put("error", "Missing company ID"));
                return;
            }

            long id = request.getLong("id");
            Optional<Company> companyOpt = companyDao.findById(id);
            if (companyOpt.isEmpty()) {
                sendJsonResponse(exchange, 404, new JSONObject().put("error", "Company not found"));
                return;
            }

            JSONObject response = new JSONObject()
                    .put("status", "success")
                    .put("company", companyToJson(companyOpt.get()));

            sendJsonResponse(exchange, 200, response);
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject().put("error", "Failed to fetch company"));
        }
    }

    public void handleListCompanies(HttpExchange exchange) throws IOException {
        try {
            List<Company> companies = companyDao.findAll();
            JSONArray companiesArray = new JSONArray();
            for (Company c : companies) {
                companiesArray.put(companyToJson(c));
            }

            sendJsonResponse(exchange, 200, new JSONObject()
                    .put("status", "success")
                    .put("companies", companiesArray));
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject().put("error", "Failed to list companies"));
        }
    }

    public void handleSearchCompanies(HttpExchange exchange) throws IOException {
        try {
            JSONObject request = parseJsonRequest(exchange);
            if (!request.has("name")) {
                sendJsonResponse(exchange, 400, new JSONObject().put("error", "Missing company name"));
                return;
            }

            String name = request.getString("name");
            List<Company> companies = companyDao.findByNameContaining(name);

            JSONArray companiesArray = new JSONArray();
            for (Company c : companies) {
                companiesArray.put(companyToJson(c));
            }

            sendJsonResponse(exchange, 200, new JSONObject()
                    .put("status", "success")
                    .put("companies", companiesArray));
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject().put("error", "Search failed"));
        }
    }

    private JSONObject companyToJson(Company company) {
        return new JSONObject()
                .put("id", company.getId())
                .put("name", company.getName())
                .put("description", company.getDescription())
                .put("website", company.getWebsite());
    }

    public void handleCreateCompany(HttpExchange exchange) throws IOException {
        try {
            JSONObject request = parseJsonRequest(exchange);

            String name = request.getString("name");
            String description = request.optString("description", null);
            String website = request.optString("website", null);

            Company company = new Company(name, description, website);
            Company savedCompany = companyDao.save(company);

            JSONObject response = new JSONObject()
                    .put("status", "success")
                    .put("company", companyToJson(savedCompany));

            sendJsonResponse(exchange, 201, response);
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, new JSONObject().put("error", "Failed to create company"));
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {}
}
