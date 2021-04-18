package me.ping.bot.commands.elevated;

import me.ping.bot.core.Settings;
import me.ping.bot.core.StringUtils;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class EmbedColorCmd {
    private Settings settings;

    public EmbedColorCmd() {
        this.settings = Settings.getInstance();
    }

    public void chaneEmbedColor(MessageReceivedEvent event) {
        String msg = event.getMessage().getContentRaw();
        String[] parts = msg.split(" ");
        if(parts.length == 1) {
            event.getChannel().sendMessage("The current embed color is: " + settings.getEmbedColor()).queue();
            return;
        } else if(parts.length == 2) {
            if(!StringUtils.validateHexCode(parts[1])) {
                event.getChannel().sendMessage("**Usage:** " + settings.getCmdPrefix() + "embedcolor #029A2F").queue();
            } else {
                settings.setEmbedColor(parts[1]);
                event.getChannel().sendMessage("Embed color set to: **" + parts[1] + "**").queue();
            }
        } else {
            event.getChannel().sendMessage("**Usage:** " + settings.getCmdPrefix() + "embedcolor #029A2F").queue();
        }
    }
}
