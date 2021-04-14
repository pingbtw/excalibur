package me.ping.bot.core;

import net.dv8tion.jda.api.JDA;

import java.sql.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Date;

public class Heartbeat implements Runnable {
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);
    private JDA jda;
    private Connection connection;


    public Heartbeat(JDA jda) {
        this.jda = jda;
        start(this);
    }

    private void start(Runnable r) {
        Runnable beeper = r;
        ScheduledFuture<?> beeperHandle =
                scheduler.scheduleAtFixedRate(beeper, 0, 10, SECONDS);
    }

    public void run() {
        checkDatabaseForReminder();
    }

    private void checkDatabaseForReminder() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:excalibur.db");

            String sql = "SELECT * FROM reminders WHERE reminder_time<?";
            long now = new Date().getTime();
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setLong(1, now);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                long uid = rs.getLong("user_id");
                long serverId = rs.getLong("server_id");
                long channelId = rs.getLong("channel_id");
                String reminder = rs.getString("reminder");
                long reminderTime = rs.getLong("reminder_time");
                int recordId = rs.getInt("id");
                //server_id, channel_id, user_id, reminder, reminder_time

                stmt = connection.prepareStatement("DELETE FROM reminders WHERE id=?");
                stmt.setInt(1, recordId);
                stmt.execute();
                if (stmt.getUpdateCount() > 0) {

                }

                dispatchReminder(serverId, channelId, uid, reminder);

            }
        } catch (SQLException e) {
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

    private void dispatchReminder(long serverId, long channelId, long userId, String reminder) {
        try {
            jda.getGuildById(serverId)
                    .getTextChannelById(channelId)
                    .sendMessage("Reminder: " + jda
                            .retrieveUserById(userId).complete().getAsMention() +
                            " - " +
                            reminder).queue();
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("---EXCEPTION---");
            System.out.println("Something went wrong while trying to dispatch this reminder");
            /*
            System.out.println("server id: " + serverId);
            System.out.println("chan   id: " + channelId);
            System.out.println("user   id: " + userId);
            System.out.println("reminder : " + reminder);
            */
            System.out.println("---END EXCEPTION---");

        }
    }
}
