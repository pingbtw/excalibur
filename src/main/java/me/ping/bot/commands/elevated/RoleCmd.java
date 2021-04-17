package me.ping.bot.commands.elevated;

import me.ping.bot.core.Settings;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class RoleCmd {
    private Settings settings;

    public RoleCmd() {
        this.settings = Settings.getInstance();
    }

    public void addRole(MessageReceivedEvent event) {
        StringBuilder responseMessage = new StringBuilder();
        List<Role> mentionedRoles = event.getMessage().getMentionedRoles();
        String msg = event.getMessage().getContentRaw();

        if (!mentionedRoles.isEmpty()) {
            for (Role role : mentionedRoles) {
                if(settings.addRole(role.getIdLong())){
                    responseMessage.append("Role **").append(role.getName()).append("** added.\n");
                } else {
                    responseMessage.append("Role **").append(role.getName()).append("** already on allow list.\n");
                }
            }
            event.getChannel().sendMessage(responseMessage).queue();
        } else {
            String[] parts = msg.split(" ");
            if (parts.length == 1) {
                event.getChannel().sendMessage("**Usage:** " + settings.getCmdPrefix() + "addrole role_id OR @role").queue();
            } else {
                for (int i = 1; i < parts.length; i++) {
                    try {
                        long givenId = Long.parseLong(parts[i]);
                        Role role = event.getGuild().getRoleById(givenId);
                        if(role != null) {
                            if(settings.addRole(role.getIdLong())){
                                responseMessage.append("Role **").append(role.getName()).append("** added.\n");
                            } else {
                                responseMessage.append("Role **").append(role.getName()).append("** already on allow list.\n");
                            }
                        }
                    } catch (NumberFormatException e) {
                        event.getChannel().sendMessage("**Usage:** " + settings.getCmdPrefix() + "addrole role_id OR @role").queue();
                        return;
                    }
                }
                event.getChannel().sendMessage(responseMessage).queue();
            }
        }
    }

    public void removeRole(MessageReceivedEvent event) {
        StringBuilder responseMessage = new StringBuilder();
        List<Role> mentionedRoles = event.getMessage().getMentionedRoles();
        String msg = event.getMessage().getContentRaw();

        if (!mentionedRoles.isEmpty()) {
            for (Role role : mentionedRoles) {
                if(settings.removeRole(role.getIdLong())) {
                    responseMessage.append("Role **").append(role.getName()).append("** removed.\n");
                } else {
                    responseMessage.append("Role **").append(role.getName()).append("** not in allow list.\n");
                }
            }
            event.getChannel().sendMessage(responseMessage).queue();
        } else {
            String[] parts = msg.split(" ");
            if (parts.length == 1) {
                event.getChannel().sendMessage("**Usage:** " + settings.getCmdPrefix() + "removerole role_id OR @role").queue();
            } else {
                for (int i = 1; i < parts.length; i++) {
                    try {
                        long givenId = Long.parseLong(parts[i]);
                        Role role = event.getGuild().getRoleById(givenId);
                        if(role != null) {
                            if(settings.removeRole(role.getIdLong())) {
                                responseMessage.append("Role **").append(role.getName()).append("** removed.\n");
                            } else {
                                responseMessage.append("Role **").append(role.getName()).append("** not in allow list.\n");
                            }
                        }
                    } catch (NumberFormatException e) {
                        event.getChannel().sendMessage("**Usage:** " + settings.getCmdPrefix() + "removerole role_id OR @role").queue();
                        return;
                    }
                }
                event.getChannel().sendMessage(responseMessage).queue();
            }
        }
    }

    public void listRoles(MessageReceivedEvent event) {
        StringBuilder responseMessage = new StringBuilder("**__Allowed Roles__**\n");
        List<Long> roleIds = settings.getRoleIds();
        for(Long roleId : roleIds) {
            Role role = event.getGuild().getRoleById(roleId);
            if(role != null) {
                responseMessage.append(role.getName()).append(" [").append(role.getIdLong()).append("]\n");
            } else {
                responseMessage.append("**Role ID could not be identified [").append(roleId).append("]**\n");
            }
        }
        event.getChannel().sendMessage(responseMessage).queue();
    }
}
