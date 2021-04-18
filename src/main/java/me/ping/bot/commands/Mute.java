package me.ping.bot.commands;

import me.ping.bot.core.Settings;
import me.ping.bot.core.StringUtils;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

public class Mute extends ListenerAdapter {
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        Guild guild = event.getGuild();
        MessageChannel channel = event.getChannel();
        Settings settings = new Settings();
        if (StringUtils.startsWithIgnoreCase(msg.getContentRaw(), settings.getCmdPrefix() + "mute") ) {
            List<Member> memberToMute = event.getMessage().getMentionedMembers();
            Role memberRole = event.getMember().getRoles().stream().filter(role -> role.getName().equals("moterator")).findFirst().orElse(null);
            if (memberRole != null) {
                if (memberToMute.size() > 1) {
                    channel.sendMessage("Only one user may be muted at a time").queue();
                } else {
                    Role role = guild.getRoles().stream().filter(role1 -> role1.getName().equals("muted")).findFirst().orElse(null);
                    if (role != null) {
                        guild.addRoleToMember(memberToMute.get(0).getIdLong(), role).queue();
                    } else {
                        channel.sendMessage("Role not found").queue();
                    }
                }
            } else {
                channel.sendMessage("User does not have permission to mute").queue();
            }
        }
        if (msg.getContentRaw().toLowerCase().startsWith("-unmute")) {
            List<Member> memberToUnmute = event.getMessage().getMentionedMembers();
            Role memberRole = event.getMember().getRoles().stream().filter(role -> role.getName().equals("moterator")).findFirst().orElse(null);
            Role role = guild.getRoles().stream().filter(role1 -> role1.getName().equals("muted")).findFirst().orElse(null);
            if (memberRole != null) {
                if (memberToUnmute.size() > 1) {
                    channel.sendMessage("Only one user may be unmuted at a time").queue();
                } else if (memberToUnmute.get(0).getRoles().stream().filter(role1 -> role1.getName().equals("muted")).findFirst().orElse(null) != null) {
                    guild.removeRoleFromMember(memberToUnmute.get(0).getIdLong(), role).queue();
                } else {
                    channel.sendMessage("Role not found").queue();
                }
            } else {
                channel.sendMessage("User does not have permission to unmute").queue();
            }
        }

    }
}


