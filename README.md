# Job Application API

## Overview  
This project is a **Job Application Management API** built with **Java, JDBC, and Maven**. It provides functionality to manage job applications, track statuses, and interact with a database for storing job-related data.  

## Features  
- Add, update, and delete job applications  
- Track application status (applied, interviewing, accepted, rejected)  
- Retrieve job details from the database  
- Designed with **JDBC** for database interaction  
- Built and managed using **Maven**  

## Technologies Used  
- **Java** (Backend logic)  
- **JDBC** (Database connectivity)  
- **Maven** (Dependency management)  
- **MySQL/PostgreSQL** (Specify which DB you’re using)  

## Installation  

1. Clone the repository:  
   ```bash
   git clone https://github.com/your-username/job-application-api.git
   cd job-application-api
   ```  
2. Build the project with Maven:  
   ```bash
   mvn clean install
   ```  
3. Configure the database connection in `application.properties` or `config.java`  
4. Run the application:  
   ```bash
   java -jar target/job-application-api.jar
   ```  

## API Endpoints  
| Method | Endpoint | Description |  
|--------|----------|-------------|  
| GET | `/applications` | Get all job applications |  
| POST | `/applications` | Add a new job application |  
| PUT | `/applications/{id}` | Update a job application |  
| DELETE | `/applications/{id}` | Delete a job application |  

## Contributing  
Feel free to submit issues or pull requests if you want to improve this project.  

## License  
This project is licensed under [MIT License](LICENSE).
