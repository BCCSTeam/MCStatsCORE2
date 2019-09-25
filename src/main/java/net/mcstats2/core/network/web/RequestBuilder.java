package net.mcstats2.core.network.web;

import net.mcstats2.core.MCSCore;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class RequestBuilder implements Cloneable {
    private String request;
    private HashMap<String, Object> headers = new HashMap<>();
    private HashMap<String, Object> params = new HashMap<>();
    private HashMap<String, ArrayList<Object>> paramsArray = new HashMap<>();

    private HttpAsyncClientBuilder client = HttpAsyncClients.custom();

    public RequestBuilder(String request) {
        this.request = request;

        client.setUserAgent("MCStatsCORE2/" + MCSCore.getInstance().getServer().getDescription().getVersion());
    }


    public void mergeHeaders(HashMap<String, Object> headers) {
        params.forEach(this::putHeader);
    }

    public boolean putHeader(String key, Object value) {
        headers.put(key, value);

        return headers.containsKey(key) && headers.get(key).equals(value);
    }

    public boolean removeHeader(String key) {
        headers.remove(key);

        return !headers.containsKey(key);
    }


    private List<NameValuePair> createParams() {
        List<NameValuePair> values = new ArrayList<>();

        params.forEach((key, data) -> values.add(new BasicNameValuePair(key, data.toString())));
        paramsArray.forEach((key, entry) -> entry.forEach(data -> values.add(new BasicNameValuePair(key, data.toString()))));

        return values;
    }

    public void mergeParams(HashMap<String, Object> params) {
        params.forEach(this::putParam);
    }

    public boolean putParam(String key, Object value) {
        if (key.endsWith("[]"))
            return addParamArray(key, value);
        else
            params.put(key, value);

        return params.containsKey(key) && params.get(key).equals(value);
    }

    public boolean addParamArray(String key, Object value) {
        ArrayList<Object> params = paramsArray.containsKey(key) ? paramsArray.get(key) : new ArrayList<>();
        params.add(value);
        paramsArray.put(key, params);

        return paramsArray.containsKey(key) && params.contains(value);
    }

    public boolean removeParam(String key) {
        params.remove(key);

        return !params.containsKey(key);
    }


    public String getURL() {
        return request;
    }


    public RequestResponse get() throws IOException, InterruptedException, ExecutionException {
        CloseableHttpAsyncClient client = this.client.build();

        String params = "";
        for (NameValuePair value : createParams()) {
            if (params == "")
                params += "?";
            else
                params += "&";

            params += URLEncoder.encode(value.getName(), "UTF-8") + "=" + URLEncoder.encode(value.getValue(), "UTF-8");
        }

        try {
            client.start();

            HttpPost http = new HttpPost(request + params);

            for (Map.Entry<String, Object> arg : headers.entrySet())
                http.addHeader(new BasicHeader(arg.getKey(), arg.getValue().toString()));

            Future<HttpResponse> future = client.execute(http, null);

            return new RequestResponse(future.get());
        } finally {
            client.close();
        }
    }

    public RequestResponse post() throws IOException, InterruptedException, ExecutionException {
        CloseableHttpAsyncClient client = this.client.build();

        try {
            client.start();

            HttpPost http = new HttpPost(request);

            for (Map.Entry<String, Object> arg: headers.entrySet())
                http.addHeader(new BasicHeader(arg.getKey(), arg.getValue().toString()));

            List<NameValuePair> values = createParams();
            http.setEntity(new UrlEncodedFormEntity(values));

            Future<HttpResponse> future = client.execute(http, null);

            return new RequestResponse(future.get());
        } finally {
            client.close();
        }
    }

    public RequestResponse put() throws IOException, InterruptedException, ExecutionException {
        CloseableHttpAsyncClient client = this.client.build();

        try {
            client.start();

            HttpPut http = new HttpPut(request);

            for (Map.Entry<String, Object> arg : headers.entrySet())
                http.addHeader(new BasicHeader(arg.getKey(), arg.getValue().toString()));

            List<NameValuePair> values = createParams();
            http.setEntity(new UrlEncodedFormEntity(values));

            Future<HttpResponse> future = client.execute(http, null);

            return new RequestResponse(future.get());
        } finally {
            client.close();
        }
    }

    public RequestResponse delete() throws IOException, InterruptedException, ExecutionException {
        CloseableHttpAsyncClient client = this.client.build();

        String params = "";
        for (NameValuePair value : createParams()) {
            if (params == "")
                params += "?";
            else
                params += "&";

            params += URLEncoder.encode(value.getName(), "UTF-8") + "=" + URLEncoder.encode(value.getValue(), "UTF-8");
        }

        try {
            client.start();

            HttpDelete http = new HttpDelete(request + params);

            for (Map.Entry<String, Object> arg: headers.entrySet())
                http.addHeader(new BasicHeader(arg.getKey(), arg.getValue().toString()));

            Future<HttpResponse> future = client.execute(http, null);

            return new RequestResponse(future.get());
        } finally {
            client.close();
        }
    }

    public RequestBuilder clone() throws CloneNotSupportedException {
        return (RequestBuilder) super.clone();
    }
}
