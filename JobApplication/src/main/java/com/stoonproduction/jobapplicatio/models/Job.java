package com.stoonproduction.jobapplicatio.models;

import java.time.LocalDateTime;

public class Job {
    private Long id;
    private String title;
    private String description;
    private String location;
    private Double salary;  // Changed from salaryMin/salaryMax
    private Long postedBy;
    private LocalDateTime createdAt;

    public Job(String title, String description, String location,
               Double salary, Long postedBy) {
        this(null, title, description, location, salary, postedBy, LocalDateTime.now());
    }

    public Job(Long id, String title, String description, String location,
               Double salary, Long postedBy, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.salary = salary;
        this.postedBy = postedBy;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }


    public Long getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(Long postedBy) {
        this.postedBy = postedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Job{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", salary=" + salary +
                ", postedBy=" + postedBy +
                ", createdAt=" + createdAt +
                '}';
    }
}