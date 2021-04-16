package me.ping.bot.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;

public class Pin extends ListenerAdapter {
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        TextChannel channel = event.getTextChannel();

        if (msg.getContentRaw().toLowerCase().startsWith("-pin ")) {
            String[] parsedMessage = msg.getContentRaw().split("\\s+");
            try {
                List<Message> pinnedMesages = new ArrayList<>(channel.retrievePinnedMessages().complete());
                if (pinnedMesages.size() != 50) {
                    channel.pinMessageById(channel.retrieveMessageById(parsedMessage[1]).complete().getIdLong()).queue();
                } else {
                    channel.sendMessage("There are already 50 pins!").queue();
                }
            } catch (Exception e) {
                channel.sendMessage("Something went wrong").queue();
            }

        }
    }
}