package net.mcstats2.core.api.MCSEntity;

import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.chat.BaseComponent;
import net.mcstats2.core.api.config.Configuration;

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
    public void sendMessage(BaseComponent s) {
        MCSCore.getInstance().getServer().sendConsole(s);
    }

    @Override
    public void sendMessage(BaseComponent[] s) {
        MCSCore.getInstance().getServer().sendConsole(s);
    }
}
