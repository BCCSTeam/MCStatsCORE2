package net.mcstats2.core.modules.chatlog.data;

import net.mcstats2.core.api.MCSEntity.MCSPlayer;

import java.util.UUID;

public class ChatLogDataMessage extends ChatLogData {
    private final String message;

    public ChatLogDataMessage(UUID sender, String message) {
        super(sender);
        this.message = message;
    }

    public ChatLogDataMessage(UUID sender, MCSPlayer[] online, String message) {
        super(sender, online);
        this.message = message;
    }

    public ChatLogDataMessage(UUID sender, UUID[] online, String message) {
        super(sender, online);
        this.message = message;
    }

}
