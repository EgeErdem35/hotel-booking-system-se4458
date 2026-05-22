# Implementation Log

This file is the working memory for the SE4458 Group 1 Hotel Booking System implementation.

Main source of truth: `docs/SE4458_Final_202526_02_spring.pdf`

Project technology choices:

- Frontend: Next.js + Tailwind CSS
- Backend: Java Spring Boot
- API Gateway: Spring Cloud Gateway
- Authentication / IAM: Supabase Auth
- Main relational DB: Supabase PostgreSQL
- Comments NoSQL DB: AWS DynamoDB
- Hotel details cache: Upstash Redis
- Queue: RabbitMQ locally, CloudAMQP for cloud
- Scheduled jobs: Spring Boot `@Scheduled`
- AI Agent: OpenAI API

## Current Status

### Completed

- Created/confirmed monorepo scaffold:
  - `frontend/`
  - `api-gateway-service/`
  - `hotel-admin-service/`
  - `hotel-search-service/`
  - `booking-service/`
  - `comments-service/`
  - `notification-service/`
  - `ai-agent-service/`
  - `docs/`
- Added Group 1 roadmap based on the PDF:
  - `docs/GROUP1_ROADMAP.md`
- Added PDF text extraction for easier local reference:
  - `docs/SE4458_Final_202526_02_spring.txt`
- Added main Supabase PostgreSQL database scripts:
  - `docs/database/001_schema.sql`
  - `docs/database/002_seed.sql`
  - `docs/database/README.md`
- Added Supabase setup guide:
  - `docs/SUPABASE_SETUP.md`
- Added DynamoDB comments setup guide:
  - `docs/DYNAMODB_COMMENTS_SETUP.md`
- Created real Supabase project and ran:
  - `docs/database/001_schema.sql`
  - `docs/database/002_seed.sql`
- Configured local `.env` files for:
  - `hotel-admin-service`
  - `hotel-search-service`
- Verified Supabase DB connectivity from local backend services.
- Added schema tables:
  - `hotels`
  - `rooms`
  - `room_availability`
  - `bookings`
  - `hotel_admins`
  - `notifications`
- Added demo seed data for Istanbul, Antalya, and Izmir hotels.
- Updated documentation to clarify:
  - PDF is the main source of truth.
  - Supabase/DynamoDB/Upstash/AWS/CloudAMQP are our implementation choices.
  - Comments graphs must include service-based rating distribution, not only star distribution.
- Fixed `PROJECT_DOCUMENTATION.md` Markdown code block formatting.
- Implemented first real Hotel Admin Service layer:
  - JDBC/PostgreSQL dependency
  - local `.env` loading with `spring-dotenv`
  - datasource config
  - admin auth header resolver
  - DTO validation
  - controller
  - service
  - repository
  - global exception handler
- Implemented first real Hotel Search Service layer:
  - JDBC/PostgreSQL dependency
  - local `.env` loading with `spring-dotenv`
  - datasource config
  - search controller
  - search service
  - search repository
  - pagination response
  - hotel detail response
  - hotel map response
  - global exception handler
- Implemented first real Booking Service layer:
  - JDBC/PostgreSQL dependency
  - local `.env` loading with `spring-dotenv`
  - datasource config
  - RabbitMQ config and durable `reservation.created` queue declaration
  - booking controller
  - booking service
  - booking repository
  - DTO validation
  - transaction-based capacity decrease
  - reservation-created event publishing after DB commit
  - global exception handler
- Implemented first real Notification Service queue consumer layer:
  - JDBC/PostgreSQL dependency
  - local `.env` loading with `spring-dotenv`
  - datasource config
  - RabbitMQ config and durable `reservation.created` queue declaration
  - `reservation.created` consumer
  - reservation detail notification persistence
  - user notification endpoint
  - admin notification endpoint
  - nightly low-capacity scheduled job
  - manual nightly job demo endpoint
  - global exception handler
- Implemented API Gateway routes:
  - `/api/v1/admin/**` -> Hotel Admin Service
  - `/api/v1/hotels/**` -> Hotel Search Service
  - `/api/v1/bookings/**` -> Booking Service
  - `/api/v1/comments/**` -> Comments Service
  - `/api/v1/notifications/**` -> Notification Service
  - `/api/v1/ai/**` -> AI Agent Service
  - service URLs come from environment variables
  - CORS configured for frontend origin
  - `Authorization` and `X-User-Id` headers are allowed through CORS and forwarded by gateway
- Implemented Supabase JWT verification:
  - Hotel Admin Service verifies Bearer tokens with `SUPABASE_JWT_SECRET`.
  - Hotel Admin Service uses JWT `sub` as authenticated user id.
  - Hotel Admin Service rejects mismatched optional `X-User-Id`.
  - Hotel Search Service applies 15% discount only for valid Supabase JWTs.
  - Legacy HS256 tokens are verified with the JWT secret.
  - ES256/RS256 tokens are verified with Supabase JWKS from `SUPABASE_PROJECT_URL`.
  - No local authentication implementation was added.
- Implemented first real Comments Service layer:
  - AWS DynamoDB client config
  - local `.env` loading with `spring-dotenv`
  - comment controller
  - comment service
  - DynamoDB repository
  - comment creation endpoint
  - paginated comment list endpoint
  - rating summary endpoint
  - star distribution endpoint
  - service-based rating distribution endpoint
  - global exception handler
- Added Upstash Redis hotel detail cache in Hotel Search Service:
  - Uses `UPSTASH_REDIS_REST_URL` and `UPSTASH_REDIS_REST_TOKEN`.
  - Uses `hotel:details:{hotelId}` keys for unfiltered hotel detail responses.
  - Stores only base, undiscounted detail payloads so authenticated discounts are still applied per request.
  - Bypasses cache when `checkIn`, `checkOut`, or `guests` are present so availability-sensitive responses stay live.
  - Uses `HOTEL_DETAIL_CACHE_TTL_SECONDS`, defaulting to `300`.
- Connected the frontend core search/detail flow to the API Gateway:
  - Uses `NEXT_PUBLIC_API_BASE_URL`, defaulting to `http://localhost:8080`.
  - Search form calls `GET /api/v1/hotels/search`.
  - Selecting a result calls `GET /api/v1/hotels/{hotelId}` with `checkIn`, `checkOut`, and `guests`.
  - Shows available hotels, lowest nightly price, hotel detail, amenities, and date-aware room options.
  - Kept admin/comments UI for later steps.
- Added the frontend booking UI flow:
  - Starts from the connected search/detail page.
  - Adds a demo `userId` input for booking requests.
  - Room cards can call `POST /api/v1/bookings` through the API Gateway.
  - Sends `roomId`, `userId`, `checkIn`, `checkOut`, and `guestCount`.
  - Shows booking confirmation with reservation id, status, hotel, room type, and total price.
  - Reloads selected hotel detail after booking so date-aware availability can refresh.
- Added frontend comments and rating graph UI:
  - Starts from the connected search/detail/booking page.
  - Selecting a hotel loads comments through `GET /api/v1/comments/hotel/{hotelId}`.
  - Loads overall rating summary through `GET /api/v1/comments/hotel/{hotelId}/summary`.
  - Loads star distribution through `GET /api/v1/comments/hotel/{hotelId}/distribution`.
  - Loads service-based rating distribution through `GET /api/v1/comments/hotel/{hotelId}/service-distribution`.
  - Shows average rating, total comments, star distribution bars, service rating bars, and recent comments.
- Added frontend admin dashboard flow:
  - Starts from the connected search/detail/booking/comments page.
  - Uses admin `Authorization` token input and optional `X-User-Id` input.
  - Hotel form calls `POST /api/v1/admin/hotels` and `PUT /api/v1/admin/hotels/{hotelId}`.
  - Room form calls `POST /api/v1/admin/hotels/{hotelId}/rooms` and `PUT /api/v1/admin/rooms/{roomId}`.
  - Availability form calls `POST /api/v1/admin/rooms/{roomId}/availability`.
  - Shows admin operation success and error states in the dashboard.
- Added frontend map view:
  - Starts from the connected search/detail/booking/comments/admin page.
  - Plots hotel search result latitude/longitude values as pins in a lightweight coordinate map panel.
  - Selecting a hotel loads `GET /api/v1/hotels/{hotelId}/map` through the API Gateway.
  - Shows map endpoint coordinates beside search/detail coordinates for the selected hotel.
  - Uses no external map dependency.
- Added frontend AI Agent chat UI:
  - Starts from the connected search/detail/booking/comments/admin/map page.
  - Adds a chat panel that calls `POST /api/v1/ai/chat` through the API Gateway.
  - Sends `sessionId` and `message`.
  - Reads flexible text/JSON reply fields so the UI can tolerate the first AI Agent implementation.
  - Shows loading, assistant/user messages, and API error state.
- Implemented AI Agent backend chat endpoint:
  - Adds `POST /api/v1/ai/chat` in `ai-agent-service`.
  - Request body: `sessionId` and `message`.
  - Response body includes `sessionId`, `reply`, parsed intent, API call metadata, and returned project API data.
  - Parses demo hotel search intent for destination, ISO check-in/check-out dates, and guest count.
  - Parses booking intent when `book`/`reserve`, `roomId`, `userId`, dates, and guest count are present.
  - Calls project APIs through `API_GATEWAY_URL`.
  - Search flow calls `GET /api/v1/hotels/search`.
  - Booking flow calls `POST /api/v1/bookings`.
  - Uses OpenAI Responses API through `OPENAI_API_KEY`, `OPENAI_MODEL`, and `OPENAI_BASE_URL`.
  - Falls back to deterministic project-API-based replies when `OPENAI_API_KEY` is not configured.
- Added Dockerfiles for every backend service and frontend:
  - Added Java multi-stage Dockerfiles for `api-gateway-service`, `hotel-admin-service`, `hotel-search-service`, `booking-service`, `comments-service`, `notification-service`, and `ai-agent-service`.
  - Added `.dockerignore` files so `.env`, logs, and build outputs are not copied into images.
  - Added frontend standalone Dockerfile.
  - Updated `frontend/next.config.mjs` with `output: "standalone"`.
  - Updated `docker-compose.yml` to build and run RabbitMQ, all backend services, and the frontend.
  - Compose keeps secrets and environment-specific values external through environment interpolation.
  - Internal service URLs use Docker service names.
- Prepared final delivery documentation:
  - Rewrote `README.md` with current implementation status, local URLs, required environment variables, Docker Compose usage, API Gateway routes, and final deployed URL placeholders.
  - Added `docs/DEPLOYMENT_NOTES.md` with local Docker Compose usage, cloud deployment notes, required environment variables, and verification checklist.
  - Added `docs/ER_DIAGRAM.md` with Mermaid ER diagram for Supabase PostgreSQL plus DynamoDB, Redis, and RabbitMQ data models.
  - Added `docs/VIDEO_SCRIPT.md` with a sub-5-minute demo video flow.
  - Updated `PROJECT_DOCUMENTATION.md` final deliverables section to reference the delivery files.

### Implemented Hotel Admin Endpoints

Base path: `/api/v1/admin`

- `POST /hotels`
- `PUT /hotels/{hotelId}`
- `POST /hotels/{hotelId}/rooms`
- `PUT /rooms/{roomId}`
- `POST /rooms/{roomId}/availability`

Current admin auth behavior:

- Requires a valid Supabase `Authorization: Bearer ...` JWT.
- Uses JWT `sub` as the authenticated user id.
- `X-User-Id` is optional; if provided, it must match JWT `sub`.
- Checks `hotel_admins` table for update/room/availability operations
- Full Supabase JWT signature and expiration verification is implemented with `SUPABASE_JWT_SECRET`.

### Implemented Booking Endpoints

Base path: `/api/v1/bookings`

- `POST /`
  - Creates booking.
  - Checks room capacity and selected date availability.
  - Uses `checkIn <= available_date < checkOut`.
  - Decreases `room_availability.available_count` in the same DB transaction.
  - Stores booking as `CONFIRMED`.
  - Publishes `reservation.created` after DB commit.
  - Does not implement payment, matching the PDF.
- `GET /{bookingId}`
  - Returns booking detail.
- `GET /user/{userId}`
  - Returns user's bookings.

### Implemented Notification Endpoints

Base path: `/api/v1/notifications`

- `GET /user/{userId}`
  - Returns notifications addressed to the user.
- `GET /admin/{adminId}`
  - Returns notifications for hotels administered by the admin user.

Current Notification Service behavior:

- Consumes `reservation.created` from RabbitMQ.
- Stores notification records in Supabase PostgreSQL `notifications`.
- Simulates notification sending by DB persistence.
- Runs nightly low-capacity checks with Spring `@Scheduled`.
- Supports manual low-capacity job trigger for demos.

### Implemented Comments Endpoints

Base path: `/api/v1/comments`

- `POST /`
  - Stores a hotel comment in DynamoDB.
  - Includes `overallRating`.
  - Includes service-based ratings such as cleanliness, location, staff, comfort.
- `GET /hotel/{hotelId}?page=0&size=10`
  - Returns comments for a hotel.
- `GET /hotel/{hotelId}/summary`
  - Returns total comments and average overall rating.
- `GET /hotel/{hotelId}/distribution`
  - Returns 1-5 star distribution.
- `GET /hotel/{hotelId}/service-distribution`
  - Returns service-based rating averages and counts.

Comments are not stored in Supabase PostgreSQL; they are stored in DynamoDB as required by the PDF.

### Verification Done

- `mvn -q -DskipTests compile` passed for `hotel-admin-service`.
- `mvn -q -DskipTests compile` passed for `api-gateway-service`.
- `mvn -q -DskipTests compile` passed for `hotel-search-service`.
- `mvn -q -DskipTests compile` passed for `booking-service`.
- `mvn -q -DskipTests compile` passed for `notification-service`.
- `mvn -q -DskipTests compile` passed for `comments-service`.
- `mvn -q -DskipTests package` passed for `hotel-admin-service`.
- `mvn -q -DskipTests package` passed for `api-gateway-service`.
- `mvn -q -DskipTests package` passed for `hotel-search-service`.
- `mvn -q -DskipTests package` passed for `booking-service`.
- `mvn -q -DskipTests package` passed for `notification-service`.
- `hotel-admin-service` health check passed against Supabase:
  - `GET http://localhost:8081/actuator/health` -> `UP`
- `hotel-search-service` health check passed against Supabase:
  - `GET http://localhost:8082/actuator/health` -> `UP`
- Real Supabase search test passed:
  - `GET /api/v1/hotels/search?destination=Istanbul&checkIn=2026-07-15&checkOut=2026-07-18&guests=2&page=0&size=10`
  - Returned `Istanbul Bosphorus Suites`
  - Anonymous price: `210.00`
  - Logged-in demo price with `Authorization: Bearer demo-token`: `178.50`
- Fixed Turkish locale search bug:
  - Java `toLowerCase()` converted `Istanbul` to `ıstanbul` under Turkish locale.
  - Changed destination normalization to `toLowerCase(Locale.ROOT)`.
- Real Supabase booking test passed:
  - `POST /api/v1/bookings`
  - Room: `aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa`
  - User: `88888888-8888-8888-8888-888888888888`
  - Dates: `2026-07-15` to `2026-07-18`
  - Created booking: `5d1622e0-0196-49bd-bb9c-d3ea976eb803`
  - Total price: `630.00`
  - Status: `CONFIRMED`
  - `GET /api/v1/bookings/{bookingId}` returned the created booking.
- Real Supabase capacity decrease test passed:
  - `hotel-search-service` detail endpoint showed `Deluxe Double Room` min availability decreased from `5` to `4`.
- RabbitMQ local container verified:
  - `docker compose up -d rabbitmq` succeeded after Docker Desktop was running.
  - RabbitMQ management API responded at `http://localhost:15672`.
  - RabbitMQ version: `3.13.7`.
- Booking to RabbitMQ event flow verified:
  - Created booking `87c30925-fb83-4712-bd8a-3e796063fa07`.
  - Verified message in `reservation.created` queue through RabbitMQ management API.
- RabbitMQ JSON date serialization improved:
  - Updated `RabbitMqConfig` to use Spring Boot's configured `ObjectMapper`.
  - Created booking `56f5099a-9c20-48e3-b799-8bbe1f020a9a`.
  - Verified queue payload uses ISO dates: `"checkIn":"2026-07-15"` and `"checkOut":"2026-07-18"`.
- Notification Service consumer test passed:
  - Started `notification-service` while RabbitMQ had `reservation.created` messages.
  - Consumer stored reservation detail notification in Supabase.
  - `GET /api/v1/notifications/user/66666666-6666-6666-6666-666666666666` returned notification for booking `56f5099a-9c20-48e3-b799-8bbe1f020a9a`.
  - Notification type: `RESERVATION_DETAILS`.
  - Notification status: `UNREAD`.
- Admin notification endpoint test passed:
  - `GET /api/v1/notifications/admin/99999999-9999-9999-9999-999999999999` returned reservation notifications for the seeded Istanbul/Antalya admin.
- Notification Service nightly job test passed:
  - `POST /api/v1/notifications/test-nightly-job`
  - Date window: `2026-05-11` to `2026-06-10`.
  - Found `3` low-capacity room/admin matches.
  - Created low-capacity admin notifications in Supabase.
  - `GET /api/v1/notifications/admin/99999999-9999-9999-9999-999999999999` returned `LOW_CAPACITY` notifications.
  - Duplicate prevention is based on admin, hotel, message, status, and one-day window.
- API Gateway smoke test passed:
  - `GET http://localhost:8080/actuator/health` -> `UP`
  - `GET http://localhost:8080/api/v1/hotels/search?...` returned `Istanbul Bosphorus Suites`.
  - `GET http://localhost:8080/api/v1/notifications/admin/99999999-9999-9999-9999-999999999999` returned admin notifications.
- Supabase JWT verification compile checks passed:
  - `mvn -q -DskipTests compile` passed for `hotel-admin-service`.
  - `mvn -q -DskipTests compile` passed for `hotel-search-service`.
- Real test token used ES256, so verifier was upgraded from legacy-HS256-only to JWKS-based asymmetric verification.
- Real Supabase JWT verification tests passed:
  - Search without token returned anonymous price `210.00`.
  - Search with real Supabase ES256 access token returned authenticated discounted price `178.50`.
  - Hotel Admin protected availability endpoint accepted the real Supabase ES256 token.
  - Hotel Admin endpoint used JWT `sub` user `b850aca5-55cc-4060-b241-a68d60e92d47`.
  - `POST /api/v1/admin/rooms/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa/availability` wrote `2026-06-01` to `2026-06-02` availability with count `3`.
- Backend placeholders were compile-checked. A couple of parallel Maven runs showed Windows `Erisim engellendi` resource-copy noise, but single-service reruns passed.
- Real DynamoDB comments flow verified:
  - `comments-service/.env` contains AWS region, credentials, and `hotel_comments` table name.
  - `comments-service` was packaged and run locally on port `8084`.
  - Created comments in real DynamoDB table `hotel_comments`:
    - `caecbdbf-0c51-4cf1-84c8-37648ed2e21f`
    - `79abe3ec-21e1-4a2f-aa60-ab16e4bbacac`
  - `GET /api/v1/comments/hotel/11111111-1111-1111-1111-111111111111?page=0&size=10` returned `2` comments.
  - Summary endpoint returned `totalComments: 2`, `averageRating: 4.2`.
  - Star distribution returned one 4-star bucket item and one 5-star bucket item.
  - Service distribution returned averages for `cleanliness`, `location`, `staff`, and `comfort`.
- Comments Service AWS credentials handling improved:
  - `AwsDynamoDbConfig` now reads `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` through Spring configuration.
  - This keeps `.env` support reliable with `spring-dotenv` instead of relying only on OS-level environment variables.
- Upstash Redis hotel detail cache compile check passed:
  - `mvn -q -DskipTests compile` passed for `hotel-search-service`.
  - Windows printed `Erişim engellendi`, but Maven returned exit code `0`.
- Frontend API Gateway connection build check passed:
  - `npm run build` was blocked by the local PowerShell execution policy for `npm.ps1`.
  - `npm.cmd run build` passed for `frontend`.
  - Next.js built `/` successfully.
  - Webpack printed cache snapshot warnings, but the build returned exit code `0`.
  - Frontend dev server was started on `http://localhost:3000`.
  - `GET http://localhost:3000` returned HTTP `200`.
- Frontend booking UI build check passed:
  - `npm.cmd run build` passed for `frontend`.
  - Next.js built `/` successfully.
  - Webpack printed cache snapshot warnings, but the build returned exit code `0`.
  - `GET http://localhost:3000` returned HTTP `200`.
- Frontend comments/rating UI build check passed:
  - `npm.cmd run build` passed for `frontend`.
  - Next.js built `/` successfully.
  - Webpack printed cache snapshot warnings, but the build returned exit code `0`.
  - A stale Next.js dev server returned HTTP `500` after the code change.
  - The stale Node processes were stopped, the dev server was restarted on `http://localhost:3000`, and `GET http://localhost:3000` returned HTTP `200`.
- Frontend admin dashboard UI build check passed:
  - `npm.cmd run build` passed for `frontend`.
  - Next.js built `/` successfully.
  - Webpack printed cache snapshot warnings, but the build returned exit code `0`.
  - A stale Next.js dev server returned HTTP `500` after the code change.
  - The stale Node processes were stopped, the dev server was restarted on `http://localhost:3000`, and `GET http://localhost:3000` returned HTTP `200`.
- Frontend map view build check passed:
  - First `npm.cmd run build` caught a JSX literal text issue around `{hotelId}`.
  - Fixed the literal text and reran `npm.cmd run build`.
  - Next.js built `/` successfully.
  - Webpack printed cache snapshot warnings, but the build returned exit code `0`.
  - A stale Next.js dev server returned HTTP `500` after the code change.
  - The stale Node processes were stopped, the dev server was restarted on `http://localhost:3000`, and `GET http://localhost:3000` returned HTTP `200`.
- Frontend AI Agent chat UI build check passed:
  - `npm.cmd run build` passed for `frontend`.
  - Next.js built `/` successfully.
  - Webpack printed cache snapshot warnings, but the build returned exit code `0`.
  - A stale Next.js dev server returned HTTP `500` after the code change.
  - The stale Node processes were stopped, the dev server was restarted on `http://localhost:3000`, and `GET http://localhost:3000` returned HTTP `200`.
- AI Agent backend compile check passed:
  - `mvn -q -DskipTests compile` passed for `ai-agent-service`.
  - Windows printed `Erişim engellendi`, but Maven returned exit code `0`.
- Dockerfile / compose verification passed:
  - `docker compose config` rendered successfully.
  - Docker printed `Access is denied` warnings while reading the local Docker config file, but compose config still returned exit code `0`.
  - `npm.cmd run build` passed for `frontend` with Next standalone output enabled.
  - `mvn -q -DskipTests compile` passed sequentially for all backend services:
    `api-gateway-service`, `hotel-admin-service`, `hotel-search-service`, `booking-service`, `comments-service`, `notification-service`, and `ai-agent-service`.
  - Windows printed `Erişim engellendi` after Maven runs, but the aggregate compile command returned exit code `0`.
- Documentation-safe checks passed:
  - Verified expected final delivery files exist: `README.md`, `docs/DEPLOYMENT_NOTES.md`, `docs/ER_DIAGRAM.md`, `docs/VIDEO_SCRIPT.md`, and `PROJECT_DOCUMENTATION.md`.
  - Searched for final deliverable terms across README and docs.

## Important Notes

- Frontend files already had user/workspace changes before backend implementation. Avoid touching frontend unless the current task requires it.
- Comments must be stored in a separate NoSQL DB. Do not add comments to Supabase PostgreSQL.
- SQLite is not allowed by the PDF.
- Admin hotel image upload is implemented as an optional enhancement with a separate multipart endpoint, nullable `hotels.image_url`, and frontend admin upload controls.
- Payment transaction is explicitly not required.
- Real-time AI messaging is not required.

## Next Tasks

### P0 - Continue PDF Core Flow

P0 core backend flow is implemented:

- Admin can manage hotels, rooms, and availability.
- Search returns available hotels and discounted prices for logged-in users.
- Booking decreases capacity and publishes reservation events.
- Notification Service consumes reservation events.
- Notification Service creates nightly low-capacity admin notifications.

Next P1 task:

1. Fill actual deployed URLs and demo video link after cloud deployment.

### P1 - Required Integrations

1. Upstash Redis hotel detail cache is implemented.
2. Frontend is connected to the API Gateway for search and hotel detail.
3. Frontend booking UI flow is connected to the API Gateway.
4. Frontend comments and rating graph UI is connected to the API Gateway.
5. Frontend admin dashboard UI flow is connected to the API Gateway.
6. Frontend map view is connected to the API Gateway.
7. Frontend AI Agent chat UI is connected to the API Gateway route.
8. AI Agent backend chat endpoint is implemented and calls project APIs.
9. Dockerfiles and docker-compose service definitions are added.
10. Final delivery notes, ER diagram, and video script are prepared.

### P2 - Demo/UI/Delivery

8. Frontend search/detail flow is connected to API Gateway.
9. Frontend booking UI flow is connected to API Gateway.
10. Frontend admin dashboard flow is connected to API Gateway.
11. Frontend comments and graph UI is connected to API Gateway.
12. Frontend map view is connected to API Gateway.
13. AI Agent backend chat calls project APIs.
14. Dockerfiles for every service and frontend are added.
15. Deployment notes, README final URL placeholders, ER diagram, and video script are prepared.

## Implemented Hotel Search Endpoints

Base path: `/api/v1/hotels`

- `GET /search`
  - Required query params: `destination`, `checkIn`, `checkOut`, `guests`
  - Optional query params: `page`, `size`
  - Filters rooms using `checkIn <= available_date < checkOut`
  - Requires availability for every night in the date range
  - Returns only hotels with matching available rooms
  - Returns latitude/longitude for map display
  - Applies 15% discount when `Authorization: Bearer ...` is present
- `GET /{hotelId}`
  - Returns hotel detail and room options
  - Optional availability filter: `checkIn`, `checkOut`, `guests`
  - If one availability filter param is provided, all three must be provided
  - Applies 15% discount when `Authorization: Bearer ...` is present
- `GET /{hotelId}/map`
  - Returns hotel map coordinates

Hotel detail cache with Upstash Redis is implemented for unfiltered detail responses and intentionally bypassed for availability-filtered responses.

## Suggested Next Prompt

Fill actual deployed URLs and demo video link after cloud deployment according to `docs/SE4458_Final_202526_02_spring.pdf` and continue from `docs/IMPLEMENTATION_LOG.md`.

Scope:

- Update `README.md` and `docs/DEPLOYMENT_NOTES.md`.
- Replace `TBD` final URLs with deployed frontend, API Gateway, GitHub, and video links.
- Keep secrets out of documentation.
