package net.mcstats2.core.api.MCSServer;

import cloud.timo.TimoCloud.api.TimoCloudAPI;
import de.dytanic.cloudnet.api.CloudAPI;
import de.dytanic.cloudnet.lib.server.ProxyGroupMode;
import de.dytanic.cloudnet.lib.server.ServerGroupMode;
import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.MCSEntity.MCSConsole;
import net.mcstats2.core.api.MCSEntity.MCSEntity;
import net.mcstats2.core.api.MCSEntity.MCSPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import org.bukkit.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MCSBungeeServer implements MCSServer, Listener {
    private Plugin plugin;

    public MCSBungeeServer(Plugin plugin) {
        this.plugin = plugin;

        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @Override
    public PluginDescription getDescription() {
        return new PluginDescription() {
            @Override
            public File getPlugin() {
                return plugin.getFile();
            }

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
            public boolean isCloudSystem() {
                if (isPluginEnabled("CloudNetAPI"))
                    return true;

                if (isPluginEnabled("TimoCloudAPI"))
                    return true;

                return false;
            }

            @Override
            public CloudDetails getCloudSystem() {
                if (!isCloudSystem())
                    return null;

                return new CloudDetails() {
                    @Override
                    public String getId() {
                        if (isPluginEnabled("CloudNetAPI"))
                            return String.valueOf(CloudAPI.getInstance().getServiceId().getId());

                        if (isPluginEnabled("TimoCloudAPI"))
                            return TimoCloudAPI.getBungeeAPI().getThisProxy().getId();

                        return null;
                    }

                    @Override
                    public String getWrapperId() {
                        if (isPluginEnabled("CloudNetAPI"))
                            return CloudAPI.getInstance().getWrapperId();

                        if (isPluginEnabled("TimoCloudAPI"))
                            return TimoCloudAPI.getBungeeAPI().getThisProxy().getBase();

                        return null;
                    }

                    @Override
                    public String getGroup() {
                        if (isPluginEnabled("CloudNetAPI"))
                            return CloudAPI.getInstance().getGroup();

                        if (isPluginEnabled("TimoCloudAPI"))
                            return TimoCloudAPI.getBungeeAPI().getThisProxy().getName();

                        if (plugin.getProxy().getName().isEmpty())
                            return "Bungee";

                        return plugin.getProxy().getName();
                    }

                    @Override
                    public boolean isStatic() {
                        if (isPluginEnabled("CloudNetAPI"))
                            return CloudAPI.getInstance().getProxyGroupData(CloudAPI.getInstance().getGroup()).getProxyGroupMode().equals(ProxyGroupMode.STATIC);

                        if (isPluginEnabled("TimoCloudAPI"))
                            return TimoCloudAPI.getBungeeAPI().getThisProxy().getGroup().isStatic();

                        return true;
                    }

                    @Override
                    public CloudType getType() {
                        if (isPluginEnabled("CloudNetAPI"))
                            return CloudType.CLOUDNET;

                        if (isPluginEnabled("TimoCloudAPI"))
                            return CloudType.TIMOCLOUD;

                        return null;
                    }
                };
            }

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
    public void shutdown(String message) {
        plugin.getProxy().stop(message);
    }

    @Override
    public void shutdown() {
        plugin.getProxy().stop();
    }

    @Override
    public MCSPlayer[] getPlayers() throws InterruptedException, ExecutionException, IOException {
        List<MCSPlayer> data = new ArrayList<>();

        for(ProxiedPlayer pp : plugin.getProxy().getPlayers())
            data.add(MCSCore.getInstance().getPlayer(pp.getUniqueId()));

        return data.toArray(new MCSPlayer[0]);
    }

    @Override
    public boolean isOnline(MCSPlayer player) {
        return plugin.getProxy().getPlayer(player.getUUID()) != null && plugin.getProxy().getPlayer(player.getUUID()).isConnected();
    }

    @Override
    public void broadcast(String message) {
        plugin.getProxy().broadcast(TextComponent.fromLegacyText(message));
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
        if (isOnline(player))
            plugin.getProxy().getPlayer(player.getUUID()).disconnect(TextComponent.fromLegacyText(reason));
    }

    @Override
    public void sendMessage(MCSPlayer player, String message) {
        if (isOnline(player))
            plugin.getProxy().getPlayer(player.getUUID()).sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public void sendConsole(String message) {
        plugin.getProxy().getConsole().sendMessage(TextComponent.fromLegacyText(message));
    }

    public boolean dispatchCommand(CommandSender cs, String cmd, String[] args) throws InterruptedException, ExecutionException, IOException {
        MCSEntity s;
        if (cs instanceof ProxiedPlayer)
            s = MCSCore.getInstance().getPlayer(((ProxiedPlayer)cs).getUniqueId());
        else
            s = new MCSConsole();

        return MCSCore.getInstance().dispatchCommand(s, cmd, args);
    }
    
    private boolean isPluginEnabled(String name) {
        for (Plugin plugin1 : plugin.getProxy().getPluginManager().getPlugins()) {
            if (plugin1.getDescription().getName().equalsIgnoreCase(name))
                return true;
        }
        
        return false;
    }
}
