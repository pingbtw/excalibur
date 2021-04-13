package me.ping.bot;

import me.ping.bot.commands.Ping;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class Bot {
    public static void main(String[] arguments) throws Exception
    {
        String token = "";
        JDA api = JDABuilder.createDefault(token)
                .addEventListeners(new Ping())
                .build();
    }
}
