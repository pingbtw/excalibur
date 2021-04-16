package me.ping.bot.classes;

public class Reminder {
    private long uid;
    private long serverId;
    private long channelId;
    private String reminder;
    private int recordId;

    public Reminder(
            long serverId,
            long channelId,
            long uid,
            String reminder,
            int recordId
    ) {
        this.channelId      = channelId;
        this.uid            = uid;
        this.serverId       = serverId;
        this.recordId       = recordId;
        this.reminder       = reminder;

    }

    public long getUid() {
        return uid;
    }

    public long getServerId() {
        return serverId;
    }

    public long getChannelId() {
        return channelId;
    }

    public String getReminder() {
        return reminder;
    }

    public int getRecordId() {
        return recordId;
    }
}
