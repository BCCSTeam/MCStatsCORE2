package net.mcstats2.core.network.web.data.task.player;

import net.mcstats2.core.network.web.data.task.MCSTask;

import java.util.UUID;

public class MCSTaskPlayerCBan implements MCSTask {
    private UUID UUID;
    private UUID STAFF;
    private String text;
    private int expire;

    public UUID getUUID() {
        return UUID;
    }

    public UUID getSTAFF() {
        return STAFF;
    }

    public String getText() {
        return text;
    }

    public int getExpire() {
        return expire;
    }
}
