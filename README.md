# 🥇 MetalBroker

> A production-style Spring Boot backend for real-time precious metals trading, market data ingestion, wallet management, and historical price analytics.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8-blue)
![Redis](https://img.shields.io/badge/Redis-Cache-red)
![NATS](https://img.shields.io/badge/NATS-Messaging-green)
![Flyway](https://img.shields.io/badge/Flyway-Database-brown)
![JWT](https://img.shields.io/badge/Auth-JWT-success)
![Docker](https://img.shields.io/badge/Docker-Compose-blue)

---

# 📌 Overview

MetalBroker is a backend microservice that simulates a digital precious metals trading platform.

The application periodically ingests live metal prices, stores historical and intraday market data, allows authenticated users to manage wallets and execute trades, and exposes REST APIs secured with JWT authentication.

The project demonstrates enterprise backend development using layered architecture, asynchronous messaging, caching, scheduled jobs, protocol buffers, and clean separation of responsibilities.

---

# ✨ Features

## Authentication

- JWT Authentication
- Login endpoint
- Secure REST APIs
- Custom Authentication Filters
- Role-based authorization support

---

## Wallet Management

- Create wallet
- Fetch wallet details
- Manage wallet balances
- Wallet metal holdings

---

## Trading

- Buy metals
- Sell metals
- Trade history
- Trade validation
- Portfolio updates

---

## Market Data

- Live metal prices
- Latest spot prices
- Historical daily prices
- Intraday (5-minute) tick storage
- Price analytics

---

## Scheduled Jobs

- Automatic market feed ingestion
- Historical data updates
- Cleanup jobs
- Background processing

---

## Event Driven Architecture

Uses **NATS Messaging** for asynchronous processing.

Publishers

- Rate ingestion publisher

Consumers

- History Consumer
- Intraday Consumer
- Cleanup Consumer

---

## Database

- MySQL
- Flyway migrations
- JDBC DAO Layer
- Versioned schema migrations

---

## Caching

- Redis Cache
- Cached market data
- Improved API performance

---

## Serialization

- Google Protocol Buffers
- JSON ↔ Proto mapping utilities

---

# 🏗 Architecture

```
                   External Metal Feed
                           │
                           ▼
                GoldBrokerApiClient
                           │
                           ▼
                 Rate Ingestion Service
                           │
               Scheduled Fetch Jobs
                           │
                 Publish Events (NATS)
                           │
          ┌────────────────┼───────────────┐
          ▼                ▼               ▼
 History Consumer   Intraday Consumer   Cleanup Consumer
          │                │               │
          └──────────────┬─────────────────┘
                         ▼
                     MySQL Database
                         │
                    DAO Layer (JDBC)
                         │
                    Service Layer
                         │
                    Facade Layer
                         │
                 REST Controllers
                         │
                     JWT Security
                         │
                     Client APIs
```

---

# 🛠 Tech Stack

| Category | Technology |
|------------|------------|
| Language | Java 21 |
| Framework | Spring Boot |
| Database | MySQL |
| Cache | Redis |
| Messaging | NATS |
| Security | Spring Security + JWT |
| Migration | Flyway |
| Serialization | Protocol Buffers |
| Scheduler | Spring Scheduling |
| Build Tool | Gradle |
| Containerization | Docker Compose |

---

# 📂 Project Structure

```
src/main/java
│
├── config
├── controller
├── facade
├── service
├── dao
├── security
├── schedulers
├── feed
├── nats
├── utils
└── domain
```

Architecture follows

```
Controller
      ↓
Facade
      ↓
Service
      ↓
DAO
      ↓
Database
```

This separation improves maintainability, testing, and scalability.

---

# 🔐 Authentication Flow

```
Client
   │
Login
   │
   ▼
JWT Token
   │
Include Token in Authorization Header
   │
Bearer <token>
   │
Protected APIs
```

---

# ⚙ Environment Variables

Create a `.env` file (or configure your IDE) with the following variables.

```properties
DB_NAME=metalbroker_dev

DB_URL=jdbc:mysql://localhost:3306/metalbroker_dev?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true

DB_USER=root
DB_USERNAME=root
DB_PASSWORD=root_secure_password123

APP_USER=admin_user_here
APP_PASSWORD=any_secure_password_here

JWT_SECRET=superSecretKeyMustBeVeryLongToPassTheFrameworkVerificationBlockForSigningTokens123!
```

---

# 🚀 Running the Project

## 1 Clone Repository

```bash
git clone https://github.com/yourusername/metalbroker.git

cd metalbroker
```

---

## 2 Start Infrastructure

```bash
docker compose up -d
```

This starts:

- MySQL
- Redis
- NATS

---

## 3 Build

Linux/Mac

```bash
./gradlew build
```

Windows

```cmd
gradlew.bat build
```

---

## 4 Run

```bash
./gradlew bootRun
```

or

Run

```
MetalBrokerApplication.java
```

from your IDE.

---

# 🗄 Database Migrations

Database schema is managed using Flyway.

Current migrations include:

- Users
- Wallets
- Trades
- Market Tables
- Seed Metals

Located in

```
src/main/resources/db/migration
```

---

# 📡 REST APIs

## Authentication

```
POST /auth/login
```

---

## Metals

```
GET /metals

GET /metals/latest
```

---

## Rates

```
GET /rates

GET /rates/history
```

---

## Wallet

```
GET /wallet

POST /wallet
```

---

## Trade

```
POST /trade/buy

POST /trade/sell

GET /trade/history
```

---

# 📈 Design Highlights

✔ Layered Architecture

✔ SOLID Principles

✔ Dependency Injection

✔ Event-driven Processing

✔ JWT Authentication

✔ Redis Caching

✔ Scheduled Background Jobs

✔ Protocol Buffers

✔ Flyway Versioning

✔ Clean Exception Handling

✔ RESTful APIs

✔ Docker Support

---


Current tests include

- Scheduler Tests
- Spring Boot Context Tests

---

# 📸 Future Improvements

- Kubernetes Deployment
- Prometheus Metrics
- Grafana Dashboard
- Swagger/OpenAPI Documentation
- Kafka Integration
- Elasticsearch
- CI/CD Pipeline
- OAuth2 Authentication
- Multi-currency Support
- WebSocket Live Price Streaming

---

# 👨‍💻 Author

**Ayush Mishra**

Backend Developer | Java | Spring Boot | Distributed Systems

LinkedIn:
> [https://linkedin.com/in/your-profile](https://www.linkedin.com/in/ayush-mishra-a122cs/)

GitHub:
> https://github.com/ayushmishra90

---

# ⭐ If you found this project interesting

Feel free to **Star** the repository and connect with me on GitHub.
