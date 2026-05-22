-- Demo seed data for SE4458 Group 1 Hotel Booking System.
-- Run after docs/database/001_schema.sql in Supabase SQL Editor.

insert into hotels (
  id,
  name,
  description,
  destination,
  address,
  latitude,
  longitude,
  star_rating,
  amenities
) values
(
  '11111111-1111-1111-1111-111111111111',
  'Istanbul Bosphorus Suites',
  'Central hotel close to the Bosphorus with breakfast and pool access.',
  'Istanbul',
  'Meclis-i Mebusan Cd. No:24, Beyoglu, Istanbul',
  41.031238,
  28.984810,
  4.6,
  array['Free Wi-Fi', 'Breakfast', 'Pool', 'Sea View']
),
(
  '22222222-2222-2222-2222-222222222222',
  'Antalya Marina Resort',
  'Resort hotel near the marina with family rooms and outdoor pool.',
  'Antalya',
  'Selcuk Mah. Marina Sk. No:10, Antalya',
  36.884140,
  30.705630,
  4.4,
  array['Free Wi-Fi', 'Breakfast', 'Pool', 'Spa']
),
(
  '33333333-3333-3333-3333-333333333333',
  'Izmir Kordon Hotel',
  'Business-friendly hotel near Kordon with easy access to city attractions.',
  'Izmir',
  'Ataturk Cd. No:75, Alsancak, Izmir',
  38.432890,
  27.143560,
  4.1,
  array['Free Wi-Fi', 'Breakfast', 'Parking']
)
on conflict (id) do update set
  name = excluded.name,
  description = excluded.description,
  destination = excluded.destination,
  address = excluded.address,
  latitude = excluded.latitude,
  longitude = excluded.longitude,
  star_rating = excluded.star_rating,
  amenities = excluded.amenities;

insert into rooms (
  id,
  hotel_id,
  room_type,
  capacity,
  total_count,
  price_per_night
) values
(
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  '11111111-1111-1111-1111-111111111111',
  'Deluxe Double Room',
  2,
  8,
  210.00
),
(
  'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
  '11111111-1111-1111-1111-111111111111',
  'Family Suite',
  4,
  4,
  320.00
),
(
  'cccccccc-cccc-cccc-cccc-cccccccccccc',
  '22222222-2222-2222-2222-222222222222',
  'Standard Sea View',
  2,
  10,
  180.00
),
(
  'dddddddd-dddd-dddd-dddd-dddddddddddd',
  '33333333-3333-3333-3333-333333333333',
  'Business Room',
  2,
  6,
  145.00
)
on conflict (id) do update set
  hotel_id = excluded.hotel_id,
  room_type = excluded.room_type,
  capacity = excluded.capacity,
  total_count = excluded.total_count,
  price_per_night = excluded.price_per_night;

insert into room_availability (room_id, available_date, available_count)
select 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'::uuid, day::date, 5
from generate_series(date '2026-07-15', date '2026-07-20', interval '1 day') as day
on conflict (room_id, available_date) do update set
  available_count = excluded.available_count;

insert into room_availability (room_id, available_date, available_count)
select 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid, day::date, 2
from generate_series(date '2026-07-15', date '2026-07-20', interval '1 day') as day
on conflict (room_id, available_date) do update set
  available_count = excluded.available_count;

insert into room_availability (room_id, available_date, available_count)
select 'cccccccc-cccc-cccc-cccc-cccccccccccc'::uuid, day::date, 3
from generate_series(date '2026-08-01', date '2026-08-08', interval '1 day') as day
on conflict (room_id, available_date) do update set
  available_count = excluded.available_count;

insert into room_availability (room_id, available_date, available_count)
select 'dddddddd-dddd-dddd-dddd-dddddddddddd'::uuid, day::date, 1
from generate_series(date '2026-07-15', date '2026-07-18', interval '1 day') as day
on conflict (room_id, available_date) do update set
  available_count = excluded.available_count;

insert into hotel_admins (user_id, hotel_id, role) values
(
  '99999999-9999-9999-9999-999999999999',
  '11111111-1111-1111-1111-111111111111',
  'ADMIN'
),
(
  '99999999-9999-9999-9999-999999999999',
  '22222222-2222-2222-2222-222222222222',
  'ADMIN'
)
on conflict (user_id, hotel_id) do update set
  role = excluded.role;
