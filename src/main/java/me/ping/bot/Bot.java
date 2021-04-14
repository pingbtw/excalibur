package me.ping.bot;

import io.github.cdimascio.dotenv.Dotenv;
import me.ping.bot.commands.*;
import me.ping.bot.core.Heartbeat;
import me.ping.bot.listeners.Points;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import java.sql.*;

public class Bot {
    public static void main(String[] args) throws Exception {
        Bot bot = new Bot();
        JDA jda = bot.prepareJDA();
        bot.prepareDatabase();

        new Heartbeat(jda);
    }

    private JDA prepareJDA() {
        String token = "ODMxODA2MzY4ODM4MTg5MDg2.YHamDA.kVyiDDWHQScgQ2LXGDMp3Rid6sU";
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
                            new Points()
                    )
                    .build();
            return api;
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }
    // this wont create anything if it exists already
    private void prepareDatabase() {
        Connection connection = null;
        try
        {
            connection = DriverManager.getConnection("jdbc:sqlite:excalibur.db");
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS reminders (id INTEGER PRIMARY KEY, server_id INTEGER, channel_id INTEGER, user_id INTEGER, reminder TEXT, reminder_time INTEGER)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS points (id INTEGER PRIMARY KEY, server_id INTEGER, user_id INTEGER, points INTEGER)");
            connection.commit();
        }
        catch(SQLException e)
        {
            System.out.println(e.getMessage());
        }
        finally
        {
            try
            {
                if(connection != null)
                    connection.close();
            }
            catch(SQLException e)
            {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }
}
