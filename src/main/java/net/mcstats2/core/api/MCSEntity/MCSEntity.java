package net.mcstats2.core.api.MCSEntity;

import java.util.UUID;

public interface MCSEntity {

    UUID getUUID();

    String getName();

    boolean hasPermission(String perm);
}
