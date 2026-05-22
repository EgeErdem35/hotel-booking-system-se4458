package com.se4458.hotelbooking.notificationservice.notification;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationRepository {

    private final JdbcTemplate jdbcTemplate;

    public NotificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public NotificationResponse createReservationNotification(ReservationCreatedEvent event, String message) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into notifications (user_id, hotel_id, booking_id, message, type, status)
                    values (?, ?, ?, ?, 'RESERVATION_DETAILS', 'UNREAD')
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setObject(1, event.userId());
            statement.setObject(2, event.hotelId());
            statement.setObject(3, event.bookingId());
            statement.setString(4, message);
            return statement;
        }, keyHolder);

        UUID notificationId = (UUID) keyHolder.getKeys().get("id");
        return findNotificationById(notificationId);
    }

    public int createLowCapacityNotification(LowCapacityHotel lowCapacityHotel, String message) {
        return jdbcTemplate.update("""
                insert into notifications (user_id, hotel_id, booking_id, message, type, status)
                select ?, ?, null, ?, 'LOW_CAPACITY', 'UNREAD'
                where not exists (
                  select 1
                  from notifications
                  where user_id = ?
                    and hotel_id = ?
                    and type = 'LOW_CAPACITY'
                    and message = ?
                    and status = 'UNREAD'
                    and created_at >= now() - interval '1 day'
                )
                """,
                lowCapacityHotel.adminUserId(),
                lowCapacityHotel.hotelId(),
                message,
                lowCapacityHotel.adminUserId(),
                lowCapacityHotel.hotelId(),
                message
        );
    }

    public List<LowCapacityHotel> findLowCapacityHotels(LocalDate startDate, LocalDate endDateExclusive) {
        return jdbcTemplate.query("""
                with room_capacity as (
                  select
                    h.id as hotel_id,
                    h.name as hotel_name,
                    r.id as room_id,
                    r.room_type,
                    sum(r.total_count) as total_capacity,
                    coalesce(sum(ra.available_count), 0) as available_capacity
                  from hotels h
                  join rooms r on r.hotel_id = h.id
                  left join room_availability ra
                    on ra.room_id = r.id
                   and ra.available_date >= ?
                   and ra.available_date < ?
                  group by h.id, h.name, r.id, r.room_type
                )
                select
                  rc.hotel_id,
                  ha.user_id as admin_user_id,
                  rc.hotel_name,
                  rc.room_type,
                  rc.total_capacity,
                  rc.available_capacity,
                  case
                    when rc.total_capacity = 0 then 0
                    else rc.available_capacity::numeric / rc.total_capacity::numeric
                  end as available_ratio
                from room_capacity rc
                join hotel_admins ha on ha.hotel_id = rc.hotel_id
                where rc.total_capacity > 0
                  and (rc.available_capacity::numeric / rc.total_capacity::numeric) < 0.20
                order by rc.available_capacity asc, rc.hotel_name asc, rc.room_type asc
                """, (rs, rowNum) -> new LowCapacityHotel(
                rs.getObject("hotel_id", UUID.class),
                rs.getObject("admin_user_id", UUID.class),
                rs.getString("hotel_name"),
                rs.getString("room_type"),
                rs.getLong("total_capacity"),
                rs.getLong("available_capacity"),
                rs.getBigDecimal("available_ratio")
        ), startDate, endDateExclusive);
    }

    public List<NotificationResponse> findUserNotifications(UUID userId) {
        return jdbcTemplate.query("""
                select id, user_id, hotel_id, booking_id, message, type, status, created_at
                from notifications
                where user_id = ?
                order by created_at desc
                """, (rs, rowNum) -> new NotificationResponse(
                rs.getObject("id", UUID.class),
                rs.getObject("user_id", UUID.class),
                rs.getObject("hotel_id", UUID.class),
                rs.getObject("booking_id", UUID.class),
                rs.getString("message"),
                rs.getString("type"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toInstant()
        ), userId);
    }

    public List<NotificationResponse> findAdminNotifications(UUID adminId) {
        return jdbcTemplate.query("""
                select distinct n.id, n.user_id, n.hotel_id, n.booking_id, n.message, n.type, n.status, n.created_at
                from notifications n
                join hotel_admins ha on ha.hotel_id = n.hotel_id
                where ha.user_id = ?
                order by n.created_at desc
                """, (rs, rowNum) -> new NotificationResponse(
                rs.getObject("id", UUID.class),
                rs.getObject("user_id", UUID.class),
                rs.getObject("hotel_id", UUID.class),
                rs.getObject("booking_id", UUID.class),
                rs.getString("message"),
                rs.getString("type"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toInstant()
        ), adminId);
    }

    private NotificationResponse findNotificationById(UUID notificationId) {
        return jdbcTemplate.queryForObject("""
                select id, user_id, hotel_id, booking_id, message, type, status, created_at
                from notifications
                where id = ?
                """, (rs, rowNum) -> new NotificationResponse(
                rs.getObject("id", UUID.class),
                rs.getObject("user_id", UUID.class),
                rs.getObject("hotel_id", UUID.class),
                rs.getObject("booking_id", UUID.class),
                rs.getString("message"),
                rs.getString("type"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toInstant()
        ), notificationId);
    }
}
