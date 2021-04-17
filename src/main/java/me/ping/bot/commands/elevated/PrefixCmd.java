package me.ping.bot.commands.elevated;

import me.ping.bot.core.Settings;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PrefixCmd {
    private Settings settings;

    public PrefixCmd() {
        this.settings = Settings.getInstance();
    }

    public void changePrefix(MessageReceivedEvent event) {
        String msg = event.getMessage().getContentRaw();
        String[] parts = msg.split(" ");
        if(parts.length == 1) {
            event.getChannel().sendMessage("The current prefix is: **" + settings.getCmdPrefix() + "**").queue();
            return;
        } else if(parts.length == 2) {
            settings.setCmdPrefix(parts[1]);
            event.getChannel().sendMessage("Prefix set to: **" + parts[1] + "**").queue();
        } else {
            event.getChannel().sendMessage("**Usage:** " + settings.getCmdPrefix() + "prefix *new_prefix*").queue();
        }
    }
}
