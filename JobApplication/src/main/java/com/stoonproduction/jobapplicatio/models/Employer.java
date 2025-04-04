package com.stoonproduction.jobapplicatio.models;

import java.time.LocalDateTime;

public class Employer extends User {
    private String name;
    private Long companyId;
    private boolean isAdmin;

    public Employer(String email, String password, String name, Long companyId, boolean isAdmin) {
        super(email, password, UserRole.EMPLOYER);
        this.name = name;
        this.companyId = companyId;
        this.isAdmin = isAdmin;
    }

    public Employer(String email, String password,String name,long companyId ) {
        this(email, password, name ,companyId, false);
    }

/*    public Employer(String email, String password, UserRole role, long companyId, boolean isAdmin) {
        this.companyId=companyId;
        this.isAdmin=false;
    }*/

    public Employer(long id, String email, String password, UserRole role, LocalDateTime createdAt,String name ,long companyId, boolean isAdmin) {
        super(id,email,password,role,createdAt);
        this.name = name;
        this.companyId=companyId;
        this.isAdmin=isAdmin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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