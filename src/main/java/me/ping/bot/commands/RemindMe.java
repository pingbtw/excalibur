package me.ping.bot.commands;

import me.ping.bot.core.StringUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.*;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RemindMe extends ListenerAdapter {
    private final long DAYS_LIMIT = 30L;
    protected String[] units = {"s", "m", "h", "d"};
    private Connection connection;

    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        if (StringUtils.startsWithIgnoreCase(event.getMessage().getContentRaw(), "-remindme")) {
            handleRemindMeCmd(event);
        }
    }

    private void handleRemindMeCmd(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        Long uid = event.getAuthor().getIdLong();
        long channelId = event.getChannel().getIdLong();
        long serverId = event.getGuild().getIdLong();
        String time;

        String reminder = msg.getContentRaw().replace("-remindme ", "");
        try {
            time = reminder.substring(0, reminder.indexOf(" "));
        } catch (StringIndexOutOfBoundsException e) {
            event.getChannel().sendMessage("Empty reminder body").queue();
            return;
        }
        reminder = reminder.replace(time, "").trim();
        String unitStr = getTimeUnit(time);
        String durationStr = null;
        long duration = 0;

        if (unitStr != null) {
            durationStr = time.replace(unitStr, "");
        } else {
            returnError(event.getChannel(), "Invalid time unit provided.");
            return;
        }

        try {
            duration = Long.parseLong(durationStr);
        } catch (NumberFormatException e) {
            returnError(event.getChannel(), "Invalid duration provided.");
            return;
        }

        if (validateTimeLimitations(duration, strToTimeUnit(unitStr))) {
            setReminder(serverId, channelId, uid, reminder, duration, strToTimeUnit(unitStr));
            event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", your reminder is set").queue();
        } else {
            event.getChannel().sendMessage(String.format("Reminder duration cannot exceed %d days", DAYS_LIMIT)).queue();
        }
    }

    private void returnError(MessageChannel channel, String message) {
        channel.sendMessage(message + "\nUsage: -remindme <5s|m|h|d> <message to be reminded of>").queue();
    }

    private String getTimeUnit(String time) {
        String unit = Character.toString(time.charAt(time.length() - 1));
        boolean contains = Arrays.stream(units).anyMatch(unit::equalsIgnoreCase);

        if (contains) {
            return unit;
        }
        return null;
    }

    private TimeUnit strToTimeUnit(String unit) {
        return (
                unit.equalsIgnoreCase("s") ? TimeUnit.SECONDS :
                        unit.equalsIgnoreCase("m") ? TimeUnit.MINUTES :
                                unit.equalsIgnoreCase("h") ? TimeUnit.HOURS :
                                        unit.equalsIgnoreCase("d") ? TimeUnit.DAYS : null);
    }

    private boolean validateTimeLimitations(Long duration, TimeUnit unit) {
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

    private void setReminder(
            long serverId,
            long channelId,
            long userId,
            String reminder,
            long duration,
            TimeUnit unit) {

        long durationMs = userDurationToMs(duration, unit);
        long currentTimeMs = new Date().getTime();
        long reminderTime = currentTimeMs + durationMs;

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:excalibur.db");

            String sql = "INSERT INTO reminders (server_id, channel_id, user_id, reminder, reminder_time) VALUES(?,?,?,?,?)";

            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setLong(1, serverId);
            stmt.setLong(2, channelId);
            stmt.setLong(3, userId);
            stmt.setString(4, reminder);
            stmt.setLong(5, reminderTime);
            stmt.execute();

        } catch (
                SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }

    private long userDurationToMs(long duration, TimeUnit unit) {
        long durationMs = 0;
        switch (unit) {
            case SECONDS:
                durationMs = duration * 1000;
                break;
            case MINUTES:
                durationMs = duration * 60 * 1000;
                break;
            case HOURS:
                durationMs = duration * 60 * 60 * 1000;
                break;
            case DAYS:
                durationMs = duration * 60 * 60 * 24 * 1000;
                break;
        }
        return durationMs;
    }
}
