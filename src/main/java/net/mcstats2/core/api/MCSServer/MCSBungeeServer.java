package net.mcstats2.core.api.MCSServer;

import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.MCSEntity.MCSPlayer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MCSBungeeServer implements MCSServer {
    private Plugin plugin;

    public MCSBungeeServer(Plugin plugin) {
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
                return plugin.getDescription().getAuthor();
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
                return plugin.getProxy().getConfig().getListeners().iterator().next().getHost().getPort();
            }

            @Override
            public boolean isOnlineMode() {
                return plugin.getProxy().getConfig().isOnlineMode();
            }

            @Override
            public ServerType getType() {
                return ServerType.BUNGEE;
            }

            @Override
            public String getVersion() {
                return plugin.getProxy().getVersion();
            }
        };
    }

    @Override
    public MCSPlayer[] getPlayers() throws InterruptedException, ExecutionException, IOException {
        List<MCSPlayer> data = new ArrayList<>();

        for(ProxiedPlayer pp : plugin.getProxy().getPlayers())
            data.add(MCSCore.getInstance().getPlayer(pp.getUniqueId()));

        MCSPlayer[] players = new MCSPlayer[data.size()];
        data.toArray(players);
        return players;
    }

    @Override
    public boolean isOnline(MCSPlayer player) {
        return plugin.getProxy().getPlayer(player.getUUID()) != null && plugin.getProxy().getPlayer(player.getUUID()).isConnected();
    }

    @Override
    public void broadcast(String perm, String message) {
        for (ProxiedPlayer pp : plugin.getProxy().getPlayers()) {
            if (pp.hasPermission(perm))
                pp.sendMessage(TextComponent.fromLegacyText(message));
        }
    }

    @Override
    public boolean hasPermission(MCSPlayer player, String s) {
        if (plugin.getProxy().getPlayer(player.getUUID()) == null || !plugin.getProxy().getPlayer(player.getUUID()).isConnected())
            return false;

        return plugin.getProxy().getPlayer(player.getUUID()).hasPermission(s);
    }

    @Override
    public void disconnect(MCSPlayer player, String reason) {
        if (plugin.getProxy().getPlayer(player.getUUID()).isConnected())
            plugin.getProxy().getPlayer(player.getUUID()).disconnect(TextComponent.fromLegacyText(reason));
    }

    @Override
    public void sendMessage(MCSPlayer player, String message) {
        if (plugin.getProxy().getPlayer(player.getUUID()).isConnected())
            plugin.getProxy().getPlayer(player.getUUID()).sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public void sendConsole(String message) {
        plugin.getProxy().getConsole().sendMessage(TextComponent.fromLegacyText(message));
    }
}
