package com.se4458.hotelbooking.notificationservice.notification;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReservationCreatedConsumer {

    private final NotificationService notificationService;

    public ReservationCreatedConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${app.messaging.reservation-created-queue}")
    public void consumeReservationCreated(ReservationCreatedEvent event) {
        notificationService.handleReservationCreated(event);
    }
}
