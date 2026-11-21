# Branch Protection Configuration

This document describes the branch protection rules for the FarmaMed repository.

## Main Branch Protection

### Configuration (GitHub Settings)

```
Repository Settings > Branches > Add Branch Protection Rule
```

### Branch Name Pattern
```
main
```

### Protection Rules

| Rule | Setting | Value |
|------|---------|-------|
| **Require pull request reviews before merging** | ✅ Enabled | |
| | Required approving reviews | 2 |
| | Dismiss stale reviews when new commits are pushed | ✅ Enabled |
| | Require review from code owners | ✅ Enabled |
| **Require status checks to pass before merging** | ✅ Enabled | |
| | Require branches to be up to date before merging | ✅ Enabled |
| | Required status checks | `test`, `build`, `code-quality`, `dependency-check`, `frontend-build` |
| **Require conversation resolution before merging** | ✅ Enabled | |
| **Require signed commits** | ✅ Enabled | |
| **Require linear history** | ✅ Enabled | |
| **Include administrators** | ✅ Enabled | |
| **Restrict who can push** | ✅ Enabled | Only: `maintain` team |
| **Allow force pushes** | ❌ Disabled | |
| **Allow deletions** | ❌ Disabled | |

### Required Status Checks

| Check | Description | Required |
|-------|-------------|----------|
| `test` | Unit & Integration Tests | ✅ |
| `build` | Maven Build & Package | ✅ |
| `code-quality` | Spotless + Checkstyle | ✅ |
| `dependency-check` | OWASP Dependency Check | ✅ |
| `frontend-build` | Frontend Build & Tests | ✅ |

## Develop Branch Protection

### Configuration

```
Branch pattern: develop
```

### Protection Rules

| Rule | Setting |
|------|---------|
| Require PR reviews | ✅ (1 approval) |
| Require status checks | ✅ |
| Require conversation resolution | ✅ |
| Require signed commits | ✅ |
| Allow force pushes | ❌ |

## Release Branch Protection

### Pattern
```
release/*
```

### Protection Rules

| Rule | Setting |
|------|---------|
| Require PR reviews | ✅ (1 approval) |
| Require status checks | ✅ |
| Allow force pushes | ❌ |

## Hotfix Branch Protection

### Pattern
```
hotfix/*
```

### Protection Rules

| Rule | Setting |
|------|---------|
| Require PR reviews | ✅ (1 approval) |
| Require status checks | ✅ |
| Allow force pushes | ✅ (for hotfixes) |

## Feature Branch Guidelines

No protection rules - developers have full control over feature branches.

### Naming Convention
```
feature/JIRA-123-short-description
fix/JIRA-123-bug-description
hotfix/JIRA-123-critical-fix
```

## Code Owners

### CODEOWNERS File

Create `.github/CODEOWNERS`:

```text
# Global owners
* @tech-lead @senior-dev

# Backend
/src/main/java/com/medicalstore/pos/service/ @backend-team @senior-dev
/src/main/java/com/medicalstore/pos/controller/ @backend-team
/src/main/java/com/medicalstore/pos/security/ @security-team @tech-lead
/src/main/java/com/medicalstore/pos/config/ @backend-team @tech-lead

# Frontend
/frontend/src/ @frontend-team @tech-lead

# Security & Config
/src/main/resources/application*.yml @tech-lead @security-team
/src/main/java/com/medicalstore/pos/security/ @security-team @tech-lead
/.github/ @tech-lead @devops-team

# Documentation
/docs/ @docs-team
/*.md @tech-lead @docs-team

# CI/CD
/.github/workflows/ @devops-team @tech-lead
```

### Code Owner Rules

- PRs require approval from **at least one code owner**
- Code owners are automatically requested for review
- Multiple owners = any one can approve

## Required Status Checks Detail

### Test Job (`test`)
```yaml
# Runs in CI
mvn verify -Dspring.profiles.active=test
```
- Runs unit + integration tests
- Generates JaCoCo coverage report
- Fails if coverage < 70% (instructions) or 60% (branches)

### Build Job (`build`)
```yaml
mvn clean package -DskipTests
```
- Compiles and packages JAR
- Only runs on main/develop after tests pass

### Code Quality (`code-quality`)
```yaml
mvn spotless:check
```
- Google Java Format compliance
- License headers
- Import ordering

### Dependency Check (`dependency-check`)
```yaml
OWASP Dependency Check
```
- Fails on CVSS ≥ 7
- Generates HTML report

### Frontend Build (`frontend-build`)
```yaml
npm run build
npm run test -- --watch=false
```
- Angular production build
- Unit tests (Karma/Jasmine)
- Linting

## Bypass Procedures (Emergency Only)

### Hotfix Emergency Bypass
1. Create hotfix branch from `main`
2. Make minimal fix
2. PR with label `hotfix`
3. Tech lead approval only (1 review)
4. Merge to `main` and `develop`
5. Tag release

### Force Push (Extreme Cases Only)
```bash
# Only with tech lead approval
git push --force-with-lease origin main
```
Requires:
- Written approval from tech lead
- Documented reason in PR
- Post-mortem within 48 hours

## Audit & Compliance

### Audit Log
All branch protection changes logged in:
- GitHub Audit Log
- Security team review quarterly

### Compliance Checks
- Monthly: Verify all required checks still configured
- Quarterly: Review CODEOWNERS assignments
- Annually: Full branch protection audit

---

## Quick Reference

| Branch | Reviews | Checks | Force Push |
|--------|---------|--------|------------|
| `main` | 2 + code owners | All 5 | ❌ |
| `develop` | 1 | All 5 | ❌ |
| `release/*` | 1 | All 5 | ❌ |
| `hotfix/*` | 1 | All 5 | ✅ (emergency) |
| `feature/*` | 0 | Recommended | ✅ |

---

*Last updated: 2024*
*Owner: Tech Lead Team*