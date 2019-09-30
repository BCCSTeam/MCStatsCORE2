package net.mcstats2.core.network.web.data;

public class MCSUpdaterData implements MCSBasicData {
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
        private String name;
        private String version;
        private String hash;
        private String downloadURL;

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public String getHash() {
            return hash;
        }

        public String getDownloadURL() {
            return downloadURL;
        }
    }
}
