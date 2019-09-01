package net.mcstats2.core.network.socket;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Server;
import net.mcstats2.core.MCSCore;
import net.mcstats2.core.network.socket.listeners.MasterListener;
import net.mcstats2.core.network.socket.packets.player.*;
import net.mcstats2.core.network.socket.packets.server.*;

import java.io.IOException;

public class MasterSocket {
    private MCSCore core;

    public MasterSocket(MCSCore core){
        this.core = core;

        Server s = new Server();
        s.start();
        try {
            s.bind(54555, 54777);
        } catch (IOException e) {
            e.printStackTrace();
        }
        s.addListener(new MasterListener(core));

        Kryo k = s.getKryo();
        k.register(PlayerBanPacket.class);
        k.register(PlayerFetchPacket.class);
        k.register(PlayerInstanceJoinPacket.class);
        k.register(PlayerInstanceQuitPacket.class);
        k.register(PlayerJoinPacket.class);
        k.register(PlayerMutePacket.class);
        k.register(PlayerQuitPacket.class);

        k.register(ServerAuthPacket.class);
        k.register(ServerConnectPacket.class);
        k.register(ServerDisconnectPacket.class);
        k.register(ServerRegisterPacket.class);
    }
}
