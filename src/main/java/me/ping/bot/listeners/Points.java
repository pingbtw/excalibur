package me.ping.bot.listeners;

import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.*;

public class Points extends ListenerAdapter {
    public void onMessageReceived(MessageReceivedEvent event) {
        insertIntoPointsTable(event);
        if (event.getMessage().getContentRaw().equalsIgnoreCase("-points")) {
            getUserPoints(event);
        }
    }

    public long getUserPoints(MessageReceivedEvent event) {
        Connection connection;
        long points = 0;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:excalibur.db");

            String sql = "SELECT * FROM points WHERE user_id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setLong(1, event.getAuthor().getIdLong());
            ResultSet rs = stmt.executeQuery();
            points = rs.getLong("points");
            connection.close();

            event.getChannel().sendMessage("You have " + points + " points").queue();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        System.out.println(points);
        return points;
    }

    public void insertIntoPointsTable(MessageReceivedEvent event) {
        Connection connection;
        long serverId = event.getGuild().getIdLong();
        long userId = event.getAuthor().getIdLong();
        int points = calculatePointsToAward(event);
        long oldPoints = 0;

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:excalibur.db");

            try {

                String sql = "SELECT * FROM points WHERE user_id = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setLong(1, event.getAuthor().getIdLong());
                ResultSet rs = stmt.executeQuery();
                oldPoints = rs.getLong("points");

                String sql2 = "UPDATE points SET points = ? WHERE user_id = ?";
                stmt = connection.prepareStatement(sql2);
                stmt.setLong(1, points + oldPoints);
                stmt.setLong(2, userId);
                stmt.execute();
                connection.close();
            } catch (SQLException e) {
                System.out.println("Adding new user to points table");
                connection.close();

                connection = DriverManager.getConnection("jdbc:sqlite:excalibur.db");

                String sql = "INSERT INTO main.points (server_id, user_id, points) VALUES(?,?,?)";

                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setLong(1, serverId);
                stmt.setLong(2, userId);
                stmt.setLong(3, points);
                stmt.execute();
                stmt.close();
            }

        } catch (
                SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public int calculatePointsToAward(MessageReceivedEvent event) {
        int points = 0;
        TextChannel channel = event.getTextChannel();
        if (channel.getParent().getName().equalsIgnoreCase("serverstuff")) {
            points += 1;
        }
        if (channel.getParent().getName().equalsIgnoreCase("hot-topic")) {
            points += 2;
        }
        if (channel.getParent().getName().equalsIgnoreCase("muzak")) {
            points += 2;
        }
        return points;

    }
}

