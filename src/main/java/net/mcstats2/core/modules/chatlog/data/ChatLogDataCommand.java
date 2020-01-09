package net.mcstats2.core.modules.chatlog.data;

import net.mcstats2.core.api.MCSEntity.MCSPlayer;

import java.util.UUID;

public class ChatLogDataCommand extends ChatLogData {
    private final String command;

    public ChatLogDataCommand(UUID sender, String command) {
        super(sender);
        this.command = command;
    }

    public ChatLogDataCommand(UUID sender, MCSPlayer[] online, String command) {
        super(sender, online);
        this.command = command;
    }

    public ChatLogDataCommand(UUID sender, UUID[] online, String command) {
        super(sender, online);
        this.command = command;
    }

}
