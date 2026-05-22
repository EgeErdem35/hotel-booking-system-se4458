-- Adds optional image URL support for admin-uploaded hotel images.

alter table hotels
  add column if not exists image_url text;
