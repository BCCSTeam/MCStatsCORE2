package net.mcstats2.core.commands;

import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.ChatColor;
import net.mcstats2.core.api.Command;
import net.mcstats2.core.api.MCSEntity.MCSEntity;
import net.mcstats2.core.api.MCSEntity.MCSPlayer;
import net.mcstats2.core.api.config.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class Kick extends Command {

    public Kick(String command) {
        super(command);
    }

    @Override
    public void execute(MCSEntity p, String[] args) throws InterruptedException, ExecutionException, IOException {
        Configuration lang = MCSCore.getInstance().getLang((p instanceof MCSPlayer) ? ((MCSPlayer) p).getSession().getAddressDetails().getLanguage() : "default");

        int m = p instanceof MCSPlayer ? ((MCSPlayer) p).getMax("MCStatsNET.kick.power") : -1;

        if (m == 0) {
            p.sendMessage((ChatColor.translateAlternateColorCodes('&', lang.getString("kick.prefix") + lang.getString("noPermissions"))));
            return;
        }

        if (args.length >= 1) {
            MCSPlayer tp = MCSCore.getInstance().getPlayer(args[0]);

            if (tp == null || !tp.isOnline()) {
                p.sendMessage((ChatColor.translateAlternateColorCodes('&', lang.getString("kick.prefix") + lang.getString("kick.offline"))));
                return;
            }

            if (p instanceof MCSPlayer) {
                if (p.equals(tp)) {
                    p.sendMessage((ChatColor.translateAlternateColorCodes('&', lang.getString("kick.prefix") + lang.getString("kick.self"))));
                    return;
                }
            }

            int tm = tp.getMax("MCStatsNET.kick.power");

            if (m != -1)
                if (m <= tm || tp.hasPermission("MCStatsNET.kick.bypass")) {
                    p.sendMessage((ChatColor.translateAlternateColorCodes('&', lang.getString("kick.prefix") + lang.getString("kick.notAllowed").replace("%playername%", tp.getName() ))));
                    return;
                }

            Configuration tl = MCSCore.getInstance().getLang(tp.getSession().getAddressDetails().getLanguage());

            StringBuilder reason = new StringBuilder();
            for(int i=1;i<args.length;i++) {
                if (reason.length() > 0)
                    reason.append(" ");
                reason.append(args[i]);
            }

            HashMap<String, Object> replace = new HashMap<>();
            replace.put("reason", (reason.length() == 0) ? "&8&o<none>&r" : reason.toString());
            tp.disconnect(MCSCore.getInstance().buildScreen(tl, "kick.screen", replace));

            replace.put("playername", tp.getName());
            replace.put("staffname", p.getName());
            try {
                MCSCore.getInstance().broadcast("MCStatsNET.kick.alert", "kick.prefix", "kick.alert", replace);
            } catch (InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }
        } else
            p.sendMessage(("§a/kick <player> <reason>"));
    }

    /*@Override
    public Iterable<String> onTabComplete(CommandSender cs, String[] args) {
        if (cs instanceof MCSPlayer) {
            try {
                if (MCSCore.getInstance().getPlayer(((MCSPlayer) cs).getUniqueId()).getMax("MCStatsNET.kick.power") == 0)
                    return null;
            } catch (IOException | InterruptedException | ExecutionException e) {
                return null;
            }
        }

        if (args.length != 1)
            return null;

        ArrayList<String> names = new ArrayList<>();

        for (MCSPlayer pp : ProxyServer.getInstance().getPlayers()) {
            if (!pp.getName().startsWith(args[0]))
                continue;

            if (pp.getName().equals(p.getName()))
                continue;

            names.add(pp.getName());
        }

        return names;
    }*/
}
