package net.mcstats2.core.network.manager.drivers;

import de.dytanic.cloudnet.api.CloudAPI;

public class BungeeDriver extends NetworkDriver {
    @Override
    public void init() {
        /*Bukkit.getMessenger().registerOutgoingPluginChannel(Core.getInstance(), "MCS::CORE");
        Bukkit.getMessenger().registerIncomingPluginChannel(Core.getInstance(), "MCS::CORE", (s, player, bytes) -> {});

        CloudAPI.getInstance();
        TimoCloudAPI.getMessageAPI().sendMessageToCore(new PluginMessage("MCS::CORE::"));*/
    }

    @Override
    public boolean test() {
        return false;
    }

    @Override
    public void receive(Object data) {

    }

    @Override
    public void send(Object data) {

    }
}
