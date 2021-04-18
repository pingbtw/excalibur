package me.ping.bot.commands;

import me.ping.bot.core.Settings;
import me.ping.bot.core.StringUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

public class Nuke extends ListenerAdapter {
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        Guild guild = event.getGuild();
        MessageChannel channel = event.getChannel();
        Member member;
        Settings settings = new Settings();

        if (StringUtils.startsWithIgnoreCase(msg.getContentRaw(), settings.getCmdPrefix() + "nuke") ) {
            if(event.getMember().getPermissions().contains(Permission.MANAGE_CHANNEL)){
                String[] numberToNuke = msg.getContentRaw().split("\\s+");
                if (Integer.parseInt(numberToNuke[1]) < 50) {
                    List<Message> messages = channel.getHistory().retrievePast(Integer.parseInt(numberToNuke[1])).complete();
                    channel.purgeMessages(messages);
                } else {
                    channel.sendMessage("You've exceeded the 50 message limit").queue();
                }
            } else {
                channel.sendMessage("You aren't ping").queue();
            }
        }
    }
}