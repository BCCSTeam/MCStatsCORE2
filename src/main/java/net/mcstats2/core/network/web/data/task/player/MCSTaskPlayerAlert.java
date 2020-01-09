package net.mcstats2.core.network.web.data.task.player;

import net.mcstats2.core.api.ChatColor;
import net.mcstats2.core.network.web.data.task.MCSTask;

public class MCSTaskPlayerAlert implements MCSTask {
    private String message;
    private String permission;

    public String getMessage() {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getPermission() {
        return permission;
    }
}
