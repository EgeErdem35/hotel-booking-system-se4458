package com.se4458.hotelbooking.notificationservice.notification;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NightlyCapacityJob {

    private final NotificationService notificationService;

    public NightlyCapacityJob(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void runNightlyCapacityCheck() {
        notificationService.runNightlyCapacityCheck();
    }
}
