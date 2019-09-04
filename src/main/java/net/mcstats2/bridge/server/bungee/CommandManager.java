package net.mcstats2.bridge.server.bungee;

import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.MCSServer.MCSBungeeServer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class CommandManager extends Command {

    public CommandManager(String name) {
        super(name);
    }

    public CommandManager(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        try {
            ((MCSBungeeServer) MCSCore.getInstance().getServer()).dispatchCommand(cs, getName(), args);
        } catch (InterruptedException | IOException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
