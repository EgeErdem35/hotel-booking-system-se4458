# SE4458 Hotel Booking System

Group 1 final project for SE4458 Software Architecture & Design of Modern Large Scale Systems.

This repository contains a service-oriented Hotel Booking System similar to Hotels.com. It includes a Next.js frontend, Spring Boot backend services, an API Gateway, Supabase PostgreSQL, AWS DynamoDB, Upstash Redis, RabbitMQ, scheduled notifications, and an OpenAI-backed AI Agent.

## Final URLs

Update these values after cloud deployment:

| Component | URL |
| --- | --- |
| Frontend | `https://main.db41rjk85z4bx.amplifyapp.com/` |
| API Gateway | `TBD` |
| Swagger / API Gateway | `TBD/swagger-ui.html` |
| Demo video | `TBD` |
| Public GitHub repository | `https://github.com/EgeErdem35/hotel-booking-system-se4458` |

Local development URLs:

| Component | URL |
| --- | --- |
| Frontend | `http://localhost:3000` |
| API Gateway | `http://localhost:8080` |
| RabbitMQ Management | `http://localhost:15672` |

## Technology Stack

- Frontend: Next.js + Tailwind CSS
- Backend: Java Spring Boot
- API Gateway: Spring Cloud Gateway
- Authentication: Supabase Auth
- Main relational database: Supabase PostgreSQL
- Comments NoSQL database: AWS DynamoDB
- Distributed cache: Upstash Redis
- Queue / messaging: RabbitMQ locally, CloudAMQP for cloud
- Scheduled jobs: Spring Boot `@Scheduled`
- AI Agent: OpenAI API
- API documentation: Swagger / OpenAPI
- Containerization: Dockerfiles + Docker Compose

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
```

## Implemented Features

- Hotel admin can create/update hotels, rooms, and availability.
- Hotel Admin Service requires Supabase JWT authentication.
- Users can search hotels by destination, dates, and guest count.
- Search returns only hotels with rooms available for every selected night.
- Logged-in users receive a 15 percent discount in hotel search/detail responses.
- Hotel detail responses are cached in Upstash Redis when availability filters are not present.
- Users can create bookings from hotel detail.
- Booking decreases room capacity for selected dates.
- Booking publishes `reservation.created` messages to RabbitMQ.
- Notification Service consumes reservation events and creates user notifications.
- Notification Service runs a nightly low-capacity job and supports a manual demo trigger.
- Comments are stored in AWS DynamoDB.
- Rating summary, star distribution, and service-based rating distribution are exposed for graphs.
- Frontend includes search, detail, booking, comments/graphs, map view, admin dashboard, and AI chat panels.
- AI Agent endpoint parses demo hotel search/booking intent, calls project APIs, and can use OpenAI Responses API.

## Required Environment Variables

Do not commit real secrets. Copy each `.env.example` file to a local `.env` file when running individual services.

Common backend:

```env
SERVER_PORT=
SUPABASE_DB_URL=
SUPABASE_DB_USERNAME=
SUPABASE_DB_PASSWORD=
SUPABASE_JWT_SECRET=
SUPABASE_PROJECT_URL=
```

DynamoDB comments:

```env
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
AWS_REGION=
DYNAMODB_COMMENTS_TABLE=hotel_comments
```

Upstash Redis:

```env
UPSTASH_REDIS_REST_URL=
UPSTASH_REDIS_REST_TOKEN=
HOTEL_DETAIL_CACHE_TTL_SECONDS=300
```

RabbitMQ:

```env
RABBITMQ_URL=amqp://guest:guest@localhost:5672
```

OpenAI:

```env
OPENAI_API_KEY=
OPENAI_MODEL=gpt-4.1-mini
OPENAI_BASE_URL=https://api.openai.com
```

Frontend:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_SUPABASE_URL=
NEXT_PUBLIC_SUPABASE_ANON_KEY=
```

## Local Docker Compose

Start all containers:

```bash
docker compose up --build
```

Start only RabbitMQ:

```bash
docker compose up -d rabbitmq
```

Render and validate compose configuration:

```bash
docker compose config
```

Docker Compose reads environment values from your shell or a root `.env` file. The compose file intentionally does not hardcode API keys, database passwords, AWS credentials, Upstash tokens, or OpenAI keys.

## Local Manual Run

Run frontend:

```bash
cd frontend
npm install
npm run dev
```

Run a backend service:

```bash
cd hotel-search-service
mvn spring-boot:run
```

Compile all backend services from their folders:

```bash
mvn -q -DskipTests compile
```

Build frontend:

```bash
cd frontend
npm.cmd run build
```

On Windows PowerShell, `npm run build` may be blocked by script execution policy. Use `npm.cmd run build`.

## API Gateway Routes

| Route | Service |
| --- | --- |
| `/api/v1/admin/**` | Hotel Admin Service |
| `/api/v1/hotels/**` | Hotel Search Service |
| `/api/v1/bookings/**` | Booking Service |
| `/api/v1/comments/**` | Comments Service |
| `/api/v1/notifications/**` | Notification Service |
| `/api/v1/ai/**` | AI Agent Service |

## Documentation

- Project documentation: `PROJECT_DOCUMENTATION.md`
- Implementation log: `docs/IMPLEMENTATION_LOG.md`
- Deployment notes: `docs/DEPLOYMENT_NOTES.md`
- ER diagram: `docs/ER_DIAGRAM.md`
- Video script: `docs/VIDEO_SCRIPT.md`
- Supabase setup: `docs/SUPABASE_SETUP.md`
- DynamoDB setup: `docs/DYNAMODB_COMMENTS_SETUP.md`
- Database scripts: `docs/database/`

## Known Notes

- Payment is not implemented because it is not required by the PDF.
- Real-time AI messaging is not required.
- Admin hotel image upload is implemented as an optional enhancement. Uploaded files are stored by `hotel-admin-service`, and `imageUrl` is returned in admin/search/detail hotel responses.
- Comments are intentionally stored in DynamoDB, not Supabase PostgreSQL.
- The AI Agent requires `OPENAI_API_KEY` for OpenAI-generated replies; without it, the service still returns deterministic fallback replies based on project API calls.
