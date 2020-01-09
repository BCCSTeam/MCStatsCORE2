package net.mcstats2.core.network.web.data.task.player;

import net.mcstats2.core.api.ChatColor;
import net.mcstats2.core.network.web.data.task.MCSTask;

import java.util.UUID;

public class MCSTaskPlayerActionBar implements MCSTask {
    private UUID UUID;
    private String message;
    private int duration = 10;

    public UUID getUUID() {
        return UUID;
    }

    public String getMessage() {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public int getDuration() {
        return duration;
    }
}
