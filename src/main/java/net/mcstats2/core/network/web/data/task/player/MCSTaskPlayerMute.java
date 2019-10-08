package net.mcstats2.core.network.web.data.task.player;

import net.mcstats2.core.MCSCore;
import net.mcstats2.core.network.web.data.task.MCSTask;

import java.sql.SQLException;
import java.util.UUID;

public class MCSTaskPlayerMute implements MCSTask {
    private java.util.UUID UUID;
    private UUID STAFF;
    private String templateID;

    public UUID getUUID() {
        return UUID;
    }

    public UUID getSTAFF() {
        return STAFF;
    }

    public String getTemplateID() {
        return templateID;
    }

    public MCSCore.MuteTemplate getTemplate() throws SQLException {
        if (getTemplateID() == null)
            return null;

        return MCSCore.getInstance().getMuteTemplateByID(getTemplateID());
    }
}
