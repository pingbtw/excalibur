package me.ping.bot.commands.elevated;

import me.ping.bot.core.DbHandler;
import me.ping.bot.core.QueryResult;
import me.ping.bot.core.Settings;
import me.ping.bot.exceptions.DuplicateKeyException;
import me.ping.bot.exceptions.InvalidDataTypeException;
import me.ping.bot.exceptions.ParameterCountMismatchException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;

public class MacroCmd {
    private Settings settings;
    private final String addUsage, removeUsage, editUsage;
    private final String[] protectedMacros = {
            "addrole",
            "removerole",
            "roles",
            "prefix",
            "embedcolor",
            "addmacro",
            "editmacro",
            "removemacro",
            "flip",
            "mute",
            "nuke",
            "pfp",
            "pin",
            "ping",
            "remindme"
    };

    public MacroCmd() {
        this.settings = Settings.getInstance();
        StringBuilder usageBuilder = new StringBuilder();
        usageBuilder.append("**Usage:** ")
                .append(settings.getCmdPrefix())
                .append("addmacro ")
                .append("*<macro_name>* ")
                .append("*<macro_content>*");
        this.addUsage = usageBuilder.toString();
        usageBuilder.setLength(0);
        usageBuilder.append("**Usage:** ")
                .append(settings.getCmdPrefix())
                .append("editmacro ")
                .append("*<macro_name>* ")
                .append("*<macro_content>*");
        this.editUsage = usageBuilder.toString();

        usageBuilder.setLength(0);
        usageBuilder.append("**Usage:** ")
                .append(settings.getCmdPrefix())
                .append("removemacro ")
                .append("*<macro_name>* ");
        this.removeUsage = usageBuilder.toString();

    }

    public void addMacro(MessageReceivedEvent event) {
        String msg = event.getMessage().getContentRaw();
        String macroName = null;
        String macroContent = null;
        int index = (settings.getCmdPrefix() + "addmacro ").length();
        try {
            macroContent = msg.substring(index);
            // get macro name from entire message, starting after
            // the command
            macroName = extractMacroName(macroContent);
        } catch (StringIndexOutOfBoundsException e) {
            event.getChannel().sendMessage(addUsage).queue();
            return;
        }
        if (macroName == null) {
            event.getChannel().sendMessage(addUsage).queue();
        } else {
            boolean contains = Arrays.stream(protectedMacros).anyMatch(macroName::equalsIgnoreCase);

            if(contains) {
                event.getChannel().sendMessage("Cannot create macro with keyword: **" + macroName +"**").queue();
                return;
            }

            macroContent = macroContent.replaceFirst(macroName, "").trim();
            DbHandler db = DbHandler.getInstance();

            String sql = "INSERT INTO macros (command, content, server_id, user_id) VALUES(?,?,?,?)";
            long serverId = event.getGuild().getIdLong();
            long userId = event.getAuthor().getIdLong();

            try {
                QueryResult qr = db.insert(sql, macroName, macroContent, serverId, userId);
                if (qr.getAffectedRows() > 0) {
                    event.getChannel().sendMessage("Macro **" + macroName + "** added!").queue();
                }
            } catch (InvalidDataTypeException | ParameterCountMismatchException e) {
                e.printStackTrace();
            } catch (DuplicateKeyException e) {
                event.getChannel().sendMessage("**" + macroName + "** already exists!").queue();
            }
        }
    }

    public void removeMacro(MessageReceivedEvent event) {
        String msg = event.getMessage().getContentRaw();
        String macroName = null;
        String[] parts = msg.split(" ");
        if (parts.length == 2) {
            DbHandler db = DbHandler.getInstance();
            String sql = "DELETE FROM macros WHERE command=?";
            try {
                QueryResult qr = db.delete(sql, parts[1]);
                if (qr.getAffectedRows() > 0)
                    event.getChannel().sendMessage("Macro **" + parts[1] + "** removed.").queue();
                else
                    event.getChannel().sendMessage("Macro **" + parts[1] + "** not found.").queue();
            } catch (InvalidDataTypeException | ParameterCountMismatchException | DuplicateKeyException e) {
                e.printStackTrace();
            }
        } else {
            event.getChannel().sendMessage(removeUsage).queue();
        }
    }

    public void editMacro(MessageReceivedEvent event) {
        String msg = event.getMessage().getContentRaw();
        String macroName = null;
        String macroContent = null;
        int index = (settings.getCmdPrefix() + "editmacro ").length();
        try {
            macroContent = msg.substring(index);
            // get macro name from entire message, starting after
            // the command
            macroName = extractMacroName(macroContent);
        } catch (StringIndexOutOfBoundsException e) {
            event.getChannel().sendMessage(addUsage).queue();
            return;
        }
        if (macroName == null) {
            event.getChannel().sendMessage(addUsage).queue();
        } else {
            macroContent = macroContent.replaceFirst(macroName, "").trim();
            DbHandler db = DbHandler.getInstance();

            String sql = "UPDATE macros SET command=?, content=?, server_id=?, user_id=? WHERE command=?";
            long serverId = event.getGuild().getIdLong();
            long userId = event.getAuthor().getIdLong();

            try {
                QueryResult qr = db.update(sql, macroName, macroContent, serverId, userId, macroName);
                if (qr.getAffectedRows() > 0) {
                    event.getChannel().sendMessage("Macro **" + macroName + "** edited!!").queue();
                } else {
                    event.getChannel().sendMessage("Macro **" + macroName + "** not found.").queue();
                }
            } catch (InvalidDataTypeException | DuplicateKeyException | ParameterCountMismatchException e) {
                e.printStackTrace();
            }
        }
    }

    private String extractMacroName(String msg) {
        String result = null;
        int firstSpace = msg.indexOf(' ');
        if (firstSpace == -1) {
            return null;
        } else {
            return msg.substring(0, firstSpace);
        }
    }
}
