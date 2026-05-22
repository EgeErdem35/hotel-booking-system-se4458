# Demo Video Script

Target length: under 5 minutes.

## 0:00 - 0:25 Opening

Introduce the project:

- SE4458 Group 1 Hotel Booking System.
- Hotels.com-like booking workflow.
- Service-oriented architecture with API Gateway.

Show the repository structure:

- `frontend`
- `api-gateway-service`
- `hotel-admin-service`
- `hotel-search-service`
- `booking-service`
- `comments-service`
- `notification-service`
- `ai-agent-service`

## 0:25 - 0:55 Architecture

Show `docker-compose.yml` or architecture explanation:

- Frontend calls API Gateway.
- Gateway routes to backend services.
- Supabase PostgreSQL stores hotels, rooms, availability, bookings, admins, notifications.
- DynamoDB stores comments.
- Upstash Redis caches hotel details.
- RabbitMQ carries reservation events.
- AI Agent uses OpenAI and project APIs.

## 0:55 - 1:35 Search and Hotel Detail

Open frontend:

```text
http://localhost:3000
```

Demo:

- Search Istanbul.
- Dates: `2026-07-15` to `2026-07-18`.
- Guests: `2`.
- Show available hotel results.
- Select a hotel.
- Show date-aware room options.
- Mention 15 percent discount for authenticated users.
- Show map panel and hotel coordinates.

## 1:35 - 2:10 Booking Flow

Demo:

- Select a room.
- Use demo user id.
- Click booking button.
- Show booking confirmation.
- Explain capacity decreases in `room_availability`.
- Explain Booking Service publishes `reservation.created` to RabbitMQ.

## 2:10 - 2:45 Notifications

Show RabbitMQ or notification endpoint:

- Reservation event is consumed by Notification Service.
- User notification is stored.
- Manual nightly low-capacity job endpoint can be triggered:

```text
POST /api/v1/notifications/test-nightly-job
```

Explain:

- The scheduled job checks the next month.
- It notifies admins when capacity is below 20 percent.

## 2:45 - 3:20 Comments and Graphs

In frontend hotel detail:

- Show comments panel.
- Show average rating.
- Show star distribution graph.
- Show service-based ratings such as cleanliness, location, staff, comfort.

Mention:

- Comments are stored in DynamoDB, not PostgreSQL.

## 3:20 - 3:55 Admin Dashboard

Show admin panel:

- Enter Supabase JWT.
- Create/update hotel.
- Create/update room.
- Update room availability.

Mention:

- Admin endpoints require authentication.
- Admin ownership is checked through the `hotel_admins` table.

## 3:55 - 4:25 AI Agent

Show AI chat panel:

- Ask: `Find me a hotel in Istanbul from 2026-07-15 to 2026-07-18 for 2 guests.`
- Explain:
  - The AI Agent parses intent.
  - It calls project APIs through the API Gateway.
  - It uses OpenAI API when `OPENAI_API_KEY` is configured.
  - It can fall back to deterministic responses for demo safety.

## 4:25 - 4:50 Deployment and Docker

Show:

- Dockerfiles exist for every service and frontend.
- `docker compose up --build` can run the local stack.
- Environment variables are external.
- No secrets are committed.

Mention planned deployment:

- Frontend: AWS Amplify
- Backend services and gateway: AWS Elastic Beanstalk
- Queue: CloudAMQP
- Database/cache/NoSQL: Supabase, Upstash, DynamoDB

## 4:50 - 5:00 Closing

Summarize PDF alignment:

- REST APIs and API Gateway.
- Authenticated admin service.
- Search, booking, comments, notifications, cache, queue, scheduled job, AI Agent.
- Dockerfiles and final documentation are included.
