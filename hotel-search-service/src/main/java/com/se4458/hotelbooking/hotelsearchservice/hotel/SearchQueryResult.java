package com.se4458.hotelbooking.hotelsearchservice.hotel;

import java.util.List;

record SearchQueryResult(List<HotelSearchResultResponse> hotels, long totalElements) {
}
