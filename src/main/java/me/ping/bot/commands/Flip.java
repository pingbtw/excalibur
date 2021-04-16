package me.ping.bot.commands;

import me.ping.bot.core.StringUtils;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Flip extends ListenerAdapter {

    public void onMessageReceived(MessageReceivedEvent event) {
        if (StringUtils.startsWithIgnoreCase(event.getMessage().getContentRaw(), "-flip")) {
            List<String> options;
            try {
                options = Arrays.asList(event.getMessage().getContentRaw().replace("-flip", "").split("\\|"));
                if (!options.isEmpty() && options.size() != 1) {
                    Random rand = new Random();
                    event.getChannel().sendMessage(options.get(rand.nextInt(options.size())).trim()).queue();
                }
                else {
                    event.getChannel().sendMessage("No options provided").queue();
                }
                } catch (IllegalArgumentException e) {
                event.getChannel().sendMessage("Something went wrong").queue();
            }
            }
        }
    }

