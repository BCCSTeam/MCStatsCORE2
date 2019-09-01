package net.mcstats2.core.network.web;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RequestResponse {
    private HttpResponse response = null;
    private String content = null;

    public RequestResponse(HttpResponse response) {
        this.response = response;
    }

    public StatusLine getStatusLine() {
        return response.getStatusLine();
    }

    public int getStatusCode() {
        return getStatusLine().getStatusCode();
    }

    public HttpEntity getEntity() {
        if (getStatusCode() == 200)
            return response.getEntity();
        return null;
    }

    public String getContent() throws IOException {
        if(content != null)
            return content;

        BufferedReader reader = new BufferedReader(new InputStreamReader(getEntity().getContent()));

        StringBuffer buffer = new StringBuffer();
        int read;
        char[] chars = new char[1024];
        while ((read = reader.read(chars)) != -1)
            buffer.append(chars, 0, read);

        content = buffer.toString();

        if(!content.isEmpty())
            return content;

        return null;
    }

    public JsonObject getContentJsonObject() throws IOException {
        if (getStatusCode()==200) {
            JsonParser parser = new JsonParser();
            JsonElement je = parser.parse(getContent());
            return je.getAsJsonObject();
        }

        return null;
    }
}
