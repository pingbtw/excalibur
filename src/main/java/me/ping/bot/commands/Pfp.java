package me.ping.bot.commands;

import me.ping.bot.core.Settings;
import me.ping.bot.core.StringUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Pfp extends ListenerAdapter {
    private Settings settings;

    public Pfp() {
        this.settings = Settings.getInstance();
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        MessageChannel channel = event.getChannel();
        if (StringUtils.startsWithIgnoreCase(event.getMessage().getContentRaw(), settings.getCmdPrefix() + "pfp")) {
            if (msg.getMentionedMembers().isEmpty()) {
                channel.sendMessage("Please provide a user to fetch").queue();
            } else {
                for (Member member : msg.getMentionedMembers()) {
                    event.getChannel().sendMessage(member.getUser().getEffectiveAvatarUrl()).queue();
                }
            }
        }
    }
}