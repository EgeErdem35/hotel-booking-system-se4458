# Data Models and ER Diagram

The main relational data model is stored in Supabase PostgreSQL. Comments are intentionally stored separately in AWS DynamoDB, and hotel detail cache entries are stored in Upstash Redis.

## Supabase PostgreSQL ER Diagram

```mermaid
erDiagram
    HOTELS ||--o{ ROOMS : has
    HOTELS ||--o{ HOTEL_ADMINS : managed_by
    HOTELS ||--o{ BOOKINGS : receives
    HOTELS ||--o{ NOTIFICATIONS : referenced_by
    ROOMS ||--o{ ROOM_AVAILABILITY : has
    ROOMS ||--o{ BOOKINGS : booked_as
    BOOKINGS ||--o{ NOTIFICATIONS : creates

    HOTELS {
        uuid id PK
        varchar name
        text description
        varchar destination
        text address
        numeric latitude
        numeric longitude
        numeric star_rating
        text_array amenities
        timestamptz created_at
        timestamptz updated_at
    }

    ROOMS {
        uuid id PK
        uuid hotel_id FK
        varchar room_type
        integer capacity
        integer total_count
        numeric price_per_night
        timestamptz created_at
        timestamptz updated_at
    }

    ROOM_AVAILABILITY {
        uuid id PK
        uuid room_id FK
        date available_date
        integer available_count
        timestamptz created_at
        timestamptz updated_at
    }

    BOOKINGS {
        uuid id PK
        uuid hotel_id FK
        uuid room_id FK
        uuid user_id
        date check_in
        date check_out
        integer guest_count
        numeric total_price
        varchar status
        timestamptz created_at
        timestamptz updated_at
    }

    HOTEL_ADMINS {
        uuid id PK
        uuid user_id
        uuid hotel_id FK
        varchar role
        timestamptz created_at
    }

    NOTIFICATIONS {
        uuid id PK
        uuid user_id
        uuid hotel_id FK
        uuid booking_id FK
        text message
        varchar type
        varchar status
        timestamptz created_at
    }
```

## DynamoDB Comments Model

Table: `hotel_comments`

Recommended access pattern:

- Partition by `hotelId`
- Sort by `createdAt`

Item shape:

```json
{
  "commentId": "uuid",
  "hotelId": "uuid",
  "userId": "uuid",
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
```

## Upstash Redis Cache Model

Hotel detail cache key:

```text
hotel:details:{hotelId}
```

Rules:

- Cache only unfiltered hotel detail responses.
- Do not cache availability-sensitive responses when `checkIn`, `checkOut`, or `guests` filters are present.
- Store base, undiscounted responses and apply logged-in discount per request.

## RabbitMQ Message Model

Queue:

```text
reservation.created
```

Message shape:

```json
{
  "bookingId": "uuid",
  "hotelId": "uuid",
  "roomId": "uuid",
  "userId": "uuid",
  "checkIn": "2026-07-15",
  "checkOut": "2026-07-18",
  "guestCount": 2,
  "totalPrice": 630.00,
  "createdAt": "2026-05-11T12:00:00Z"
}
```
