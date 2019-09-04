package net.mcstats2.bridge.server.bukkit;

import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.MCSServer.MCSBukkitServer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CommandManager extends BukkitCommand {

    CommandManager(String name, String description, String usageMessage, List<String> aliases) {
        super(name, description, usageMessage, aliases);
    }

    @Override
    public boolean execute(CommandSender cs, String s, String[] args) {
        try {
            return ((MCSBukkitServer)MCSCore.getInstance().getServer()).dispatchCommand(cs, getName(), args);
        } catch (InterruptedException | IOException | ExecutionException e) {
            e.printStackTrace();
        }

        return true;
    }
}
