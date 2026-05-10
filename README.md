# SE4458 Hotel Booking System

Group 1 final project for SE4458 Software Architecture & Design of Modern Large Scale Systems.

This repository is a monorepo scaffold for a service-oriented Hotel Booking System similar to Hotels.com. Business logic is intentionally not implemented yet.

## Technology Stack

- Frontend: Next.js + Tailwind CSS
- Backend: Java Spring Boot
- API Gateway: Spring Cloud Gateway
- Authentication: Supabase Auth
- Main Database: Supabase PostgreSQL
- Comments NoSQL Database: AWS DynamoDB
- Cache: Upstash Redis
- Queue / Messaging: RabbitMQ
- AI Agent: OpenAI API
- API Documentation: Swagger / OpenAPI

## Monorepo Structure

```text
frontend/
api-gateway-service/
hotel-admin-service/
hotel-search-service/
booking-service/
comments-service/
notification-service/
ai-agent-service/
docs/
docker-compose.yml
README.md
PROJECT_DOCUMENTATION.md
.gitignore
```

## Local Infrastructure

Start RabbitMQ for local development:

```bash
docker compose up -d rabbitmq
```

RabbitMQ management UI:

- URL: http://localhost:15672
- Username: `guest`
- Password: `guest`

## Frontend Placeholder

The frontend scaffold is in `frontend/`.

```bash
cd frontend
npm install
npm run dev
```

Copy `frontend/.env.example` to `frontend/.env.local` when real environment values are available.

## Backend Placeholders

Each backend service is a standalone Maven Spring Boot placeholder:

- `api-gateway-service`
- `hotel-admin-service`
- `hotel-search-service`
- `booking-service`
- `comments-service`
- `notification-service`
- `ai-agent-service`

Run a backend placeholder from its folder:

```bash
mvn spring-boot:run
```

Each backend service includes a `.env.example` file with the environment variables expected by that service.

## Documentation

Detailed project requirements and future implementation prompts are available in `PROJECT_DOCUMENTATION.md`.
