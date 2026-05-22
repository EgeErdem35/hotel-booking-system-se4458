package com.se4458.hotelbooking.hotelsearchservice.hotel;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class HotelSearchRepository {

    private final JdbcTemplate jdbcTemplate;

    public HotelSearchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SearchQueryResult search(SearchCriteria criteria, long nights) {
        int offset = criteria.page() * criteria.size();
        String destinationPattern = "%" + criteria.destination().trim().toLowerCase(Locale.ROOT) + "%";
        List<HotelSearchRow> rows = jdbcTemplate.query("""
                with available_rooms as (
                  select
                    r.id as room_id,
                    r.hotel_id,
                    r.price_per_night,
                    min(ra.available_count) as min_available_count
                  from rooms r
                  join room_availability ra on ra.room_id = r.id
                  where r.capacity >= ?
                    and ra.available_date >= ?
                    and ra.available_date < ?
                  group by r.id, r.hotel_id, r.price_per_night
                  having count(distinct ra.available_date) = ?
                     and min(ra.available_count) > 0
                ),
                hotel_matches as (
                  select
                    h.id,
                    h.name,
                    h.description,
                    h.destination,
                    h.address,
                    h.latitude,
                    h.longitude,
                    h.star_rating,
                    h.amenities,
                    to_jsonb(h)->>'image_url' as image_url,
                    min(ar.price_per_night) as lowest_price_per_night,
                    count(ar.room_id) as available_room_types
                  from hotels h
                  join available_rooms ar on ar.hotel_id = h.id
                  where lower(h.destination) like ?
                  group by h.id, h.name, h.description, h.destination, h.address,
                           h.latitude, h.longitude, h.star_rating, h.amenities, to_jsonb(h)->>'image_url'
                )
                select *, count(*) over() as total_elements
                from hotel_matches
                order by lowest_price_per_night asc, star_rating desc, name asc
                limit ? offset ?
                """, (rs, rowNum) -> new HotelSearchRow(
                mapSearchResult(rs),
                rs.getLong("total_elements")
        ), criteria.guests(), criteria.checkIn(), criteria.checkOut(), nights, destinationPattern, criteria.size(), offset);

        long totalElements = rows.stream().findFirst().map(HotelSearchRow::totalElements).orElse(0L);
        List<HotelSearchResultResponse> hotels = rows.stream()
                .map(HotelSearchRow::result)
                .toList();
        return new SearchQueryResult(hotels, totalElements);
    }

    public Optional<HotelDetailData> findHotel(UUID hotelId) {
        return jdbcTemplate.query("""
                select id, name, description, destination, address, latitude, longitude, star_rating, amenities,
                       to_jsonb(hotels)->>'image_url' as image_url
                from hotels
                where id = ?
                """, (rs, rowNum) -> new HotelDetailData(
                rs.getObject("id", UUID.class),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("destination"),
                rs.getString("address"),
                rs.getBigDecimal("latitude"),
                rs.getBigDecimal("longitude"),
                rs.getBigDecimal("star_rating"),
                readTextArray(rs, "amenities"),
                rs.getString("image_url")
        ), hotelId).stream().findFirst();
    }

    public List<RoomOptionResponse> findAllRooms(UUID hotelId) {
        return jdbcTemplate.query("""
                select id, hotel_id, room_type, capacity, total_count, price_per_night, null::integer as min_available_count
                from rooms
                where hotel_id = ?
                order by price_per_night asc, room_type asc
                """, (rs, rowNum) -> mapRoom(rs), hotelId);
    }

    public List<RoomOptionResponse> findAvailableRooms(UUID hotelId, DetailCriteria criteria, long nights) {
        return jdbcTemplate.query("""
                select
                  r.id,
                  r.hotel_id,
                  r.room_type,
                  r.capacity,
                  r.total_count,
                  r.price_per_night,
                  min(ra.available_count) as min_available_count
                from rooms r
                join room_availability ra on ra.room_id = r.id
                where r.hotel_id = ?
                  and r.capacity >= ?
                  and ra.available_date >= ?
                  and ra.available_date < ?
                group by r.id, r.hotel_id, r.room_type, r.capacity, r.total_count, r.price_per_night
                having count(distinct ra.available_date) = ?
                   and min(ra.available_count) > 0
                order by r.price_per_night asc, r.room_type asc
                """, (rs, rowNum) -> mapRoom(rs), hotelId, criteria.guests(), criteria.checkIn(), criteria.checkOut(), nights);
    }

    public Optional<HotelMapResponse> findHotelMap(UUID hotelId) {
        return jdbcTemplate.query("""
                select id, name, destination, latitude, longitude
                from hotels
                where id = ?
                """, (rs, rowNum) -> new HotelMapResponse(
                rs.getObject("id", UUID.class),
                rs.getString("name"),
                rs.getString("destination"),
                rs.getBigDecimal("latitude"),
                rs.getBigDecimal("longitude")
        ), hotelId).stream().findFirst();
    }

    private HotelSearchResultResponse mapSearchResult(ResultSet rs) throws SQLException {
        return new HotelSearchResultResponse(
                rs.getObject("id", UUID.class),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("destination"),
                rs.getString("address"),
                rs.getBigDecimal("latitude"),
                rs.getBigDecimal("longitude"),
                rs.getBigDecimal("star_rating"),
                readTextArray(rs, "amenities"),
                rs.getString("image_url"),
                rs.getBigDecimal("lowest_price_per_night"),
                rs.getLong("available_room_types")
        );
    }

    private RoomOptionResponse mapRoom(ResultSet rs) throws SQLException {
        Object minAvailableCount = rs.getObject("min_available_count");
        return new RoomOptionResponse(
                rs.getObject("id", UUID.class),
                rs.getObject("hotel_id", UUID.class),
                rs.getString("room_type"),
                rs.getInt("capacity"),
                rs.getInt("total_count"),
                rs.getBigDecimal("price_per_night"),
                minAvailableCount == null ? null : ((Number) minAvailableCount).intValue()
        );
    }

    private List<String> readTextArray(ResultSet rs, String columnName) throws SQLException {
        Array array = rs.getArray(columnName);
        if (array == null) {
            return List.of();
        }
        return List.of((String[]) array.getArray());
    }
}
