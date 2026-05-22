package com.se4458.hotelbooking.hoteladminservice.hotel;

import com.se4458.hotelbooking.hoteladminservice.auth.AdminAuthentication;
import com.se4458.hotelbooking.hoteladminservice.common.ForbiddenException;
import com.se4458.hotelbooking.hoteladminservice.common.NotFoundException;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class HotelAdminService {

    private final HotelAdminRepository hotelAdminRepository;
    private final HotelImageStorageService hotelImageStorageService;

    public HotelAdminService(
            HotelAdminRepository hotelAdminRepository,
            HotelImageStorageService hotelImageStorageService
    ) {
        this.hotelAdminRepository = hotelAdminRepository;
        this.hotelImageStorageService = hotelImageStorageService;
    }

    @Transactional
    public HotelResponse createHotel(AdminAuthentication authentication, CreateHotelRequest request) {
        return hotelAdminRepository.createHotel(authentication.userId(), request);
    }

    @Transactional
    public HotelResponse updateHotel(AdminAuthentication authentication, UUID hotelId, UpdateHotelRequest request) {
        ensureHotelExists(hotelId);
        ensureAdminForHotel(authentication.userId(), hotelId);
        HotelResponse hotel = hotelAdminRepository.updateHotel(hotelId, request);
        if (hotel == null) {
            throw new NotFoundException("Hotel was not found.");
        }
        return hotel;
    }

    @Transactional
    public RoomResponse createRoom(AdminAuthentication authentication, UUID hotelId, CreateRoomRequest request) {
        ensureHotelExists(hotelId);
        ensureAdminForHotel(authentication.userId(), hotelId);
        return hotelAdminRepository.createRoom(hotelId, request);
    }

    @Transactional
    public RoomResponse updateRoom(AdminAuthentication authentication, UUID roomId, UpdateRoomRequest request) {
        ensureRoomExists(roomId);
        ensureAdminForRoom(authentication.userId(), roomId);
        RoomResponse room = hotelAdminRepository.updateRoom(roomId, request);
        if (room == null) {
            throw new NotFoundException("Room was not found.");
        }
        return room;
    }

    @Transactional
    public AvailabilityResponse upsertAvailability(
            AdminAuthentication authentication,
            UUID roomId,
            AvailabilityRequest request
    ) {
        ensureRoomExists(roomId);
        ensureAdminForRoom(authentication.userId(), roomId);
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("endDate must be greater than or equal to startDate.");
        }

        int affectedDays = hotelAdminRepository.upsertAvailability(
                roomId,
                request.startDate(),
                request.endDate(),
                request.availableCount()
        );
        long requestedDays = ChronoUnit.DAYS.between(request.startDate(), request.endDate()) + 1;
        return new AvailabilityResponse(roomId, request.startDate(), request.endDate(), request.availableCount(),
                Math.max(affectedDays, (int) requestedDays));
    }

    @Transactional
    public HotelResponse uploadHotelImage(AdminAuthentication authentication, UUID hotelId, MultipartFile image) {
        ensureHotelExists(hotelId);
        ensureAdminForHotel(authentication.userId(), hotelId);

        String imageUrl = hotelImageStorageService.store(hotelId, image);
        HotelResponse hotel = hotelAdminRepository.updateHotelImageUrl(hotelId, imageUrl);
        if (hotel == null) {
            throw new NotFoundException("Hotel was not found.");
        }
        return hotel;
    }

    private void ensureHotelExists(UUID hotelId) {
        if (!hotelAdminRepository.hotelExists(hotelId)) {
            throw new NotFoundException("Hotel was not found.");
        }
    }

    private void ensureRoomExists(UUID roomId) {
        if (!hotelAdminRepository.roomExists(roomId)) {
            throw new NotFoundException("Room was not found.");
        }
    }

    private void ensureAdminForHotel(UUID userId, UUID hotelId) {
        if (!hotelAdminRepository.isAdminForHotel(userId, hotelId)) {
            throw new ForbiddenException("User is not an admin for this hotel.");
        }
    }

    private void ensureAdminForRoom(UUID userId, UUID roomId) {
        if (!hotelAdminRepository.isAdminForRoom(userId, roomId)) {
            throw new ForbiddenException("User is not an admin for this room.");
        }
    }
}
