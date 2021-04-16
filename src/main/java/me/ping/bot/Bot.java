package me.ping.bot;

import me.ping.bot.commands.*;
import me.ping.bot.core.DbHandler;
import me.ping.bot.core.Heartbeat;
import me.ping.bot.exceptions.InvalidDataTypeException;
import me.ping.bot.exceptions.ParameterCountMismatchException;
import me.ping.bot.listeners.Points;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class Bot {
    public static void main(String[] args) throws Exception {
        Bot bot = new Bot();
        JDA jda = bot.prepareJDA();
        bot.prepareDatabase();
        new Heartbeat(jda);
    }

    private JDA prepareJDA() {
        String token = "fake_token";
        JDA api;
        try {
            api = JDABuilder.createDefault(token)
                    .addEventListeners(
                            new Ping(),
                            new Mute(),
                            new Nuke(),
                            new RemindMe(),
                            new Pfp(),
                            new Pin(),
                            new Points(),
                            new Flip()
                    )
                    .build();
            return api;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    // this wont create anything if it exists already
    private void prepareDatabase() {
        DbHandler db = DbHandler.getInstance();

        try {
            db.create("CREATE TABLE IF NOT EXISTS reminders (id INTEGER PRIMARY KEY, server_id INTEGER, channel_id INTEGER, user_id INTEGER, reminder TEXT, reminder_time INTEGER)");
            db.create("CREATE TABLE IF NOT EXISTS points (id INTEGER PRIMARY KEY, server_id INTEGER, user_id INTEGER UNIQUE, points INTEGER)");
        } catch (InvalidDataTypeException | ParameterCountMismatchException e) {
            e.printStackTrace();
        }
        db.close();
    }
}
