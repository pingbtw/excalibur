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

public class Macro extends ListenerAdapter {
    private DbHandler db;
    private Settings settings;

    public Macro(){
        this.db = DbHandler.getInstance();
        this.settings = Settings.getInstance();
    }
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().isBot())
            return;

        String msg = event.getMessage().getContentRaw();
        if(msg.startsWith(settings.getCmdPrefix())) {
            msg = msg.replaceFirst(settings.getCmdPrefix(),"");

            try {
                QueryResult qr = db.select("SELECT content FROM macros WHERE command=?", msg);

                if(qr.hasResultSet()) {
                    while (qr.getRs().next()) {
                        event.getChannel().sendMessage(qr.getRs().getString("content")).queue();
                    }
                }
                qr.close();
                db.close();
            } catch (InvalidDataTypeException | DuplicateKeyException | ParameterCountMismatchException | SQLException e) {
                e.printStackTrace();
            }
        }

    }
}
