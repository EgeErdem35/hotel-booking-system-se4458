-- Real hotel demo data for SE4458 Group 1 Hotel Booking System.
-- Hotel names, addresses, and coordinates are based on public hotel/location pages.
-- Room prices, room counts, and availability are demo-only values.

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
  '44444444-4444-4444-4444-444444444444',
  'Four Seasons Hotel Istanbul at the Bosphorus',
  'Luxury Bosphorus-side hotel in Besiktas with spa, pool, dining, and palace-style rooms.',
  'Istanbul',
  'Ciragan Cad. No. 28, Besiktas, Istanbul, Turkiye',
  41.042527,
  29.011807,
  4.8,
  array['Free Wi-Fi', 'Spa', 'Pool', 'Bosphorus View', 'Fine Dining']
),
(
  '55555555-5555-5555-5555-555555555555',
  'Pera Palace Hotel',
  'Historic hotel in Tepebasi/Beyoglu with heritage rooms and central access.',
  'Istanbul',
  'Mesrutiyet Cad. No:52, Tepebasi, Istanbul, Turkiye',
  41.030976,
  28.973544,
  4.7,
  array['Free Wi-Fi', 'Historic Building', 'Spa', 'Restaurant', 'City Center']
),
(
  '66666666-6666-6666-6666-666666666666',
  'Ciragan Palace Kempinski Istanbul',
  'Bosphorus palace hotel between Besiktas and Ortakoy with luxury rooms and waterfront grounds.',
  'Istanbul',
  'Ciragan Caddesi 32, Istanbul, Turkiye',
  41.044460,
  29.016860,
  4.8,
  array['Free Wi-Fi', 'Bosphorus View', 'Pool', 'Spa', 'Palace Hotel']
),
(
  '77777777-7777-7777-7777-777777777777',
  'Rixos Downtown Antalya',
  'Resort hotel near Konyaalti Beach and Ataturk Culture Park with pool, spa, and all-inclusive facilities.',
  'Antalya',
  'Sakip Sabanci Bulvari Falez Mevki, Muratpasa, Antalya, Turkiye',
  36.884630,
  30.669880,
  4.6,
  array['Free Wi-Fi', 'Pool', 'Spa', 'Beach Access', 'All Inclusive']
),
(
  '88888888-8888-8888-8888-888888888888',
  'Swissotel Buyuk Efes Izmir',
  'Five-star hotel in Alsancak overlooking Kordon Promenade with landscaped gardens and spa.',
  'Izmir',
  'Gaziosmanpasa Bulvari No 1, Alsancak, Izmir, Turkiye',
  38.427876,
  27.133994,
  4.6,
  array['Free Wi-Fi', 'Spa', 'Pool', 'Garden', 'Kordon Promenade']
),
(
  '99999999-9999-9999-9999-999999999998',
  'Izmir Marriott Hotel',
  'Seafront five-star hotel in Konak/Alsancak with sea views, restaurants, spa, and rooftop facilities.',
  'Izmir',
  'Akdeniz Mahallesi Gazi Bulvari No 1, Alsancak, Konak, Izmir, Turkiye',
  38.423900,
  27.132600,
  4.5,
  array['Free Wi-Fi', 'Sea View', 'Spa', 'Indoor Pool', 'Rooftop Bar']
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
('10000000-0000-0000-0000-000000000001', '44444444-4444-4444-4444-444444444444', 'Superior Bosphorus Room', 2, 12, 580.00),
('10000000-0000-0000-0000-000000000002', '44444444-4444-4444-4444-444444444444', 'Palace Suite', 4, 4, 1150.00),
('10000000-0000-0000-0000-000000000003', '55555555-5555-5555-5555-555555555555', 'Deluxe Pera Room', 2, 14, 260.00),
('10000000-0000-0000-0000-000000000004', '55555555-5555-5555-5555-555555555555', 'Grand Pera Studio', 3, 6, 420.00),
('10000000-0000-0000-0000-000000000005', '66666666-6666-6666-6666-666666666666', 'Bosphorus View Room', 2, 10, 620.00),
('10000000-0000-0000-0000-000000000006', '66666666-6666-6666-6666-666666666666', 'Palace Suite', 4, 5, 1400.00),
('10000000-0000-0000-0000-000000000007', '77777777-7777-7777-7777-777777777777', 'Deluxe Sea View Room', 2, 16, 240.00),
('10000000-0000-0000-0000-000000000008', '77777777-7777-7777-7777-777777777777', 'Family Suite', 4, 8, 380.00),
('10000000-0000-0000-0000-000000000009', '88888888-8888-8888-8888-888888888888', 'Classic Garden Room', 2, 15, 210.00),
('10000000-0000-0000-0000-000000000010', '88888888-8888-8888-8888-888888888888', 'Executive Sea View Room', 2, 8, 320.00),
('10000000-0000-0000-0000-000000000011', '99999999-9999-9999-9999-999999999998', 'Deluxe City Room', 2, 12, 190.00),
('10000000-0000-0000-0000-000000000012', '99999999-9999-9999-9999-999999999998', 'Sea View Suite', 3, 6, 310.00)
on conflict (id) do update set
  hotel_id = excluded.hotel_id,
  room_type = excluded.room_type,
  capacity = excluded.capacity,
  total_count = excluded.total_count,
  price_per_night = excluded.price_per_night;

insert into room_availability (room_id, available_date, available_count)
select room_id::uuid, day::date, available_count
from (
  values
    ('10000000-0000-0000-0000-000000000001', 7),
    ('10000000-0000-0000-0000-000000000002', 2),
    ('10000000-0000-0000-0000-000000000003', 8),
    ('10000000-0000-0000-0000-000000000004', 3),
    ('10000000-0000-0000-0000-000000000005', 6),
    ('10000000-0000-0000-0000-000000000006', 2),
    ('10000000-0000-0000-0000-000000000007', 9),
    ('10000000-0000-0000-0000-000000000008', 4),
    ('10000000-0000-0000-0000-000000000009', 10),
    ('10000000-0000-0000-0000-000000000010', 5),
    ('10000000-0000-0000-0000-000000000011', 8),
    ('10000000-0000-0000-0000-000000000012', 3)
) as rooms_to_seed(room_id, available_count)
cross join generate_series(date '2026-07-15', date '2026-08-10', interval '1 day') as day
on conflict (room_id, available_date) do update set
  available_count = excluded.available_count;

insert into hotel_admins (user_id, hotel_id, role)
select '99999999-9999-9999-9999-999999999999'::uuid, hotel_id::uuid, 'ADMIN'
from (
  values
    ('44444444-4444-4444-4444-444444444444'),
    ('55555555-5555-5555-5555-555555555555'),
    ('66666666-6666-6666-6666-666666666666'),
    ('77777777-7777-7777-7777-777777777777'),
    ('88888888-8888-8888-8888-888888888888'),
    ('99999999-9999-9999-9999-999999999998')
) as hotel_ids(hotel_id)
on conflict (user_id, hotel_id) do update set
  role = excluded.role;
