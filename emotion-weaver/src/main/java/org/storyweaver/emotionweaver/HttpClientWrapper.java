package org.storyweaver.emotionweaver;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONObject;

public class HttpClientWrapper {
    private final HttpClient client;

    public HttpClientWrapper() {
        this.client = HttpClient.newHttpClient();
    }

    public void sendTxt2ImgRequest(String prompt, JSONObject payload) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:7860/sdapi/v1/txt2img"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP request failed with status code: " + response.statusCode());
        }
    }
}