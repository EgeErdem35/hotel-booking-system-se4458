# Supabase Setup

This guide is for setting up the Supabase PostgreSQL database and Auth service for the SE4458 Group 1 Hotel Booking System.

Main source of truth for project requirements: `docs/SE4458_Final_202526_02_spring.pdf`

Official Supabase reference:

- https://supabase.com/docs/guides/database/connecting-to-postgres
- https://supabase.com/docs/reference/postgres/connection-strings

## 1. Create The Supabase Project

1. Go to https://supabase.com/dashboard.
2. Create a new project.
3. Use a clear project name, for example:
   - `se4458-hotel-booking`
4. Choose the closest available region.
5. Save the database password somewhere safe.

Do not commit the database password to GitHub.

## 2. Create The Database Schema

Open the Supabase project dashboard.

1. Go to SQL Editor.
2. Open `docs/database/001_schema.sql` from this repository.
3. Paste it into the SQL Editor.
4. Run it.
5. Confirm these tables exist:
   - `hotels`
   - `rooms`
   - `room_availability`
   - `bookings`
   - `hotel_admins`
   - `notifications`

## 3. Seed Demo Data

1. Stay in SQL Editor.
2. Open `docs/database/002_seed.sql`.
3. Paste it into the SQL Editor.
4. Run it.
5. Confirm demo records exist for:
   - Istanbul
   - Antalya
   - Izmir

## 4. Get Database Connection Settings

In the Supabase project dashboard, click `Connect`.

For local Spring Boot services, prefer the Session Pooler connection string because it supports IPv4 and works well for persistent backend services.

You need these values:

```text
SUPABASE_DB_URL=
SUPABASE_DB_USERNAME=
SUPABASE_DB_PASSWORD=
```

For Spring Boot JDBC, convert the Supabase connection string to this format:

```text
jdbc:postgresql://HOST:PORT/postgres?sslmode=require
```

Example shape:

```text
SUPABASE_DB_URL=jdbc:postgresql://aws-0-eu-central-1.pooler.supabase.com:5432/postgres?sslmode=require
SUPABASE_DB_USERNAME=postgres.PROJECT_REF
SUPABASE_DB_PASSWORD=your-database-password
```

Your exact host, username, project ref, and region will be different.

## 5. Local Environment Files

Create local `.env` files or configure environment variables for these services first:

- `hotel-admin-service`
- `hotel-search-service`
- `booking-service`

Required shared variables:

```text
SUPABASE_DB_URL=jdbc:postgresql://HOST:PORT/postgres?sslmode=require
SUPABASE_DB_USERNAME=postgres.PROJECT_REF
SUPABASE_DB_PASSWORD=your-database-password
```

Do not commit `.env` files.

The current backend services use `spring-dotenv`, so local `.env` files in each service folder are loaded automatically during `mvn spring-boot:run`.

## 6. First Connection Tests

After environment variables are set, run:

```bash
cd hotel-admin-service
mvn spring-boot:run
```

Then in another terminal:

```bash
cd hotel-search-service
mvn spring-boot:run
```

Health checks:

```text
http://localhost:8081/actuator/health
http://localhost:8082/actuator/health
```

Demo search request:

```text
GET http://localhost:8082/api/v1/hotels/search?destination=Istanbul&checkIn=2026-07-15&checkOut=2026-07-18&guests=2&page=0&size=10
```

Logged-in discount request:

```text
GET http://localhost:8082/api/v1/hotels/search?destination=Istanbul&checkIn=2026-07-15&checkOut=2026-07-18&guests=2&page=0&size=10
Authorization: Bearer real-supabase-access-token
```

The second request shows 15% lower prices only when the token is a valid Supabase JWT.

## 7. Auth Setup

Supabase Auth is required by the PDF because local authentication is not accepted.

Current backend behavior:

- Search Service treats only a valid Supabase JWT as logged-in for discount behavior.
- Hotel Admin Service requires a valid Supabase JWT.
- Hotel Admin Service uses the JWT `sub` claim as the authenticated user id.
- `X-User-Id` is optional; if present, it must match JWT `sub`.

Later we will:

1. Enable Supabase Auth email/password.
2. Add frontend login/register.
3. Use Supabase user IDs in `bookings.user_id` and `hotel_admins.user_id`.

## 8. JWT Secret

Backend services that verify Supabase JWTs need:

```text
SUPABASE_JWT_SECRET=
SUPABASE_PROJECT_URL=https://your-project-ref.supabase.co
```

Find it in the Supabase dashboard under project settings/API or authentication JWT settings.

Do not commit this value.

Current backend behavior:

- Hotel Admin Service requires a valid Supabase JWT.
- Hotel Admin Service uses the JWT `sub` claim as the authenticated user id.
- `X-User-Id` is optional for gateway/client consistency; if present, it must match the JWT `sub`.
- Hotel Search Service applies the 15% discount only when the Bearer token is a valid Supabase JWT.
- HS256 legacy tokens are verified with `SUPABASE_JWT_SECRET`.
- ES256/RS256 asymmetric tokens are verified using the Supabase JWKS endpoint:
  - `https://your-project-ref.supabase.co/auth/v1/.well-known/jwks.json`
