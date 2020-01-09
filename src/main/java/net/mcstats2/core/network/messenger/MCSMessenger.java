package net.mcstats2.core.network.messenger;

import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class MCSMessenger {
    private ArrayList<Consumer<Object>> receivers = new ArrayList();

    public abstract void send(String receiver, Object message);

    private void receive(String receiver, Object message) {
        receivers.forEach(f -> f.accept(message));
    }

    public void addReceiver(Consumer<Object> receiver) {
        receivers.add(receiver);
    }
}
