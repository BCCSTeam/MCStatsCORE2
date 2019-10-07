package net.mcstats2.core.api.MCSEvent.player;

import net.mcstats2.core.api.MCSEntity.MCSPlayer;
import net.mcstats2.core.api.MCSEvent.MCSEvent;

import java.util.UUID;

public class MCSEventPlayerUpdate implements MCSEvent {
    private MCSPlayer player;

    public MCSEventPlayerUpdate(MCSPlayer player) {
        this.player = player;
    }

    public MCSPlayer getPlayer() {
        return player;
    }

    public UUID getUUID() {
        return player.getUUID();
    }
}
