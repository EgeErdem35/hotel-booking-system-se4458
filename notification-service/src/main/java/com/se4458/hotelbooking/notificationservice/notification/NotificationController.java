package com.se4458.hotelbooking.notificationservice.notification;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/user/{userId}")
    public List<NotificationResponse> getUserNotifications(@PathVariable UUID userId) {
        return notificationService.getUserNotifications(userId);
    }

    @GetMapping("/admin/{adminId}")
    public List<NotificationResponse> getAdminNotifications(@PathVariable UUID adminId) {
        return notificationService.getAdminNotifications(adminId);
    }

    @PostMapping("/test-nightly-job")
    public NightlyJobResponse runNightlyJobForDemo() {
        return notificationService.runNightlyCapacityCheck();
    }
}
