package net.mcstats2.core.api.MCSEntity;

import net.mcstats2.core.api.config.Configuration;
import net.mcstats2.core.exceptions.MCSError;

import java.util.UUID;

public interface MCSEntity {

    UUID getUUID();

    String getName();

    boolean hasPermission(String perm);

    Configuration getLang();

    void sendMessage();

    void sendMessage(String s);

    default boolean equals(MCSEntity entity) {
        return getUUID().equals(entity.getUUID());
    }
}
