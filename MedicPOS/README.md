# FarmaMed - Hospital Pharmacy Management System

[![Build Status](https://github.com/your-org/farmamed/workflows/CI/CD%20Pipeline/badge.svg)](https://github.com/your-org/farmamed/actions)
[![Coverage](https://img.shields.io/badge/coverage-70%25-brightgreen)](https://github.com/your-org/farmamed/actions)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-17-orange)](https://openjdk.java.net/)
[![Angular](https://img.shields.io/badge/angular-17-red)](https://angular.io/)

> **FarmaMed** - Hospital Pharmacy Management System for Hospital Santa Maria del Socorro, Ica, Peru

## Overview

FarmaMed is a comprehensive hospital pharmacy management system designed to ensure medication safety, traceability, and regulatory compliance. Built with modern technologies following clean architecture principles.

## Features

### Core Modules
| Module | Description |
|--------|-------------|
| 🔐 **Auth & RBAC** | JWT authentication, 4 roles (Jefe, Tecnico, Logistica, Admin TI) |
| 📋 **Medicamentos** | Catalog management, stock levels, QR codes |
| 📦 **Inventario & Lotes** | FEFO dispensing, expiration tracking, semaphore alerts |
| 💊 **Dispensación** | POS dispensing, prescription validation, ticket generation |
| 👥 **Pacientes** | Patient registry, DNI validation via Factiliza (RENIEC) |
| 🚚 **Abastecimiento** | Supplier management, purchase orders, stock suggestions |
| ⚠️ **Alertas** | Expiration alerts, stock alerts, FEFO exceptions |
| 📊 **Reportes** | Consumption, inventory, bajas, purchase orders (PDF/Excel) |
| 🔍 **Auditoría** | Complete traceability of all operations |
| ⚙️ **Configuración** | Semaphore thresholds, system parameters |

### Key Features
- **FEFO Dispensing**: First Expired, First Out automatic lot selection
- **DNI Validation**: Real-time RENIEC consultation via Factiliza API
- **QR/Barcode Generation**: Medications, lots, dispensing tickets
- **Full Audit Trail**: Immutable audit log for all operations
- **Responsive UI**: Mobile-first Angular 17 + Material Design 3
- **Offline-Ready**: Local mock mode for development

## Tech Stack

### Backend
| Technology | Version |
|------------|---------|
| Java | 17 |
| Spring Boot | 3.2.x |
| Spring Security | 6.x (JWT) |
| Spring Data JPA | 3.x |
| PostgreSQL | 16 |
| WebFlux | Reactive Factiliza client |
| Caffeine | In-memory caching |
| Bucket4j | Rate limiting |
| iText 7 / Apache POI | PDF/Excel export |

### Frontend
| Technology | Version |
|------------|---------|
| Angular | 17.x |
| Material Design | 3 (Custom tokens) |
| RxJS | 7.x |
| TypeScript | 5.x |
| Karma/Jasmine | Unit testing |

### Infrastructure
| Tool | Purpose |
|------|---------|
| Maven | Build & dependency management |
| GitHub Actions | CI/CD |
| JaCoCo | Code coverage |
| Spotless | Code formatting |
| OWASP Dependency Check | Security scanning |
| Docker | Containerization |

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.9+
- Node.js 20+
- PostgreSQL 16+
- Docker (optional)

### Backend
```bash
cd MedicPOS
cp .env.example .env
# Edit .env with your configuration
mvn spring-boot:run
```

### Frontend
```bash
cd MedicPOS/frontend
npm install
npm start
```

### Docker
```bash
docker-compose up -d
```

## API Documentation

Swagger UI available at: `http://localhost:8080/swagger-ui.html`

## Testing

```bash
# Backend tests with coverage
cd MedicPOS
mvn clean verify

# Frontend tests
cd frontend
npm run test -- --watch=false --browsers=ChromeHeadless
```

## CI/CD Pipeline

GitHub Actions workflow (`.github/workflows/ci.yml`):
- ✅ Unit & Integration Tests (JaCoCo ≥ 70%)
- ✅ Build & Package
- ✅ Code Quality (Spotless, Checkstyle)
- ✅ Security (OWASP Dependency Check)
- ✅ Frontend Build & Tests
- ✅ Dependency Vulnerability Scan

## Security

- JWT authentication with RS256
- BCrypt password hashing
- Role-based access control (4 roles)
- Rate limiting (Bucket4j)
- Security headers (CSP, HSTS, X-Frame-Options)
- Rate limiting on auth endpoints
- CORS configuration
- OWASP Dependency Check in CI

## Project Structure

```
MedicPOS/
├── src/main/java/com/medicalstore/pos/
│   ├── config/           # Configuration classes
│   ├── controller/       # REST controllers
│   ├── dto/              # DTOs (request/response)
│   ├── entity/           # JPA entities
│   ├── repository/       # Spring Data repositories
│   ├── service/          # Business logic
│   ├── security/         # JWT, filters, config
│   └── exception/        # Global exception handling
├── src/test/             # Unit & integration tests
├── frontend/             # Angular application
│   ├── src/app/
│   │   ├── core/         # Core services, guards
│   │   ├── modules/      # Feature modules
│   │   └── shared/       # Shared components
│   └── ...
├── .github/workflows/    # CI/CD pipelines
├── docs/                 # Documentation
└── docker-compose.yml
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DATABASE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/farmamed` |
| `DATABASE_USERNAME` | DB username | `farmamed_app` |
| `DATABASE_PASSWORD` | DB password | - |
| `JWT_SECRET` | JWT signing secret | - |
| `FACTILIZA_TOKEN` | Factiliza API token | - |
| `FACTILIZA_MOCK_ENABLED` | Enable mock mode | `false` |
| `RATE_LIMIT_RPM` | Requests per minute | `100` |
| `CORS_ORIGINS` | Allowed origins | `http://localhost:4200` |

## License

MIT License - see [LICENSE](LICENSE) for details.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## Support

- 📧 Email: support@farmamed.org
- 🐛 Issues: [GitHub Issues](https://github.com/your-org/farmamed/issues)
- 📖 Docs: [Wiki](https://github.com/your-org/farmamed/wiki)

---

**FarmaMed** - Making hospital pharmacy management safe, efficient, and compliant. 🏥