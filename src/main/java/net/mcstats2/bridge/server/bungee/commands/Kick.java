package net.mcstats2.bridge.server.bungee.commands;

import net.mcstats2.bridge.server.bungee.Core;
import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.MCSEntity.MCSConsole;
import net.mcstats2.core.api.MCSEntity.MCSEntity;
import net.mcstats2.core.api.MCSEntity.MCSPlayer;
import net.mcstats2.core.api.config.Configuration;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class Kick extends Command implements TabExecutor {

    public Kick(String command) {
        super(command);
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

        Configuration lang = MCSCore.getInstance().getLang((p instanceof MCSPlayer) ? ((MCSPlayer) p).getSession().getAddressDetails().getLanguage() : "default");

        int m = cs instanceof ProxiedPlayer ? ((MCSPlayer) p).getMax("MCStatsNET.kick.power") : -1;

        if (m == 0) {
            cs.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', lang.getString("kick.prefix") + lang.getString("noPermissions"))));
            return;
        }

        if (args.length >= 1) {
            ProxiedPlayer tpp = Core.getInstance().getProxy().getPlayer(args[0]);

            if (tpp == null || !tpp.isConnected()) {
                cs.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', lang.getString("kick.prefix") + lang.getString("kick.offline"))));
                return;
            }

            if (cs instanceof ProxiedPlayer) {
                if (((ProxiedPlayer)cs).getUniqueId().equals(tpp.getUniqueId())) {
                    cs.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', lang.getString("kick.prefix") + lang.getString("kick.self"))));
                    return;
                }
            }

            MCSPlayer tp = null;
            try {
                tp = MCSCore.getInstance().getPlayer(tpp.getUniqueId());
            } catch (IOException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            if (tp == null) {
                cs.sendMessage(TextComponent.fromLegacyText("§cError with the other Profile!"));
                return;
            }

            int tm = tp.getMax("MCStatsNET.kick.power");

            if (m != -1)
                if (m <= tm || tpp.hasPermission("MCStatsNET.kick.bypass")) {
                    cs.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', lang.getString("kick.prefix") + lang.getString("kick.notAllowed").replace("%playername%", tpp.getName() ))));
                    return;
                }

            Configuration tl = MCSCore.getInstance().getLang(tp.getSession().getAddressDetails().getLanguage());

            String reason = "";
            for(int i=1;i<args.length;i++) {
                if (!reason.isEmpty())
                    reason += " ";
                reason += args[i];
            }

            HashMap<String, Object> replace = new HashMap<>();
            replace.put("reason", reason);
            tpp.disconnect(MCSCore.getInstance().buildScreen(tl, "kick.screen", replace));

            replace.put("playername", tp.getName());
            replace.put("staffname", p.getName());
            try {
                MCSCore.getInstance().broadcast("MCStatsNET.kick.alert", "kick.prefix", "kick.alert", replace);
            } catch (InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }
        } else
            cs.sendMessage(TextComponent.fromLegacyText("§a/kick <player> <reason>"));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender cs, String[] args) {
        if (cs instanceof ProxiedPlayer) {
            try {
                if (MCSCore.getInstance().getPlayer(((ProxiedPlayer) cs).getUniqueId()).getMax("MCStatsNET.kick.power") == 0)
                    return null;
            } catch (IOException | InterruptedException | ExecutionException e) {
                return null;
            }
        }

        if (args.length != 1)
            return null;

        ArrayList<String> names = new ArrayList<>();

        for (ProxiedPlayer pp : ProxyServer.getInstance().getPlayers()) {
            if (!pp.getName().startsWith(args[0]))
                continue;

            if (pp.getName().equals(cs.getName()))
                continue;

            names.add(pp.getName());
        }

        return names;
    }
}
