package net.mcstats2.core.network.web.data.task.player;

import net.mcstats2.core.api.ChatColor;
import net.mcstats2.core.network.web.data.task.MCSTask;

import java.util.UUID;

public class MCSTaskPlayerTitle implements MCSTask {
    private UUID UUID;
    private Integer fadeIn = 3;
    private Integer fadeOut = 3;
    private Integer stay = 50;
    private String title = "";
    private String subTitle = "";

    public UUID getUUID() {
        return UUID;
    }

    public Integer getFadeIn() {
        return fadeIn;
    }

    public Integer getFadeOut() {
        return fadeOut;
    }

    public Integer getStay() {
        return stay;
    }

    public String getTitle() {
        return ChatColor.translateAlternateColorCodes('&', title);
    }

    public String getSubTitle() {
        return ChatColor.translateAlternateColorCodes('&', subTitle);
    }
}
