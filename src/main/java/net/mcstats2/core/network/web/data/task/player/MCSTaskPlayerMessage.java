package net.mcstats2.core.network.web.data.task.player;

import net.mcstats2.core.api.ChatColor;
import net.mcstats2.core.network.web.data.task.MCSTask;

import java.util.UUID;

public class MCSTaskPlayerMessage implements MCSTask {
    private UUID receiver;
    private String message;

    public UUID getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
