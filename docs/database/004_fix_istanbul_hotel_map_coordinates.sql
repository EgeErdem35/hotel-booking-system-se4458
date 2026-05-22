-- Correct Istanbul hotel map coordinates for Google Maps-oriented demos.
-- Also replaces the original fake Istanbul demo hotel with a real hotel record.
-- Room/availability rows keep their existing IDs so booking demos continue to work.

update hotels
set
  name = 'Swissotel The Bosphorus Istanbul',
  description = 'Five-star hotel in Macka/Besiktas overlooking the Bosphorus, surrounded by historic palace gardens.',
  destination = 'Istanbul',
  address = 'Visnezade Mah. Acisu Sok. No. 19, Macka, Besiktas, Istanbul, Turkiye',
  latitude = 41.041463,
  longitude = 28.998556,
  star_rating = 4.7,
  amenities = array['Free Wi-Fi', 'Spa', 'Pool', 'Bosphorus View', 'Garden']
where id = '11111111-1111-1111-1111-111111111111';

update hotels
set
  latitude = 41.042527,
  longitude = 29.011807
where id = '44444444-4444-4444-4444-444444444444';

update hotels
set
  latitude = 41.030976,
  longitude = 28.973544
where id = '55555555-5555-5555-5555-555555555555';

update hotels
set
  latitude = 41.044460,
  longitude = 29.016860
where id = '66666666-6666-6666-6666-666666666666';
