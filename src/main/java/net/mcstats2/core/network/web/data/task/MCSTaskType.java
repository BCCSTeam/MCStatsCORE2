package net.mcstats2.core.network.web.data.task;

import com.google.gson.annotations.SerializedName;

public enum MCSTaskType {
    @SerializedName("RELOAD_PLAYER_PROFILE") RELOAD_PLAYER_PROFILE("net.mcstats2.core.network.web.data.task.player.MCSTaskPlayerUpdate"),
    @SerializedName("CREATE_MUTE_REASON") CREATE_MUTE_REASON(null),
    @SerializedName("CREATE_BAN_REASON") CREATE_BAN_REASON(null),
    @SerializedName("MUTE") MUTE(null),
    @SerializedName("BAN") BAN(null),
    @SerializedName("GMUTE") GMute(null),
    @SerializedName("GBAN") GBan(null);

    private String className;

    MCSTaskType(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
