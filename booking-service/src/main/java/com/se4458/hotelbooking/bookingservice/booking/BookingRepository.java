package com.se4458.hotelbooking.bookingservice.booking;

import java.math.BigDecimal;
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
public class BookingRepository {

    private final JdbcTemplate jdbcTemplate;

    public BookingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<RoomBookingData> findRoomBookingData(UUID roomId) {
        return jdbcTemplate.query("""
                select
                  r.id as room_id,
                  r.hotel_id,
                  r.capacity,
                  r.price_per_night,
                  h.name as hotel_name
                from rooms r
                join hotels h on h.id = r.hotel_id
                where r.id = ?
                """, (rs, rowNum) -> new RoomBookingData(
                rs.getObject("room_id", UUID.class),
                rs.getObject("hotel_id", UUID.class),
                rs.getString("hotel_name"),
                rs.getInt("capacity"),
                rs.getBigDecimal("price_per_night")
        ), roomId).stream().findFirst();
    }

    public boolean hasAvailabilityForEveryNight(UUID roomId, LocalDate checkIn, LocalDate checkOut, long nights) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(*)
                from room_availability
                where room_id = ?
                  and available_date >= ?
                  and available_date < ?
                  and available_count > 0
                """, Integer.class, roomId, checkIn, checkOut);
        return count != null && count == nights;
    }

    public int decreaseAvailability(UUID roomId, LocalDate checkIn, LocalDate checkOut) {
        return jdbcTemplate.update("""
                update room_availability
                set available_count = available_count - 1
                where room_id = ?
                  and available_date >= ?
                  and available_date < ?
                  and available_count > 0
                """, roomId, checkIn, checkOut);
    }

    public BookingResponse createBooking(CreateBookingCommand command) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into bookings (
                      hotel_id, room_id, user_id, check_in, check_out, guest_count, total_price, status
                    ) values (?, ?, ?, ?, ?, ?, ?, 'CONFIRMED')
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setObject(1, command.hotelId());
            statement.setObject(2, command.roomId());
            statement.setObject(3, command.userId());
            statement.setObject(4, command.checkIn());
            statement.setObject(5, command.checkOut());
            statement.setInt(6, command.guestCount());
            statement.setBigDecimal(7, command.totalPrice());
            return statement;
        }, keyHolder);

        UUID bookingId = (UUID) keyHolder.getKeys().get("id");
        return findBookingById(bookingId).orElseThrow();
    }

    public Optional<BookingResponse> findBookingById(UUID bookingId) {
        return jdbcTemplate.query("""
                select
                  b.id,
                  b.hotel_id,
                  b.room_id,
                  b.user_id,
                  b.check_in,
                  b.check_out,
                  b.guest_count,
                  b.total_price,
                  b.status,
                  b.created_at,
                  h.name as hotel_name,
                  r.room_type
                from bookings b
                join hotels h on h.id = b.hotel_id
                join rooms r on r.id = b.room_id
                where b.id = ?
                """, (rs, rowNum) -> new BookingResponse(
                rs.getObject("id", UUID.class),
                rs.getObject("hotel_id", UUID.class),
                rs.getObject("room_id", UUID.class),
                rs.getObject("user_id", UUID.class),
                rs.getString("hotel_name"),
                rs.getString("room_type"),
                rs.getObject("check_in", LocalDate.class),
                rs.getObject("check_out", LocalDate.class),
                rs.getInt("guest_count"),
                rs.getBigDecimal("total_price"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toInstant()
        ), bookingId).stream().findFirst();
    }

    public List<BookingResponse> findBookingsByUserId(UUID userId) {
        return jdbcTemplate.query("""
                select
                  b.id,
                  b.hotel_id,
                  b.room_id,
                  b.user_id,
                  b.check_in,
                  b.check_out,
                  b.guest_count,
                  b.total_price,
                  b.status,
                  b.created_at,
                  h.name as hotel_name,
                  r.room_type
                from bookings b
                join hotels h on h.id = b.hotel_id
                join rooms r on r.id = b.room_id
                where b.user_id = ?
                order by b.created_at desc
                """, (rs, rowNum) -> new BookingResponse(
                rs.getObject("id", UUID.class),
                rs.getObject("hotel_id", UUID.class),
                rs.getObject("room_id", UUID.class),
                rs.getObject("user_id", UUID.class),
                rs.getString("hotel_name"),
                rs.getString("room_type"),
                rs.getObject("check_in", LocalDate.class),
                rs.getObject("check_out", LocalDate.class),
                rs.getInt("guest_count"),
                rs.getBigDecimal("total_price"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toInstant()
        ), userId);
    }

    public record RoomBookingData(
            UUID roomId,
            UUID hotelId,
            String hotelName,
            int capacity,
            BigDecimal pricePerNight
    ) {
    }
}
