# Healthcare Microservices - Eureka Server

## Overview

The **Eureka Server** serves as the central **Service Registry** for the Healthcare Microservices platform.

In a microservices architecture, services should not communicate using hardcoded URLs because service instances can be scaled, restarted, or moved dynamically. Eureka provides **Service Discovery**, allowing services to register themselves and discover other services at runtime.

This project implements a production-ready Eureka Server using:

* Java 21
* Spring Boot 3.2.5
* Spring Cloud Netflix Eureka
* Spring Security
* Spring Boot Actuator
* Docker Multi-Stage Builds

---

## Architecture

```text
                    +------------------+
                    |   API Gateway    |
                    +--------+---------+
                             |
                             v
                    +------------------+
                    |  Eureka Server   |
                    |    Port 8761     |
                    +--------+---------+
                             ^
                             |
        -------------------------------------------------
        |                 |                |            |
        v                 v                v            v
+---------------+ +---------------+ +---------------+ +---------------+
| Patient Svc   | | Doctor Svc    | | Appointment   | | Billing Svc   |
| Registers     | | Registers     | | Registers     | | Registers     |
+---------------+ +---------------+ +---------------+ +---------------+
```

### Responsibilities

The Eureka Server acts as:

* A service registry for all microservices
* A discovery mechanism for inter-service communication
* A load-balancing support component
* A centralized view of running service instances

### Benefits

* No hardcoded service URLs
* Dynamic service discovery
* Improved scalability
* Better fault tolerance
* Supports multiple service instances

---

# Project Structure

```text
healthcare-microservices/
└── eureka-server/
    ├── src/
    │   └── main/
    │       ├── java/
    │       │   └── com/
    │       │       └── healthcare/
    │       │           └── eureka/
    │       │               ├── EurekaServerApplication.java
    │       │               └── config/
    │       │                   └── SecurityConfig.java
    │       └── resources/
    │           └── application.yml
    ├── Dockerfile
    ├── pom.xml
    └── README.md
```

---

# Technology Stack

| Technology      | Version        |
| --------------- | -------------- |
| Java            | 21             |
| Spring Boot     | 3.2.5          |
| Spring Cloud    | 2023.0.1       |
| Eureka Server   | Netflix Eureka |
| Maven           | 3.9+           |
| Docker          | Latest         |
| Spring Security | Included       |
| Spring Actuator | Included       |

---

# Key Features

## Service Registry

Allows all microservices to register themselves automatically during startup.

## Service Discovery

Other services can discover registered services without knowing physical locations.

## Dashboard

Provides a web-based dashboard showing:

* Registered services
* Instance health
* Registry information
* Server status

## Security

The Eureka dashboard is protected using:

* HTTP Basic Authentication
* Spring Security
* Restricted access to administrative endpoints

## Health Monitoring

Spring Boot Actuator provides:

* Health checks
* Metrics
* Application information

## Docker Support

Includes:

* Multi-stage build
* Optimized image size
* Non-root runtime user
* Container-friendly JVM configuration

---

# Configuration

## Server Port

```yaml
server:
  port: 8761
```

---

## Application Name

```yaml
spring:
  application:
    name: eureka-server
```

---

## Dashboard Credentials

```yaml
spring:
  security:
    user:
      name: admin
      password: admin123
```

### Login Credentials

| Field    | Value    |
| -------- | -------- |
| Username | admin    |
| Password | admin123 |

---

## Eureka Configuration

```yaml
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

### Why?

Since this application itself is the Eureka Server:

* It should not register with itself.
* It should not fetch registry information from itself.

---

## Self-Preservation Mode

```yaml
eureka:
  server:
    enable-self-preservation: false
```

### Development Environment

Self-preservation is disabled for easier testing.

### Production Recommendation

Enable it:

```yaml
eureka:
  server:
    enable-self-preservation: true
```

This prevents accidental removal of healthy services during network issues.

---

# Security Configuration

The dashboard is secured using Spring Security.

## Features

### HTTP Basic Authentication

Protects the Eureka Dashboard.

### CSRF Handling

```java
.ignoringRequestMatchers("/eureka/**")
```

CSRF protection is disabled only for Eureka endpoints because service registration uses POST requests.

### Open Actuator Endpoints

```java
.requestMatchers("/actuator/**").permitAll()
```

Allows monitoring tools to access health information.

---

# Running the Application

## Prerequisites

* Java 21
* Maven 3.9+
* Docker (optional)

---

## Clone Repository

```bash
git clone <repository-url>
cd healthcare-microservices/eureka-server
```

---

## Build

```bash
mvn clean package -DskipTests
```

---

## Run Locally

```bash
mvn spring-boot:run
```

---

## Verify Startup

### Health Endpoint

```bash
curl http://localhost:8761/actuator/health
```

Expected:

```json
{
  "status": "UP"
}
```

---

## Access Dashboard

Open:

```text
http://localhost:8761
```

Login using:

```text
Username: admin
Password: admin123
```

---

## Registry API

```bash
curl -u admin:admin123 http://localhost:8761/eureka/apps
```

---

# Docker Deployment

## Build Docker Image

```bash
docker build -t healthcare/eureka-server:1.0.0 .
```

---

## Run Container

```bash
docker run -d \
  --name eureka-server \
  -p 8761:8761 \
  healthcare/eureka-server:1.0.0
```

---

## Verify Container

```bash
docker ps
```

Open:

```text
http://localhost:8761
```

---

# Expected Startup Logs

```text
Started EurekaServerApplication in 4.3 seconds
Tomcat started on port(s): 8761
```

---

# Expected Dashboard State

Since no microservices have been registered yet, the dashboard should display:

```text
No instances currently registered with Eureka
```

This is expected during Phase 1.

---

# Monitoring Endpoints

## Health

```text
GET /actuator/health
```

---

## Metrics

```text
GET /actuator/metrics
```

---

## Application Info

```text
GET /actuator/info
```

---

# Production Recommendations

Before deploying to production:

### Enable Self-Preservation

```yaml
eureka:
  server:
    enable-self-preservation: true
```

### Externalize Credentials

Store credentials using:

* Environment Variables
* Kubernetes Secrets
* Docker Secrets
* Vault

### Enable HTTPS

Protect dashboard access using TLS certificates.

### Configure High Availability

Run multiple Eureka instances behind a load balancer.

### Add Centralized Logging

Recommended:

* ELK Stack
* OpenSearch
* Grafana Loki

---

# Phase 1 Completion Checklist

| Feature                   | Status    |
| ------------------------- | --------- |
| Eureka Service Registry   | Completed |
| Service Discovery         | Completed |
| Dashboard UI              | Completed |
| Spring Security           | Completed |
| HTTP Basic Authentication | Completed |
| Actuator Monitoring       | Completed |
| Docker Support            | Completed |
| Multi-stage Build         | Completed |
| Non-root Container User   | Completed |
| Health Endpoints          | Completed |

## Phase 1 Summary

| Item                | Value                  |
| ------------------- | ---------------------- |
| Service             | Eureka Server          |
| Port                | 8761                   |
| Dashboard           | http://localhost:8761  |
| Authentication      | HTTP Basic             |
| Username            | admin                  |
| Password            | admin123               |
| Self Preservation   | Disabled (Development) |
| Registered Services | None                   |
| Health Endpoint     | /actuator/health       |

---

**Phase 1 successfully establishes the Service Discovery foundation for the Healthcare Microservices Platform.**
