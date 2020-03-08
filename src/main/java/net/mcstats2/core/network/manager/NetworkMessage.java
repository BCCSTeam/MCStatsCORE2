package net.mcstats2.core.network.manager;

import cloud.timo.TimoCloud.api.messages.objects.PluginMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NetworkMessage {
    private String type;
    private Map<String, Object> data;

    public NetworkMessage(String type) {
        this(type, new HashMap());
    }

    public NetworkMessage(String type, Map<String, Object> data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Object getObject(String key) {
        return getData().get(key);
    }

    public Set<String> getKeys() {
        return getData().keySet();
    }

    public Integer getInteger(String key) {
        Object object = getObject(key);
        return object == null ? null : ((Number)object).intValue();
    }

    public Long getLong(String key) {
        Object object = getObject(key);
        return object == null ? null : ((Number)object).longValue();
    }

    public Short getShort(String key) {
        Object object = getObject(key);
        return object == null ? null : ((Number)object).shortValue();
    }

    public Double getDouble(String key) {
        Object object = getObject(key);
        return object == null ? null : ((Number)object).doubleValue();
    }

    public Float getFloat(String key) {
        Object object = getObject(key);
        return object == null ? null : ((Number)object).floatValue();
    }

    public String getString(String key) {
        return (String)getObject(key);
    }

    public NetworkMessage setType(String type) {
        this.type = type;
        return this;
    }

    public NetworkMessage set(String key, Object value) {
        getData().put(key, value);
        return this;
    }
}
