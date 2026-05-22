package com.se4458.hotelbooking.hoteladminservice.hotel;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class HotelAdminRepository {

    private final JdbcTemplate jdbcTemplate;

    public HotelAdminRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public HotelResponse createHotel(UUID adminUserId, CreateHotelRequest request) {
        UUID hotelId = UUID.randomUUID();
        String[] amenities = amenitiesArray(request.amenities());

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into hotels (
                      id, name, description, destination, address, latitude, longitude, star_rating, amenities
                    ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """);
            statement.setObject(1, hotelId);
            statement.setString(2, request.name());
            statement.setString(3, request.description());
            statement.setString(4, request.destination());
            statement.setString(5, request.address());
            statement.setBigDecimal(6, request.latitude());
            statement.setBigDecimal(7, request.longitude());
            statement.setBigDecimal(8, request.starRating());
            Array amenitiesSqlArray = connection.createArrayOf("text", amenities);
            statement.setArray(9, amenitiesSqlArray);
            return statement;
        });

        jdbcTemplate.update("""
                insert into hotel_admins (user_id, hotel_id, role)
                values (?, ?, 'ADMIN')
                on conflict (user_id, hotel_id) do nothing
                """, adminUserId, hotelId);

        return findHotelById(hotelId).orElseThrow();
    }

    public HotelResponse updateHotel(UUID hotelId, UpdateHotelRequest request) {
        String[] amenities = amenitiesArray(request.amenities());
        int updated = jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    update hotels
                    set name = ?, description = ?, destination = ?, address = ?,
                        latitude = ?, longitude = ?, star_rating = ?, amenities = ?
                    where id = ?
                    """);
            statement.setString(1, request.name());
            statement.setString(2, request.description());
            statement.setString(3, request.destination());
            statement.setString(4, request.address());
            statement.setBigDecimal(5, request.latitude());
            statement.setBigDecimal(6, request.longitude());
            statement.setBigDecimal(7, request.starRating());
            Array amenitiesSqlArray = connection.createArrayOf("text", amenities);
            statement.setArray(8, amenitiesSqlArray);
            statement.setObject(9, hotelId);
            return statement;
        });
        if (updated == 0) {
            return null;
        }
        return findHotelById(hotelId).orElseThrow();
    }

    public RoomResponse createRoom(UUID hotelId, CreateRoomRequest request) {
        UUID roomId = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into rooms (id, hotel_id, room_type, capacity, total_count, price_per_night)
                values (?, ?, ?, ?, ?, ?)
                """, roomId, hotelId, request.roomType(), request.capacity(), request.totalCount(), request.pricePerNight());
        return findRoomById(roomId).orElseThrow();
    }

    public RoomResponse updateRoom(UUID roomId, UpdateRoomRequest request) {
        int updated = jdbcTemplate.update("""
                update rooms
                set room_type = ?, capacity = ?, total_count = ?, price_per_night = ?
                where id = ?
                """, request.roomType(), request.capacity(), request.totalCount(), request.pricePerNight(), roomId);
        if (updated == 0) {
            return null;
        }
        return findRoomById(roomId).orElseThrow();
    }

    public int upsertAvailability(UUID roomId, LocalDate startDate, LocalDate endDate, int availableCount) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into room_availability (room_id, available_date, available_count)
                    select ?, day::date, ?
                    from generate_series(?::date, ?::date, interval '1 day') as day
                    on conflict (room_id, available_date)
                    do update set available_count = excluded.available_count
                    returning id
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setObject(1, roomId);
            statement.setInt(2, availableCount);
            statement.setObject(3, startDate);
            statement.setObject(4, endDate);
            return statement;
        }, keyHolder);
        return keyHolder.getKeyList().size();
    }

    public boolean hotelExists(UUID hotelId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from hotels where id = ?",
                Integer.class,
                hotelId
        );
        return count != null && count > 0;
    }

    public boolean roomExists(UUID roomId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from rooms where id = ?",
                Integer.class,
                roomId
        );
        return count != null && count > 0;
    }

    public boolean isAdminForHotel(UUID userId, UUID hotelId) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(*)
                from hotel_admins
                where user_id = ? and hotel_id = ?
                """, Integer.class, userId, hotelId);
        return count != null && count > 0;
    }

    public boolean isAdminForRoom(UUID userId, UUID roomId) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(*)
                from hotel_admins ha
                join rooms r on r.hotel_id = ha.hotel_id
                where ha.user_id = ? and r.id = ?
                """, Integer.class, userId, roomId);
        return count != null && count > 0;
    }

    public Optional<HotelResponse> findHotelById(UUID hotelId) {
        return jdbcTemplate.query("""
                select id, name, description, destination, address, latitude, longitude, star_rating, amenities,
                       to_jsonb(hotels)->>'image_url' as image_url
                from hotels
                where id = ?
                """, (rs, rowNum) -> new HotelResponse(
                rs.getObject("id", UUID.class),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("destination"),
                rs.getString("address"),
                rs.getBigDecimal("latitude"),
                rs.getBigDecimal("longitude"),
                rs.getBigDecimal("star_rating"),
                List.of((String[]) rs.getArray("amenities").getArray()),
                rs.getString("image_url")
        ), hotelId).stream().findFirst();
    }

    public HotelResponse updateHotelImageUrl(UUID hotelId, String imageUrl) {
        ensureImageUrlColumn();
        int updated = jdbcTemplate.update("""
                update hotels
                set image_url = ?
                where id = ?
                """, imageUrl, hotelId);
        if (updated == 0) {
            return null;
        }
        return findHotelById(hotelId).orElseThrow();
    }

    public Optional<RoomResponse> findRoomById(UUID roomId) {
        return jdbcTemplate.query("""
                select id, hotel_id, room_type, capacity, total_count, price_per_night
                from rooms
                where id = ?
                """, (rs, rowNum) -> new RoomResponse(
                rs.getObject("id", UUID.class),
                rs.getObject("hotel_id", UUID.class),
                rs.getString("room_type"),
                rs.getInt("capacity"),
                rs.getInt("total_count"),
                rs.getBigDecimal("price_per_night")
        ), roomId).stream().findFirst();
    }

    private String[] amenitiesArray(List<String> amenities) {
        if (amenities == null) {
            return new String[0];
        }
        return amenities.stream()
                .filter(amenity -> amenity != null && !amenity.isBlank())
                .map(String::trim)
                .toArray(String[]::new);
    }

    private void ensureImageUrlColumn() {
        jdbcTemplate.execute("alter table hotels add column if not exists image_url text");
    }
}
