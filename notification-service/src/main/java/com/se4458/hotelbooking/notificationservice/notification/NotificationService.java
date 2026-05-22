package com.se4458.hotelbooking.notificationservice.notification;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public NotificationResponse handleReservationCreated(ReservationCreatedEvent event) {
        String message = "Reservation confirmed. Booking "
                + event.bookingId()
                + " is for "
                + event.guestCount()
                + " guest(s) from "
                + event.checkIn()
                + " to "
                + event.checkOut()
                + ". Total price: "
                + event.totalPrice()
                + ".";
        return notificationRepository.createReservationNotification(event, message);
    }

    public List<NotificationResponse> getUserNotifications(UUID userId) {
        return notificationRepository.findUserNotifications(userId);
    }

    public List<NotificationResponse> getAdminNotifications(UUID adminId) {
        return notificationRepository.findAdminNotifications(adminId);
    }

    public NightlyJobResponse runNightlyCapacityCheck() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(30);
        List<LowCapacityHotel> lowCapacityHotels = notificationRepository.findLowCapacityHotels(startDate, endDate);
        int notificationsCreated = 0;
        for (LowCapacityHotel lowCapacityHotel : lowCapacityHotels) {
            notificationsCreated += notificationRepository.createLowCapacityNotification(
                    lowCapacityHotel,
                    lowCapacityMessage(lowCapacityHotel, startDate, endDate)
            );
        }
        return new NightlyJobResponse(startDate, endDate, lowCapacityHotels.size(), notificationsCreated);
    }

    private String lowCapacityMessage(LowCapacityHotel lowCapacityHotel, LocalDate startDate, LocalDate endDate) {
        String percent = lowCapacityHotel.availableRatio()
                .multiply(java.math.BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP)
                .toPlainString();
        return "Low capacity warning for "
                + lowCapacityHotel.hotelName()
                + " / "
                + lowCapacityHotel.roomType()
                + " between "
                + startDate
                + " and "
                + endDate
                + ". Available capacity is "
                + lowCapacityHotel.availableCapacity()
                + " of "
                + lowCapacityHotel.totalCapacity()
                + " ("
                + percent
                + "%).";
    }
}
