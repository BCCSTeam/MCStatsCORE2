package net.mcstats2.core.api.MCSServer;

import net.mcstats2.core.api.MCSEntity.MCSPlayer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface MCSServer {

    PluginDescription getDescription();

    ServerDetails getServerDetails();

    void shutdown(String message);
    void shutdown();

    MCSPlayer[] getPlayers() throws InterruptedException, ExecutionException, IOException;

    boolean isOnline(MCSPlayer player);

    void broadcast(String perm, String message);

    boolean hasPermission(MCSPlayer player, String s);

    void disconnect(MCSPlayer player, String reason);

    void sendMessage(MCSPlayer player, String message);

    void sendConsole(String message);

    interface PluginDescription {
        File getPlugin();

        String getName();

        String getAuthor();

        String getVersion();
    }

    interface ServerDetails {
        boolean isCloudSystem();
        CloudDetails getCloudSystem();

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
            TIMOCLOUD
        }
    }
}