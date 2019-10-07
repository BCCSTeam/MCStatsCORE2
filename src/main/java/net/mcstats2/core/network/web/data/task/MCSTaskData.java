package net.mcstats2.core.network.web.data.task;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.mcstats2.core.exceptions.MCSError;

public class MCSTaskData {
    private String id;
    private MCSTaskType type;
    private JsonElement task;
    private long expire;
    private String timestamp;

    public String getId() {
        return id;
    }

    public MCSTaskType getType() {
        return type;
    }

    public MCSTask getTask() throws MCSError {
        if (type.getClassName() == null)
            throw new MCSError("Unsupported TaskType(" + getType() + "), maybe outdated?");

        Class klass = getObjectClass(type.getClassName());
        return (MCSTask) new Gson().fromJson(task, klass);
    }

    public long getExpire() {
        return expire;
    }

    public String getTimestamp() {
        return timestamp;
    }

    private Class getObjectClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new JsonParseException(e.getMessage());
        }
    }
}
