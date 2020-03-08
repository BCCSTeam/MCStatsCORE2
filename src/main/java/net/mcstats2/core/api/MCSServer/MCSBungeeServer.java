package net.mcstats2.core.api.MCSServer;

import cloud.timo.TimoCloud.api.TimoCloudAPI;
import de.dytanic.cloudnet.api.CloudAPI;
import de.dytanic.cloudnet.lib.server.ProxyGroupMode;
import net.mcstats2.bridge.server.bungee.MCPerms;
import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.MCSEntity.MCSConsole;
import net.mcstats2.core.api.MCSEntity.MCSEntity;
import net.mcstats2.core.api.MCSEntity.MCSPlayer;
import net.mcstats2.core.api.chat.BaseComponent;
import net.mcstats2.core.api.chat.serializer.ComponentSerializer;
import net.mcstats2.core.modules.chatlog.data.ChatLogDataCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class MCSBungeeServer implements MCSServer, Listener {
    private Plugin plugin;

    public MCSBungeeServer(Plugin plugin) {
        this.plugin = plugin;

        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void on(ChatEvent e) {
        if (!(e.getSender() instanceof ProxiedPlayer))
            return;

        if (!e.isCommand() || e.isProxyCommand())
            MCSCore.getInstance().getChatLog().log(new ChatLogDataCommand(((ProxiedPlayer) e.getSender()).getUniqueId(), e.getMessage()));
            //MCSCore.getInstance().getChatLog().log(((ProxiedPlayer)e.getSender()).getUniqueId(), e.isCommand() ? ChatLogType.COMMAND : ChatLogType.MESSAGE, e.getMessage());

        try {
            MCSPlayer p = MCSCore.getInstance().getPlayer(((ProxiedPlayer)e.getSender()).getUniqueId());

            e.setCancelled(MCSCore.getInstance().getChatFilter().check(p, e.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
                            return CloudAPI.getInstance().getServiceId().getWrapperId();

                        if (isPluginEnabled("TimoCloudAPI"))
                            return TimoCloudAPI.getBungeeAPI().getThisProxy().getBase();

                        return null;
                    }

                    @Override
                    public String getGroup() {
                        if (isPluginEnabled("CloudNetAPI"))
                            return CloudAPI.getInstance().getServiceId().getGroup();

                        if (isPluginEnabled("TimoCloudAPI"))
                            return TimoCloudAPI.getBungeeAPI().getThisProxy().getName();

                        return "Bungee";
                    }

                    @Override
                    public boolean isStatic() {
                        if (isPluginEnabled("CloudNetAPI"))
                            return CloudAPI.getInstance().getProxyGroupData(CloudAPI.getInstance().getServiceId().getGroup()).getProxyGroupMode().equals(ProxyGroupMode.STATIC);

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
            public String getName() {
                return "Bungee";
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
    public String getDisplayName(MCSPlayer player) {
        if (isOnline(player)) {
            ProxiedPlayer p = plugin.getProxy().getPlayer(player.getUUID());
            assert p != null;

            if (p.getDisplayName() == null)
                return p.getName();

            return p.getDisplayName();
        }

        return player.getName();
    }

    @Override
    public MCSPlayer[] getPlayers() {
        return plugin.getProxy().getPlayers().stream()
                .map((player -> {
                    try {
                        return MCSCore.getInstance().getPlayer(player.getUniqueId());
                    } catch (IOException | InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    return null;
                }))
                .collect(Collectors.toList()).toArray(new MCSPlayer[0]);
    }

    @Override
    public int getPing(MCSPlayer player) {
        if (isOnline(player))
            return plugin.getProxy().getPlayer(player.getUUID()).getPing();

        return -1;
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
        if (isPluginEnabled("MCPerms"))
            if (MCPerms.getInstance().getManager().hasPermission(player, s).isAllowed())
                return true;

        if (!isOnline(player))
            return false;

        return plugin.getProxy().getPlayer(player.getUUID()).hasPermission(s);
    }

    @Override
    public void disconnect(MCSPlayer player, String reason) {
        if (isOnline(player))
            plugin.getProxy().getPlayer(player.getUUID()).disconnect(TextComponent.fromLegacyText(reason));
    }

    @Override
    public void playSound(MCSPlayer player, String sound, float volume, float pitch) {

    }

    @Override
    public void sendTitle(MCSPlayer player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subTitle) {
        if (!isOnline(player))
            return;

        ProxiedPlayer pp = plugin.getProxy().getPlayer(player.getUUID());
        plugin.getProxy().createTitle()
                .title(TextComponent.fromLegacyText(title))
                .subTitle(TextComponent.fromLegacyText(subTitle))
                .fadeIn(fadeIn)
                .stay(stay)
                .fadeOut(fadeOut)
                .send(pp);
    }

    @Override
    public void sendActionBar(MCSPlayer player, String message, int duration) {

    }

    @Override
    public void sendMessage(MCSPlayer player, String message) {
        if (isOnline(player))
            plugin.getProxy().getPlayer(player.getUUID()).sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public void sendMessage(MCSPlayer player, BaseComponent message) {
        if (isOnline(player))
            plugin.getProxy().getPlayer(player.getUUID()).sendMessage(net.md_5.bungee.chat.ComponentSerializer.parse(ComponentSerializer.toString(message)));
    }

    @Override
    public void sendMessage(MCSPlayer player, BaseComponent[] message) {
        if (isOnline(player))
            plugin.getProxy().getPlayer(player.getUUID()).sendMessage(net.md_5.bungee.chat.ComponentSerializer.parse(ComponentSerializer.toString(message)));
    }

    @Override
    public void sendConsole(String message) {
        plugin.getProxy().getConsole().sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public void sendConsole(BaseComponent message) {
        plugin.getProxy().getConsole().sendMessage(net.md_5.bungee.chat.ComponentSerializer.parse(ComponentSerializer.toString(message)));
    }

    @Override
    public void sendConsole(BaseComponent[] message) {
        plugin.getProxy().getConsole().sendMessage(net.md_5.bungee.chat.ComponentSerializer.parse(ComponentSerializer.toString(message)));
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
