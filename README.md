# Shipment Tracking System

Production-grade distributed logistics platform — Java microservices, Redis, Kafka, PostgreSQL.
Designed to scale to **5M+ requests/day** via horizontal scaling, caching, and async processing.

## Quick Start

```bash
docker compose up --build
# Wait ~5 min for first build, then open:
# http://localhost        → React dashboard
# demo@gocomet.com / demo123
```

## Architecture

```
Browser → Nginx LB → API Gateway (JWT) → Microservices
                                              ↓        ↓
                                         PostgreSQL  Redis
                                              ↓
                                            Kafka → Tracking + Notifications
```

## Services

| Service               | Port | Role                                   |
|-----------------------|------|----------------------------------------|
| api-gateway           | 8080 | JWT validation, routing, rate limiting |
| user-service          | 8081 | Register / login / JWT                 |
| shipment-service      | 8082 | CRUD + Redis cache + Kafka producer    |
| tracking-service      | 8083 | Kafka consumer + Redis live state      |
| notification-service  | 8084 | Email/SMS simulation via Kafka         |
| ai-prediction-service | 8085 | ETA + risk scoring                     |

## Core APIs

```
POST /api/users/register       Register
POST /api/users/login          Login → JWT token
POST /api/shipments            Create shipment
GET  /api/shipments/{id}       Get shipment (Redis cached)
PUT  /api/shipments/{id}/status Update status
GET  /api/tracking/{id}/live   Live status from Redis
GET  /api/tracking/{id}/history Full audit trail
POST /api/predictions/predict  AI ETA + risk prediction
GET  /api/predictions/{id}/eta ETA only
GET  /api/predictions/{id}/risk Risk only
```

## AI Prediction Formula (Java)

```
ETA = (distanceKm / carrierSpeed) × trafficFactor × weatherFactor × weightPenalty + handlingTime

Risk score = overduePoints + distancePoints + weightPoints + carrierPoints + weekendPoints
Risk level: LOW (0-29) | MEDIUM (30-59) | HIGH (60-100)
```

## Scalability Design

- **Redis cache-aside** — absorbs 80%+ of GET reads, sub-ms latency
- **Kafka async** — decouples writes from tracking/notifications, zero blocking
- **PostgreSQL partitioning** — monthly partitions on shipments + events
- **Stateless services** — horizontal scale with `docker compose up --scale shipment-service=5`
- **HikariCP pooling** — 100 DB connections per instance
- **Kafka concurrency** — 10 consumer threads in tracking-service

## Capacity Math

```
5M req/day ÷ 86,400s = 58 avg req/s → 175 peak req/s (3× factor)
Per service instance handles ~100-200 req/s → 2-3 instances at peak
Redis absorbs 80% reads → DB only sees 35 writes/s
```

## Troubleshooting

```bash
docker compose logs -f <service>         # stream logs
docker compose restart shipment-service  # restart one service
docker compose exec redis redis-cli ping # verify Redis
docker compose exec postgres psql -U gocomet -c "\l"  # verify DB
curl http://localhost/health             # Nginx health
```
