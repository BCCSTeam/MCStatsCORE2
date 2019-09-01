package net.mcstats2.core.network.socket.packets.server;

public class ServerRegisterPacket {
    public String GUID;
    public String Secret;
    public ServerDetails server;
    public PluginDetails plugin;

    public class ServerDetails {
        public int port;
        public boolean onlinemode;
        public String type;
        public String version;
    }

    public class PluginDetails {
        public String name;
        public String author;
        public String version;
    }
}
