package net.mcstats2.core.modules.chatlog.uploader;

import com.google.gson.Gson;
import net.mcstats2.core.MCSCore;
import net.mcstats2.core.modules.chatlog.data.ChatLogData;
import net.mcstats2.core.network.web.RequestBuilder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MCStatsChatUploader implements ChatUploader {


    @Override
    public void uploadChat(List<ChatLogData> queue) {
        try {
            String data = new Gson().toJson(queue);
            RequestBuilder rb = MCSCore.getInstance().getAuthedRequest("/chatlog/push");
            rb.putParam("data", data);
            rb.post();
        } catch (IOException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }
    }
}


