# Database Setup

This folder contains the Supabase PostgreSQL scripts for the main relational data source required by the SE4458 Group 1 Hotel Booking System.

Run the files in this order:

1. `001_schema.sql`
2. `002_seed.sql`
3. `003_real_hotels_seed.sql`
4. `004_fix_istanbul_hotel_map_coordinates.sql`
5. `005_add_hotel_image_url.sql`

## Tables

- `hotels`
- `rooms`
- `room_availability`
- `bookings`
- `hotel_admins`
- `notifications`

`005_add_hotel_image_url.sql` is safe to run on an existing database because it uses `add column if not exists`.

## PDF Alignment

- Hotel admins can manage rooms and availability between dates.
- Users can search only rooms that have availability for the selected dates.
- Booking records are stored without payment transactions.
- Capacity can be decreased per selected date range through `room_availability`.
- Notification records can represent reservation messages and low-capacity admin alerts.
- SQLite is not used; the target database is Supabase PostgreSQL.

Comments are intentionally not stored here. Per the PDF, comments must use a separate NoSQL database, which this project maps to AWS DynamoDB.
