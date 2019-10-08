package net.mcstats2.core.network.web.data.task.server;

import net.mcstats2.core.network.web.data.task.MCSTask;

import java.util.List;

public class MCSTaskServerBanCreateReason implements MCSTask {
    private String name;
    private String reason;
    private int power;
    private List<Integer> expires;

    public String getName() {
        return name;
    }

    public String getReason() {
        return reason;
    }

    public int getPower() {
        return power;
    }

    public List<Integer> getExpires() {
        return expires;
    }
}
