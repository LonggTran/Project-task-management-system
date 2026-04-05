# Project task management system

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-Framework-brightgreen)
![Spring Security](https://img.shields.io/badge/Security-JWT-orange)
![Database](https://img.shields.io/badge/Database-MySQL%20%7C%20PostgreSQL-blue)
![Redis](https://img.shields.io/badge/Cache-Redis-red)
![WebSocket](https://img.shields.io/badge/Realtime-WebSocket-purple)
![Build](https://img.shields.io/badge/Build-Maven-yellow)
![Docker](https://img.shields.io/badge/Deploy-Docker-blue)

---

## Overview
This project is a **Task Management Backend System** designed to support Agile workflows.  
It provides RESTful APIs for managing projects, tasks, sprints, epics, comments, attachments, and notifications.

The system is built with a focus on **scalability, performance, and security**.

---

## Tech Stack

### Programming Language
![Java](https://img.shields.io/badge/Java-17-blue)

---

### Framework & Technologies
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-Framework-brightgreen)
![Spring Security](https://img.shields.io/badge/Security-JWT-orange)
![Spring Data JPA](https://img.shields.io/badge/JPA-Hibernate-yellowgreen)
![WebSocket](https://img.shields.io/badge/WebSocket-Realtime-purple)
![JavaMail](https://img.shields.io/badge/Email-JavaMailSender-lightgrey)

---

### Database
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue)
![Redis](https://img.shields.io/badge/Redis-Cache-red)

---

### Tools
![Docker](https://img.shields.io/badge/Docker-Containerization-blue)
![Gradle](https://img.shields.io/badge/Gradle-Build-red)

---

### Knowledge
![REST API](https://img.shields.io/badge/Architecture-RESTful_API-green)
![JWT](https://img.shields.io/badge/Auth-JWT-black)
![RBAC](https://img.shields.io/badge/Security-RBAC-important)
![Caching](https://img.shields.io/badge/Performance-Caching-blueviolet)
![WebSocket](https://img.shields.io/badge/Realtime-WebSocket-purple)
![Agile](https://img.shields.io/badge/Methodology-Agile-orange)

---

## Performance

- Redis caching applied for:
  - Project list
  - Comments (list & detail)
  - Sprint, Task, Subtask and Epic management data
- Cache-aside pattern (`getOrLoad`) to reduce database load
- Optimized queries to avoid N+1 issues
- Clear cache invalidation strategy

---

## Security

- JWT Authentication:
  - Access Token
  - Refresh Token
- Redis-based token management:
  - Token blacklist on logout
  - Refresh token validation
- Google Login (OAuth2)
- OTP Email Verification
- Request validation (Spring Validation)
- Secure file upload (sanitized filename)

---

## Authorization

- Role-based & Permission-based system
- Roles: OWNER, MEMBER, etc.
- Fine-grained permissions:
  - PROJECT_UPDATE
  - TASK_CREATE
  - COMMENT_DELETE
- Authorization handled at Service layer

---

## Performance Testing

- Tested with:
  - Postman
- Metrics:
  - API response time
  - Cache efficiency
- Compared performance with and without Redis cache

---

## Getting Started

### Clone repository
```bash
git clone Github.com/LonggTran/Project-task-management-system.git
