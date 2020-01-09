package net.mcstats2.core.modules.chatlog.uploader;

import net.mcstats2.core.modules.chatlog.data.ChatLogData;

import java.util.List;

public interface ChatUploader {

    void uploadChat(List<ChatLogData> queue);
}