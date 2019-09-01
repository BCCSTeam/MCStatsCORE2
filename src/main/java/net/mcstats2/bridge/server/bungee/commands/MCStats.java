package net.mcstats2.bridge.server.bungee.commands;

import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.MCSEntity.MCSConsole;
import net.mcstats2.core.api.MCSEntity.MCSEntity;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MCStats extends Command {

    public MCStats(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        MCSEntity p = null;
        if (cs instanceof ProxiedPlayer) {
            try {
                p = MCSCore.getInstance().getPlayer(((ProxiedPlayer) cs).getUniqueId());
            } catch (IOException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } else
            p = new MCSConsole();

        if (p == null) {
            cs.sendMessage(TextComponent.fromLegacyText("§cError with your Profile!"));
            return;
        }

        if (p.hasPermission("MCStatsNET.admin")) {
            if (args.length == 2 && args[0].equalsIgnoreCase("")) {

            }
        }
    }
}
