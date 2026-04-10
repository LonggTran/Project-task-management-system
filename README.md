# Project task management system

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-Framework-brightgreen)
![Spring Security](https://img.shields.io/badge/Security-JWT-orange)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue)
![Redis](https://img.shields.io/badge/Redis-Cache-red)
![WebSocket](https://img.shields.io/badge/WebSocket-Realtime-purple)
![Docker](https://img.shields.io/badge/Docker-Containerization-blue)

---

## Overview
This project is a **Project task management Backend System** designed to support Agile workflows.

It provides RESTful APIs for:
- Project management
- Task, Subtask, Sprint, Epic management
- Comment & attachment handling
- Realtime notifications

The system focuses on **performance optimization, security, and scalability**.

---

## Tech Stack

### Programming Language
<p>
  <img src="https://img.shields.io/badge/Java-21-blue"/>
</p>

### Framework & Technologies
<p>
  <img src="https://img.shields.io/badge/SpringBoot-Framework-brightgreen"/>
  <img src="https://img.shields.io/badge/SpringSecurity-JWT-orange"/>
  <img src="https://img.shields.io/badge/JPA-Hibernate-yellowgreen"/>
  <img src="https://img.shields.io/badge/WebSocket-Realtime-purple"/>
  <img src="https://img.shields.io/badge/JavaMail-Email-lightgrey"/>
</p>

### Database
<p>
  <img src="https://img.shields.io/badge/PostgreSQL-Database-blue"/>
  <img src="https://img.shields.io/badge/Redis-Cache-red"/>
</p>

### Tools
<p>
  <img src="https://img.shields.io/badge/Docker-Containerization-blue"/>
  <img src="https://img.shields.io/badge/Gradle-Build-red"/>
  <img src="https://img.shields.io/badge/Postman-API_Testing-orange"/>
</p>

### Knowledge
<p>
  <img src="https://img.shields.io/badge/REST-API-green"/>
  <img src="https://img.shields.io/badge/JWT-Auth-black"/>
  <img src="https://img.shields.io/badge/RBAC-Authorization-important"/>
  <img src="https://img.shields.io/badge/Caching-Redis-blueviolet"/>
  <img src="https://img.shields.io/badge/Agile-Workflow-orange"/>
</p>

---

## Key Features

- JWT Authentication (Access + Refresh Token)
- Role-based Authorization (RBAC)
- Redis caching for performance optimization
- Token blacklist & refresh token management with Redis
- Realtime notification using WebSocket
- OTP Email verification
- File upload & attachment management
- Activity logging system

---

## Performance & Testing

- API Testing using Postman
- Load testing with 200+ concurrent requests
- Tested across 10+ APIs
- Average response time:
  - ~30–45ms/request (with Redis caching)

- Performance optimization:
  - Cache-aside pattern
  - Reduced database load using Redis
  - Optimized queries to avoid N+1 issues

---

## Security

- JWT Authentication (Access & Refresh Token)
- Token blacklist (Redis)
- Role-based access control (RBAC)
- Input validation (Spring Validation)
- Secure file upload (sanitize filename)
- OTP Email Verification
- Google OAuth2 Login (optional)

---

## Architecture

- Layered Architecture:
  - Controller
  - Service
  - Repository
- DTO Pattern
- Global Exception Handling
- Event-driven components:
  - Notification
  - Activity Logging

---

## Getting Started

### Clone repository
```bash
git clone Github.com/LonggTran/Project-task-management-system.git
