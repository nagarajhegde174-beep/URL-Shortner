# URL Shortener SaaS

A production-grade **URL Shortener SaaS monolith** built with **Spring Boot 3**, **React + Vite + Bootstrap**, **PostgreSQL**, **Redis**, **Kafka**, and **Razorpay** payments. Supports FREE and PRO plans with full analytics, rate limiting, expiring links, and custom short codes.

---

## Architecture

```mermaid
graph TD
    Browser["🌐 Browser / Client"]

    subgraph "Frontend (Port 80)"
        Nginx["Nginx\n(Serves React SPA)"]
        React["React + Vite + Bootstrap\nTypeScript / Chart.js"]
    end

    subgraph "Backend (Port 8080)"
        SpringBoot["Spring Boot 3 / Java 21\n- JWT Auth\n- URL CRUD\n- Redirect\n- Rate Limiting\n- Analytics API\n- Razorpay Payments"]
    end

    subgraph "Data & Messaging"
        Postgres[("PostgreSQL 15\nPort: 5432 internal / 5433 host")]
        Redis[("Redis 7\nPort: 6379 internal / 6380 host")]
        Kafka[("Kafka 7.5 (KRaft)\nPort: 9092")]
    end

    Browser -->|"HTTP :80"| Nginx
    Nginx -->|"SPA routing"| React
    Nginx -->|"Proxy /api/*"| SpringBoot

    SpringBoot -->|"Entities / JPA"| Postgres
    SpringBoot -->|"Cache + Rate Limiting"| Redis
    SpringBoot -->|"Publishes ClickEvent"| Kafka
    Kafka -->|"Consumes ClickEvent"| SpringBoot
    SpringBoot -->|"Webhooks"| Razorpay["💳 Razorpay\n(External)"]
```

---

## Features

| Feature | Description |
|---|---|
| **Base62 URL Shortening** | Unique 7-char short codes generated from database IDs |
| **Custom Short Codes** | PRO plan users can specify a custom alias |
| **Link Expiry** | Links can be set to expire at a specific timestamp |
| **JWT Auth** | Access tokens stored **in memory only** (Zustand), refresh tokens via HttpOnly cookies |
| **Redis URL Cache** | Redirects check Redis first to minimize database load |
| **Redis Rate Limiting** | Sliding-window Lua-script rate limiter enforced per IP/endpoint |
| **Kafka Analytics** | Click events published asynchronously to Kafka; persisted via consumer |
| **Analytics API** | Device, browser, OS, referrer, and click-timeline breakdowns |
| **Razorpay PRO** | ₹499/30-day subscription activated via server-side signature verification |
| **React Frontend** | Responsive SaaS UI with Bootstrap and Chart.js dashboards |

---

## Project Structure

```
URL Shortener/
├── backend/                 # Spring Boot monolith (Java 21)
│   ├── src/main/java/...
│   ├── src/test/java/...
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   └── db/migration/    # Flyway V1–V3 SQL migrations
│   ├── pom.xml
│   └── Dockerfile
├── frontend/                # React + Vite + TypeScript + Bootstrap
│   ├── src/
│   │   ├── pages/           # LandingPage, Login, Register, Dashboard, Analytics, Upgrade
│   │   ├── components/      # Sidebar, PrivateRoute
│   │   ├── store/           # Zustand authStore
│   │   └── lib/             # Axios interceptor (401 refresh queue)
│   ├── nginx.conf
│   └── Dockerfile
├── environment/
│   ├── .env                 # Local Docker secrets (git-ignored)
│   └── .env.example         # Template for secrets
├── .github/
│   └── workflows/ci.yml     # GitHub Actions CI pipeline
├── docker-compose.yml
├── verify-project.sh        # Bash verification script
├── verify-project.bat       # Windows CMD verification script
└── README.md
```

---

## Local Development Setup

### Prerequisites
- Java 21 (JDK)
- Maven 3.9+
- Node.js 20+
- Docker Desktop

### 1. Start Infrastructure Services (Docker)

```bash
docker compose up -d postgres redis kafka
```

This starts:
- PostgreSQL on port `5433`
- Redis on port `6380`
- Kafka on port `9092`

### 2. Configure Local Application Settings

The following files are **git-ignored** and must exist locally:

- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/application-dev.yml`

Copy from `backend/.gitignore` — see comments in file for structure.

### 3. Start Backend

```bash
cd backend
mvn spring-boot:run
```

Backend runs on: **http://localhost:8080**

### 4. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on: **http://localhost:5173**

> The Vite dev proxy forwards all `/api` requests to `http://localhost:8080` automatically.

---

## Production Deployment (Docker Compose)

### 1. Configure Secrets

Copy the environment template and fill in real values:

```bash
cp environment/.env.example environment/.env
```

Edit `environment/.env`:

```env
JWT_SECRET=<your-256-bit-secret-key>
RAZORPAY_KEY_ID=rzp_test_...
RAZORPAY_KEY_SECRET=<your-razorpay-key-secret>
RAZORPAY_WEBHOOK_SECRET=<your-webhook-secret>
```

> ⚠️ Never commit real secrets. The `environment/.env` file is git-ignored at the root level.

### 2. Build and Start All Services

```bash
docker compose up --build -d
```

This starts 5 containers:
| Service | Port |
|---|---|
| Frontend (Nginx + React) | http://localhost **(:80)** |
| Backend (Spring Boot) | http://localhost:8080 |
| PostgreSQL | localhost:5433 |
| Redis | localhost:6380 |
| Kafka | localhost:9092 |

### 3. Access the Application

- **Frontend:** http://localhost/
- **Backend API:** http://localhost:8080/api/
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

---

## API Reference

### Authentication

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | — | Register new user |
| POST | `/api/auth/login` | — | Login, returns access token |
| POST | `/api/auth/refresh` | Cookie | Refresh access token |
| POST | `/api/auth/logout` | ✅ JWT | Logout |
| GET | `/api/auth/me` | ✅ JWT | Get profile |

### URL Management

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/links` | ✅ JWT | Create short URL |
| GET | `/api/links` | ✅ JWT | List all user links |
| PUT | `/api/links/{id}` | ✅ JWT | Update link settings |
| DELETE | `/api/links/{id}` | ✅ JWT | Delete link |
| GET | `/{shortCode}` | — | Redirect to long URL |

### Analytics

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/analytics/dashboard` | ✅ JWT | Dashboard statistics |
| GET | `/api/analytics/links/{id}` | ✅ JWT | Per-link analytics |

### Payments

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/payments/orders` | ✅ JWT | Create Razorpay order |
| POST | `/api/payments/verify` | ✅ JWT | Verify payment signature |
| POST | `/api/payments/webhook` | — (HMAC) | Razorpay webhook handler |

---

## Running Tests

### Backend Tests (45 unit + integration tests)

```bash
cd backend
mvn clean test
```

Test suites include:
- `UrlServiceTest` — Base62 encoding, collision handling, FREE plan limits, expiry rules
- `RedirectControllerTest` — Redis cache HIT/MISS, expired/disabled link validation
- `RedisRateLimiterServiceTest` — Sliding window rate limiter
- `ClickEventConsumerTest` — Kafka consumer idempotency, duplicate detection, link-not-found handling
- `PaymentServiceTest` — Order creation, signature verification, webhook dispatch
- `PaymentActivationServiceTest` — PRO activation, subscription upsert, idempotency

### Frontend Lint + Build

```bash
cd frontend
npm ci
npm run lint      # Oxlint static analysis
npm run build     # TypeScript compile + Vite production bundle
```

### Verify Full Project (Windows)

```batch
verify-project.bat
```

---

## Environment Variables Reference

| Variable | Required | Description |
|---|---|---|
| `JWT_SECRET` | ✅ | Min 32-char string for HMAC-SHA256 JWT signing |
| `RAZORPAY_KEY_ID` | ✅ | Razorpay API Key ID (starts with `rzp_test_` or `rzp_live_`) |
| `RAZORPAY_KEY_SECRET` | ✅ | Razorpay API Key Secret (backend only, never frontend) |
| `RAZORPAY_WEBHOOK_SECRET` | ✅ | Razorpay Webhook Secret for HMAC signature verification |
| `SPRING_DATASOURCE_URL` | ✅ Docker | PostgreSQL JDBC URL |
| `SPRING_REDIS_HOST` | ✅ Docker | Redis hostname |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | ✅ Docker | Kafka bootstrap address |

---

## Razorpay Webhook Notes

> [!IMPORTANT]
> Razorpay requires a **publicly reachable HTTPS URL** to deliver real webhook events.
> Testing webhooks against `localhost` will not work.
>
> For local webhook testing, use a tunnel tool such as [ngrok](https://ngrok.com/) or [localtunnel](https://theboroer.github.io/localtunnel-www/) to expose your local backend to the internet.
>
> In production, configure the webhook URL as:
> `https://yourdomain.com/api/payments/webhook`

---

## Security Notes

- **Access Token:** Stored in-memory only (Zustand store). Never written to `localStorage` or `sessionStorage`.
- **Refresh Token:** Sent via HttpOnly cookie. Handled transparently by Axios interceptors.
- **HMAC Verification:** All Razorpay payment signatures are verified using `MessageDigest.isEqual()` (constant-time comparison) on the backend.
- **Rate Limiting:** Sliding-window Lua script atomically enforces per-IP request limits via Redis sorted sets.
- **Secrets:** No credentials are hardcoded in any committed file. All sensitive values are injected via environment variables.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend Framework | Spring Boot 3.3.2 / Java 21 |
| Security | Spring Security + JJWT 0.12.5 |
| Database | PostgreSQL 15 + Flyway Migrations |
| Cache / Rate Limiter | Redis 7 |
| Messaging | Apache Kafka 3.7 (KRaft mode) |
| Payment Gateway | Razorpay Java SDK 1.4.3 |
| Frontend Framework | React 19 + Vite 8 + TypeScript 6 |
| UI Library | Bootstrap 5.3 |
| Charts | Chart.js 4 + react-chartjs-2 |
| HTTP Client | Axios 1.x (with silent refresh interceptor) |
| State Management | Zustand 5 |
| API Documentation | SpringDoc OpenAPI (Swagger UI) |
| Containerization | Docker + Docker Compose |
| CI/CD | GitHub Actions |
