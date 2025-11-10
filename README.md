# 🏖️ TaskFlow Holiday Manager API

> **Technical Assessment Project** - Full-Stack Vacation Management System  
> A RESTful API built with Quarkus for managing employeeRecordModels and vacation requests with role-based access control.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.29.0-blue.svg)](https://quarkus.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

---

## 📋 Table of Contents

- [About the Project](#about-the-project)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Database Management](#database-management)
- [Testing](#testing)
- [Deployment](#deployment)
- [Project Structure](#project-structure)
- [Business Rules](#business-rules)
- [About the Developer](#about-the-developer)

---

## 🎯 About the Project

**TaskFlow Holiday Manager** is an internal system designed for **TaskFlow Ltda.** to manage employeeRecordModels and vacation requests efficiently. The system implements role-based access control (RBAC) with three distinct user roles:

- **👨‍💼 Admin**: Full system access - create users and manage all vacation requests
- **👔 Manager**: Manage assigned employeeRecordModels - approve/reject vacation requests for their team
- **👤 Collaborator**: Self-service - create and manage personal vacation requests

### Assessment Requirements

This project fulfills the following technical requirements:

✅ **Employee Management**: CRUD operations with manager association  
✅ **Vacation Request Management**: Create, list, edit, and cancel requests  
✅ **Overlap Validation**: Prevent concurrent vacation periods between employeeRecordModels  
✅ **Status Workflow**: Pending → Approved/Rejected transitions  
✅ **Role-Based Authorization**: Granular access control per user role  
✅ **API Documentation**: OpenAPI/Swagger specification  
✅ **Database Migrations**: Flyway version control  
✅ **Clean Architecture**: Organized folder structure (Resources/Services/Repositories/Entities)

---

## ✨ Features

### Core Functionality

- 🔐 **Role-Based Access Control** (Admin, Manager, Collaborator)
- 👥 **Employee Management** (Admin only)
- 📅 **Vacation Request Workflow** (Create, Approve, Reject, Cancel)
- ⚠️ **Overlap Detection** (Prevent conflicting vacation periods)
- 📊 **Comprehensive API Documentation** (Swagger UI)
- 🗄️ **Database Version Control** (Flyway migrations)
- ✅ **Input Validation** (Bean Validation)
- 🧪 **REST API Testing** (REST Assured)

### Bonus Features (Optional)

- 🔑 Authentication/Authorization (JWT - planned)
- 📄 Pagination & Filtering
- 📆 Calendar View Integration

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|-----------|
| **Framework** | Quarkus 3.29.0 (Supersonic Subatomic Java) |
| **Language** | Java 17 |
| **Database** | PostgreSQL 16 |
| **ORM** | Hibernate ORM with Panache |
| **Migration** | Flyway |
| **API Docs** | SmallRye OpenAPI + Swagger UI |
| **Build Tool** | Maven 3.9+ |
| **Testing** | JUnit 5 + REST Assured |
| **Containerization** | Docker + Docker Compose |

---

## 📦 Prerequisites

Before running this project, ensure you have:

- ☕ **Java 17+** ([OpenJDK](https://openjdk.org/) or [Amazon Corretto](https://aws.amazon.com/corretto/))
- 🔧 **Maven 3.9+** ([Download](https://maven.apache.org/download.cgi))
- 🐳 **Docker & Docker Compose** ([Install](https://docs.docker.com/get-docker/))
- 🗄️ **PostgreSQL 16** (via Docker or local installation)

---

## 🚀 Getting Started

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/cleidson-machado/taskflow-holiday-manager.git
cd taskflow-holiday-manager
```

## 👨‍💻 About the Developer

**Cleidson Pereira Machado**  
*Senior Full-Stack Developer | Java & Quarkus Specialist*

### Professional Profile

With over **12 years of experience** in full-stack development, I specialize in building **high-performance REST APIs** and **scalable backend architectures** using **Java Quarkus**. My expertise encompasses the complete software development lifecycle, from **database design and optimization** to **modern frontend integration**.

#### Core Competencies Relevant to This Project:

🔹 **Backend Architecture**: Expert in designing robust REST APIs with Java Quarkus, implementing clean architecture patterns (Resources/Services/Repositories/Entities)  
🔹 **Database Management**: Advanced relational database modeling, SQL optimization, ORM integration (Hibernate/Panache), and migration strategies (Flyway)  
🔹 **API Development**: RESTful design principles, OpenAPI/Swagger documentation, comprehensive validation, and error handling  
🔹 **Full-Stack Integration**: Seamless connection between backend services and modern frontend frameworks (React, Vue.js, Next.js)  
🔹 **DevOps & CI/CD**: Docker containerization, automated deployment pipelines, and production-ready configurations  
🔹 **Technical Leadership**: Team coordination, code review discipline, agile methodologies, and mentoring in distributed environments

#### Technical Stack Highlights:

- **Backend**: Java 17, Quarkus, Hibernate ORM with Panache, RESTEasy, JAX-RS
- **Database**: PostgreSQL, advanced SQL, Flyway migrations, performance tuning
- **Frontend**: React, Next.js, TypeScript, Vue.js, Nuxt.js, SSR/SSG optimization
- **Architecture**: Clean Architecture, Domain-Driven Design, Microservices patterns
- **Tools**: Maven, Docker, Git, CI/CD pipelines, Swagger/OpenAPI

My approach prioritizes **architectural robustness**, **maintainability**, and **reliable delivery**, transforming complex business requirements into secure, highly available, and future-proof systems.

---

### 📬 Get in Touch

📧 **Email:** cleidson.adeveloper@gmail.com

📱 **Phone (PT):** +351 914 363 615 | +351 929 060 451

📱 **WhatsApp (BR):** +55 67 9 8407-3221

🔗 **LinkedIn:** [linkedin.com/in/cleidson-pereira-machado](https://linkedin.com/in/cleidson-pereira-machado)

🔗 **GitHub:** [github.com/cleidson-machado](https://github.com/cleidson-machado?tab=repositories)