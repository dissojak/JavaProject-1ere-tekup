package com.stoonproduction.jobapplication.models;

public class Employer extends User {
    private Long companyId;
    private boolean isAdmin;

    public Employer(String email, String password, Long companyId, boolean isAdmin) {
        super(email, password, UserRole.EMPLOYER);
        this.companyId = companyId;
        this.isAdmin = isAdmin;
    }

    public Employer(String email, String password) {
        this(email, password, null, false);
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

}