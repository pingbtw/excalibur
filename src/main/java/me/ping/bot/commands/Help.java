package me.ping.bot.commands;

import me.ping.bot.core.Heartbeat;
import me.ping.bot.core.Settings;
import me.ping.bot.core.StringUtils;
import me.ping.bot.core.UserCommandTime;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class Help extends ListenerAdapter {
    // NON ADMIN COMMANDS
    private enum COMMAND {
        FLIP,
        PFP,
        PIN,
        PING,
        REMINDERS,
        REMINDME,
        STOPREMIND,
        NULL
    }
    private EmbedBuilder helpEmbed;
    private Settings settings;

    public Help() {
        this.settings = Settings.getInstance();
        this.helpEmbed = new EmbedBuilder();
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        if (StringUtils.hasCommand(settings.getCmdPrefix() + "help", event.getMessage().getContentRaw(), true)) {
            String[] parts = event.getMessage().getContentRaw().split(" ");
            // show default help
            if (parts.length == 1) {
                event.getChannel().sendMessage(overviewHelp().build()).queue();
            } // show specific help
            else if (parts.length == 2) {
                COMMAND cmd = COMMAND.NULL;
                parts[1] = parts[1].contains(settings.getCmdPrefix()) ? parts[1].replace(settings.getCmdPrefix(), "") : parts[1];
                switch (parts[1].toLowerCase()) {
                    case "flip":
                        cmd = COMMAND.FLIP;
                        break;
                    case "pfp":
                        cmd = COMMAND.PFP;
                        break;
                    case "pin":
                        cmd = COMMAND.PIN;
                        break;
                    case "ping":
                        cmd = COMMAND.PING;
                        break;
                    case "reminders":
                        cmd = COMMAND.REMINDERS;
                        break;
                    case "remindme":
                        cmd = COMMAND.REMINDME;
                        break;
                    case "stopremind":
                        cmd = COMMAND.STOPREMIND;
                        break;
                    default:
                        cmd = COMMAND.NULL;
                        break;
                }
                event.getChannel().sendMessage(specificHelp(cmd).build()).queue();
            }
        }
    }

    private EmbedBuilder overviewHelp() {
        prepareEmbed("Command Usage");
        StringBuilder cmds = new StringBuilder();
        cmds.append(settings.getCmdPrefix()).append("pfp").append(", ");
        cmds.append(settings.getCmdPrefix()).append("pin").append(", ");
        cmds.append(settings.getCmdPrefix()).append("flip").append(", ");
        cmds.append(settings.getCmdPrefix()).append("ping").append(", ");
        cmds.append(settings.getCmdPrefix()).append("remindme").append(", ");
        cmds.append(settings.getCmdPrefix()).append("reminders").append(", ");
        cmds.append(settings.getCmdPrefix()).append("stopremind");
        helpEmbed.addField("Available Commands:", cmds.toString(), false);
        helpEmbed.addField("", "`"+settings.getCmdPrefix() + "help command_name` for help with a specific command", false);
        return helpEmbed;
    }

    private EmbedBuilder specificHelp(COMMAND cmd) {
        switch (cmd) {
            case PFP:
                prepareEmbed("Command Usage for: `" + settings.getCmdPrefix() + "pfp`");
                helpEmbed.addField("**Description:**", "Display a user profile picture", false);
                helpEmbed.addField("Usage:", "`" + settings.getCmdPrefix() + "pfp @user`", false);
                break;
            case PIN:
                prepareEmbed("Command Usage for: `" + settings.getCmdPrefix() + "pin`");
                helpEmbed.addField("**Description:**", "Pin a message to the current channel if there is space available", false);
                helpEmbed.addField("Usage:", "`" + settings.getCmdPrefix() + "pin message_id`", false);
                break;
            case FLIP:
                prepareEmbed("Command Usage for: `" + settings.getCmdPrefix() + "flip`");
                helpEmbed.addField("**Description:**", "Select a random option from the choices provided", false);
                helpEmbed.addField("Usage:", "`" + settings.getCmdPrefix() + "flip option 1 | option 2 | options 3 | option x`", false);
                break;
            case PING:
                prepareEmbed("Command Usage for: `" + settings.getCmdPrefix() + "ping`");
                helpEmbed.addField("**Description:**", "Display bot connection latency", false);
                helpEmbed.addField("Usage:", "`" + settings.getCmdPrefix() + "ping`", false);
                break;
            case REMINDME:
                prepareEmbed("Command Usage for: `" + settings.getCmdPrefix() + "remindme`");
                helpEmbed.addField("**Description:**", "Add a reminder to mention you at a later time", false);
                helpEmbed.addField("Usage:", "`" + settings.getCmdPrefix() + "remindme duration message`", false);
                helpEmbed.addField("Example:", "`" + settings.getCmdPrefix() + "remindme 1h remind me of something in 1 hour`", false);
                helpEmbed.addBlankField(false);
                String availableUnits = String.join(" | ", UserCommandTime.UNITS);;
                helpEmbed.addField("Available Units:", availableUnits, false);
                break;
            case REMINDERS:
                prepareEmbed("Command Usage for: `" + settings.getCmdPrefix() + "reminders`");
                helpEmbed.addField("**Description:**", "Show all current reminders for user or specific reminder for user", false);
                helpEmbed.addField("Usage:", "`" + settings.getCmdPrefix() + "reminders`", false);
                helpEmbed.addField("Usage:", "`" + settings.getCmdPrefix() + "reminders reminder_id`", false);
                break;
            case STOPREMIND:
                prepareEmbed("Command Usage for: `" + settings.getCmdPrefix() + "stopremind`");
                helpEmbed.addField("**Description:**", "Remove all current reminders for user or specific reminder for user", false);
                helpEmbed.addField("Usage:", "`" + settings.getCmdPrefix() + "stopremind`", false);
                helpEmbed.addField("Usage:", "`" + settings.getCmdPrefix() + "stopremind reminder_id`", false);
                break;
            default:
                return overviewHelp();
        }
        return helpEmbed;
    }

    private void prepareEmbed(String desc) {
        Color embedColor = Color.green;
        try {
            embedColor = Color.decode(settings.getEmbedColor());
        } catch (NumberFormatException ex) {
            // begone, pest!
        }
        String prefix = settings.getCmdPrefix();
        helpEmbed.clear();
        helpEmbed.setColor(embedColor);
        helpEmbed.setTitle("Excalibur Help");
        helpEmbed.setDescription(desc);
    }
}
