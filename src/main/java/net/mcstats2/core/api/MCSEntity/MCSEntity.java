package net.mcstats2.core.api.MCSEntity;

import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.chat.BaseComponent;
import net.mcstats2.core.api.chat.TextComponent;
import net.mcstats2.core.api.config.Configuration;

import java.util.UUID;

public interface MCSEntity {

    UUID getUUID();

    String getName();

    boolean hasPermission(String perm);

    default Configuration getLang() {
        return MCSCore.getInstance().getLang("default");
    }

    default void sendMessage() {
        sendMessage("");
    }
    default void sendMessage(String s) {
        sendMessage(TextComponent.fromLegacyText(s));
    }
    void sendMessage(BaseComponent s);
    void sendMessage(BaseComponent[] s);

    default boolean equals(MCSEntity entity) {
        if (getUUID() == null || entity.getUUID() == null)
            return false;

        return getUUID().equals(entity.getUUID());
    }
}
