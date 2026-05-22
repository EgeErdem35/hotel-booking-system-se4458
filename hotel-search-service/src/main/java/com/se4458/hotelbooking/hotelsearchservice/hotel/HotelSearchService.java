package com.se4458.hotelbooking.hotelsearchservice.hotel;

import com.se4458.hotelbooking.hotelsearchservice.auth.SupabaseJwtVerifier;
import com.se4458.hotelbooking.hotelsearchservice.common.NotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class HotelSearchService {

    private static final BigDecimal LOGGED_IN_DISCOUNT_RATE = new BigDecimal("0.85");

    private final HotelSearchRepository hotelSearchRepository;
    private final SupabaseJwtVerifier jwtVerifier;
    private final HotelDetailCache hotelDetailCache;

    public HotelSearchService(
            HotelSearchRepository hotelSearchRepository,
            SupabaseJwtVerifier jwtVerifier,
            HotelDetailCache hotelDetailCache
    ) {
        this.hotelSearchRepository = hotelSearchRepository;
        this.jwtVerifier = jwtVerifier;
        this.hotelDetailCache = hotelDetailCache;
    }

    public PageResponse<HotelSearchResultResponse> search(SearchCriteria criteria, String authorizationHeader) {
        SearchCriteria validCriteria = validateSearchCriteria(criteria);
        long nights = nightsBetween(validCriteria.checkIn(), validCriteria.checkOut());
        boolean loggedIn = isLoggedIn(authorizationHeader);

        SearchQueryResult queryResult = hotelSearchRepository.search(validCriteria, nights);
        List<HotelSearchResultResponse> results = queryResult.hotels().stream()
                .map(result -> applyDiscount(result, loggedIn))
                .toList();

        return PageResponse.of(results, validCriteria.page(), validCriteria.size(), queryResult.totalElements());
    }

    public HotelDetailResponse getHotelDetail(UUID hotelId, DetailCriteria criteria, String authorizationHeader) {
        boolean hasAvailabilityFilter = criteria != null && criteria.hasAvailabilityFilter();
        HotelDetailResponse baseDetail = hasAvailabilityFilter
                ? loadHotelDetail(hotelId, validateDetailCriteria(criteria))
                : hotelDetailCache.get(hotelId).orElseGet(() -> loadAndCacheHotelDetail(hotelId));

        boolean loggedIn = isLoggedIn(authorizationHeader);
        if (!loggedIn) {
            return baseDetail;
        }

        return new HotelDetailResponse(
                baseDetail.id(),
                baseDetail.name(),
                baseDetail.description(),
                baseDetail.destination(),
                baseDetail.address(),
                baseDetail.latitude(),
                baseDetail.longitude(),
                baseDetail.starRating(),
                baseDetail.amenities(),
                baseDetail.imageUrl(),
                baseDetail.rooms().stream()
                        .map(room -> applyDiscount(room, true))
                        .toList()
        );
    }

    private HotelDetailResponse loadAndCacheHotelDetail(UUID hotelId) {
        HotelDetailResponse hotelDetail = loadHotelDetail(hotelId, null);
        hotelDetailCache.put(hotelId, hotelDetail);
        return hotelDetail;
    }

    private HotelDetailResponse loadHotelDetail(UUID hotelId, DetailCriteria criteria) {
        HotelDetailData hotel = hotelSearchRepository.findHotel(hotelId)
                .orElseThrow(() -> new NotFoundException("Hotel was not found."));

        List<RoomOptionResponse> rooms;
        if (criteria != null) {
            long nights = nightsBetween(criteria.checkIn(), criteria.checkOut());
            rooms = hotelSearchRepository.findAvailableRooms(hotelId, criteria, nights);
        } else {
            rooms = hotelSearchRepository.findAllRooms(hotelId);
        }

        return new HotelDetailResponse(
                hotel.id(),
                hotel.name(),
                hotel.description(),
                hotel.destination(),
                hotel.address(),
                hotel.latitude(),
                hotel.longitude(),
                hotel.starRating(),
                hotel.amenities(),
                hotel.imageUrl(),
                rooms
        );
    }

    public HotelMapResponse getHotelMap(UUID hotelId) {
        return hotelSearchRepository.findHotelMap(hotelId)
                .orElseThrow(() -> new NotFoundException("Hotel was not found."));
    }

    private SearchCriteria validateSearchCriteria(SearchCriteria criteria) {
        if (!StringUtils.hasText(criteria.destination())) {
            throw new IllegalArgumentException("destination is required.");
        }
        if (criteria.guests() < 1) {
            throw new IllegalArgumentException("guests must be greater than 0.");
        }
        if (criteria.page() < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0.");
        }
        if (criteria.size() < 1 || criteria.size() > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100.");
        }
        nightsBetween(criteria.checkIn(), criteria.checkOut());
        return criteria;
    }

    private DetailCriteria validateDetailCriteria(DetailCriteria criteria) {
        if (criteria.checkIn() == null || criteria.checkOut() == null || criteria.guests() == null) {
            throw new IllegalArgumentException("checkIn, checkOut, and guests must be provided together.");
        }
        if (criteria.guests() < 1) {
            throw new IllegalArgumentException("guests must be greater than 0.");
        }
        nightsBetween(criteria.checkIn(), criteria.checkOut());
        return criteria;
    }

    private long nightsBetween(java.time.LocalDate checkIn, java.time.LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("checkIn and checkOut are required.");
        }
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights < 1) {
            throw new IllegalArgumentException("checkOut must be after checkIn.");
        }
        return nights;
    }

    private boolean isLoggedIn(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            return false;
        }
        return jwtVerifier.verify(authorizationHeader.substring("Bearer ".length())).isPresent();
    }

    private HotelSearchResultResponse applyDiscount(HotelSearchResultResponse result, boolean loggedIn) {
        if (!loggedIn) {
            return result;
        }
        return new HotelSearchResultResponse(
                result.id(),
                result.name(),
                result.description(),
                result.destination(),
                result.address(),
                result.latitude(),
                result.longitude(),
                result.starRating(),
                result.amenities(),
                result.imageUrl(),
                discounted(result.lowestPricePerNight()),
                result.availableRoomTypes()
        );
    }

    private RoomOptionResponse applyDiscount(RoomOptionResponse room, boolean loggedIn) {
        if (!loggedIn) {
            return room;
        }
        return new RoomOptionResponse(
                room.id(),
                room.hotelId(),
                room.roomType(),
                room.capacity(),
                room.totalCount(),
                discounted(room.pricePerNight()),
                room.minAvailableCount()
        );
    }

    private BigDecimal discounted(BigDecimal price) {
        return price.multiply(LOGGED_IN_DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
    }
}
