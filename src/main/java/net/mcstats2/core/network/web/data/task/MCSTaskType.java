package net.mcstats2.core.network.web.data.task;

import com.google.gson.annotations.SerializedName;

public enum MCSTaskType {
    @SerializedName("PLAYER_UPDATE") PLAYER_UPDATE("net.mcstats2.core.network.web.data.task.player.MCSTaskPlayerUpdate"),
    @SerializedName("PLAYER_MESSAGE") PLAYER_MESSAGE("net.mcstats2.core.network.web.data.task.player.MCSTaskPlayerMessage"),
    @SerializedName("PLAYER_MUTE") PLAYER_MUTE(null),
    @SerializedName("PLAYER_BAN") PLAYER_BAN(null),
    @SerializedName("PLAYER_GMUTE") PLAYER_GMute(null),
    @SerializedName("PLAYER_GBAN") PLAYER_GBan(null),
    @SerializedName("SERVER_MUTE_CREATE_REASON") SERVER_MUTE_CREATE_REASON(null),
    @SerializedName("SERVER_BAN_CREATE_REASON") SERVER_BAN_CREATE_REASON(null);

    private String className;

    MCSTaskType(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
