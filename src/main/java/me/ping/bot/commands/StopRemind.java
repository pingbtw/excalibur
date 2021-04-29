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

public class StopRemind extends ListenerAdapter {
    private Settings settings;

    public StopRemind() {
        this.settings = Settings.getInstance();
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        if (StringUtils.hasCommand(settings.getCmdPrefix() + "stopremind", event.getMessage().getContentRaw(), true)) {
            long uid = event.getAuthor().getIdLong();
            DbHandler db = DbHandler.getInstance();

            String[] parts = event.getMessage().getContentRaw().trim().split(" ");

            if (parts.length == 1) {

                String sql = "DELETE FROM reminders WHERE user_id=?";
                try {
                    QueryResult rs = db.query(sql, DbHandler.QueryType.DELETE, uid);
                    rs.close();
                    db.close();
                    if(rs.getAffectedRows() > 0) {
                        event.getChannel().sendMessage("Your reminders have been removed.").queue();
                    } else {
                        event.getChannel().sendMessage("You have no reminders!").queue();
                    }
                } catch (InvalidDataTypeException | DuplicateKeyException | ParameterCountMismatchException e) {
                    event.getChannel().sendMessage("You have no reminders!").queue();
                }
            } else if (parts.length == 2) {
                String sql = "DELETE FROM reminders WHERE user_id=? AND id=?";
                try {
                    long recordId = Long.parseLong(parts[1]);
                    QueryResult rs = db.query(sql, DbHandler.QueryType.DELETE, uid, recordId);
                    rs.close();
                    db.close();
                    if(rs.getAffectedRows() > 0) {
                        event.getChannel().sendMessage("Your reminder has been removed.").queue();
                    } else {
                        event.getChannel().sendMessage("No record with ID **" + recordId + "** found.").queue();
                    }
                } catch (InvalidDataTypeException | DuplicateKeyException | ParameterCountMismatchException e) {
                    event.getChannel().sendMessage("You have no reminders!").queue();
                } catch (NumberFormatException e) {
                    event.getChannel().sendMessage("Invalid record ID provided - number expected").queue();
                }
            } else {
                event.getChannel().sendMessage(usage()).queue();
            }
        }
    }

    private String usage() {
        return "Usage:```" + settings.getCmdPrefix() +
                "stopremind```OR```" + settings.getCmdPrefix() +
                "stopremind reminder_id```";
    }
}
