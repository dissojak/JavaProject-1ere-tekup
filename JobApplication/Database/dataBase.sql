-- 1. Create 'users' table (base for all users)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('EMPLOYER', 'JOB_SEEKER') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Create 'companies' table
CREATE TABLE companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    website VARCHAR(255)
);

-- 3. Create 'employers' (extends users)
CREATE TABLE employers (
    user_id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    company_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- 4. Create 'job_seekers' (extends users)
CREATE TABLE job_seekers (
    user_id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    resume_url VARCHAR(255),
    skills TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 5. Create 'jobs' (posted jobs)
CREATE TABLE jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    location VARCHAR(100),
    salary DECIMAL(10,2),
    posted_by BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (posted_by) REFERENCES employers(user_id),
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- 6. Create 'job_applications'
CREATE TABLE job_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    job_seeker_id BIGINT NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED') DEFAULT 'PENDING',
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (job_id) REFERENCES jobs(id),
    FOREIGN KEY (job_seeker_id) REFERENCES job_seekers(user_id)
);