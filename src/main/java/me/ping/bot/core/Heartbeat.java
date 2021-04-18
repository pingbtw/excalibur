package me.ping.bot.core;

import me.ping.bot.classes.Reminder;
import me.ping.bot.exceptions.DuplicateKeyException;
import me.ping.bot.exceptions.InvalidDataTypeException;
import me.ping.bot.exceptions.ParameterCountMismatchException;
import net.dv8tion.jda.api.JDA;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Heartbeat implements Runnable {
    private final int HEARTBEAT_INTERVAL = 60;
    private final TimeUnit HEARTBEAT_UNIT = SECONDS;

    private ArrayList<Reminder> reminderQueue;

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);
    private JDA jda;

    public Heartbeat(JDA jda) {
        this.jda = jda;
        this.reminderQueue = new ArrayList<Reminder>();
        start(this);
    }

    private void start(Runnable r) {
        Runnable beat = r;
        ScheduledFuture<?> handle =
                scheduler.scheduleAtFixedRate(beat, 30, HEARTBEAT_INTERVAL, HEARTBEAT_UNIT);
    }

    public void run() {
        checkDatabaseForReminder();
    }

    private void checkDatabaseForReminder() {
        try {
            DbHandler db = DbHandler.getInstance();
            String sql = "SELECT * FROM reminders WHERE reminder_time<?";
            long now = System.currentTimeMillis();

            try {
                QueryResult qr = db.select(sql, now);
                if (qr.hasResultSet()) {
                    while (qr.getRs().next()) {
                        long uid = qr.getRs().getLong("user_id");
                        long serverId = qr.getRs().getLong("server_id");
                        long channelId = qr.getRs().getLong("channel_id");
                        String reminder = qr.getRs().getString("reminder");
                        long reminderTime = qr.getRs().getLong("reminder_time");
                        int recordId = qr.getRs().getInt("id");

                        reminderQueue.add(new Reminder(serverId, channelId, uid, reminder, recordId));
                    }
                    qr.close();
                    db.close();
                    dispatchReminders();
                }
            } catch (InvalidDataTypeException | DuplicateKeyException | ParameterCountMismatchException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void dispatchReminders() {
        Thread TDispatchReminders = new Thread() {
            public void run() {
                ArrayList<Integer> idsToDelete = new ArrayList<Integer>();

                for (Reminder reminder :
                        reminderQueue) {
                    try {
                        jda.getGuildById(reminder.getServerId())
                                .getTextChannelById(reminder.getChannelId())
                                .sendMessage("Reminder: " + jda
                                        .retrieveUserById(reminder.getUid()).complete().getAsMention() +
                                        " - " +
                                        reminder.getReminder()).queue();
                        idsToDelete.add(reminder.getRecordId());
                    } catch (Exception e) {
                        System.out.println("---EXCEPTION---");
                        System.out.println("Something went wrong while trying to dispatch this reminder");
                        e.printStackTrace();
                        System.out.println("---END EXCEPTION---");
                    }
                }
                reminderQueue.removeAll(reminderQueue);
                DbHandler db = DbHandler.getInstance();
                db.deleteMultiple("DELETE FROM reminders WHERE id=?", idsToDelete);

            }
        };
        TDispatchReminders.start();
    }
}
