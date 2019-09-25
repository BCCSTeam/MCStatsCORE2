package net.mcstats2.bridge.server.bukkit;

import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.MCSServer.MCSBukkitServer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class CommandManager implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String s, String[] args) {
        try {
            return ((MCSBukkitServer)MCSCore.getInstance().getServer()).dispatchCommand(cs, cmd.getName(), args);
        } catch (InterruptedException | IOException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }
}
