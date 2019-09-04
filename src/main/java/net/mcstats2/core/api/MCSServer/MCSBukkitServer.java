package net.mcstats2.core.api.MCSServer;

import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.MCSEntity.MCSConsole;
import net.mcstats2.core.api.MCSEntity.MCSEntity;
import net.mcstats2.core.api.MCSEntity.MCSPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MCSBukkitServer implements MCSServer, Listener {
    private Plugin plugin;

    public MCSBukkitServer(Plugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void on(PlayerLoginEvent e) throws Exception {
        Player p = e.getPlayer();
        MCSCore.getInstance().playerJoin(p.getUniqueId(),
                p.getName(),
                e.getAddress().getHostAddress(),
                e.getHostname(),
                -1);
    }

    @EventHandler
    public void on(PlayerQuitEvent e) {

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

        return data.toArray(new MCSPlayer[0]);
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

    public boolean dispatchCommand(CommandSender cs, String cmd, String[] args) throws InterruptedException, ExecutionException, IOException {
        MCSEntity s;
        if (cs instanceof Player)
            s = MCSCore.getInstance().getPlayer(((Player)cs).getUniqueId());
        else
            s = new MCSConsole();

        return MCSCore.getInstance().dispatchCommand(s, cmd, args);
    }
}
