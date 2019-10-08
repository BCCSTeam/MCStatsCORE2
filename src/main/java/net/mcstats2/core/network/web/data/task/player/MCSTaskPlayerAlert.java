package net.mcstats2.core.network.web.data.task.player;

import net.mcstats2.core.api.ChatColor;
import net.mcstats2.core.network.web.data.task.MCSTask;

public class MCSTaskPlayerAlert implements MCSTask {
    private String message;
    private String permission;
    private boolean colored;

    public String getMessage() {
        if (isColored())
            return ChatColor.translateAlternateColorCodes('&', message);

        return message;
    }

    public String getPermission() {
        return permission;
    }

    public boolean isColored() {
        return colored;
    }
}
