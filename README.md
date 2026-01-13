# Solace - Digital Mental Health Literacy Hub

A comprehensive mental health support platform for higher education students, built with **Spring MVC**.

## 🎯 Project Overview

Solace is designed to provide mental health support for university students through:
- **Mood Tracking** - Daily mood journaling with analytics
- **Assessments** - PHQ-9 and GAD-7 mental health screenings  
- **AI Coach** - Supportive AI-powered conversations
- **Counsellor Appointments** - Booking system with counsellor availability
- **Learning Modules** - Mental health education content
- **Forum** - Anonymous peer support community
- **Gamification** - Points, badges, and leaderboard for engagement

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Spring MVC                              │
├─────────────────────────────────────────────────────────────┤
│  Controllers    │   Services    │   Repositories            │
│  (Web Layer)    │   (Business)  │   (Data Access)           │
├─────────────────────────────────────────────────────────────┤
│                     Thymeleaf Views                          │
├─────────────────────────────────────────────────────────────┤
│           MySQL Database (XAMPP)                             │
└─────────────────────────────────────────────────────────────┘
```

## 🛠️ Technology Stack

| Layer | Technology |
|-------|------------|
| **Framework** | Spring MVC 6.1.4 |
| **View Engine** | Thymeleaf 3.1.2 |
| **Security** | Spring Security 6.2.2 |
| **ORM** | Hibernate 6.4.4 / Spring Data JPA |
| **Database** | MySQL 8.0 (XAMPP) |
| **Build Tool** | Maven |
| **Server** | Apache Tomcat 10.x |
| **Java Version** | Java 17 |

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- **XAMPP** (with MySQL)
- **Git**

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone git@github.com:FaizSyuhada/dmhlh-mvc.git
cd dmhlh-mvc
```

### 2. Start XAMPP MySQL

1. Open XAMPP Control Panel
2. Start **MySQL** service
3. Create database:

```sql
CREATE DATABASE dmhlh CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Import Database Schema

```bash
mysql -u root dmhlh < src/main/resources/db/migration/V1__create_schema.sql
mysql -u root dmhlh < src/main/resources/db/migration/V2__seed_data.sql
mysql -u root dmhlh < src/main/resources/db/migration/V3__add_gad7_assessment.sql
mysql -u root dmhlh < src/main/resources/db/migration/V4__add_gamification_and_improvements.sql
mysql -u root dmhlh < src/main/resources/db/migration/V5__add_more_counsellors.sql
```

### 4. Configure Database Connection

Edit `src/main/resources/application.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/dmhlh?useSSL=false&serverTimezone=UTC
db.username=root
db.password=
```

### 5. Build and Run

```bash
# Build the project
mvn clean package

# Run with embedded Tomcat
mvn cargo:run
```

### 6. Access the Application

Open browser: **http://localhost:8686/solace**

## 👥 User Roles & Demo Accounts

| Role | Email | Password | Description |
|------|-------|----------|-------------|
| **Student** | student1@dmhlh.test | password123 | Access mood tracking, assessments, appointments |
| **Counsellor** | counsellor@dmhlh.test | password123 | Manage appointments, view student data |
| **Faculty** | faculty@dmhlh.test | password123 | Submit referrals, view reports |
| **Admin** | admin@dmhlh.test | password123 | System administration |

## 📁 Project Structure

```
dmhlh-mvc/
├── src/
│   ├── main/
│   │   ├── java/com/dmhlh/
│   │   │   ├── config/          # Spring configuration classes
│   │   │   ├── controller/      # MVC Controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # JPA Entities
│   │   │   ├── repository/      # Spring Data Repositories
│   │   │   ├── security/        # Security configuration
│   │   │   └── service/         # Business logic services
│   │   ├── resources/
│   │   │   ├── db/migration/    # SQL migration scripts
│   │   │   ├── static/css/      # Stylesheets
│   │   │   ├── templates/       # Thymeleaf templates
│   │   │   └── application.properties
│   │   └── webapp/WEB-INF/
│   └── test/
├── pom.xml
└── README.md
```

## 🔧 Configuration Classes

| Class | Purpose |
|-------|---------|
| `WebAppInitializer` | Replaces web.xml, initializes DispatcherServlet |
| `WebConfig` | MVC configuration, view resolvers |
| `JpaConfig` | Database and JPA configuration |
| `SecurityConfig` | Spring Security authentication/authorization |
| `RootConfig` | Root application context |

## 🎨 Features by Role

### Student
- 📊 Dashboard with mood overview
- 📝 Mood Journal with analytics
- 📋 Mental health assessments (PHQ-9, GAD-7)
- 🤖 AI Coach for supportive conversations
- 📅 Book counsellor appointments
- 📚 Learning modules
- 💬 Anonymous forum participation
- 🏆 Gamification (points, badges, leaderboard)

### Counsellor
- 📅 Appointment management (calendar/list view)
- 👥 Student session history
- 📋 Referral management
- 🛡️ Content moderation

### Faculty
- 📝 Submit student referrals
- 📊 View anonymized reports

### Admin
- 📊 System dashboard with analytics
- 📋 Assessment management
- 📚 Learning module management
- ⚙️ Forum settings
- 👥 User management

## 🗃️ Database Schema

Key tables:
- `users` - User accounts with role-based access
- `mood_logs` - Daily mood entries
- `assessment_results` - Mental health screening results
- `appointments` - Counsellor booking system
- `forum_threads` / `forum_posts` - Community forum
- `user_points` / `badges` - Gamification system

## 📝 Development Notes

### Running in Development Mode

```bash
mvn cargo:run
```

The application will be available at `http://localhost:8686/solace`

### Building for Production

```bash
mvn clean package
```

Deploy the generated `target/solace.war` to your Tomcat server.

## 👨‍💻 Author

**Faiz Syuhada**

## 📄 License

This project is developed for academic purposes as part of the Software Development and Architecture course.

---

© 2026 Solace - Digital Mental Health Literacy Hub
