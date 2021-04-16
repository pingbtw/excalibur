package me.ping.bot.core;

import me.ping.bot.exceptions.InvalidDataTypeException;
import me.ping.bot.exceptions.ParameterCountMismatchException;
import net.dv8tion.jda.api.JDA;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MINUTES;

public class Heartbeat implements Runnable {
    private final int HEARTBEAT_INTERVAL = 1;
    private final TimeUnit HEARTBEAT_UNIT = MINUTES;

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);
    private JDA jda;

    public Heartbeat(JDA jda) {
        this.jda = jda;
        start(this);
    }

    private void start(Runnable r) {
        Runnable beat = r;
        ScheduledFuture<?> beeperHandle =
                scheduler.scheduleAtFixedRate(beat, 0, HEARTBEAT_INTERVAL, HEARTBEAT_UNIT);
    }

    public void run() {
        checkDatabaseForReminder();
    }

    private void checkDatabaseForReminder() {
        try {
            DbHandler db = DbHandler.getInstance();
            String sql = "SELECT * FROM reminders WHERE reminder_time<?";
            long now = new Date().getTime();
            try {
                QueryResult qr = db.select(sql, now);
                if(qr.hasResultSet()) {
                    ResultSet rs = qr.getRs();
                    while(rs.next()) {
                        long uid = rs.getLong("user_id");
                        long serverId = rs.getLong("server_id");
                        long channelId = rs.getLong("channel_id");
                        String reminder = rs.getString("reminder");
                        long reminderTime = rs.getLong("reminder_time");
                        int recordId = rs.getInt("id");

                        db.delete("DELETE FROM reminders WHERE id=?", recordId);

                        dispatchReminder(serverId, channelId, uid, reminder);
                    }
                    qr.close();
                    db.close();
                }
            } catch (InvalidDataTypeException | ParameterCountMismatchException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
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
            System.out.println("---EXCEPTION---");
            System.out.println("Something went wrong while trying to dispatch this reminder");
            System.out.println(e.getMessage());
            System.out.println("---END EXCEPTION---");

        }
    }
}
