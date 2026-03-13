# RevHire - Premium Job Portal Application

RevHire is a comprehensive, full-stack monolithic job portal designed to bridge the gap between job seekers and employers. It provides a robust platform for managing the entire recruitment lifecycle, from job posting and discovery to application tracking and in-app communication.

## 🚀 Key Features

### For Job Seekers
- **Dynamic Homepage**: Modern landing page with a responsive image slider.
- **Advanced Job Search**: Filter jobs by title, location, type, experience, and salary.
- **Profile Management**: Build a professional profile with an automated profile strength calculator.
- **Resume Management**: Build structured resumes within the app or upload PDF/DOCX files.
- **Application Tracking**: Apply to jobs with a single click and monitor your application status.
- **Saved Jobs**: Keep track of interesting opportunities to apply later.
- **In-App Notifications**: Receive real-time alerts for status updates, employer notes, and more.

### For Employers
- **Company Branding**: Showcase your company with a dedicated profile page.
- **Job Management**: Complete control over job postings (Create, Edit, Close, Reopen, Delete).
- **Applicant Review Workflow**: Review detailed applicant profiles, download resumes, and view cover letters.
- **Selection Process**: Update applicant statuses (Shortlisted, Rejected, Selected) and add internal/candidate-facing notes.
- **Dashboard Analytics**: Visual summary of active jobs, total applicants, and pending reviews.
- **Real-Time Notifications**: Get notified instantly when candidates apply or withdraw.

---

## 🛠️ Technical Stack

- **Backend**: Java 17, Spring Boot 3.2.0
- **Security**: Spring Security 6 with JWT (JSON Web Token) Stateless Authentication
- **Data Layer**: Spring Data JPA with Hibernate
- **Database**: Oracle Database (via OJDBC11)
- **Frontend**: Thymeleaf, Bootstrap 5, FontAwesome 6, Vanilla JavaScript
- **Logging**: Log4j2
- **Build Tool**: Maven

---

## ⚙️ Project Setup

### Prerequisites
- Java 17 or higher
- Oracle Database (Free/XE)
- Maven 3.8+

### Database Configuration
1. Ensure your Oracle instance is running.
2. Execute the `setup_database.sql` script located in the root directory to create the necessary sequences.
3. Update `src/main/resources/application.properties` with your database credentials:
   ```properties
   spring.datasource.url=jdbc:oracle:thin:@localhost:1521/FREE
   spring.datasource.username=YOUR_USERNAME
   spring.datasource.password=YOUR_PASSWORD
   ```

### Running the Application
1. Clone the repository.
2. Build the project:
   ```bash
   mvn clean install
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
4. Access the portal at `http://localhost:8080`.

---

## 🧪 Testing
Run unit and integration tests using:
```bash
mvn test
```

---

## 📂 Project Structure
```text
src/main/java/com/rev/app/
├── config/         # Security and App configurations
├── controller/     # MVC Page Controllers
├── rest/           # REST API Controllers (AJAX/Interactivity)
├── entity/         # JPA Entities (Database Mapping)
├── repository/     # Data Access Layer
├── service/        # Business Logic Layer
├── dto/            # Data Transfer Objects
├── mapper/         # Entity-DTO Mappers
├── security/       # JWT and Auth logic
└── exception/      # Global Exception Handling
```

---

## 🎨 Design & UX
RevHire features a premium, state-of-the-art interface with:
- **Glassmorphism effects** and smooth transitions.
- **Modern typography** (Inter font family).
- **Vibrant yet professional color palettes**.
- **Real-time interactivity** using AJAX for notifications and badges.
- **Mobile-first responsive design**.

---
*Created with ❤️ by the RevHire Team.*
