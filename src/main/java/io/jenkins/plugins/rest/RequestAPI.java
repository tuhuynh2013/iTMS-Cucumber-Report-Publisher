package io.jenkins.plugins.rest;

import hidden.jth.org.apache.http.HttpEntity;
import hidden.jth.org.apache.http.HttpResponse;
import hidden.jth.org.apache.http.client.methods.HttpGet;
import hidden.jth.org.apache.http.client.methods.HttpPost;
import hidden.jth.org.apache.http.entity.StringEntity;
import hidden.jth.org.apache.http.impl.client.CloseableHttpClient;
import hidden.jth.org.apache.http.impl.client.HttpClientBuilder;
import net.sf.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static io.jenkins.plugins.model.ITMSConsts.GET_CYCLE_URL;
import static io.jenkins.plugins.model.ITMSConsts.URL_CONJUNCT;


public class RequestAPI {

    private String baseUrl;
    private CloseableHttpClient httpClient;

    public RequestAPI(String baseUrl) {
        this.baseUrl = baseUrl;
        httpClient = HttpClientBuilder.create().build();
    }

    public StandardResponse createPOSTRequest(JSONObject postData) {
        StandardResponse response = new StandardResponse();
        try {
            StringEntity params = new StringEntity(postData.toString());
            HttpPost request = new HttpPost(baseUrl);
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse httpResponse = httpClient.execute(request);
            response = readResponse(httpResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }


    public StandardResponse getCycleName(String projectId) {
        StandardResponse response = new StandardResponse();
        try {
            URI uri = new URI(baseUrl);
            baseUrl = uri.getScheme() + URL_CONJUNCT + uri.getAuthority() + GET_CYCLE_URL + projectId;

            HttpGet request = new HttpGet(baseUrl);
            HttpResponse httpResponse = httpClient.execute(request);
            response = readResponse(httpResponse);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return response;
    }


    private StandardResponse readResponse(HttpResponse httpResponse) {
        StringBuilder sb = null;
        try {
            HttpEntity entity = httpResponse.getEntity();
            InputStream is = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1), 8);
            sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line + "\n");
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert sb != null;
        return new StandardResponse(httpResponse.getStatusLine().getStatusCode(),
                httpResponse.getStatusLine().getReasonPhrase(), sb.toString());
    }

}