package com.se4458.hotelbooking.bookingservice.booking;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BookingEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String reservationCreatedQueue;

    public BookingEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.messaging.reservation-created-queue}") String reservationCreatedQueue
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.reservationCreatedQueue = reservationCreatedQueue;
    }

    public void publishReservationCreated(ReservationCreatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(reservationCreatedQueue, event);
        } catch (AmqpException exception) {
            // The booking is already committed at this point. Notification delivery can be retried manually/demo-side.
        }
    }
}
