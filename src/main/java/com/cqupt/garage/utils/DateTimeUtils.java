package com.cqupt.garage.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtils() {
    }

    public static String nowDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    public static LocalDateTime parseDateTime(String dateTime) {
        if (dateTime == null || dateTime.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTime, DATETIME_FORMATTER);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static boolean isValidDateTime(String dateTime) {
        return parseDateTime(dateTime) != null;
    }

    public static boolean isEndNotBeforeStart(String startDateTime, String endDateTime) {
        LocalDateTime start = parseDateTime(startDateTime);
        LocalDateTime end = parseDateTime(endDateTime);
        if (start == null || end == null) {
            return false;
        }
        return !end.isBefore(start);
    }

    public static long diffMinutes(String startDateTime, String endDateTime) {
        LocalDateTime start = parseDateTime(startDateTime);
        LocalDateTime end = parseDateTime(endDateTime);
        if (start == null || end == null) {
            return 0L;
        }
        long minutes = Duration.between(start, end).toMinutes();
        return Math.max(0, minutes);
    }

    public static String calcFeeByMinutes(long minutes) {
        if (minutes <= 0) {
            return "0";
        }
        long hours = (minutes + 59) / 60;
        return String.valueOf(hours * 5);
    }
}
