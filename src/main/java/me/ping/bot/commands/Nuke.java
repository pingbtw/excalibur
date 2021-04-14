package me.ping.bot.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction;

import java.util.ArrayList;
import java.util.List;

public class Nuke extends ListenerAdapter {
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        Guild guild = event.getGuild();
        MessageChannel channel = event.getChannel();
        if (msg.getContentRaw().toLowerCase().startsWith("-nuke")) {
            String[] numberToNuke = msg.getContentRaw().split("\\s+");
            System.out.println(numberToNuke);
            List<Message> messages = channel.getHistory().retrievePast(Integer.parseInt(numberToNuke[1])).complete();
            for (Message message : messages) {
                channel.deleteMessageById(message.getIdLong()).queue();

            }
        }
    }
}