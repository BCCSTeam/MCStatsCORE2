package net.mcstats2.core.network.web.data.task.player;

import net.mcstats2.core.api.ChatColor;
import net.mcstats2.core.network.web.data.task.MCSTask;

import java.util.UUID;

public class MCSTaskPlayerMessage implements MCSTask {
    private UUID receiver;
    private String message;
    private boolean colored;

    public UUID getReceiver() {
        return receiver;
    }

    public String getMessage() {
        if (isColored())
            return ChatColor.translateAlternateColorCodes('&', message);

        return message;
    }

    public boolean isColored() {
        return colored;
    }
}
