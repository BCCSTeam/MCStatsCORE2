package net.mcstats2.core.api.MCSServer;

import net.mcstats2.core.api.MCSEntity.MCSPlayer;
import net.mcstats2.core.api.chat.BaseComponent;
import net.mcstats2.core.api.chat.TextComponent;

import java.io.File;

public interface MCSServer {

    PluginDescription getDescription();

    ServerDetails getServerDetails();

    /**
     * Shutdown server instance with a reason
     * @param message Shutdown reason
     */
    void shutdown(String message);

    /**
     * Shutdown server instance without a reason
     */
    default void shutdown() {
        shutdown("");
    }

    default String getDisplayName(MCSPlayer player) {
        return player.getName();
    }

    default String getCustomName(MCSPlayer player) {
        return getDisplayName(player);
    }

    MCSPlayer[] getPlayers();

    /**
     * Get Status if a Player is currently online or not
     * @param player Targeted Player
     * @return Status of Player online or not
     */
    boolean isOnline(MCSPlayer player);

    /**
     * Request the current Ping of the Player
     * @param player Targeted Player
     * @return Ping of Player in ms; return -1 if offline or failed!
     */
    int getPing(MCSPlayer player);

    /**
     * Broadcast a Message on this server instance
     * @param message Broadcast Message
     */
    void broadcast(String message);

    /**
     * Broadcast a Message to Players with a permission on this server instance
     * @param perm Needed Permission to read the Message
     * @param message Broadcast Message
     */
    void broadcast(String perm, String message);

    /**
     * Check if a Player has this permission or not
     * @param player Targeted Player
     * @param s Permission to check
     * @return has requested Permission or not
     */
    boolean hasPermission(MCSPlayer player, String s);

    /**
     * Disconnect a Player with a reason
     * @param player Targeted Player
     * @param reason Disconnect reason
     */
    void disconnect(MCSPlayer player, String reason);

    /**
     * Play sound on the Players client
     * @param player Targeted Player
     * @param sound Sound name
     * @param volume Sound Volume
     * @param pitch Sound Pitch
     *
     * @apiNote Only for Bukkit Available
     */
    void playSound(MCSPlayer player, String sound, float volume, float pitch);

    /**
     * Display a Title or a SubTitle in the Players client
     * @param player Targeted Player
     * @param fadeIn FadeIn time
     * @param stay Stay time
     * @param fadeOut FadeOut time
     * @param title Title Message
     * @param subTitle subTitle Message
     *
     * @apiNote Only for Bukkit Available
     */
    void sendTitle(MCSPlayer player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subTitle);

    /**
     * Display a ActionBar in the Players client
     * @param player Targeted Player
     * @param message Message
     * @param duration Duration message keeps displayed
     */
    void sendActionBar(MCSPlayer player, String message, int duration);

    /**
     * Send a empty message(Spacer) to the Player
     * @param player Targeted Player
     */
    default void sendMessage(MCSPlayer player) {
        sendMessage(player, "");
    }

    /**
     * Send a message to the Player
     * @param player Targeted Player
     * @param message Message to send
     */
    default void sendMessage(MCSPlayer player, String message) {
        sendMessage(player, TextComponent.fromLegacyText(message));
    }

    /**
     * Send an BaseComponent message to the Player
     * @param player Targeted Player
     * @param message Message to send
     *
     * @see BaseComponent
     */
    void sendMessage(MCSPlayer player, BaseComponent message);

    /**
     * Send an BaseComponent message to the Player
     * @param player Targeted Player
     * @param message Message to send
     *
     * @see BaseComponent
     */
    void sendMessage(MCSPlayer player, BaseComponent[] message);

    /**
     * Send a message to the Console
     * @param message Message to send
     */
    default void sendConsole(String message) {
        sendConsole(TextComponent.fromLegacyText(message));
    }

    /**
     * Send an BaseComponent message to the Console
     * @param message Message to send
     */
    void sendConsole(BaseComponent message);

    /**
     * Send an BaseComponent message to the Console
     * @param message Message to send
     */
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