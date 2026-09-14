<h1 align="center">URL Shortener</h1>

<p align="center">
  <img src="https://img.shields.io/badge/JAVA-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/SPRING_BOOT-3.3.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 3.3.2" />
  <img src="https://img.shields.io/badge/SPRING_SECURITY-6.3-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" alt="Spring Security" />
  <img src="https://img.shields.io/badge/JWT-0.12.5-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" alt="JWT" />
  <img src="https://img.shields.io/badge/TYPESCRIPT-6.0-3178C6?style=for-the-badge&logo=typescript&logoColor=white" alt="TypeScript" />
  <img src="https://img.shields.io/badge/REACT-19-20232A?style=for-the-badge&logo=react&logoColor=61DAFB" alt="React 19" />
  <img src="https://img.shields.io/badge/VITE-8.2-646CFF?style=for-the-badge&logo=vite&logoColor=white" alt="Vite 8" />
  <img src="https://img.shields.io/badge/BOOTSTRAP-5.3-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white" alt="Bootstrap 5.3" />
  <img src="https://img.shields.io/badge/POSTGRESQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL 15" />
  <img src="https://img.shields.io/badge/REDIS-7-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redis 7" />
  <img src="https://img.shields.io/badge/KAFKA-3.7-231F20?style=for-the-badge&logo=apachekafka&logoColor=white" alt="Kafka 3.7" />
  <img src="https://img.shields.io/badge/RAZORPAY-1.4.3-02042B?style=for-the-badge&logo=razorpay&logoColor=3395FF" alt="Razorpay" />
  <!-- <img src="https://img.shields.io/badge/SWAGGER-OpenAPI_3.0-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" alt="Swagger" /> -->
  <img src="https://img.shields.io/badge/DOCKER-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker" />
  <!-- <img src="https://img.shields.io/badge/NGINX-1.26-009639?style=for-the-badge&logo=nginx&logoColor=white" alt="Nginx" /> -->
  <!-- <img src="https://img.shields.io/badge/LICENSE-MIT-yellow?style=for-the-badge" alt="License MIT" /> -->
</p>

<p align="center">A production-grade URL Shortener monolith designed for speed, security, and scalability.</p>

---

## Highlights

🔗 Shorten long URLs with unique Base62 short codes.

✏️ Create custom aliases for eligible users.

⏳ Set URL expiration times.

🔐 Secure authentication using JWT + HttpOnly refresh cookies.

📧 Forgot-password and email reset flow.

⚡ Redis caching and API rate limiting.

📊 Kafka-based asynchronous click analytics.

💳 Razorpay Test Mode subscription support.

🌙 Dark / Light theme.

📈 Analytics dashboard with Chart.js.

🐳 Dockerized deployment with Nginx.

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
        SpringBoot["Spring Boot 3 / Java 21\n- JWT Auth\n- URL CRUD\n- Redirect\n- Rate Limiting\n- Analytics API\n- Razorpay Payments\n- SMTP Mail"]
    end

    subgraph "Data & Messaging"
        Postgres[("PostgreSQL 15\nPort: 5432 internal / 5433 host")]
        Redis[("Redis 7\nPort: 6379 internal / 6380 host")]
        Kafka[("Kafka 3.7 (KRaft)\nPort: 9092")]
    end

    Browser -->|"HTTP :80"| Nginx
    Nginx -->|"SPA routing"| React
    Nginx -->|"Proxy /api/*"| SpringBoot

    SpringBoot -->|"Entities / JPA"| Postgres
    SpringBoot -->|"Cache + Rate Limiting"| Redis
    SpringBoot -->|"Publishes ClickEvent"| Kafka
    Kafka -->|"Consumes ClickEvent"| SpringBoot
    Razorpay["💳 Razorpay\n(External)"] -->|"Webhooks"| SpringBoot
```

---

## Features

| Feature | Description |
|---|---|
| **Base62 URL Shortening** | Unique 7-char short codes generated from database IDs |
| **Custom Short Codes** | PRO plan users can specify a custom alias |
| **Link Expiry** | Links can be set to expire at a specific timestamp |
| **JWT Auth** | Access tokens stored **in memory only** (Zustand), refresh tokens via HttpOnly cookies |
| **Forgot Password & Email** | Real email password reset flow via Spring Boot Mail / SMTP |
| **Dark / Light Theme** | Production-quality dark and light mode system using CSS variables and React Context |
| **Redis URL Cache** | Redirects check Redis first to minimize database load |
| **Redis Rate Limiting** | Sliding-window Lua-script rate limiter enforced per IP/endpoint |
| **Kafka Analytics** | Click events published asynchronously to Kafka; persisted via consumer |
| **Analytics API** | Device, browser, OS, referrer, and click-timeline breakdowns |
| **Razorpay PRO** | ₹499/30-day subscription activated via server-side signature verification |
| **React Frontend** | Responsive SaaS UI with Bootstrap, Lucide icons, and Chart.js dashboards |

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

## Getting Started

Prerequisites

- Java 21
- Maven 3.9+
- Node.js 20+
- Docker Desktop

1. Start Infrastructure

```
docker compose up -d postgres redis kafka
```

2. Start Backend

```
cd backend
mvn spring-boot:run
```

Backend:

http://localhost:8080

3. Start Frontend

```
cd frontend
npm install
npm run dev
```

Frontend:

http://localhost:5173

Configuration

Create the local environment file:

```
cp environment/.env.example environment/.env
```

Configure the required values:

```
JWT_SECRET=<your-secret>

RAZORPAY_KEY_ID=rzp_test_...
RAZORPAY_KEY_SECRET=<your-secret>
RAZORPAY_WEBHOOK_SECRET=<your-secret>
```

---

## Production

Build and start all services:

```
docker compose up --build -d
```

Application:

http://localhost

Check services:

```
docker compose ps
```

Stop services:

```
docker compose down
```

---

## 📄 License

MIT

---
