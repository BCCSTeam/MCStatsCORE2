package net.mcstats2.core.api.commands;

import net.mcstats2.core.api.Command;
import net.mcstats2.core.api.MCSEntity.MCSEntity;

public class MCStats extends Command {

    public MCStats(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(MCSEntity p, String[] args) {
        if (p.hasPermission("MCStatsNET.admin")) {
            if (args.length == 2 && args[0].equalsIgnoreCase("")) {

            }
        }
    }
}
