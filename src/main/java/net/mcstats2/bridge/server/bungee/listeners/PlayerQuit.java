package net.mcstats2.bridge.server.bungee.listeners;

import net.mcstats2.bridge.server.bungee.Core;
import net.mcstats2.core.MCSCore;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class PlayerQuit implements Listener {

    public PlayerQuit(Core core) {
    }

    @EventHandler
    public void on(PlayerDisconnectEvent e) throws InterruptedException, ExecutionException, IOException {
        ProxiedPlayer pp = e.getPlayer();

        MCSCore.getInstance().playerQuit(pp.getUniqueId());
    }
}
