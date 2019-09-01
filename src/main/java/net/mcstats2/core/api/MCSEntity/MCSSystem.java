package net.mcstats2.core.api.MCSEntity;

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

    public boolean hasPermission(String perm) {
        return true;
    }
}
