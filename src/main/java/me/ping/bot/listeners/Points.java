package me.ping.bot.listeners;

import me.ping.bot.core.DbHandler;
import me.ping.bot.core.QueryResult;
import me.ping.bot.core.StringUtils;
import me.ping.bot.exceptions.InvalidDataTypeException;
import me.ping.bot.exceptions.ParameterCountMismatchException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.SQLException;
import java.util.List;

public class Points extends ListenerAdapter {
    public void onMessageReceived(MessageReceivedEvent event) {
        insertIntoPointsTable(event);
        if (StringUtils.startsWithIgnoreCase(event.getMessage().getContentRaw(), "-points")) {
            if (event.getMessage().getMentionedMembers().size() > 1) {
                event.getChannel().sendMessage("Too many users provided").queue();
            } else if (event.getMessage().getMentionedMembers().isEmpty()) {
                getUserPoints(event);
            } else {
                getUserPoints(event, event.getMessage().getMentionedMembers().get(0));
            }
        }
    }

    public void getUserPoints(MessageReceivedEvent event) {
        long points = 0;
        try {
            DbHandler db = DbHandler.getInstance();
            String sql = "SELECT * FROM points WHERE user_id=?";
            QueryResult qr = db.select(sql, event.getAuthor().getIdLong());
            if (qr.hasResultSet()) {
                while (qr.getRs().next()) {
                    points = qr.getRs().getInt("points");
                }
            }
            qr.close();
            db.close();
        } catch (InvalidDataTypeException | ParameterCountMismatchException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            // do nothing jon snow
            e.printStackTrace();
        }
        event.getChannel().sendMessage("You have " + points + " point(s)").queue();
    }

    public void getUserPoints(MessageReceivedEvent event, Member member) {
        long points = 0;
        try {
            DbHandler db = DbHandler.getInstance();
            String sql = "SELECT * FROM points WHERE user_id=?";
            QueryResult qr = db.select(sql, member.getIdLong());
            if (qr.hasResultSet()) {
                while (qr.getRs().next()) {
                    points = qr.getRs().getInt("points");
                }
            }
            qr.close();
            db.close();
        } catch (InvalidDataTypeException | ParameterCountMismatchException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            // do nothing jon snow
            e.printStackTrace();
        }
        event.getChannel().sendMessage(member.getAsMention() + " has " + points + " point(s)").queue();
    }

    public void insertIntoPointsTable(MessageReceivedEvent event) {
        long serverId = event.getGuild().getIdLong();
        long userId = event.getAuthor().getIdLong();
        int points = calculatePointsToAward(event);
        long oldPoints = 0;

        String sql =
                "INSERT INTO points (user_id, server_id, points) VALUES (?,?,?) " +
                        "ON CONFLICT(user_id) DO UPDATE SET points = points + ?";

        DbHandler db = DbHandler.getInstance();

        try {
            db.query(sql, DbHandler.QueryType.INSERT, userId, serverId, points, points);
        } catch (InvalidDataTypeException | ParameterCountMismatchException e) {
            e.printStackTrace();
        }
        db.close();
    }

    public int calculatePointsToAward(MessageReceivedEvent event) {
        int points = 1;
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

