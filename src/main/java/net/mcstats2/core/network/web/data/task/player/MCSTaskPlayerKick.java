package net.mcstats2.core.network.web.data.task.player;

import net.mcstats2.core.api.ChatColor;
import net.mcstats2.core.network.web.data.task.MCSTask;

import java.util.UUID;

public class MCSTaskPlayerKick implements MCSTask {
    private UUID UUID;
    private String reason;

    public UUID getUUID() {
        return UUID;
    }

    public String getReason() {
        return ChatColor.translateAlternateColorCodes('&', reason);
    }
}
