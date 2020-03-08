package net.mcstats2.core.commands;

import net.mcstats2.core.api.Command;
import net.mcstats2.core.api.MCSEntity.MCSEntity;

public class Chat extends Command {

    public Chat(String command) {
        super(command);
    }

    @Override
    public void execute(MCSEntity p, String[] args) {
        p.sendMessage("/chat clear [player]");
        p.sendMessage("/chat global clear [server]");
        p.sendMessage("/chat disable");
        p.sendMessage("/chat global enable [server]");
        p.sendMessage("/chat global disable [server]");
    }
}
