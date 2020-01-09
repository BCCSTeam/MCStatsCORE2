package net.mcstats2.core.modules.chatlog.data;

import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.MCSEntity.MCSPlayer;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class ChatLogData {
    private final UUID sender;
    private final UUID[] online;
    private final long timestamp;

    public ChatLogData(UUID sender) {
        this(sender, Arrays.asList(MCSCore.getInstance().getServer().getPlayers()).stream()
                .map((player -> player.getUUID()))
                .collect(Collectors.toList()).toArray(new UUID[0]));
    }

    public ChatLogData(UUID sender, MCSPlayer[] online) {
        this(sender, Arrays.asList(online).stream()
                .map((player -> player.getUUID()))
                .collect(Collectors.toList()).toArray(new UUID[0]));
    }

    public ChatLogData(UUID sender, UUID[] online) {
        this.sender = sender;
        this.online = online;
        this.timestamp = System.currentTimeMillis();
    }
}
