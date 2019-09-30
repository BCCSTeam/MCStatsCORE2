package net.mcstats2.core.network.web.data;

import com.google.gson.annotations.SerializedName;
import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.ChatColor;

import java.util.HashMap;
import java.util.UUID;

public class MCSQueryData {
    public Response response;
    public System system;

    public class Response {
        public String serverID;
        public String instanceID;
        public boolean isMaster;
        public boolean hasAdminPanel;
        public HashMap<MCSAuthData.Response.URL, String> URLs;
        public Task[] tasks;
        public Alert[] alerts;
        public Moderators[] moderators;

        public class Task {
            public String id;
            public TaskType type;
            public UUID UUID;
            public String STAFF;
            public Reason reason;
            public String[] proofs;
            public int expire;

            public class Reason {
                public String id;
                public String name;
                public String text;
            }
        }

        public class Alert {
            public AlertType type;
            public String text;
            public int delay;
        }

        public class Moderators {
            public RankType rank;
            public UUID UUID;
        }
    }

    public class System {
        public int status;
        public String message;
    }

    public enum TaskType {
        @SerializedName("RELOAD_PLAYER_PROFILE") RELOAD_PLAYER_PROFILE,
        @SerializedName("CREATE_MUTE_REASON") CREATE_MUTE_REASON,
        @SerializedName("CREATE_BAN_REASON") CREATE_BAN_REASON,
        @SerializedName("MUTE") MUTE,
        @SerializedName("BAN") BAN,
        @SerializedName("GMUTE") GMute,
        @SerializedName("GBAN") GBan
    }

    public enum AlertType {
        @SerializedName("SUCCESS") SUCCESS("[SUCCESS]", ChatColor.GREEN),
        @SerializedName("INFO") INFO("[INFO]", ChatColor.BLUE),
        @SerializedName("WARNING") WARNING("[WARNING]", ChatColor.YELLOW),
        @SerializedName("DANGER") DANGER("[DANGER]", ChatColor.RED);

        private String prefix;
        private ChatColor chatcolor;

        AlertType(String prefix, ChatColor chatcolor) {
            this.prefix = prefix;
            this.chatcolor = chatcolor;
        }

        public String getPrefix() {
            return prefix;
        }

        public ChatColor getChatColor() {
            return chatcolor;
        }
    }

    public enum RankType {
        @SerializedName("MODERATOR") MODERATOR("MCSMod"),
        @SerializedName("DEVELOPER") DEVELOPER("MCSDev"),
        @SerializedName("ADMIN") ADMIN("MCSAdmin"),
        @SerializedName("OWNER") OWNER("MCSOwner");

        private String prefix;

        RankType(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }
    }
}
