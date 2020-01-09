package net.mcstats2.core.network.web.data.task;

import com.google.gson.annotations.SerializedName;

public enum MCSTaskType {
    @SerializedName("PLAYER_UPDATE") PLAYER_UPDATE("net.mcstats2.core.network.web.data.task.player.MCSTaskPlayerUpdate"),
    @SerializedName("PLAYER_MESSAGE") PLAYER_MESSAGE("net.mcstats2.core.network.web.data.task.player.MCSTaskPlayerMessage"),
    @SerializedName("PLAYER_TITLE") PLAYER_TITLE("net.mcstats2.core.network.web.data.task.player.MCSTaskPlayerTitle"),
    @SerializedName("PLAYER_ACTIONBAR") PLAYER_ACTIONBAR("net.mcstats2.core.network.web.data.task.player.MCSTaskPlayerActionBar"),
    @SerializedName("PLAYER_SOUND") PLAYER_SOUND("net.mcstats2.core.network.web.data.task.player.MCSTaskPlayerSound"),
    @SerializedName("PLAYER_ALERT") PLAYER_ALERT("net.mcstats2.core.network.web.data.task.player.MCSTaskPlayerAlert"),
    @SerializedName("PLAYER_KICK") PLAYER_KICK("net.mcstats2.core.network.web.data.task.player.MCSTaskPlayerKick"),
    @SerializedName("PLAYER_MUTE") PLAYER_MUTE("net.mcstats2.core.network.web.data.task.player.MCSTaskPlayerMute"),
    @SerializedName("PLAYER_BAN") PLAYER_BAN("net.mcstats2.core.network.web.data.task.player.MCSTaskPlayerBan"),
    @SerializedName("PLAYER_CMUTE") PLAYER_CMUTE("net.mcstats2.core.network.web.data.task.player.MCSTaskPlayerCMute"),
    @SerializedName("PLAYER_CBAN") PLAYER_CBAN("net.mcstats2.core.network.web.data.task.player.MCSTaskPlayerCBan"),
    @SerializedName("PLAYER_GMUTE") PLAYER_GMUTE(PLAYER_KICK.getClassName()),
    @SerializedName("PLAYER_GBAN") PLAYER_GBAN(PLAYER_KICK.getClassName()),
    @SerializedName("SERVER_MUTE_CREATE_REASON") SERVER_MUTE_CREATE_REASON("net.mcstats2.core.network.web.data.task.server.MCSTaskServerMuteCreateReason"),
    @SerializedName("SERVER_BAN_CREATE_REASON") SERVER_BAN_CREATE_REASON("net.mcstats2.core.network.web.data.task.server.MCSTaskServerBanCreateReason");

    private String className;

    MCSTaskType(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
