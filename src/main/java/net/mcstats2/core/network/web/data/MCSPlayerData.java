package net.mcstats2.core.network.web.data;

public class MCSPlayerData {
    public Response response;
    public System system;

    public class Response {
        public String UUID;
        public String name;
        public Name[] names;
        public Skin skin;
        public Session session;
        public CData[] cdata;

        public class Name {
            public String name;
            public String changedToAt;
            public Boolean played;
        }

        public class Skin {
            public String id;
            public String value;
            public String signature;
        }

        public class Session {
            public AddressDetails addressDetails;
            public Checks checks;

            public class AddressDetails {
                public String continent_code;
                public String continent_name;
                public String country_iso_code;
                public String country_name;
                public String timezone;
                public String language;
                public boolean is_in_european_union;
            }

            public class Checks {
                public Skin skin;
                public Name name;
                public VPN vpn;
                public GMute gmute;
                public GBan gban;

                public class Skin {
                    public String id;
                    public String reason;
                    public boolean block;
                }

                public class Name {
                    public String id;
                    public String reason;
                    public boolean block;
                }

                public class VPN {
                    public String id;
                    public boolean block;
                }

                public class GMute {
                    public String screen;
                    public String alert;
                    public String id;
                    public String STAFF;
                    public Reason reason;
                    public Proof[] proofs;
                    public int expire;
                    public String timestamp;
                }

                public class GBan {
                    public String screen;
                    public String alert;
                    public String id;
                    public String STAFF;
                    public Reason reason;
                    public Proof[] proofs;
                    public int expire;
                    public String timestamp;
                }

                public class Reason {
                    public String id;
                    public String text;
                }

                public class Proof {
                    public String type;
                    public String data;
                }
            }
        }

        public class CData {
            public String id;
            public CDataType type;
            public String key;
            public Object value;
            public int expires;
            public String timestamp;
        }
    }

    public class System {
        public int status;
        public String message;
    }

    public enum CDataType {
        GROUP,
        PERMISSION,
        PERK,
        PET,
        INVENTORY,
        ITEMSTACK,
        MONEY,
        TEXT
    }
}
