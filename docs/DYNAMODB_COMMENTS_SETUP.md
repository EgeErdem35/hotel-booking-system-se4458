# DynamoDB Comments Setup

The PDF requires hotel comments to be stored in a separate NoSQL database. This project uses AWS DynamoDB for that requirement.

## Table

Table name:

```text
hotel_comments
```

Primary key:

```text
Partition key: hotelId   String
Sort key:      createdAt String
```

Required local environment variables for `comments-service`:

```text
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
AWS_REGION=eu-west-1
DYNAMODB_COMMENTS_TABLE=hotel_comments
```

`comments-service` reads these values from `.env` through Spring configuration. Do not commit `.env`.

## Stored Attributes

- `hotelId`
- `createdAt`
- `commentId`
- `userId`
- `overallRating`
- `serviceRatings`
- `comment`

## Endpoints

- `POST /api/v1/comments`
- `GET /api/v1/comments/hotel/{hotelId}?page=0&size=10`
- `GET /api/v1/comments/hotel/{hotelId}/summary`
- `GET /api/v1/comments/hotel/{hotelId}/distribution`
- `GET /api/v1/comments/hotel/{hotelId}/service-distribution`

## Verified Real Table

Verified against AWS DynamoDB table `hotel_comments` in `eu-west-1`:

- `POST /api/v1/comments` created two comments.
- List endpoint returned `2` comments for hotel `11111111-1111-1111-1111-111111111111`.
- Summary endpoint returned average rating `4.2`.
- Star distribution endpoint returned one 4-star bucket item and one 5-star bucket item.
- Service distribution endpoint returned averages for `cleanliness`, `location`, `staff`, and `comfort`.
