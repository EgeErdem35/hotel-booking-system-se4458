package com.se4458.hotelbooking.hoteladminservice.hotel;

import com.se4458.hotelbooking.hoteladminservice.auth.AdminAuthentication;
import com.se4458.hotelbooking.hoteladminservice.auth.AdminAuthenticationResolver;
import jakarta.validation.Valid;
import java.util.concurrent.TimeUnit;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin")
public class HotelAdminController {

    private final HotelAdminService hotelAdminService;
    private final AdminAuthenticationResolver authenticationResolver;
    private final HotelImageStorageService hotelImageStorageService;

    public HotelAdminController(
            HotelAdminService hotelAdminService,
            AdminAuthenticationResolver authenticationResolver,
            HotelImageStorageService hotelImageStorageService
    ) {
        this.hotelAdminService = hotelAdminService;
        this.authenticationResolver = authenticationResolver;
        this.hotelImageStorageService = hotelImageStorageService;
    }

    @PostMapping("/hotels")
    @ResponseStatus(HttpStatus.CREATED)
    public HotelResponse createHotel(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @Valid @RequestBody CreateHotelRequest request
    ) {
        AdminAuthentication authentication = authenticationResolver.resolve(authorization, userId);
        return hotelAdminService.createHotel(authentication, request);
    }

    @PutMapping("/hotels/{hotelId}")
    public HotelResponse updateHotel(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @PathVariable UUID hotelId,
            @Valid @RequestBody UpdateHotelRequest request
    ) {
        AdminAuthentication authentication = authenticationResolver.resolve(authorization, userId);
        return hotelAdminService.updateHotel(authentication, hotelId, request);
    }

    @PostMapping("/hotels/{hotelId}/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse createRoom(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @PathVariable UUID hotelId,
            @Valid @RequestBody CreateRoomRequest request
    ) {
        AdminAuthentication authentication = authenticationResolver.resolve(authorization, userId);
        return hotelAdminService.createRoom(authentication, hotelId, request);
    }

    @PutMapping("/rooms/{roomId}")
    public RoomResponse updateRoom(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @PathVariable UUID roomId,
            @Valid @RequestBody UpdateRoomRequest request
    ) {
        AdminAuthentication authentication = authenticationResolver.resolve(authorization, userId);
        return hotelAdminService.updateRoom(authentication, roomId, request);
    }

    @PostMapping("/rooms/{roomId}/availability")
    public AvailabilityResponse upsertAvailability(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @PathVariable UUID roomId,
            @Valid @RequestBody AvailabilityRequest request
    ) {
        AdminAuthentication authentication = authenticationResolver.resolve(authorization, userId);
        return hotelAdminService.upsertAvailability(authentication, roomId, request);
    }

    @PostMapping(value = "/hotels/{hotelId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public HotelResponse uploadHotelImage(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @PathVariable UUID hotelId,
            @RequestParam("image") MultipartFile image
    ) {
        AdminAuthentication authentication = authenticationResolver.resolve(authorization, userId);
        return hotelAdminService.uploadHotelImage(authentication, hotelId, image);
    }

    @GetMapping("/hotel-images/{fileName:.+}")
    public ResponseEntity<Resource> getHotelImage(@PathVariable String fileName) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .contentType(hotelImageStorageService.mediaType(fileName))
                .body(hotelImageStorageService.load(fileName));
    }
}
