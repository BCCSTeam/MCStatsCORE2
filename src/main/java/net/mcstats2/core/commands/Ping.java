package net.mcstats2.core.commands;

import net.mcstats2.core.api.ChatColor;
import net.mcstats2.core.api.Command;
import net.mcstats2.core.api.MCSEntity.MCSEntity;
import net.mcstats2.core.api.MCSEntity.MCSPlayer;

public class Ping extends Command {

    public Ping(String command) {
        super(command);
    }

    @Override
    public void execute(MCSEntity p, String[] args) {
        if (p instanceof MCSPlayer) {
            int ping = ((MCSPlayer) p).getPing();
            ChatColor color;
            if (ping < 0) {
                color = ChatColor.DARK_GRAY;
            } else if (ping <= 150) {
                color = ChatColor.GREEN;
            } else if (ping <= 300) {
                color = ChatColor.YELLOW;
            } else if (ping <= 400) {
                color = ChatColor.GOLD;
            } else if (ping <= 500) {
                color = ChatColor.RED;
            } else {
                color = ChatColor.DARK_RED;
            }

            p.sendMessage(ChatColor.translateAlternateColorCodes('&', p.getLang().getString("ping").replace("%ping%", color + String.valueOf(ping))));
        }
    }
}
