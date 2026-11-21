# Contributing to FarmaMed

Thank you for contributing to FarmaMed! This document provides guidelines for contributing to the project.

## Table of Contents
- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Code Style](#code-style)
- [Testing](#testing)
- [Commit Messages](#commit-messages)
- [Pull Requests](#pull-requests)
- [Security](#security)

## Code of Conduct

This project follows the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md). By participating, you agree to uphold this code.

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.9+
- Node.js 20+ and npm
- PostgreSQL 16+
- Docker (optional)

### Local Development Setup

```bash
# Clone repository
git clone https://github.com/your-org/farmamed.git
cd farmamed

# Backend
cd MedicPOS
mvn clean install

# Run backend
mvn spring-boot:run

# Frontend (new terminal)
cd frontend
npm install
npm start
```

### Environment Variables

Copy `.env.example` to `.env` and configure:

```bash
cp .env.example .env
# Edit .env with your configuration
```

## Development Workflow

### Branching Strategy

We follow **GitFlow** with these branches:

| Branch | Purpose |
|--------|---------|
| `main` | Production-ready code |
| `develop` | Integration branch |
| `feature/*` | New features |
| `fix/*` | Bug fixes |
| `hotfix/*` | Critical production fixes |
| `release/*` | Release preparation |

### Creating a Feature

```bash
# Start from develop
git checkout develop
git pull origin develop

# Create feature branch
git checkout -b feature/JIRA-123-add-feature

# Make changes, commit with conventional messages
git add .
git commit -m "feat(module): add new feature"

# Push and create PR
git push origin feature/JIRA-123-add-feature
```

## Code Style

### Java (Backend)

We use **Spotless** with **Google Java Format**:

```bash
# Check formatting
mvn spotless:check

# Auto-format
mvn spotless:apply
```

Rules:
- 4-space indentation
- 100-character line limit
- Google Java Format style
- License header required
- Import ordering: java, javax, org, com

### TypeScript/Angular (Frontend)

```bash
# Lint
npm run lint

# Format
npm run format
```

Rules:
- 2-space indentation
- 100-character line limit
- Single quotes
- Trailing commas
- Prettier + ESLint

## Testing

### Backend Tests

```bash
# Run all tests
mvn test

# Run with coverage
mvn verify jacoco:report

# Run specific test
mvn test -Dtest=AuthServiceTest
```

### Frontend Tests

```bash
# Unit tests
npm run test

# E2E tests
npm run e2e

# Watch mode
npm run test:watch
```

### Test Requirements

| Metric | Target |
|--------|--------|
| Unit test coverage | ≥ 70% |
| Branch coverage | ≥ 60% |
| Integration tests | All critical paths |

### Writing Tests (TDD)

Follow **Red-Green-Refactor**:

```java
// 1. RED - Write failing test first
@Test
void crearPaciente_debeLanzarExcepcion_cuandoDniDuplicado() {
    when(repo.existsByDni("12345678")).thenReturn(true);
    assertThrows(DniDuplicadoException.class, 
        () -> service.crearPaciente("12345678", "Juan", "Perez"));
}

// 2. GREEN - Write minimal code to pass
public void crearPaciente(String dni, String nombres, String apellidos) {
    if (repo.existsByDni(dni)) throw new DniDuplicadoException();
    repo.save(new Paciente(dni, nombres, apellidos));
}

// 3. REFACTOR - Improve without breaking tests
```

## Commit Messages

Follow **Conventional Commits**:

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

### Types

| Type | Description |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation |
| `style` | Formatting |
| `refactor` | Refactoring |
| `test` | Tests |
| `chore` | Maintenance |
| `perf` | Performance |
| `ci` | CI/CD |
| `build` | Build system |
| `security` | Security |

### Examples

```
feat(auth): add JWT token refresh endpoint
fix(dispensacion): fix FEFO logic for expired lotes
test(dni): add unit tests for DniService
docs(readme): update API documentation
ci(github): add CI/CD pipeline
security(rate-limit): add login rate limiting
```

## Pull Requests

### PR Requirements

- [ ] All tests pass (CI green)
- [ ] Code coverage ≥ 70%
- [ ] No linting errors
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No merge conflicts
- [ ] 2 approvals from code owners

### PR Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation
- [ ] Refactoring
- [ ] Tests

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests pass
- [ ] Manual testing performed

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Tests pass locally
- [ ] Documentation updated
- [ ] No breaking changes (or documented)
```

## Security

### Reporting Vulnerabilities

**DO NOT** create public issues for security vulnerabilities.

Email: security@farmamed.org

### Security Checklist

- [ ] No secrets in code
- [ ] Input validation on all endpoints
- [ ] SQL injection prevention (parameterized queries)
- [ ] XSS prevention (CSP headers)
- [ ] CSRF protection (SameSite cookies)
- [ ] Rate limiting on auth endpoints
- [ ] Dependency scanning (OWASP)

### Reporting Security Issues

1. Email security@farmamed.org
2. Include: Description, Steps to reproduce, Impact, Suggested fix
3. We'll respond within 48 hours

---

Thank you for contributing to FarmaMed! 🏥