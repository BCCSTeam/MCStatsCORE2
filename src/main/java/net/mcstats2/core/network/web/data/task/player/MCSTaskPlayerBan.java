package net.mcstats2.core.network.web.data.task.player;

import net.mcstats2.core.MCSCore;
import net.mcstats2.core.network.web.data.task.MCSTask;

import java.sql.SQLException;
import java.util.UUID;

public class MCSTaskPlayerBan implements MCSTask {
    private UUID UUID;
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

    public MCSCore.BanTemplate getTemplate() throws SQLException {
        if (getTemplateID() == null)
            return null;

        return MCSCore.getInstance().getBanTemplateByID(getTemplateID());
    }
}
