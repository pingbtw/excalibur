package me.ping.bot.core;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class Logger {
    private static final String file = "error_log";
    public static void log(String msg) {
        try {
            LocalDate timestamp = LocalDate.now();
            FileWriter writer = new FileWriter(file, true);
            writer.write(timestamp.toString());
            writer.write('\t');
            writer.write(msg + "\n");
            writer.close();

        } catch(IOException ex) {
            System.out.println("Unable to write to error_log");
        }
    }
}
