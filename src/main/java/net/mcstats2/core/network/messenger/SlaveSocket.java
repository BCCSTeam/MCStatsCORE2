package net.mcstats2.core.network.messenger;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import net.mcstats2.core.MCSCore;
import net.mcstats2.core.network.messenger.listeners.SlaveListener;
import net.mcstats2.core.network.messenger.packets.player.*;
import net.mcstats2.core.network.messenger.packets.server.ServerConnectPacket;
import net.mcstats2.core.network.messenger.packets.server.ServerDisconnectPacket;

import java.io.IOException;

public class SlaveSocket {
    private MCSCore core;

    public SlaveSocket(MCSCore core) {
        this.core = core;
        Client c = new Client();
        c.start();
        try {
            c.connect(5000,"",54555, 54777);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (c.isConnected()) {
            c.addListener(new SlaveListener(core));
            c.sendTCP(new ServerConnectPacket());
        }

        Kryo k = c.getKryo();
        k.register(PlayerBanPacket.class);
        k.register(PlayerFetchPacket.class);
        k.register(PlayerInstanceJoinPacket.class);
        k.register(PlayerInstanceQuitPacket.class);
        k.register(PlayerJoinPacket.class);
        k.register(PlayerMutePacket.class);
        k.register(PlayerQuitPacket.class);

        k.register(ServerConnectPacket.class);
        k.register(ServerDisconnectPacket.class);
    }
}
