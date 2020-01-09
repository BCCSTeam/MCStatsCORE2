package net.mcstats2.core.network.web.data.task.player;

import net.mcstats2.core.network.web.data.task.MCSTask;

import java.util.UUID;

public class MCSTaskPlayerSound implements MCSTask {
    private UUID UUID;
    private String sound;
    private float volume = 1;
    private float pitch = 1;

    public UUID getUUID() {
        return UUID;
    }

    public String getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }
}
