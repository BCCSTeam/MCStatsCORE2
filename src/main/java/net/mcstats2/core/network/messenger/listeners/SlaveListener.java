package net.mcstats2.core.network.messenger.listeners;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import net.mcstats2.core.MCSCore;

public class SlaveListener extends Listener {
    private MCSCore core;

    public SlaveListener(MCSCore core) {
        this.core = core;
    }

    @Override
    public void received(Connection con, Object o) {
    }
}
