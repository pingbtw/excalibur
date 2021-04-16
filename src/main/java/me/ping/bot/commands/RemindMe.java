package me.ping.bot.commands;

import me.ping.bot.core.DbHandler;
import me.ping.bot.core.QueryResult;
import me.ping.bot.core.StringUtils;
import me.ping.bot.core.UserCommandTime;
import me.ping.bot.exceptions.InvalidDataTypeException;
import me.ping.bot.exceptions.InvalidTimeDurationException;
import me.ping.bot.exceptions.InvalidTimeUnitException;
import me.ping.bot.exceptions.ParameterCountMismatchException;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class RemindMe extends ListenerAdapter {
    private final long DAYS_LIMIT = 30L;
    protected String[] units = {"s", "m", "h", "d"};
    private UserCommandTime userCommandTime;

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

        String sql =
                "INSERT INTO reminders (server_id, channel_id, user_id, "+
                        "reminder, reminder_time) VALUES(?,?,?,?,?)";
        //sid, cid, uid, r, rt

        DbHandler db = DbHandler.getInstance();

        try {
            QueryResult qr = db.insert(sql, serverId, channelId, userId, reminder, reminderTime);
        } catch (InvalidDataTypeException | ParameterCountMismatchException e) {
            e.printStackTrace();
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
