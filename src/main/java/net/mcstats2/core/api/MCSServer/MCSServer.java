package net.mcstats2.core.api.MCSServer;

import net.mcstats2.core.api.MCSEntity.MCSPlayer;
import net.mcstats2.core.api.chat.BaseComponent;
import net.mcstats2.core.api.chat.TextComponent;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface MCSServer {

    PluginDescription getDescription();

    ServerDetails getServerDetails();

    void shutdown(String message);
    void shutdown();

    MCSPlayer[] getPlayers();// throws InterruptedException, ExecutionException, IOException;

    boolean isOnline(MCSPlayer player);

    void broadcast(String message);

    void broadcast(String perm, String message);

    boolean hasPermission(MCSPlayer player, String s);

    void disconnect(MCSPlayer player, String reason);

    void playSound(MCSPlayer player, String sound, float volume, float pitch);

    void sendTitle(MCSPlayer player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subTitle);

    void sendActionBar(MCSPlayer player, String message, int duration);

    default void sendMessage(MCSPlayer player) {
        sendMessage(player, "");
    }
    default void sendMessage(MCSPlayer player, String message) {
        sendMessage(player, TextComponent.fromLegacyText(message));
    }
    void sendMessage(MCSPlayer player, BaseComponent message);
    void sendMessage(MCSPlayer player, BaseComponent[] message);

    default void sendConsole(String message) {
        sendConsole(TextComponent.fromLegacyText(message));
    }
    void sendConsole(BaseComponent message);
    void sendConsole(BaseComponent[] message);

    interface PluginDescription {
        File getPlugin();

        String getName();

        String getAuthor();

        String getVersion();
    }

    interface ServerDetails {
        boolean isCloudSystem();
        CloudDetails getCloudSystem();

        String getName();

        int getPort();

        boolean isOnlineMode();

        ServerType getType();

        String getVersion();

        enum ServerType {
            BUKKIT,
            BUNGEE;
        }
    }

    interface CloudDetails {
        String getWrapperId();

        String getId();

        String getGroup();

        boolean isStatic();

        CloudType getType();

        enum CloudType {
            CLOUDNET,
            TIMOCLOUD,
            CUSTOM
        }
    }
}