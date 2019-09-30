package net.mcstats2.core.network.web.data;

import java.util.HashMap;

public class MCSAuthData implements MCSBasicData {
    private Response response;
    private MCSSystemData system;

    public Response getResponse() {
        return response;
    }

    @Override
    public MCSSystemData getSystem() {
        return system;
    }

    public static class Response {
        private String serverID;
        private String instanceID;
        private boolean hasAdminPanel;
        private HashMap<URL, String> URLs;

        public String getServerID() {
            return serverID;
        }

        public String getInstanceID() {
            return instanceID;
        }

        public HashMap<URL, String> getURLs() {
            return URLs;
        }

        public enum URL {
            REGISTER,
            LOGIN,
            SERVER_ADD,
            SERVER_PANEL
        }
    }
}
