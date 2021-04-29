package me.ping.bot.commands.elevated;

import me.ping.bot.core.Settings;
import me.ping.bot.core.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.EnumSet;
import java.util.List;

public class ElevatedListener extends ListenerAdapter {
    private EmbedBuilder helpEmbed;
    private Settings settings;
    private RoleCmd roleCmd;
    private EmbedColorCmd embedColorCmd;
    private PrefixCmd prefixCmd;
    private MacroCmd macroCmd;

    public ElevatedListener() {
        this.settings = Settings.getInstance();
        settings.loadSettings();
        this.helpEmbed = new EmbedBuilder();
        this.roleCmd = new RoleCmd();
        this.prefixCmd = new PrefixCmd();
        this.embedColorCmd = new EmbedColorCmd();
        this.macroCmd = new MacroCmd();
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;
        Member member = event.getMember();
        if (member == null)
            return;

        String prefix = settings.getCmdPrefix();
        String msg = event.getMessage().getContentRaw();
        List<Role> mentionedRoles = event.getMessage().getMentionedRoles();
        List<Role> memberRoles = member.getRoles();
        EnumSet<Permission> perms = member.getPermissions();

        /* allow admins and "admin_allowed_roles" only to run these commands */
        /* set prefix, set separator, add role, remove role */
        boolean hasRole = memberRoles.stream()
                .anyMatch(r -> settings.getRoleIds().contains(r.getIdLong()));

        if (perms.contains(Permission.MANAGE_CHANNEL) || hasRole) {
            if (msg.equalsIgnoreCase(settings.getCmdPrefix() + "adminhelp")) {
                buildHelpEmbed();
                event.getChannel().sendMessage(helpEmbed.build()).queue();
            }

            if (StringUtils.hasCommand(prefix + "addrole", msg, true)) {
                roleCmd.addRole(event);
            }
            if (StringUtils.hasCommand(prefix + "removerole", msg, true)) {
                roleCmd.removeRole(event);
            }
            if (StringUtils.hasCommand(prefix + "roles", msg, false)) {
                roleCmd.listRoles(event);
            }
            if (StringUtils.hasCommand(prefix + "prefix", msg, true)) {
                prefixCmd.changePrefix(event);
            }
            if (StringUtils.hasCommand(prefix + "embedcolor", msg, true)) {
                embedColorCmd.chaneEmbedColor(event);
            }
            if (StringUtils.hasCommand(prefix + "addmacro", msg, true)) {
                macroCmd.addMacro(event);
            }
            if (StringUtils.hasCommand(prefix + "removemacro", msg, true)) {
                macroCmd.removeMacro(event);
            }
            if (StringUtils.hasCommand(prefix + "editmacro", msg, true)) {
                macroCmd.editMacro(event);
            }
            if (StringUtils.hasCommand(prefix + "macros", msg, false)) {
                macroCmd.listAllMacros(event);
            }
        }
    }

    private void buildHelpEmbed() {
        Color embedColor = Color.green;
        try {
            embedColor = Color.decode(settings.getEmbedColor());
        } catch (NumberFormatException ex) {
            // do nothing with this one
        }
        String prefix = settings.getCmdPrefix();
        helpEmbed.clear();
        helpEmbed.setColor(embedColor);
        helpEmbed.setTitle("Excalibur Help");
        helpEmbed.setDescription("Admin Command Usage");
        helpEmbed.addField(
                "__" + prefix + "addrole__",
                "Add role(s) to access these commands.\n" +
                        "**Usage:** " + prefix + "addrole *<role_ids>* **OR** *<@mentioned_roles>* (Do not mix input type)", false);
        helpEmbed.addField(
                "__" + prefix + "removerole__",
                "Remove role(s) from access to these commands.\n" +
                        "**Usage:** " + prefix + "removerole *<role_ids>* **OR** *<@mentioned_roles>* (Do not mix input type)", false);
        helpEmbed.addField("__" + prefix + "roles__",
                "List roles that have access to these commands.\n" +
                        "**Usage:** " + prefix + "roles", false);
        helpEmbed.addField("__" + prefix + "prefix__",
                "Change command prefix, immediate effect.\n" +
                        "**Current prefix:** " + prefix + "\n" +
                        "**Usage:** " + prefix + "prefix *<new_prefix>*\n", false);
        helpEmbed.addField("__" + prefix + "embedcolor__",
                "Change embed color, immediate effect.\n" +
                        "**Usage:** " + prefix + "embedcolor *<hex_color>*\n", false);
        helpEmbed.addField("__" + prefix + "addmacro__",
                "Add a new macro\n" +
                        "**Usage:** " + prefix + "addmacro *<macro_name>* *<macro_content>*\n" +
                        "**NOTE:** Do not include command prefix in macro name.\n", false);
        helpEmbed.addField("__" + prefix + "removemacro__",
                "Add a new macro\n" +
                        "**Usage:** " + prefix + "removemacro *<macro_name>*\n", false);
        helpEmbed.addField("__" + prefix + "editmacro__",
                "Add a new macro\n" +
                        "**Usage:** " + prefix + "editmacro *<macro_name>* *<macro_content>*\n", false);
    }
}
