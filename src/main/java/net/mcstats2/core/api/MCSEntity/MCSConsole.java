package net.mcstats2.core.api.MCSEntity;

import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.config.Configuration;

import java.util.UUID;

public class MCSConsole implements MCSEntity {
    @Override
    public UUID getUUID() {
        return null;
    }

    @Override
    public String getName() {
        return "CONSOLE";
    }

    public boolean hasPermission(String perm) {
        return true;
    }

    @Override
    public Configuration getLang() {
        return MCSCore.getInstance().getLang("default");
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
