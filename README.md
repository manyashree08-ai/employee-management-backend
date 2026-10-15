# Employee Management System

A full-stack web application for managing employee records, built for the Kloudspot technical assessment (Project 1).

| Layer    | Technology                                                                       |
|----------|----------------------------------------------------------------------------------|
| Backend  | Java 17+, Spring Boot 3.3, Spring Security 6 (JWT), Spring Data JPA, Maven, Swagger/OpenAPI |
| Frontend | React 18 + Vite, React Router 6, Axios, Context API                              |
| Database | MySQL 8                                                                          |
| Testing  | JUnit 5, Mockito (backend), Vitest (frontend)                                    |

## Features

### Required Core Features
- **Employee CRUD**: Add, view details, edit, delete (with confirmation dialog), and list employees.
- **Fields**: Employee ID (`EMP0001`), Full Name, Email, Phone, Department, Designation, Date of Joining, Salary, Status (Active/Inactive).
- **Search & Filters**: Real-time debounced search by name or email, filter by department and status.
- **Pagination & Sorting**: Server-side pagination with selectable page sizes and click-to-sort columns.
- **Responsive UI**: Responsive desktop table transforms into mobile cards on small screens.
- **Validation**: Full client-side & server-side validation with actionable error feedback.

### Bonus Enhancements (Implemented)
- **Login & JWT Authentication**: Spring Security with stateless Bearer JWT tokens, login page, protected routes, and auto-seeded administrator.
- **Export to CSV**: Export employee records with RFC 4180 escaping and Excel UTF-8 BOM, respecting active search and filters.
- **Department Management**: Dynamic distinct departments extracted from records for filter dropdowns.
- **Docker Compose**: One-command orchestration (`docker compose up --build`) for MySQL, Spring Boot Backend, and Nginx Frontend.
- **Automated Frontend Tests**: Vitest test suite testing validation rules, formatters, and error handling.
- **Automated Backend Tests**: Service-layer tests covering `EmployeeService`, `AuthService`, `JwtService`, and `CsvExporter`.

---

## Default Administrator Credentials
On initial startup, if the database has no users, an administrator account is automatically seeded:
- **Username**: `admin`
- **Password**: `admin123`

---

## Project Structure

```
Assignment/
├── Backend/                 <- Spring Boot REST API
│   ├── mvnw / mvnw.cmd      <- Maven Wrapper (no separate Maven install required)
│   ├── pom.xml
│   ├── Dockerfile
│   ├── docker-compose.yml   <- Multi-container setup (MySQL + Backend + Frontend)
│   ├── .env / .env.example  <- Configurable environment variables
│   ├── database/schema.sql  <- Optional manual MySQL schema
│   ├── docs/                <- Postman Collection
│   └── src/                 <- Java source and unit tests
└── Frontend/                <- Vite + React Web Application
    ├── .env / .env.example  <- Frontend environment settings
    ├── package.json
    ├── Dockerfile
    ├── nginx.conf
    └── src/                 <- React components, pages, context, and tests
```

---

## Prerequisites

### Option A: Docker (Recommended - No manual DB installation needed)
- Docker Desktop with Docker Compose

### Option B: Local Development
- JDK 17 or later (JDK 21 supported)
- Node.js 18+ and npm
- MySQL 8 running on `localhost:3306` (or set `DB_PORT`/`DB_PASSWORD` in `.env`)

---

## Running with Docker (Option A)

From the `Backend` directory:

```bash
docker compose up --build
```

| Service    | URL                                   |
|------------|---------------------------------------|
| Frontend   | http://localhost:3000                 |
| Backend    | http://localhost:8080                 |
| Swagger UI | http://localhost:8080/swagger-ui.html |

Stop with `Ctrl+C`. To remove containers and data volumes: `docker compose down -v`.

---

## Running Locally (Option B)

### 1. Database
Ensure MySQL is running. The application automatically creates the `employee_db` database and tables on startup (`createDatabaseIfNotExist=true` and Hibernate DDL `update`), and seeds 15 demo employees.

To customize credentials, edit `.env` or set environment variables:
| Variable              | Default                                       | Description                         |
|-----------------------|-----------------------------------------------|-------------------------------------|
| `DB_HOST`             | `localhost`                                   | MySQL host                          |
| `DB_PORT`             | `3306`                                        | MySQL port                          |
| `DB_NAME`             | `employee_db`                                 | Database name                       |
| `DB_USERNAME`         | `root`                                        | Database user                       |
| `DB_PASSWORD`         | `root`                                        | Database password                   |
| `APP_ADMIN_USERNAME`  | `admin`                                       | Default seeded admin username       |
| `APP_ADMIN_PASSWORD`  | `admin123`                                    | Default seeded admin password       |
| `JWT_SECRET`          | `c3VwZXItc2VjcmV0LWtleS1mb3It...`             | 256-bit HMAC secret                 |

### 2. Start Backend

From the `Backend` directory, use the included Maven wrapper:

```powershell
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

API is available at `http://localhost:8080`.
Interactive Swagger UI is at `http://localhost:8080/swagger-ui.html`.

### 3. Start Frontend

In a separate terminal, from the `Frontend` directory:

```powershell
npm install
npm run dev
```

Open `http://localhost:5173` in your browser.
Log in using `admin` / `admin123` (or click "Autofill demo credentials").

---

## Running Automated Tests

### Backend Unit Tests
```powershell
cd Backend
.\mvnw.cmd test
```
Runs 26 unit tests verifying `EmployeeService`, `AuthService`, `JwtService`, and `CsvExporter`.

### Frontend Vitest Tests
```powershell
cd Frontend
npm test
```
Runs 15 frontend unit tests covering form validations, data formatters, and API error parsing.

---

## REST API Reference

All `/api/employees/**` endpoints require an `Authorization: Bearer <token>` header obtained from `/api/auth/login`.

### Authentication Endpoints
| Method | Endpoint          | Access | Description                          |
|--------|-------------------|--------|--------------------------------------|
| POST   | `/api/auth/login` | Public | Authenticates credentials; returns JWT |
| GET    | `/api/auth/me`    | Bearer | Returns current authenticated user   |

### Employee Endpoints
| Method | Endpoint                      | Access | Description                               |
|--------|-------------------------------|--------|-------------------------------------------|
| POST   | `/api/employees`              | Bearer | Create a new employee                     |
| GET    | `/api/employees`              | Bearer | Paginated search, filter, and sort        |
| GET    | `/api/employees/{id}`         | Bearer | Get employee details by ID                |
| PUT    | `/api/employees/{id}`         | Bearer | Update an employee record                 |
| DELETE | `/api/employees/{id}`         | Bearer | Delete an employee                        |
| GET    | `/api/employees/departments`  | Bearer | List distinct department names            |
| GET    | `/api/employees/export`       | Bearer | Export filtered employees as CSV file     |

### Postman Collection
Import `Backend/docs/Employee-Management.postman_collection.json` into Postman.
1. Run **Authentication > Log in (admin)**: the test script automatically captures the returned JWT and stores it in the `{{token}}` collection variable.
2. All other requests are pre-configured to use Bearer authentication with `{{token}}`.
