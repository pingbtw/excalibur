package me.ping.bot.commands;

import me.ping.bot.core.Settings;
import me.ping.bot.core.StringUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.temporal.ChronoUnit;

public class Ping extends ListenerAdapter {
    private Settings settings;

    public Ping() {
        this.settings = Settings.getInstance();
    }
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        if (StringUtils.startsWithIgnoreCase(msg.getContentRaw(), settings.getCmdPrefix() + "ping") ) {
            long ping = event.getMessage().getTimeCreated().until(msg.getTimeCreated(), ChronoUnit.MILLIS);
            MessageChannel channel = event.getChannel();
            channel.sendMessage("Ping: " + ping  + "ms | Websocket: " + event.getJDA().getGatewayPing() + "ms").queue();
        }
    }
}

