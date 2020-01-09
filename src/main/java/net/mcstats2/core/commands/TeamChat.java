package net.mcstats2.core.commands;

import net.mcstats2.core.api.Command;
import net.mcstats2.core.api.MCSEntity.MCSEntity;

public class TeamChat extends Command {
    public TeamChat(String name) {
        super(name);
    }

    @Override
    public void execute(MCSEntity e, String[] args) {
        if (e.hasPermission("MCStatsNET.team")) {
            if (args.length == 0) {
                e.sendMessage("§a/tc <message>");
                return;
            }

            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                if (sb.length() != 0)
                    sb.append(" ");

                sb.append(arg);
            }

            getCore().getServer().broadcast("MCStatsNET.team", "§6§l⬧§r §a" + e.getName() + " §7§l⇨§r §e" + sb.toString());
        }
    }
}
