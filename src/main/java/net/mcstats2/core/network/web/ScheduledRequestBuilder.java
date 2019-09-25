package net.mcstats2.core.network.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class ScheduledRequestBuilder {
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
                boolean run = false;
                if (queue.size() < max || System.currentTimeMillis() < next_run)
                    return;

                forcePush();
            }
        }, 500, 500);
    }

    public void forcePush() {
        if (queue.size() == 0)
            return;

        last_run = System.currentTimeMillis();
        next_run = last_run + delay;

        ArrayList<RequestBuilder> q = (ArrayList<RequestBuilder>) queue.clone();
        queue.clear();

        q.forEach(request -> {
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
                }
            } catch (IOException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean addToQueue(Entry entry) {
        try {
            RequestBuilder request = this.request.clone();

            request.mergeHeaders(entry.headers);
            request.mergeParams(entry.params);

            return queue.add(request);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return false;
    }
    public boolean addToQueue(Entry[] entries) {
        try {
            RequestBuilder request = this.request.clone();

            for (Entry entry : entries) {
                request.mergeHeaders(entry.headers);
                request.mergeParams(entry.params);
            }

            return queue.add(request);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void cancel() {
        t.cancel();
    }

    public Entry createEntry() {
        return new Entry();
    }

    public class Entry {
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
