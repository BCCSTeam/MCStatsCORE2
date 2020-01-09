package net.mcstats2.core.modules.chatlog;

import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.MCSShutdownAble;
import net.mcstats2.core.modules.chatlog.data.ChatLogData;
import net.mcstats2.core.modules.chatlog.uploader.ChatUploader;
import net.mcstats2.core.modules.chatlog.uploader.MCStatsChatUploader;
import net.mcstats2.core.network.web.RequestBuilder;
import net.mcstats2.core.network.web.RequestResponse;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ChatLog implements MCSShutdownAble {
    private MCSCore core;

    private Timer timer = new Timer();

    private long delay = 30000;
    private long last_add = System.currentTimeMillis();
    private long next_add = last_add + delay;
    private ArrayList<ChatLogData> entries = new ArrayList<>();
    private ChatUploader uploader = new MCStatsChatUploader();

    public ChatLog() {
        core = MCSCore.getInstance();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (entries.size() < 25 && System.currentTimeMillis() < next_add)
                    return;

                uploadChat();
            }
        }, 500, 500);
    }

    public String createLog(UUID[] targets, UUID staff, String reason, int amount) {
        try {
            uploadChat();


            RequestBuilder rb = core.getAuthedRequest("/chatlog/create");

            rb.putParam("targets", targets.toString());

            if (staff != null)
                rb.putParam("STAFF", staff.toString());
            if (reason != null)
                rb.putParam("reason", reason);

            rb.putParam("amount", amount);

            RequestResponse rr = rb.post();

            if (rr.getStatusCode() != 200)
                return null;
            else
                System.out.println(rr.getStatusLine().getReasonPhrase());


        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public boolean log(ChatLogData data) {
        return entries.add(data);
    }

    @Override
    public void shutdown() {
        uploadChat();
        timer.cancel();
    }

    private void uploadChat() {
        last_add = System.currentTimeMillis();
        next_add = last_add + delay;

        if (entries.size() == 0)
            return;

        ArrayList<ChatLogData> queue = new ArrayList<>();
        queue.addAll(entries);

        entries.clear();
        uploader.uploadChat(queue);
    }
}
