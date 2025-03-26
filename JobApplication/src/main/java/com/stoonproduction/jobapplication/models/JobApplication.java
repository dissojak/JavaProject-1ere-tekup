package com.stoonproduction.jobapplication.models;

import java.time.LocalDateTime;

public class JobApplication {
    private Long id;
    private Long jobId;
    private Long jobSeekerId;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;

    public enum ApplicationStatus {
        PENDING, ACCEPTED, REJECTED
    }

    public JobApplication(Long jobId, Long jobSeekerId) {
        this(null, jobId, jobSeekerId, ApplicationStatus.PENDING, LocalDateTime.now());
    }

    public JobApplication(Long id, Long jobId, Long jobSeekerId,
                          ApplicationStatus status, LocalDateTime appliedAt) {
        this.id = id;
        this.jobId = jobId;
        this.jobSeekerId = jobSeekerId;
        this.status = status;
        this.appliedAt = appliedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getJobSeekerId() {
        return jobSeekerId;
    }

    public void setJobSeekerId(Long jobSeekerId) {
        this.jobSeekerId = jobSeekerId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    @Override
    public String toString() {
        return "JobApplication{" +
                "id=" + id +
                ", jobId=" + jobId +
                ", jobSeekerId=" + jobSeekerId +
                ", status=" + status +
                ", appliedAt=" + appliedAt +
                '}';
    }
}