<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white" />
  <img src="https://img.shields.io/badge/AI%20Powered-FastAPI-009688?style=for-the-badge&logo=fastapi&logoColor=white" />
</p>

<h1 align="center">🏭 MobAI Warehouse Management System — Backend</h1>

<p align="center">
  <b>An intelligent, AI-augmented warehouse management REST API built with Spring Boot.</b><br/>
  Demand forecasting · Smart storage assignment · Optimized picking routes · Real-time monitoring
</p>

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL (or H2 for testing)
/
### Running the Application

- [Overview](#-overview)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Features](#-features)
- [Getting Started](#-getting-started)
- [API Reference](#-api-reference)
- [Security & Roles](#-security--roles)
- [AI Integration](#-ai-integration)
- [Database Schema](#-database-schema)
- [Docker Deployment](#-docker-deployment)
- [Project Structure](#-project-structure)
- [Default Credentials](#-default-credentials)

---

## 🔍 Overview

**MobAI** is a full-featured Warehouse Management System (WMS) backend that combines traditional warehouse operations with **AI/ML-powered decision support**. It manages the complete lifecycle of warehouse activities — from goods receipt to order picking, stock transfers, and delivery — while leveraging machine learning models for demand forecasting, intelligent storage placement, and optimized picking routes.

---

## 🛠 Tech Stack

| Layer              | Technology                                     |
| :----------------- | :--------------------------------------------- |
| **Framework**      | Spring Boot 3.2.2, Spring Cloud OpenFeign      |
| **Language**       | Java 17                                        |
| **Database**       | PostgreSQL 15 (prod) · MySQL (dev) · H2 (test) |
| **ORM**            | Spring Data JPA / Hibernate                    |
| **Security**       | Spring Security + JWT (jjwt 0.12.3) + BCrypt   |
| **AI Client**      | Spring WebFlux WebClient → FastAPI ML Service  |
| **API Docs**       | Springdoc OpenAPI 2.3.0 (Swagger UI)           |
| **Caching**        | Caffeine + Spring Cache                        |
| **CSV Import**     | Apache Commons CSV 1.10.0                      |
| **Object Mapping** | ModelMapper 3.2.0                              |
| **AOP Auditing**   | Spring AOP / AspectJ                           |
| **Monitoring**     | Spring Actuator                                |
| **Build**          | Maven · Lombok                                 |
| **Container**      | Docker (multi-stage) · Docker Compose          |

---

## 🏗 Architecture

```
┌─────────────┐     ┌──────────────┐     ┌─────────────────┐
│   Mobile /   │────▶│  Spring Boot  │────▶│   PostgreSQL    │
│   Web App    │◀────│   REST API   │◀────│   Database      │
└─────────────┘     └──────┬───────┘     └─────────────────┘
                           │
                           │  WebClient (async)
                           ▼
                    ┌──────────────┐
                    │  FastAPI AI  │
                    │  ML Service  │
                    │  (XGBoost,   │
                    │   Prophet)   │
                    └──────────────┘
```

**Design Patterns:**

- **Layered Architecture** — Controller → Service → Repository
- **DTO Pattern** — Separate request/response objects with `ApiResponse<T>` wrapper
- **Stock Ledger Pattern** — Append-only ledger with running balances & optimistic locking
- **Soft Deletes** — `active` flag on all major entities
- **AOP Audit Trail** — Automatic logging via AspectJ pointcuts
- **AI-in-the-Loop** — AI suggestions with human override capability (approve/override)

---

## ✨ Features

### 📦 Core Warehouse Operations

- **Goods Receipt** — Receive inventory with barcode scanning and automatic stock ledger updates
- **Stock Transfers** — Move products between locations with full traceability
- **Order Picking** — Pick products for orders with line-by-line execution
- **Delivery Management** — Outbound delivery processing
- **Stock Adjustments** — Manual inventory corrections with audit trail

### 🤖 AI-Powered Intelligence

- **Demand Forecasting** — XGBoost + Prophet ensemble predictions
- **Smart Storage Assignment** — AI-optimized location selection based on product characteristics
- **Picking Route Optimization** — Multi-chariot nearest-neighbor routing with congestion detection
- **Warehouse Simulation** — Chronological event processing for what-if analysis
- **Explainable AI** — Feature importance and prediction reasoning
- **Preparation Orders** — Automated order generation from demand forecasts

### 👥 User & Task Management

- **Role-Based Access Control** — Admin, Supervisor, Employee with fine-grained permissions
- **Employee Workflow Engine** — Assign → Start → Execute → Report → Complete lifecycle
- **Task Discrepancy Reporting** — Report and resolve issues (missing, damaged, excess, etc.)
- **Real-time Employee Positioning** — Track employee locations in the warehouse

### 📊 Monitoring & Analytics

- **Admin Dashboard** — Comprehensive overview with KPIs and stock alerts
- **Employee Dashboard** — Personal task stats and performance metrics
- **Active Operations Monitor** — Real-time operation tracking
- **Chariot Status Tracking** — Fleet management and utilization
- **Audit Logs** — Complete action trail with user attribution

### 🏷 Product & Inventory

- **Multi-Barcode Support** — EAN13, CODE128, QR per product
- **Barcode Scanning API** — Decode barcodes to identify products, locations, or chariots
- **Stock Alerts** — Automatic low-stock and overstock warnings
- **Reorder Policies** — Configurable safety stock, min order quantity, lot sizes
- **Supply Lead Times** — Per-product lead time tracking

---

## 🚀 Getting Started

### Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **PostgreSQL 15** (or Docker)

### Option 1: Docker Compose (Recommended)

```bash
# Clone the repository
git clone https://github.com/your-org/mobai-warehouse-backend.git
cd mobai-warehouse-backend

# Start everything (PostgreSQL + Backend)
docker-compose up --build -d

# API available at http://localhost:8080/api
# Swagger UI at http://localhost:8080/api/swagger-ui.html
```

### Option 2: Local Development

```bash
# 1. Start PostgreSQL (or use Docker for DB only)
docker-compose up db -d

# 2. Run the application
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Or on Windows
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

### Environment Variables

| Variable                 | Default                    | Description                     |
| :----------------------- | :------------------------- | :------------------------------ |
| `DB_HOST`                | `localhost`                | Database host                   |
| `DB_PORT`                | `5432`                     | Database port                   |
| `DB_NAME`                | `mobai_warehouse`          | Database name                   |
| `DB_USERNAME`            | `postgres`                 | Database user                   |
| `DB_PASSWORD`            | `changeme123`              | Database password               |
| `JWT_SECRET`             | _(set in config)_          | JWT signing key                 |
| `AI_SERVICE_URL`         | `http://4.251.194.25:8000` | AI/ML service URL               |
| `SPRING_PROFILES_ACTIVE` | `dev`                      | Active profile (`dev` / `prod`) |

---

## 📡 API Reference

> **Base URL:** `http://localhost:8080/api`  
> **Swagger UI:** `http://localhost:8080/api/swagger-ui.html`

### Authentication

| Method | Endpoint         | Description          |
| :----- | :--------------- | :------------------- |
| `POST` | `/auth/login`    | Login → JWT tokens   |
| `POST` | `/auth/register` | Register new user    |
| `POST` | `/auth/refresh`  | Refresh access token |
| `POST` | `/auth/logout`   | Invalidate token     |

### Products

| Method   | Endpoint                  | Auth           |
| :------- | :------------------------ | :------------- |
| `GET`    | `/products`               | `product:read` |
| `GET`    | `/products/{id}`          | `product:read` |
| `GET`    | `/products/search?query=` | `product:read` |
| `POST`   | `/admin/products`         | ADMIN          |
| `PUT`    | `/admin/products/{id}`    | ADMIN          |
| `DELETE` | `/admin/products/{id}`    | ADMIN          |

### Warehouses & Locations

| Method | Endpoint            | Auth            |
| :----- | :------------------ | :-------------- |
| `GET`  | `/warehouses`       | `location:read` |
| `GET`  | `/locations`        | `location:read` |
| `POST` | `/admin/warehouses` | ADMIN           |
| `POST` | `/admin/locations`  | ADMIN           |

### Tasks & Operations

| Method | Endpoint                    | Auth              |
| :----- | :-------------------------- | :---------------- |
| `GET`  | `/tasks/my-tasks`           | `operation:read`  |
| `POST` | `/tasks/create`             | `operation:write` |
| `PUT`  | `/tasks/{id}/assign`        | `operation:write` |
| `POST` | `/operations/start`         | `operation:write` |
| `POST` | `/operations/execute-line`  | `operation:write` |
| `POST` | `/operations/report-issue`  | `operation:write` |
| `POST` | `/operations/{id}/complete` | `operation:write` |

### Inventory & Stock

| Method | Endpoint                        | Auth             |
| :----- | :------------------------------ | :--------------- |
| `GET`  | `/inventory/stock/product/{id}` | `inventory:read` |
| `GET`  | `/stock/summary`                | `inventory:read` |
| `GET`  | `/stock/ledger`                 | `inventory:read` |
| `POST` | `/admin/inventory/adjustment`   | ADMIN            |

### AI Services

| Method | Endpoint                | Description               |
| :----- | :---------------------- | :------------------------ |
| `POST` | `/ai/predict`           | Demand prediction         |
| `POST` | `/ai/assign-storage`    | Smart storage location    |
| `POST` | `/ai/optimize-picking`  | Route optimization        |
| `POST` | `/ai/simulate`          | Warehouse simulation      |
| `POST` | `/ai/explain`           | Prediction explainability |
| `POST` | `/ai/generate-forecast` | Bulk CSV forecast         |

### Dashboard & Monitoring

| Method | Endpoint                           | Auth             |
| :----- | :--------------------------------- | :--------------- |
| `GET`  | `/dashboard/employee`              | `dashboard:read` |
| `GET`  | `/admin/dashboard`                 | ADMIN            |
| `GET`  | `/monitoring/active-operations`    | `dashboard:read` |
| `GET`  | `/admin/reports/stock-movements`   | ADMIN            |
| `GET`  | `/admin/reports/user-productivity` | ADMIN            |

<details>
<summary><b>View all 100+ endpoints →</b></summary>

See the full [API Documentation](docs/API_DOCUMENTATION.md) for the complete endpoint reference including audit logs, discrepancies, AI decisions, barcode scanning, chariots, orders, and more.

</details>

---

## 🔐 Security & Roles

### Authentication Flow

```
Client → POST /auth/login (credentials) → JWT Access Token (24h) + Refresh Token (7d)
Client → Authorization: Bearer <token> → Protected endpoints
```

### Role Hierarchy & Permissions

| Permission        | Admin | Supervisor | Employee |
| :---------------- | :---: | :--------: | :------: |
| `product:read`    |  ✅   |     ✅     |    ✅    |
| `product:write`   |  ✅   |     ✅     |    ❌    |
| `inventory:read`  |  ✅   |     ✅     |    ✅    |
| `inventory:write` |  ✅   |     ✅     |    ❌    |
| `location:read`   |  ✅   |     ✅     |    ❌    |
| `location:write`  |  ✅   |     ✅     |    ❌    |
| `operation:read`  |  ✅   |     ✅     |    ✅    |
| `operation:write` |  ✅   |     ✅     |    ✅    |
| `user:read`       |  ✅   |     ✅     |    ❌    |
| `user:write`      |  ✅   |     ❌     |    ❌    |
| `dashboard:read`  |  ✅   |     ✅     |    ✅    |

---

## 🤖 AI Integration

The backend proxies requests to an external **FastAPI ML service** via async WebClient:

```
Spring Boot Backend  ←→  FastAPI AI Service (Python)
                          ├── XGBoost (Demand Forecasting)
                          ├── Prophet (Time Series)
                          ├── Storage Optimizer
                          ├── Picking Route Planner
                          └── Simulation Engine
```

**AI Decision Flow:** AI suggestions are logged in `ai_decision_logs` with confidence scores. Supervisors and admins can **approve** or **override** AI decisions with documented reasoning — enabling a human-in-the-loop workflow.

---

## 🗄 Database Schema

16 tables across 5 domains:

```
USERS & AUTH              PRODUCTS & INVENTORY          OPERATIONS
├── users                 ├── products                  ├── transactions
├── audit_logs            ├── code_barre                ├── lignes_transaction
                          ├── stock_ledger              ├── chariots
WAREHOUSE                 ├── historique_demande         ├── task_discrepancies
├── entrepot              ├── politique_reappro
├── emplacements          ├── delais_appro              AI
                          ├── cmd_achat_ouvertes         ├── ai_decision_logs
```

---

## 🐳 Docker Deployment

### Multi-Stage Build

```dockerfile
# Stage 1: Build with JDK 17
FROM eclipse-temurin:17-jdk-alpine AS build
# Stage 2: Run with JRE 17 (G1GC, 128-256MB heap)
FROM eclipse-temurin:17-jre-alpine
```

### Docker Compose

```bash
# Start all services
docker-compose up --build -d

# View logs
docker-compose logs -f backend

# Stop
docker-compose down

# Stop and remove data
docker-compose down -v
```

**Services:**

| Service   | Image                | Port   |
| :-------- | :------------------- | :----- |
| `db`      | `postgres:15-alpine` | `5432` |
| `backend` | Custom build         | `8080` |

---

## 📁 Project Structure

```
src/main/java/org/example/backend/
├── BackendApplication.java          # Entry point
├── client/                          # AI service WebClient
├── config/                          # Security, Audit AOP, Cache, Seeder
├── controller/                      # 33 REST controllers
├── dto/
│   ├── request/                     # 39 request DTOs
│   └── response/                    # 8 response DTOs
├── entity/                          # 16 JPA entities
├── enums/                           # 14 enums (roles, statuses, types)
├── exception/                       # Global handler + custom exceptions
├── repository/                      # 16 Spring Data repositories
├── security/                        # JWT filter, service, user details
├── service/
│   ├── [interfaces]                 # 17 service interfaces
│   └── impl/                        # 16 implementations
└── util/                            # Helpers
```

---

## 🔑 Default Credentials

| Username      | Password        | Role       |
| :------------ | :-------------- | :--------- |
| `admin`       | `admin123`      | ADMIN      |
| `supervisor1` | `supervisor123` | SUPERVISOR |
| `employee1`   | `employee123`   | EMPLOYEE   |

> ⚠️ **Change default credentials before deploying to production!**

---

## 📄 License

This project was developed as part of an academic/professional project for warehouse management optimization using AI.

---

<p align="center">
  <b>Built with ❤️ using Spring Boot & AI</b>
</p>
