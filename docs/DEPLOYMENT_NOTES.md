# Deployment Notes

This document summarizes how the SE4458 Hotel Booking System can be run locally with Docker Compose and deployed to cloud services.

## Deployment Targets

Recommended final deployment mapping:

| Component | Target |
| --- | --- |
| Frontend | AWS Amplify |
| Backend services | AWS Elastic Beanstalk, one app/environment per service |
| API Gateway | AWS Elastic Beanstalk |
| Main relational DB | Supabase PostgreSQL |
| Authentication | Supabase Auth |
| Comments DB | AWS DynamoDB |
| Cache | Upstash Redis |
| Queue | CloudAMQP for cloud, RabbitMQ Docker locally |
| AI | OpenAI API |

## Local Docker Compose

The root `docker-compose.yml` can build and run:

- `frontend`
- `api-gateway-service`
- `hotel-admin-service`
- `hotel-search-service`
- `booking-service`
- `comments-service`
- `notification-service`
- `ai-agent-service`
- `rabbitmq`

Run:

```bash
docker compose up --build
```

Run only RabbitMQ:

```bash
docker compose up -d rabbitmq
```

Validate compose configuration:

```bash
docker compose config
```

Local service URLs:

| Service | URL |
| --- | --- |
| Frontend | `http://localhost:3000` |
| API Gateway | `http://localhost:8080` |
| Hotel Admin Service | `http://localhost:8081` |
| Hotel Search Service | `http://localhost:8082` |
| Booking Service | `http://localhost:8083` |
| Comments Service | `http://localhost:8084` |
| Notification Service | `http://localhost:8085` |
| AI Agent Service | `http://localhost:8086` |
| RabbitMQ Management | `http://localhost:15672` |

## Required Environment Variables

Keep all values external. Do not bake secrets into Docker images.

### Supabase PostgreSQL and Auth

Used by `hotel-admin-service`, `hotel-search-service`, `booking-service`, and `notification-service`.

```env
SUPABASE_DB_URL=
SUPABASE_DB_USERNAME=
SUPABASE_DB_PASSWORD=
SUPABASE_JWT_SECRET=
SUPABASE_PROJECT_URL=
SUPABASE_AUTH_ISSUER=
SUPABASE_JWKS_URL=
DB_MAX_POOL_SIZE=2
DB_MIN_IDLE=0
DB_CONNECTION_TIMEOUT_MS=30000
```

`SUPABASE_AUTH_ISSUER` and `SUPABASE_JWKS_URL` are optional because services derive defaults from `SUPABASE_PROJECT_URL`.
The DB pool defaults are intentionally small for Supabase session pooler limits and can be increased for larger deployments.

### DynamoDB

Used by `comments-service`.

```env
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
AWS_REGION=
DYNAMODB_COMMENTS_TABLE=hotel_comments
```

### Upstash Redis

Used by `hotel-search-service`.

```env
UPSTASH_REDIS_REST_URL=
UPSTASH_REDIS_REST_TOKEN=
HOTEL_DETAIL_CACHE_TTL_SECONDS=300
```

### RabbitMQ / CloudAMQP

Used by `booking-service` and `notification-service`.

Local Docker value:

```env
RABBITMQ_URL=amqp://guest:guest@rabbitmq:5672
```

CloudAMQP value:

```env
RABBITMQ_URL=amqps://...
```

### OpenAI

Used by `ai-agent-service`.

```env
OPENAI_API_KEY=
OPENAI_MODEL=gpt-4.1-mini
OPENAI_BASE_URL=https://api.openai.com
```

### Frontend

Used by `frontend`.

```env
NEXT_PUBLIC_API_BASE_URL=
NEXT_PUBLIC_SUPABASE_URL=
NEXT_PUBLIC_SUPABASE_ANON_KEY=
```

For local Docker Compose, `NEXT_PUBLIC_API_BASE_URL` should usually remain `http://localhost:8080` because browser requests come from the host machine.

## Backend Deployment Steps

For each Spring Boot service:

1. Configure environment variables in the cloud provider.
2. Build from that service folder using its `Dockerfile`.
3. Expose only the service port required by the deployment target.
4. Point `api-gateway-service` to deployed backend URLs:
   - `HOTEL_ADMIN_SERVICE_URL`
   - `HOTEL_SEARCH_SERVICE_URL`
   - `BOOKING_SERVICE_URL`
   - `COMMENTS_SERVICE_URL`
   - `NOTIFICATION_SERVICE_URL`
   - `AI_AGENT_SERVICE_URL`
5. Verify `/actuator/health`.
6. Verify Swagger at `/swagger-ui.html`.

## Frontend Deployment Steps

1. Connect this GitHub repository to AWS Amplify Hosting.
2. Select `frontend` as the monorepo app root. The root `amplify.yml` contains the build settings.
3. Set:
   - `NEXT_PUBLIC_API_BASE_URL`
   - `NEXT_PUBLIC_SUPABASE_URL`
   - `NEXT_PUBLIC_SUPABASE_ANON_KEY`
4. Build with:

```bash
npm ci
npm run build
```

5. Confirm the frontend can call the deployed API Gateway.

## Final URL Checklist

Fill these before final submission:

| Item | URL |
| --- | --- |
| Frontend deployed URL | `https://main.db41rjk85z4bx.amplifyapp.com/` |
| API Gateway deployed URL | `http://api-gateway-service-env.eba-tbq9cyca.eu-north-1.elasticbeanstalk.com` |
| Public GitHub repository | `https://github.com/EgeErdem35/hotel-booking-system-se4458` |
| Demo video | `TBD` |
| Supabase project dashboard | private, do not publish secrets |
| Upstash Redis dashboard | private, do not publish secrets |
| DynamoDB table | private, do not publish secrets |
| CloudAMQP dashboard | private, do not publish secrets |

## Verification Checklist

- Frontend loads.
- API Gateway health endpoint returns `UP`.
- Search returns available hotels.
- Hotel detail returns rooms.
- Booking creates a reservation and decreases availability.
- RabbitMQ receives `reservation.created`.
- Notification Service consumes reservation event.
- Manual nightly job creates low-capacity notifications.
- Comments endpoints return comments and rating graphs.
- Hotel detail cache works when no availability filters are present.
- Admin endpoint requires a valid Supabase JWT.
- AI Agent calls project APIs and returns a response.
