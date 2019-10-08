package net.mcstats2.core.network.web.data.task.player;

import net.mcstats2.core.api.ChatColor;
import net.mcstats2.core.network.web.data.task.MCSTask;

import java.util.UUID;

public class MCSTaskPlayerKick implements MCSTask {
    private UUID UUID;
    private String reason;
    private boolean colored;

    public UUID getUUID() {
        return UUID;
    }

    public String getReason() {
        if (isColored())
            return ChatColor.translateAlternateColorCodes('&', reason);

        return reason;
    }

    public boolean isColored() {
        return colored;
    }
}
