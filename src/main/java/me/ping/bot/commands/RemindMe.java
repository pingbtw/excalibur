package me.ping.bot.commands;

import me.ping.bot.core.UserCommandTime;
import me.ping.bot.exceptions.InvalidTimeDurationException;
import me.ping.bot.exceptions.InvalidTimeUnitException;
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
    private UserCommandTime userCommandTime;

    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        if (event.getMessage().getContentRaw().toLowerCase().startsWith("-remindme")) {
            handleRemindMeCmd(event);
        }
    }

    private void handleRemindMeCmd(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        Long uid = event.getAuthor().getIdLong();
        long channelId = event.getChannel().getIdLong();
        long serverId = event.getGuild().getIdLong();
        String reminder = "";

        try {
            reminder = msg.getContentRaw().substring("-remindme ".length());
            userCommandTime = new UserCommandTime(reminder);
        } catch (
                InvalidTimeDurationException |
                        InvalidTimeUnitException e) {
            returnError(event.getChannel(), e.getMessage());
            return;
        } catch (StringIndexOutOfBoundsException e) {
            returnError(event.getChannel(), "");
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        reminder = reminder.replace(userCommandTime.getTimeStr(), "").trim();

        if (userCommandTime.validateTimeLimitations()) {
            setReminder(serverId, channelId, uid, reminder, userCommandTime.getDuration(), userCommandTime.getUnit());
            event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", your reminder is set").queue();
        } else {
            event.getChannel().sendMessage(String.format("Reminder duration cannot exceed %d days", UserCommandTime.DAYS_LIMIT)).queue();
        }
    }

    private void returnError(MessageChannel channel, String message) {
        String format = String.join("|", UserCommandTime.UNITS);
        channel.sendMessage(message + "\nUsage: -remindme <5" + format + "> <message to be reminded of>").queue();
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
