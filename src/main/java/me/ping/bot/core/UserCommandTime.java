package me.ping.bot.core;

import me.ping.bot.exceptions.InvalidTimeDurationException;
import me.ping.bot.exceptions.InvalidTimeUnitException;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class UserCommandTime {
    public static final long DAYS_LIMIT = 30L;
    public static final String[] UNITS = {"m", "h", "d"};
    private String unitStr;
    private long duration;
    private TimeUnit unit;
    private String time;

    public UserCommandTime(String target) throws
            InvalidTimeUnitException,
            InvalidTimeDurationException,
            StringIndexOutOfBoundsException {
        extractTime(target);
    }

    private void extractTime(String target) throws
            InvalidTimeUnitException,
            InvalidTimeDurationException,
            StringIndexOutOfBoundsException {

        try {
            time = target.substring(0, target.indexOf(" "));
        } catch (StringIndexOutOfBoundsException e) {
            throw new StringIndexOutOfBoundsException("Empty reminder body");
        }

        target = target.replace(time, "").trim();
        String unitStr = extractTimeUnit(time);
        String durationStr = null;
        long duration = 0;

        if (unitStr != null) {
            durationStr = time.replace(unitStr, "");
        } else {
            throw new InvalidTimeUnitException("Invalid time unit provided.");
        }

        try {
            duration = Long.parseLong(durationStr);
        } catch (NumberFormatException e) {
            throw new InvalidTimeDurationException("Invalid duration provided.");
        }
        this.duration = duration;
        this.unitStr = unitStr;
        this.unit = strToTimeUnit(unitStr);
        if (this.unit == null)
            throw new InvalidTimeUnitException("Invalid time unit provided.");
    }

    private String extractTimeUnit(String time) {
        try {
            String unit = Character.toString(time.charAt(time.length() - 1));
            boolean contains = Arrays.stream(UNITS).anyMatch(unit::equalsIgnoreCase);

            if (contains) {
                return unit;
            }
            return null;
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }

    }

    private TimeUnit strToTimeUnit(String unit) {
        return (
                unit.equalsIgnoreCase("s") ? TimeUnit.SECONDS
                        : unit.equalsIgnoreCase("m") ? TimeUnit.MINUTES
                        : unit.equalsIgnoreCase("h") ? TimeUnit.HOURS
                        : unit.equalsIgnoreCase("d") ? TimeUnit.DAYS
                        : null);
    }

    public boolean validateTimeLimitations() {
        switch (unit) {
            case SECONDS:
                if (duration > (DAYS_LIMIT * 60L * 60L * 24L))
                    return false;
                break;
            case MINUTES:
                if (duration > (DAYS_LIMIT * 60L * 24L))
                    return false;
                break;
            case HOURS:
                if (duration > (DAYS_LIMIT * 24L))
                    return false;
                break;
            case DAYS:
                if (duration > DAYS_LIMIT)
                    return false;
                break;
        }
        return true;
    }

    public String getUnitStr() {
        return unitStr;
    }

    public long getDuration() {
        return duration;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public String getTimeStr() {
        return time;
    }
}
