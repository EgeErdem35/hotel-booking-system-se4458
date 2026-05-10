# SE4458 Final Project Documentation  
# Group 1 — Hotel Booking System

## 1. Project Overview

This project is a **Hotel Booking System** similar to Hotels.com.

The system allows:

- Hotel administrators to add and update hotels, rooms, and room availability.
- Users to search hotels by destination, dates, and number of people.
- Logged-in users to see discounted prices.
- Users to book available rooms.
- Users to view hotel comments, ratings, and rating distribution graphs.
- A notification service to process reservation events and run nightly capacity checks.
- An AI agent to help users search and book hotels through a chat interface.

The project follows a **service-oriented architecture**. Each major business capability is implemented as a separate service and exposed through versioned REST APIs. All public API calls go through an API Gateway.

This documentation is based on the SE4458 Final Project Group 1 requirements and the technology decisions made for the project.

---

## 2. Confirmed Technology Stack

### Frontend

```text
Next.js
Tailwind CSS
AWS Amplify
Backend
Java Spring Boot
Maven
REST APIs
Swagger / OpenAPI
AWS Elastic Beanstalk
API Gateway
Spring Cloud Gateway
Authentication / IAM
Supabase Auth
JWT-based authentication
Main Relational Database
Supabase PostgreSQL
NoSQL Database for Comments
AWS DynamoDB
Distributed Cache
Upstash Redis
Queue / Messaging
RabbitMQ

Local:
Docker RabbitMQ

Cloud:
CloudAMQP
Scheduled Task
Spring Boot @Scheduled
Inside notification-service
AI Agent
OpenAI API
API Documentation
Swagger / OpenAPI
springdoc-openapi
Development Environment
VS Code
Codex 5.5
GitHub
3. Architecture Overview

The system will be developed as a monorepo.

hotel-booking-system-se4458/
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
4. Service Responsibilities
4.1 Frontend
Folder:
frontend/

Technology:
Next.js + Tailwind CSS

Deployment:
AWS Amplify

Responsibilities:

Home page
Hotel search page
Search results page
Hotel detail page
Booking flow
Login/register pages
Admin dashboard
Comments and ratings page
Rating distribution graphs
AI agent chat window
Map view for searched hotels

The frontend communicates only with the API Gateway.

4.2 API Gateway Service
Folder:
api-gateway-service/

Technology:
Spring Boot + Spring Cloud Gateway

Responsibilities:

Route frontend requests to backend services.
Forward authentication headers.
Centralized CORS configuration.
Centralized public API entry point.
Optional request logging.
Optional rate limiting.

Example routes:

/api/v1/admin/**          -> hotel-admin-service
/api/v1/hotels/**         -> hotel-search-service
/api/v1/bookings/**       -> booking-service
/api/v1/comments/**       -> comments-service
/api/v1/notifications/**  -> notification-service
/api/v1/ai/**             -> ai-agent-service
4.3 Hotel Admin Service
Folder:
hotel-admin-service/

Technology:
Spring Boot + Supabase PostgreSQL

Responsibilities:

Add hotels.
Update hotels.
Add rooms.
Update rooms.
Add room availability between start and end dates.
Update room availability.
Require authenticated admin access.

Important requirement:

Hotel Admin Service must be authenticated.

Main endpoints:

POST /api/v1/admin/hotels
PUT /api/v1/admin/hotels/{hotelId}
POST /api/v1/admin/hotels/{hotelId}/rooms
PUT /api/v1/admin/rooms/{roomId}
POST /api/v1/admin/rooms/{roomId}/availability
4.4 Hotel Search Service
Folder:
hotel-search-service/

Technology:
Spring Boot + Supabase PostgreSQL + Upstash Redis

Responsibilities:

Search hotels by destination.
Search hotels by check-in and check-out dates.
Search hotels by number of people.
Return only hotels with available rooms for selected dates.
Apply 15% discount for logged-in users.
Return hotel details.
Cache hotel details using Upstash Redis.
Return map data for searched hotels.
Support pagination.

Main endpoints:

GET /api/v1/hotels/search
GET /api/v1/hotels/{hotelId}
GET /api/v1/hotels/{hotelId}/map

Example search request:

GET /api/v1/hotels/search?destination=Rome&checkIn=2026-07-15&checkOut=2026-07-18&guests=2&page=0&size=10

Important requirements:

Only rooms marked as available for the selected dates should appear.
Logged-in users should see 15% discounted prices.
Search results should support “show on map”.
4.5 Booking Service
Folder:
booking-service/

Technology:
Spring Boot + Supabase PostgreSQL + RabbitMQ

Responsibilities:

Create hotel bookings.
Check availability for selected dates.
Decrease capacity for selected dates after booking.
Store booking records.
Publish reservation.created event to RabbitMQ.
No payment transaction is required.

Main endpoints:

POST /api/v1/bookings
GET /api/v1/bookings/{bookingId}
GET /api/v1/bookings/user/{userId}

Important requirements:

User can book hotel from hotel detail page.
Booking must decrease capacity for selected dates.
No payment transaction is needed.
4.6 Comments Service
Folder:
comments-service/

Technology:
Spring Boot + AWS DynamoDB

Responsibilities:

Add hotel comments.
List comments by hotel.
Store overall rating.
Store service-based ratings.
Provide rating summary.
Provide rating distribution data for frontend graphs.
Support pagination.

Main endpoints:

POST /api/v1/comments
GET /api/v1/comments/hotel/{hotelId}?page=0&size=10
GET /api/v1/comments/hotel/{hotelId}/summary
GET /api/v1/comments/hotel/{hotelId}/distribution

Important requirement:

Comments must be stored in a separate NoSQL database.

DynamoDB table:

hotel_comments
4.7 Notification Service
Folder:
notification-service/

Technology:
Spring Boot + RabbitMQ + @Scheduled

Responsibilities:

Consume reservation.created events from RabbitMQ.
Generate reservation detail notifications.
Run nightly scheduled task.
Check hotel capacities for the next month.
Notify hotel administrators if capacity is below 20%.
Provide a manual test endpoint for demo purposes.

Main endpoints:

GET /api/v1/notifications/admin/{adminId}
GET /api/v1/notifications/user/{userId}
POST /api/v1/notifications/test-nightly-job

Scheduled task example:

@Scheduled(cron = "0 0 2 * * *")
public void runNightlyNotificationJob() {
    // Check hotel capacities for next month
    // Notify hotel admins if capacity is below 20%
    // Process reservation notifications
}

Important requirements:

Notification Service must write a nightly scheduled task.
It must check hotel capacities and notify admins when capacity is below 20%.
It must pull new reservations from the queue and send reservation detail messages.
4.8 AI Agent Service
Folder:
ai-agent-service/

Technology:
Spring Boot + OpenAI API

Responsibilities:

Provide AI chat endpoint.
Understand hotel search intent from user message.
Ask follow-up questions if required information is missing.
Call Hotel Search Service.
Return hotel options to frontend.
Call Booking Service after user confirmation.
Use project APIs, not only text generation.

Main endpoint:

POST /api/v1/ai/chat

Example request:

{
  "sessionId": "session_123",
  "message": "I want to book a hotel in Rome from July 15 to July 18 for two adults."
}

Example AI flow:

1. User asks for a hotel.
2. AI extracts destination, dates, and guest count.
3. AI calls Hotel Search Service.
4. AI shows hotel options.
5. User confirms a hotel.
6. AI calls Booking Service.
7. AI returns booking confirmation.

Important requirement:

AI agent must use the APIs created in this project to perform search and booking use cases.
Real-time messaging is not required.
5. Data Sources
5.1 Supabase PostgreSQL

Used for main relational business data.

Suggested tables:

hotels
rooms
room_availability
bookings
hotel_admins
notifications
hotels
id UUID PRIMARY KEY
name VARCHAR
description TEXT
destination VARCHAR
address TEXT
latitude DECIMAL
longitude DECIMAL
star_rating DECIMAL
base_price DECIMAL
amenities TEXT[]
created_at TIMESTAMP
updated_at TIMESTAMP
rooms
id UUID PRIMARY KEY
hotel_id UUID
room_type VARCHAR
capacity INTEGER
total_count INTEGER
price_per_night DECIMAL
created_at TIMESTAMP
updated_at TIMESTAMP
room_availability
id UUID PRIMARY KEY
room_id UUID
date DATE
available_count INTEGER
created_at TIMESTAMP
updated_at TIMESTAMP
bookings
id UUID PRIMARY KEY
hotel_id UUID
room_id UUID
user_id UUID
check_in DATE
check_out DATE
guest_count INTEGER
total_price DECIMAL
status VARCHAR
created_at TIMESTAMP
updated_at TIMESTAMP
hotel_admins
id UUID PRIMARY KEY
user_id UUID
hotel_id UUID
role VARCHAR
created_at TIMESTAMP
notifications
id UUID PRIMARY KEY
user_id UUID
hotel_id UUID
booking_id UUID
message TEXT
type VARCHAR
status VARCHAR
created_at TIMESTAMP
5.2 AWS DynamoDB

Used for comments and service ratings.

Table name:

hotel_comments

Suggested structure:

{
  "commentId": "comment_001",
  "hotelId": "hotel_123",
  "userId": "user_456",
  "overallRating": 4.5,
  "serviceRatings": {
    "cleanliness": 5,
    "location": 4,
    "staff": 5,
    "comfort": 4
  },
  "comment": "Great hotel with clean rooms and good location.",
  "createdAt": "2026-05-01T12:00:00Z"
}

Suggested keys:

Partition key:
hotelId

Sort key:
createdAt
5.3 Upstash Redis

Used as distributed cache.

Required cache:

hotel:details:{hotelId}

Optional search result cache:

hotel:search:{destination}:{checkIn}:{checkOut}:{guestCount}:{page}

Cache rules:

- Hotel details should be cached.
- Cache should be invalidated or updated when hotel data changes.
- Search result cache is optional.
5.4 RabbitMQ / CloudAMQP

Used for asynchronous reservation notification events.

Local:

Docker RabbitMQ

Cloud:

CloudAMQP

Queue name:

reservation.created

Example message:

{
  "bookingId": "booking_123",
  "hotelId": "hotel_456",
  "userId": "user_789",
  "checkIn": "2026-07-15",
  "checkOut": "2026-07-18",
  "guestCount": 2,
  "totalPrice": 535.50,
  "createdAt": "2026-05-01T20:30:00Z"
}
6. API Design Rules

All APIs must be versioned.

/api/v1/...

Pagination should be used where needed.

Example pagination parameters:

page=0
size=10
sort=price,asc

All services should provide Swagger/OpenAPI documentation.

Example Swagger URLs:

/api-gateway-service/swagger-ui.html
/hotel-admin-service/swagger-ui.html
/hotel-search-service/swagger-ui.html
/booking-service/swagger-ui.html
/comments-service/swagger-ui.html
/notification-service/swagger-ui.html
/ai-agent-service/swagger-ui.html

All services should expose health checks.

GET /actuator/health
7. Local Development Setup
7.1 Required Tools
VS Code
Codex 5.5 extension
Git
Java 17 or Java 21
Maven
Node.js
npm
Docker
Docker Compose
7.2 Local Infrastructure

Local RabbitMQ should be started using Docker Compose.

Example docker-compose.yml:

services:
  rabbitmq:
    image: rabbitmq:3-management
    container_name: hotel-booking-rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest

RabbitMQ management panel:

http://localhost:15672
username: guest
password: guest
8. Environment Variables

Secrets must not be hardcoded.

Each service should use environment variables.

8.1 Common Backend Variables
SERVER_PORT=
SUPABASE_DB_URL=
SUPABASE_DB_USERNAME=
SUPABASE_DB_PASSWORD=
SUPABASE_JWT_SECRET=
SUPABASE_PROJECT_URL=
SUPABASE_ANON_KEY=
8.2 DynamoDB Variables
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
AWS_REGION=
DYNAMODB_COMMENTS_TABLE=hotel_comments
8.3 Redis Variables
UPSTASH_REDIS_REST_URL=
UPSTASH_REDIS_REST_TOKEN=
8.4 RabbitMQ Variables

Local:

RABBITMQ_URL=amqp://guest:guest@localhost:5672

Cloud:

RABBITMQ_URL=amqps://cloudamqp-url
8.5 OpenAI Variables
OPENAI_API_KEY=
OPENAI_MODEL=
8.6 Frontend Variables
NEXT_PUBLIC_API_BASE_URL=
NEXT_PUBLIC_SUPABASE_URL=
NEXT_PUBLIC_SUPABASE_ANON_KEY=
9. Deployment Plan
9.1 Frontend Deployment
Service:
AWS Amplify

Application folder:
frontend/

Frontend environment variables:

NEXT_PUBLIC_API_BASE_URL
NEXT_PUBLIC_SUPABASE_URL
NEXT_PUBLIC_SUPABASE_ANON_KEY
9.2 Backend Deployment
Service:
AWS Elastic Beanstalk

Backend services to deploy:

api-gateway-service
hotel-admin-service
hotel-search-service
booking-service
comments-service
notification-service
ai-agent-service

Each backend service should include:

Dockerfile
application.yml
Swagger/OpenAPI configuration
Actuator health endpoint
9.3 External Cloud Services
Supabase:
- PostgreSQL
- Supabase Auth

AWS:
- DynamoDB
- Elastic Beanstalk
- Amplify

Upstash:
- Redis

CloudAMQP:
- RabbitMQ

OpenAI:
- AI Agent API
10. Docker Requirements

The project must include Dockerfiles.

Docker image files should not be committed.

Recommended Dockerfiles:

frontend/Dockerfile
api-gateway-service/Dockerfile
hotel-admin-service/Dockerfile
hotel-search-service/Dockerfile
booking-service/Dockerfile
comments-service/Dockerfile
notification-service/Dockerfile
ai-agent-service/Dockerfile

Root-level Docker Compose file:

docker-compose.yml

Purpose of Docker Compose:

- Run RabbitMQ locally.
- Optionally run local development dependencies.
11. VS Code + Codex 5.5 Development Guide

This project will be implemented in VS Code using Codex 5.5.

Codex should be used task by task. Do not ask Codex to implement the whole project in a single prompt.

Recommended order:

1. Create monorepo structure.
2. Create PROJECT_DOCUMENTATION.md and README.md.
3. Create Docker Compose with RabbitMQ.
4. Create Supabase PostgreSQL schema and seed files.
5. Create API Gateway service.
6. Create Hotel Admin Service.
7. Create Hotel Search Service.
8. Add Upstash Redis cache.
9. Create Booking Service.
10. Add RabbitMQ publishing.
11. Create Notification Service.
12. Add RabbitMQ consumer.
13. Add Spring @Scheduled nightly job.
14. Create Comments Service with DynamoDB.
15. Create AI Agent Service with OpenAI API.
16. Create Next.js frontend.
17. Connect frontend to API Gateway.
18. Add Swagger/OpenAPI.
19. Add Dockerfiles.
20. Prepare deployment.
21. Update README with deployed URLs and final notes.
12. Codex Working Rules

When using Codex, always follow these rules:

- Read PROJECT_DOCUMENTATION.md before making changes.
- Keep the selected technology stack.
- Do not replace Spring Boot with Node.js.
- Do not replace Next.js with plain React.
- Do not replace Supabase Auth with local authentication.
- Do not use SQLite.
- Use versioned REST APIs under /api/v1.
- Add pagination where needed.
- Keep services separated.
- Add Swagger/OpenAPI to backend services.
- Add Dockerfile for every service.
- Use environment variables instead of hardcoded secrets.
- Do not commit API keys or secrets.
- Keep the code simple enough for a university final project.
- Explain changed files after each task.
- Explain how to run and test after each task.
13. Master Prompt for Codex

Use this prompt at the beginning of the project:

You are helping me build my SE4458 final project: Group 1 Hotel Booking System.

Before making changes, read PROJECT_DOCUMENTATION.md carefully and follow it strictly.

Technology stack:
- Frontend: Next.js + Tailwind CSS
- Backend: Java Spring Boot
- API Gateway: Spring Cloud Gateway
- Auth: Supabase Auth with JWT
- Main DB: Supabase PostgreSQL
- Comments NoSQL DB: AWS DynamoDB
- Cache: Upstash Redis
- Queue: RabbitMQ
  - Local: Docker RabbitMQ
  - Cloud: CloudAMQP
- Scheduled task: Spring Boot @Scheduled inside notification-service
- AI Agent: OpenAI API
- Deployment: AWS Amplify for frontend, AWS Elastic Beanstalk for backend services
- API documentation: Swagger/OpenAPI

Create and maintain this monorepo structure:
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

Project requirements:
- Hotel admins can add/update rooms and availability between start and end dates.
- Hotel Admin Service must be authenticated.
- Users can search hotels by destination, dates, and number of people.
- Only rooms marked available for selected dates should appear.
- Logged-in users see 15% discounted prices.
- Search results must support “show on map”.
- Users can book hotels from the hotel detail page.
- Booking decreases capacity for selected dates.
- No payment transaction is required.
- Comments and ratings must be shown.
- Rating distribution graphs must be available.
- Comments must be stored in AWS DynamoDB.
- Hotel details must be cached in Upstash Redis.
- Notification Service must consume reservation events from RabbitMQ.
- Notification Service must run a nightly @Scheduled job to check hotel capacities for the next month and notify admins when capacity is below 20%.
- AI Agent must use our own APIs to perform hotel search and booking.
- All business use cases must be available via REST APIs.
- All APIs must be versioned with /api/v1.
- APIs must support pagination where needed.
- Use cloud database services. Do not use SQLite.
- Add Dockerfile for every service.
- Add Swagger/OpenAPI to every backend service.
- Use environment variables for all secrets.
- Do not hardcode API keys.
- Keep implementation simple, clean, and suitable for a university final project.

Do not implement everything at once.

First:
1. Inspect the repository.
2. Explain the implementation plan briefly.
3. Create the initial monorepo structure.
4. Create placeholder files only.
5. Do not implement business logic yet.
14. First Codex Task

After adding this documentation, give Codex this first task:

Read PROJECT_DOCUMENTATION.md.

Create the initial monorepo structure for the Hotel Booking System.

Use exactly this structure:

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

Do not implement business logic yet.

Only create:
- folder structure
- root .gitignore
- root docker-compose.yml with RabbitMQ for local development
- placeholder README sections
- placeholder .env.example files for frontend and backend services
- basic Maven project placeholders for Spring Boot services
- basic Next.js placeholder for frontend

Before modifying files, explain the plan briefly.
15. Future Codex Task Prompts
15.1 Create Supabase Schema
Create the Supabase PostgreSQL schema for this project.

Tables:
- hotels
- rooms
- room_availability
- bookings
- hotel_admins
- notifications

Requirements:
- Use UUID primary keys.
- Add created_at and updated_at fields.
- Add indexes for hotel search by destination.
- Add indexes for room availability by room_id and date.
- Add constraints for capacity and price.
- Add sample seed data for hotels, rooms, and room availability.
- Put SQL files under docs/database/.
15.2 Implement API Gateway
Implement api-gateway-service using Spring Boot and Spring Cloud Gateway.

Requirements:
- Java 17
- Maven
- Spring Cloud Gateway
- Actuator health endpoint
- CORS configuration for frontend
- Routes:
  /api/v1/admin/** -> hotel-admin-service
  /api/v1/hotels/** -> hotel-search-service
  /api/v1/bookings/** -> booking-service
  /api/v1/comments/** -> comments-service
  /api/v1/notifications/** -> notification-service
  /api/v1/ai/** -> ai-agent-service
- Forward Authorization header
- Use environment variables for service URLs
- Add Dockerfile
- Add README section for gateway
15.3 Implement Hotel Admin Service
Implement hotel-admin-service.

Requirements:
- Authenticated endpoints only.
- Admin can create hotel.
- Admin can update hotel.
- Admin can create room for hotel.
- Admin can update room.
- Admin can add or update room availability between start and end dates.
- Use Supabase PostgreSQL.
- Use DTOs and validation.
- Add Swagger annotations.
- Add proper error handling.
- Use /api/v1/admin prefix.
15.4 Implement Hotel Search Service
Implement hotel-search-service.

Requirements:
- Search hotels by destination, checkIn, checkOut, and guest count.
- Return only hotels with available rooms for all selected dates.
- Support pagination with page and size.
- Logged-in users should see 15% discounted prices.
- Anonymous users should see normal prices.
- Hotel details should be cached using Upstash Redis.
- Include map data: latitude and longitude.
- Use /api/v1/hotels prefix.
- Add Swagger/OpenAPI documentation.
15.5 Implement Booking Service
Implement booking-service.

Requirements:
- Create booking from hotel detail page.
- Check room availability for all dates.
- Decrease capacity for selected dates after booking.
- No payment transaction required.
- Publish reservation.created event to RabbitMQ after successful booking.
- Store booking in Supabase PostgreSQL.
- Use /api/v1/bookings prefix.
- Add validation and error handling.
- Add Swagger/OpenAPI documentation.
15.6 Implement Comments Service
Implement comments-service using AWS DynamoDB.

Requirements:
- Add hotel comment.
- List comments by hotelId with pagination.
- Store overall rating and service-based ratings.
- Provide rating summary endpoint.
- Provide rating distribution endpoint for frontend graphs.
- Use /api/v1/comments prefix.
- Add Swagger/OpenAPI documentation.
15.7 Implement Notification Service
Implement notification-service.

Requirements:
- Connect to RabbitMQ.
- Consume reservation.created queue.
- Generate reservation notification messages.
- Implement Spring Boot @Scheduled nightly job.
- Nightly job checks hotel capacities for the next month.
- If capacity is below 20%, create notification for hotel admin.
- Add test endpoint to trigger nightly job manually for demo.
- Use /api/v1/notifications prefix.
- Add Swagger/OpenAPI documentation.
15.8 Implement AI Agent Service
Implement ai-agent-service using OpenAI API.

Requirements:
- Provide POST /api/v1/ai/chat endpoint.
- Accept sessionId and message.
- Extract hotel search intent from user message.
- Ask follow-up questions if destination, dates, or guest count are missing.
- Call hotel-search-service through the API Gateway or internal service URL.
- Return hotel options to frontend.
- If user confirms booking, call booking-service.
- Do not only generate text; actually call project APIs.
- Use environment variable OPENAI_API_KEY.
- Add Swagger/OpenAPI documentation.
15.9 Implement Frontend
Implement the Next.js frontend with Tailwind CSS.

Pages:
- Home page with hotel search form
- Search results page
- Hotel detail page
- Booking confirmation flow
- Login/register page using Supabase Auth
- Admin dashboard for managing hotels, rooms, and availability
- Comments page with graphs
- AI agent chat window on main application screen
- Map view for searched hotels

Requirements:
- Use NEXT_PUBLIC_API_BASE_URL for API calls.
- Send Authorization Bearer token when user is logged in.
- Show 15% discounted price for logged-in users.
- Use responsive design.
- Keep UI simple but functional.
15.10 Add Docker Support
Add Docker support.

Requirements:
- Add Dockerfile for every Spring Boot service.
- Add Dockerfile for frontend.
- Add root docker-compose.yml.
- docker-compose.yml should include local RabbitMQ.
- Use environment variables.
- Do not include Docker image files.
16. Assumptions

The following assumptions are made:

- Payment transaction is not implemented because it is not required.
- Image upload is optional and may be skipped.
- Notification sending can be simulated by storing notification records or logging messages.
- Supabase Auth user IDs are used as user identifiers in backend services.
- Admin role is checked through the hotel_admins table.
- AI Agent can use structured API calls instead of real-time messaging.
- Real-time messaging is not required.
- Email/SMS sending is optional.
- Search result cache is optional, but hotel detail cache is required.
17. Final Deliverables

Final deliverables:

- Public GitHub repository link
- README document in GitHub repository
- Final deployed URLs
- Design and assumptions
- Issues encountered
- Data models / ER diagram
- Short video link, maximum 5 minutes
18. Minimum Viable Product Plan

First working MVP:

1. Admin can create hotel.
2. Admin can create room.
3. Admin can set room availability.
4. User can search hotels by destination, dates, and guests.
5. User can view hotel detail.
6. User can create booking.
7. Booking decreases capacity.
8. Booking publishes RabbitMQ event.
9. Notification Service consumes event.

After MVP:

1. Supabase Auth
2. Logged-in user discount
3. Comments with DynamoDB
4. Rating graphs
5. Upstash Redis cache
6. AI Agent
7. Map view
8. Deployment
9. Final README and video
19. Final Confirmed Stack
Frontend:
Next.js + Tailwind CSS

Backend:
Java Spring Boot

API Gateway:
Spring Cloud Gateway

Authentication:
Supabase Auth

Main Database:
Supabase PostgreSQL

Comments NoSQL Database:
AWS DynamoDB

Distributed Cache:
Upstash Redis

Queue:
RabbitMQ
Local: Docker RabbitMQ
Cloud: CloudAMQP

Scheduled Task:
Spring Boot @Scheduled in Notification Service

AI Agent:
OpenAI API

Deployment:
AWS Amplify for frontend
AWS Elastic Beanstalk for backend services

API Documentation:
Swagger / OpenAPI

Development Environment:
VS Code + Codex 5.5