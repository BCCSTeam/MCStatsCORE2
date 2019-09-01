package net.mcstats2.core.network.socket.listeners;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import net.mcstats2.core.MCSCore;

public class MasterListener extends Listener {
    private MCSCore core;

    public MasterListener(MCSCore core) {
        this.core = core;
    }

    @Override
    public void received(Connection connection, Object o) {
    }
}
