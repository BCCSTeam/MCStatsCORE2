package net.mcstats2.core.network.web;

import com.google.gson.Gson;
import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.MCSShutdownAble;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class ScheduledRequestBuilder implements MCSShutdownAble {
    private Timer t = new Timer();

    private RequestBuilder request;
    private RequestMethod method;
    private long delay;
    private int max;

    private ArrayList<RequestBuilder> queue = new ArrayList<>();

    private long last_run = 0;
    private long next_run;

    public ScheduledRequestBuilder(RequestBuilder request, RequestMethod method) {
        this(request, method, 1000);
    }

    public ScheduledRequestBuilder(RequestBuilder request, RequestMethod method, long delay) {
        this(request, method, delay, 50);
    }

    public ScheduledRequestBuilder(RequestBuilder request, RequestMethod method, long delay, int max) {
        if (request == null)
            throw new NullPointerException();

        this.request = request;
        this.method = method == null ? RequestMethod.GET : method;
        this.delay = delay < 1000 ? 1000 : delay;
        this.max = max;

        next_run = System.currentTimeMillis() + delay;

        schedule();
    }

    private void schedule() {
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if (queue.size() < max || System.currentTimeMillis() < next_run)
                    return;

                forcePush();
            }
        }, 500, 500);
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMax() {
        return max;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getDelay() {
        return delay;
    }

    public long getNext_run() {
        return next_run;
    }

    public void forcePush() {
        if (queue.size() == 0)
            return;

        last_run = System.currentTimeMillis();
        next_run = last_run + delay;

        if (request.getURL().equals(MCSCore.getInstance().getAuthedRequest("/chatlog/push").getURL()))
            System.out.println("PUSH2: " + new Gson().toJson(queue));

        queue.forEach(request -> {
            try {
                switch (method) {
                    case GET:
                        request.get();
                        break;
                    case POST:
                        request.post();
                        break;
                    case PUT:
                        request.put();
                        break;
                    case DELETE:
                        request.delete();
                        break;
                    default:
                        throw new UnsupportedOperationException(method + " is not Supported in a ScheduledRequestBuilder");
                }
            } catch (IOException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        queue.clear();
        queue = new ArrayList<>();
    }

    public boolean addToQueue(Entry entry) {
        try {
             RequestBuilder rb = (RequestBuilder) request.clone();

            rb.mergeHeaders(entry.headers);
            rb.mergeParams(entry.params);

            return queue.add(rb);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return false;
    }
    public boolean addToQueue(ArrayList<Entry> entries) {
        return addToQueue(entries.toArray(new ScheduledRequestBuilder.Entry[0]));
    }
    public boolean addToQueue(Entry[] entries) {
        try {
            RequestBuilder rb = (RequestBuilder) request.clone();

            if (rb.getURL().equals(MCSCore.getInstance().getAuthedRequest("/chatlog/push").getURL())) {
                System.out.println("CLONE(O): " + new Gson().toJson(request));
                System.out.println("CLONE(C): " + new Gson().toJson(rb));
                System.out.println("QUEUED2: " + new Gson().toJson(entries));
            }

            for (Entry entry : entries) {
                rb.mergeHeaders(entry.headers);
                rb.mergeParams(entry.params);
            }

            return queue.add(rb);
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public void cancel() {
        t.cancel();
    }

    public Entry createEntry() {
        return new Entry();
    }

    @Override
    public void shutdown() {
        forcePush();
        cancel();
    }

    public static class Entry {
        private HashMap<String, Object> headers = new HashMap<>();
        private HashMap<String, Object> params = new HashMap<>();

        public boolean putHeader(String key, Object value) {
            headers.put(key, value);

            return headers.containsKey(key) && headers.get(key).equals(value);
        }

        public boolean removeHeader(String key) {
            headers.remove(key);

            return !headers.containsKey(key);
        }


        public boolean putParam(String key, Object value) {
            params.put(key, value);

            return params.containsKey(key) && params.get(key).equals(value);
        }

        public boolean removeParam(String key) {
            params.remove(key);

            return !params.containsKey(key);
        }
    }
}
