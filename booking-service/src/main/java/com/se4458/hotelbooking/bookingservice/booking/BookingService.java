package com.se4458.hotelbooking.bookingservice.booking;

import com.se4458.hotelbooking.bookingservice.booking.BookingRepository.RoomBookingData;
import com.se4458.hotelbooking.bookingservice.common.ConflictException;
import com.se4458.hotelbooking.bookingservice.common.NotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingEventPublisher bookingEventPublisher;

    public BookingService(BookingRepository bookingRepository, BookingEventPublisher bookingEventPublisher) {
        this.bookingRepository = bookingRepository;
        this.bookingEventPublisher = bookingEventPublisher;
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        long nights = validateRequest(request);
        RoomBookingData room = bookingRepository.findRoomBookingData(request.roomId())
                .orElseThrow(() -> new NotFoundException("Room was not found."));

        if (request.guestCount() > room.capacity()) {
            throw new ConflictException("Guest count exceeds room capacity.");
        }

        if (!bookingRepository.hasAvailabilityForEveryNight(
                request.roomId(),
                request.checkIn(),
                request.checkOut(),
                nights
        )) {
            throw new ConflictException("Room is not available for the selected dates.");
        }

        int updatedDays = bookingRepository.decreaseAvailability(request.roomId(), request.checkIn(), request.checkOut());
        if (updatedDays != nights) {
            throw new ConflictException("Room availability changed before booking could be completed.");
        }

        BigDecimal totalPrice = room.pricePerNight()
                .multiply(BigDecimal.valueOf(nights))
                .setScale(2, RoundingMode.HALF_UP);

        BookingResponse booking = bookingRepository.createBooking(new CreateBookingCommand(
                room.hotelId(),
                room.roomId(),
                request.userId(),
                request.checkIn(),
                request.checkOut(),
                request.guestCount(),
                totalPrice
        ));

        ReservationCreatedEvent event = new ReservationCreatedEvent(
                booking.id(),
                booking.hotelId(),
                booking.roomId(),
                booking.userId(),
                booking.checkIn(),
                booking.checkOut(),
                booking.guestCount(),
                booking.totalPrice(),
                Instant.now()
        );
        publishAfterCommit(event);

        return booking;
    }

    public BookingResponse getBooking(UUID bookingId) {
        return bookingRepository.findBookingById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking was not found."));
    }

    public List<BookingResponse> getUserBookings(UUID userId) {
        return bookingRepository.findBookingsByUserId(userId);
    }

    private long validateRequest(CreateBookingRequest request) {
        if (request.checkOut().isBefore(request.checkIn()) || request.checkOut().isEqual(request.checkIn())) {
            throw new IllegalArgumentException("checkOut must be after checkIn.");
        }
        return ChronoUnit.DAYS.between(request.checkIn(), request.checkOut());
    }

    private void publishAfterCommit(ReservationCreatedEvent event) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            bookingEventPublisher.publishReservationCreated(event);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                bookingEventPublisher.publishReservationCreated(event);
            }
        });
    }
}
