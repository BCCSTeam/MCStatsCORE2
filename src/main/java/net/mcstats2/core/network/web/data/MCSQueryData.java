package net.mcstats2.core.network.web.data;

import com.google.gson.annotations.SerializedName;
import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.ChatColor;
import net.mcstats2.core.network.web.data.task.MCSTask;
import net.mcstats2.core.network.web.data.task.MCSTaskData;
import net.mcstats2.core.network.web.data.task.MCSTaskType;

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
        public MCSTaskData[] tasks;
        public Alert[] alerts;
        public Moderators[] moderators;

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
