package net.mcstats2.core.api.MCSServer;

import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.MCSEntity.MCSPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MCSBukkitServer implements MCSServer {
    private Plugin plugin;

    public MCSBukkitServer(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public PluginDescription getDescription() {
        return new PluginDescription() {
            @Override
            public String getName() {
                return plugin.getDescription().getName();
            }

            @Override
            public String getAuthor() {
                StringBuilder sb = new StringBuilder();

                for (String b : plugin.getDescription().getAuthors()) {
                    if (sb.length() != 0)
                        sb.append(", ");
                    sb.append(b);
                }

                return sb.toString();
            }

            @Override
            public String getVersion() {
                return plugin.getDescription().getVersion();
            }
        };
    }

    @Override
    public ServerDetails getServerDetails() {
        return new ServerDetails() {
            @Override
            public int getPort() {
                return plugin.getServer().getPort();
            }

            @Override
            public boolean isOnlineMode() {
                return plugin.getServer().getOnlineMode();
            }

            @Override
            public ServerType getType() {
                return ServerType.BUKKIT;
            }

            @Override
            public String getVersion() {
                return plugin.getServer().getVersion();
            }
        };
    }

    @Override
    public MCSPlayer[] getPlayers() throws InterruptedException, ExecutionException, IOException {
        List<MCSPlayer> data = new ArrayList<>();

        for(Player p : plugin.getServer().getOnlinePlayers())
            data.add(MCSCore.getInstance().getPlayer(p.getUniqueId()));

        MCSPlayer[] players = new MCSPlayer[data.size()];
        data.toArray(players);
        return players;
    }

    @Override
    public boolean isOnline(MCSPlayer player) {
        return plugin.getServer().getPlayer(player.getUUID()) != null && plugin.getServer().getPlayer(player.getUUID()).isOnline();
    }

    @Override
    public void broadcast(String perm, String message) {
        plugin.getServer().broadcast(perm, message);
    }

    @Override
    public boolean hasPermission(MCSPlayer player, String s) {
        return plugin.getServer().getPlayer(player.getUUID()).hasPermission(s);
    }

    @Override
    public void disconnect(MCSPlayer player, String reason) {
        if (plugin.getServer().getPlayer(player.getUUID()).isOnline())
            plugin.getServer().getPlayer(player.getUUID()).kickPlayer(reason);
    }

    @Override
    public void sendMessage(MCSPlayer player, String message) {
        if (plugin.getServer().getPlayer(player.getUUID()).isOnline())
            plugin.getServer().getPlayer(player.getUUID()).sendMessage(message);
    }

    @Override
    public void sendConsole(String message) {
        plugin.getServer().getConsoleSender().sendMessage(message);
    }
}
