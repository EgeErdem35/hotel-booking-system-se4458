package com.se4458.hotelbooking.aiagentservice.ai;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class IntentParser {

    private static final Pattern ISO_DATE = Pattern.compile("\\b\\d{4}-\\d{2}-\\d{2}\\b");
    private static final Pattern GUESTS = Pattern.compile("\\b(\\d+)\\s*(guest|guests|person|people|adult|adults)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern UUID_PATTERN = Pattern.compile("\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\b");
    private static final List<String> KNOWN_DESTINATIONS = List.of("Istanbul", "Antalya", "Izmir");

    public ParsedIntent parse(String message) {
        String normalized = message.toLowerCase(Locale.ROOT);
        List<LocalDate> dates = parseDates(message);
        List<UUID> uuids = parseUuids(message);

        String action = normalized.contains("book") || normalized.contains("reserve")
                ? "booking"
                : "search";

        return new ParsedIntent(
                action,
                parseDestination(message),
                dates.size() > 0 ? dates.get(0) : null,
                dates.size() > 1 ? dates.get(1) : null,
                parseGuests(message),
                uuids.size() > 0 ? uuids.get(0) : null,
                uuids.size() > 1 ? uuids.get(1) : null
        );
    }

    private String parseDestination(String message) {
        String normalized = message.toLowerCase(Locale.ROOT);
        for (String destination : KNOWN_DESTINATIONS) {
            if (normalized.contains(destination.toLowerCase(Locale.ROOT))) {
                return destination;
            }
        }
        return null;
    }

    private List<LocalDate> parseDates(String message) {
        Matcher matcher = ISO_DATE.matcher(message);
        List<LocalDate> dates = new ArrayList<>();
        while (matcher.find()) {
            dates.add(LocalDate.parse(matcher.group()));
        }
        return dates;
    }

    private Integer parseGuests(String message) {
        Matcher matcher = GUESTS.matcher(message);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    private List<UUID> parseUuids(String message) {
        Matcher matcher = UUID_PATTERN.matcher(message);
        List<UUID> uuids = new ArrayList<>();
        while (matcher.find()) {
            uuids.add(UUID.fromString(matcher.group()));
        }
        return uuids;
    }
}
