package com.diamyaraam.dossier.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Component
public class RealtimePublisher {

    private static final Logger log = LoggerFactory.getLogger(RealtimePublisher.class);
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private static final String BROADCAST_URL = "http://localhost:8084/ws-broadcast";

    public RealtimePublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    public void publish(String destination, String type, Object data) {
        try {
            Map<String, Object> body = Map.of(
                    "destination", destination,
                    "type", type,
                    "data", data
            );
            String json = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BROADCAST_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(2))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .whenComplete((res, err) -> {
                        if (err != null) {
                            log.debug("Realtime publish failed: {}", err.getMessage());
                        } else {
                            log.info("📡 [RealtimePublisher/Dossier] Événement diffusé vers {} ({})", destination, type);
                        }
                    });
        } catch (Exception e) {
            log.debug("Erreur broadcast: {}", e.getMessage());
        }
    }
}
