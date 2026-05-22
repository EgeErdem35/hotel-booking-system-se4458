package com.se4458.hotelbooking.hotelsearchservice.hotel;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hotels")
public class HotelSearchController {

    private final HotelSearchService hotelSearchService;

    public HotelSearchController(HotelSearchService hotelSearchService) {
        this.hotelSearchService = hotelSearchService;
    }

    @GetMapping("/search")
    public PageResponse<HotelSearchResultResponse> searchHotels(
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam int guests,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        SearchCriteria criteria = new SearchCriteria(destination, checkIn, checkOut, guests, page, size);
        return hotelSearchService.search(criteria, authorization);
    }

    @GetMapping("/{hotelId}")
    public HotelDetailResponse getHotelDetail(
            @PathVariable UUID hotelId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) Integer guests,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        DetailCriteria criteria = new DetailCriteria(checkIn, checkOut, guests);
        return hotelSearchService.getHotelDetail(hotelId, criteria, authorization);
    }

    @GetMapping("/{hotelId}/map")
    public HotelMapResponse getHotelMap(@PathVariable UUID hotelId) {
        return hotelSearchService.getHotelMap(hotelId);
    }
}
