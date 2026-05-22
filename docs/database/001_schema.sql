-- SE4458 Group 1 Hotel Booking System
-- Supabase PostgreSQL schema for the main relational data source.

create extension if not exists pgcrypto;

create or replace function set_updated_at()
returns trigger as $$
begin
  new.updated_at = now();
  return new;
end;
$$ language plpgsql;

create table if not exists hotels (
  id uuid primary key default gen_random_uuid(),
  name varchar(160) not null,
  description text,
  destination varchar(120) not null,
  address text not null,
  latitude numeric(9, 6) not null,
  longitude numeric(9, 6) not null,
  star_rating numeric(2, 1) not null check (star_rating between 1 and 5),
  amenities text[] not null default '{}',
  image_url text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists rooms (
  id uuid primary key default gen_random_uuid(),
  hotel_id uuid not null references hotels(id) on delete cascade,
  room_type varchar(120) not null,
  capacity integer not null check (capacity > 0),
  total_count integer not null check (total_count > 0),
  price_per_night numeric(10, 2) not null check (price_per_night > 0),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists room_availability (
  id uuid primary key default gen_random_uuid(),
  room_id uuid not null references rooms(id) on delete cascade,
  available_date date not null,
  available_count integer not null check (available_count >= 0),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint uq_room_availability_room_date unique (room_id, available_date)
);

create table if not exists bookings (
  id uuid primary key default gen_random_uuid(),
  hotel_id uuid not null references hotels(id),
  room_id uuid not null references rooms(id),
  user_id uuid not null,
  check_in date not null,
  check_out date not null,
  guest_count integer not null check (guest_count > 0),
  total_price numeric(10, 2) not null check (total_price >= 0),
  status varchar(40) not null default 'CONFIRMED',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint ck_booking_dates check (check_out > check_in)
);

create table if not exists hotel_admins (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null,
  hotel_id uuid not null references hotels(id) on delete cascade,
  role varchar(40) not null default 'ADMIN',
  created_at timestamptz not null default now(),
  constraint uq_hotel_admin_user_hotel unique (user_id, hotel_id)
);

create table if not exists notifications (
  id uuid primary key default gen_random_uuid(),
  user_id uuid,
  hotel_id uuid references hotels(id) on delete set null,
  booking_id uuid references bookings(id) on delete set null,
  message text not null,
  type varchar(60) not null,
  status varchar(40) not null default 'UNREAD',
  created_at timestamptz not null default now()
);

create index if not exists idx_hotels_destination
  on hotels (lower(destination));

create index if not exists idx_hotels_star_rating
  on hotels (star_rating);

create index if not exists idx_rooms_hotel_id
  on rooms (hotel_id);

create index if not exists idx_room_availability_room_date
  on room_availability (room_id, available_date);

create index if not exists idx_room_availability_date
  on room_availability (available_date);

create index if not exists idx_bookings_user_id
  on bookings (user_id);

create index if not exists idx_bookings_hotel_id
  on bookings (hotel_id);

create index if not exists idx_notifications_user_id
  on notifications (user_id);

drop trigger if exists trg_hotels_updated_at on hotels;
create trigger trg_hotels_updated_at
before update on hotels
for each row execute function set_updated_at();

drop trigger if exists trg_rooms_updated_at on rooms;
create trigger trg_rooms_updated_at
before update on rooms
for each row execute function set_updated_at();

drop trigger if exists trg_room_availability_updated_at on room_availability;
create trigger trg_room_availability_updated_at
before update on room_availability
for each row execute function set_updated_at();

drop trigger if exists trg_bookings_updated_at on bookings;
create trigger trg_bookings_updated_at
before update on bookings
for each row execute function set_updated_at();
