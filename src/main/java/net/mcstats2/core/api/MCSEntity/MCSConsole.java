package net.mcstats2.core.api.MCSEntity;

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
}
