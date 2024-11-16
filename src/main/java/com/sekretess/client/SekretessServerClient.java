package com.sekretess.client;

import com.google.gson.Gson;
import com.sekretess.client.request.SendMessage;
import com.sekretess.client.response.ConsumerKeysResponse;
import com.sekretess.client.response.SendMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class SekretessServerClient {

    private HttpClient httpClient;
    private final String businessServerUrl;
    private final String consumerServerUrl;

    private static final Logger logger = LoggerFactory.getLogger(SekretessServerClient.class);

    public SekretessServerClient(HttpClient httpClient,
                                 @Value("${app.config.server.business.url}") String businessServerUrl,
                                 @Value("${app.config.server.consumer.url}") String consumerServerUrl) {
        this.httpClient = httpClient;
        this.businessServerUrl = businessServerUrl;
        this.consumerServerUrl = consumerServerUrl;
    }

    public String sendMessage(String sender, String text, String consumer) throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(new SendMessage(text, sender, consumer))))
                .uri(URI.create(businessServerUrl + "/api/v1/businesses/messages"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            logger.error("Failed to send text message to consumer! {}", consumer);
            throw new RuntimeException("Failed to send text message to consumer!" + consumer);
        } else {
            logger.info("Successfully forwarded message for consumer! {}", consumer);
            Gson gson = new Gson();
            SendMessageResponse sendMessageResponse = gson.fromJson(response.body(), SendMessageResponse.class);
            return sendMessageResponse.ik();
        }

    }

    public ConsumerKeysResponse getConsumerKeys(String consumer) throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(consumerServerUrl + "/api/v1/consumers/" + consumer + "/key-bundles"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (HttpStatus.OK.value() == response.statusCode()) {
            Gson gson = new Gson();
            ConsumerKeysResponse consumerKeysResponse = gson.fromJson(response.body(), ConsumerKeysResponse.class);
            logger.info("Received response from server for consumer: {}, {}", consumer, consumerKeysResponse);
            return consumerKeysResponse;
        } else {
            logger.error("Exception happened! {}", response.statusCode());
            throw new RuntimeException();
        }
    }

}
