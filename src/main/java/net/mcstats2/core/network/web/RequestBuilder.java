package net.mcstats2.core.network.web;

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

public class RequestBuilder {
    private String request;
    private HashMap<String, Object> headers = new HashMap<>();
    private HashMap<String, Object> params = new HashMap<>();

    private HttpAsyncClientBuilder client = HttpAsyncClients.custom();

    public RequestBuilder(String request) {
        this.request = request;
    }

    private List<NameValuePair> createParams() {
        List<NameValuePair> values = new ArrayList<NameValuePair>();
        for (Map.Entry<String, Object> arg: params.entrySet())
            values.add(new BasicNameValuePair(arg.getKey(), arg.getValue().toString()));
        return values;
    }

    public boolean putParam(String key, Object value) {
        params.put(key, value);

        return params.containsKey(key) && params.get(key).equals(value);
    }

    public boolean removeParam(String key) {
        params.remove(key);

        return !params.containsKey(key);
    }

    public boolean putHeader(String key, Object value) {
        headers.put(key, value);

        return headers.containsKey(key) && headers.get(key).equals(value);
    }

    public boolean removeHeader(String key) {
        headers.remove(key);

        return !headers.containsKey(key);
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
}
