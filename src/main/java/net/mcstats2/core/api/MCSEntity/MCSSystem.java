package net.mcstats2.core.api.MCSEntity;

import net.mcstats2.core.MCSCore;

import java.util.UUID;

public class MCSSystem implements MCSEntity {
    @Override
    public UUID getUUID() {
        return null;
    }

    @Override
    public String getName() {
        return "MCStats.net // System";
    }

    @Override
    public boolean hasPermission(String perm) {
        return true;
    }

    @Override
    public void sendMessage() {
        sendMessage("");
    }

    @Override
    public void sendMessage(String s) {
        MCSCore.getInstance().getServer().sendConsole(s);
    }
}
