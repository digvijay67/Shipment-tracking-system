# 🧪 QA Testing — Shipment Tracking System

> **QA Engineer:** Digvijaysing Vilas Rajput  
> **Project:** Production-grade Distributed Logistics Platform  
> **Tech Stack:** Java · Spring Boot · Kafka · Redis · PostgreSQL · Docker · GitHub Actions  
> **Testing Period:** November 2024

---

## 📁 QA Artifacts in This Folder

| File | Description |
|------|-------------|
| `STS_TestCases.xlsx` | 45 manual test cases across 6 modules with Pass/Fail status |
| `STS_BugReport.xlsx` | 8 bugs logged in Jira-style format with severity & reproduction steps |
| `STS_Postman_Collection.json` | Ready-to-import Postman collection with automated test scripts |
| `README.md` | This file — QA overview, scope, and setup |

---

## 🎯 Test Scope

| Module | Test Cases | Bugs Found |
|--------|-----------|------------|
| User Service (Register/Login/JWT) | 10 | 1 |
| Shipment Service (CRUD + Cache) | 12 | 3 |
| Tracking Service (Live + History) | 7 | 1 |
| Notification Service (Async/Kafka) | 4 | 1 |
| AI Prediction Service (ETA + Risk) | 6 | 1 |
| API Gateway & Rate Limiting | 6 | 1 |
| **TOTAL** | **45** | **8** |

---

## 🐛 Critical Bugs Summary

| Bug ID | Title | Severity | Status |
|--------|-------|----------|--------|
| BUG-001 | Invalid status transition accepted (DELIVERED → CREATED) | 🔴 High | Open |
| BUG-002 | Redis cache not invalidated on shipment status update | 🔴 High | Open |
| BUG-003 | Rate limiter returns 500 instead of 429 on limit exceeded | 🟡 Medium | Open |
| BUG-004 | AI Prediction returns null ETA for distance > 4000km | 🟡 Medium | In Progress |
| BUG-005 | Notification service crashes on malformed Kafka message | 🔴 High | Open |
| BUG-006 | Tracking history returns events in wrong chronological order | 🟡 Medium | Open |
| BUG-007 | User enumeration possible via distinct login error messages | 🟡 Medium | Open |
| BUG-008 | Pagination returns duplicate records at page boundary | 🟢 Low | Open |

---

## 🚀 Setup — Run the System Locally

```bash
# Clone repo
git clone https://github.com/digvijay67/Shipment-tracking-system.git
cd Shipment-tracking-system

# Start all services (first build ~5 min)
docker compose up --build

# Verify all 11 containers are running
docker compose ps

# Access
# Dashboard  → http://localhost
# API Base   → http://localhost/api
# Login      → demo@gocomet.com / demo123
```

---

## 🔬 How to Run Tests

### Option 1 — Postman (Manual + Automated)
1. Open Postman → Import → `STS_Postman_Collection.json`
2. Set collection variable `baseUrl = http://localhost`
3. Run **User Service** folder first (gets JWT token auto-saved)
4. Run remaining folders in order

### Option 2 — Manual Testing (via Test Cases Excel)
1. Open `STS_TestCases.xlsx`
2. Follow **Steps / Input** column exactly
3. Compare actual output with **Expected Result**
4. Update **Status** column and add remarks

### Option 3 — Postman Collection Runner
1. Import collection
2. Click **Run Collection**
3. All test scripts execute automatically — pass/fail report generated

---

## 🗺️ Architecture Reference

```
Browser → Nginx (LB) → API Gateway :8080 (JWT + Rate Limit)
                              ↓
        ┌─────────────────────┼──────────────────────┐
   User :8081         Shipment :8082         Tracking :8083
        └──────────────────── Kafka ──────────────────┘
                              ↓
                    Notification :8084    AI Prediction :8085
                              ↓
                    PostgreSQL + Redis
```

---

## ✅ Testing Checklist

- [x] Functional testing — all CRUD APIs
- [x] Authentication & authorization (JWT)
- [x] Rate limiting validation (1000 req/s per IP)
- [x] Redis cache behaviour (hit/miss/invalidation)
- [x] Kafka async event processing
- [x] SQL data validation (PostgreSQL)
- [x] Error handling & edge cases
- [x] Concurrent load simulation
- [x] CI/CD pipeline validation (GitHub Actions)
- [ ] Cross-browser UI testing (React dashboard)
- [ ] Performance/load testing (JMeter)

---

*QA artifacts maintained and updated by Digvijaysing Vilas Rajput*
