package net.mcstats2.core.network.manager.drivers;

public abstract class NetworkDriver {

    public abstract void init();

    public abstract boolean test();

    public abstract void receive(Object data);

    public abstract void send(Object data);
}