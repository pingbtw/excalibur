package me.ping.bot.commands;

import me.ping.bot.core.DbHandler;
import me.ping.bot.core.QueryResult;
import me.ping.bot.core.Settings;
import me.ping.bot.core.StringUtils;
import me.ping.bot.exceptions.DuplicateKeyException;
import me.ping.bot.exceptions.InvalidDataTypeException;
import me.ping.bot.exceptions.ParameterCountMismatchException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.SQLException;

public class Reminders extends ListenerAdapter {
    private Settings settings;

    public Reminders() {
        this.settings = Settings.getInstance();
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        if (StringUtils.hasCommand(settings.getCmdPrefix() + "reminders", event.getMessage().getContentRaw(), true)) {
            long uid = event.getAuthor().getIdLong();
            DbHandler db = DbHandler.getInstance();

            String[] parts = event.getMessage().getContentRaw().trim().split(" ");

            if (parts.length == 1) {
                String sql = "SELECT * FROM reminders WHERE user_id=?";
                try {
                    QueryResult rs = db.select(sql, uid);
                    StringBuilder response = new StringBuilder();
                    boolean hasResult = false;
                    response.append("```");
                    while (rs.getRs().next()) {
                        hasResult = true;
                        String reminderId = Long.toString(rs.getRs().getLong("id"));
                        String reminderMessage = rs.getRs().getString("reminder");
                        reminderMessage = reminderMessage.length() > 20 ? reminderMessage.substring(0, 20) + "..." : reminderMessage;
                        response.append(reminderId)
                                .append(": ")
                                .append(reminderMessage)
                                .append("\n");
                    }
                    response.append("```");
                    rs.close();
                    db.close();
                    if (hasResult) {
                        event.getChannel().sendMessage("Your reminders: " + response).queue();
                    } else {
                        event.getChannel().sendMessage("You have no reminders!").queue();
                    }
                } catch (ParameterCountMismatchException | DuplicateKeyException | InvalidDataTypeException | SQLException e) {
                    // ignore
                    e.printStackTrace();
                }
            } else if (parts.length == 2) {
                String sql = "SELECT * FROM reminders WHERE user_id=? AND id=?";
                long recordId = Long.parseLong(parts[1]);
                try {
                    QueryResult rs = db.select(sql, uid, recordId);
                    StringBuilder response = new StringBuilder();
                    while (rs.getRs().next()) {
                        response.append("```")
                                .append(rs.getRs().getString("reminder"))
                                .append("```");
                    }
                    rs.close();
                    db.close();
                    if(response.length() == 0) {
                        event.getChannel().sendMessage("No record with ID **" + recordId + "** found.").queue();
                    } else {
                        event.getChannel().sendMessage(response).queue();
                    }
                } catch (InvalidDataTypeException | ParameterCountMismatchException | DuplicateKeyException | SQLException e) {
                    e.printStackTrace();
                } catch (NumberFormatException e) {
                    event.getChannel().sendMessage("Invalid record ID provided - number expected").queue();
                }

            } else {

            }

        }
    }
}
