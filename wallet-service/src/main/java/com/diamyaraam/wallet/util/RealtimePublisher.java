// Déclaration du package Java : `com.diamyaraam.wallet.util`
package com.diamyaraam.wallet.util;

// Import de la classe `ObjectMapper` (paquet com.fasterxml.jackson.databind)
import com.fasterxml.jackson.databind.ObjectMapper;
// Import de la classe `Logger` (paquet org.slf4j)
import org.slf4j.Logger;
// Import de la classe `LoggerFactory` (paquet org.slf4j)
import org.slf4j.LoggerFactory;
// Import de la classe `Component` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Component;

// Import de la classe `URI` (paquet java.net)
import java.net.URI;
// Import de la classe `HttpClient` (paquet java.net.http)
import java.net.http.HttpClient;
// Import de la classe `HttpRequest` (paquet java.net.http)
import java.net.http.HttpRequest;
// Import de la classe `HttpResponse` (paquet java.net.http)
import java.net.http.HttpResponse;
// Import de la classe `Duration` (paquet java.time)
import java.time.Duration;
// Import de la classe `Map` (paquet java.util)
import java.util.Map;

// Composant Spring détecté et instancié automatiquement
@Component
// Déclaration de la classe `RealtimePublisher`
public class RealtimePublisher {

    // Constante `log` de type Logger [privée] ; valeur initiale : `LoggerFactory.getLogger(RealtimePublisher.class)`
    private static final Logger log = LoggerFactory.getLogger(RealtimePublisher.class);
    // Attribut `httpClient` de type HttpClient — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final HttpClient httpClient;
    // Attribut `objectMapper` de type ObjectMapper — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final ObjectMapper objectMapper;
    // Attribut `broadcastUrl` de type chaîne de caractères — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final String broadcastUrl;

    // Constructeur de `RealtimePublisher`
    public RealtimePublisher(
            // Paramètre `objectMapper` de type ObjectMapper
            ObjectMapper objectMapper,
            // Suite de la signature de la méthode : @org.springframework.beans.factory.annotation.Value("${ws.broadcast.u…
            @org.springframework.beans.factory.annotation.Value("${ws.broadcast.url:http://localhost:8084/ws-broadcast}") String broadcastUrl
    ) {
        // Initialise l'attribut `objectMapper` avec la valeur de objectMapper
        this.objectMapper = objectMapper;
        // Initialise l'attribut `broadcastUrl` avec la valeur de broadcastUrl
        this.broadcastUrl = broadcastUrl;
        // Initialise l'attribut `httpClient` avec `HttpClient.newBuilder()`
        this.httpClient = HttpClient.newBuilder()
                // Enchaînement : appelle `connectTimeout(Duration.ofSeconds(2))`
                .connectTimeout(Duration.ofSeconds(2))
                // Enchaînement : appelle `build();`
                .build();
    }

    // Méthode `publish` (publique) — paramètres : `destination` (chaîne de caractères), `type` (chaîne de caractères), `data` (objet générique) ; retourne : aucune valeur ; intention : publie (publish)
    public void publish(String destination, String type, Object data) {
        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Déclare la variable `body` (dictionnaire clé/valeur) initialisée avec `Map.of(`
            Map<String, Object> body = Map.of(
                    // Paire clé/valeur : clé « destination » associée à la valeur de destination
                    "destination", destination,
                    // Paire clé/valeur : clé « type » associée à la valeur de type
                    "type", type,
                    // Paire clé/valeur : clé « data » associée à la valeur de data
                    "data", data
            );
            // Déclare la variable `json` (chaîne de caractères) initialisée avec `objectMapper.writeValueAsString(body)`
            String json = objectMapper.writeValueAsString(body);
            // Déclare la variable `request` (HttpRequest) initialisée avec `HttpRequest.newBuilder()`
            HttpRequest request = HttpRequest.newBuilder()
                    // Enchaînement : appelle `uri(URI.create(this.broadcastUrl))`
                    .uri(URI.create(this.broadcastUrl))
                    // Enchaînement : appelle `header("Content-Type", "application/json")`
                    .header("Content-Type", "application/json")
                    // Enchaînement : appelle `POST(HttpRequest.BodyPublishers.ofString(json))`
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    // Enchaînement : appelle `timeout(Duration.ofSeconds(2))`
                    .timeout(Duration.ofSeconds(2))
                    // Enchaînement : appelle `build();`
                    .build();

            // Appelle la méthode `sendAsync` sur `httpClient` : httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    // Enchaînement : appelle `whenComplete((res, err) -> {`
                    .whenComplete((res, err) -> {
                        // Condition : exécute le bloc suivant seulement si `err != null`
                        if (err != null) {
                            // Écrit un message de débogage dans les journaux : "Realtime publish failed: {}", err.getMessage());
                            log.debug("Realtime publish failed: {}", err.getMessage());
                        // Sinon (cas contraire de la condition précédente)
                        } else {
                            // Écrit un message informatif dans les journaux : "📡 [RealtimePublisher/Wallet] Événement diffusé vers {} ({})", destin…
                            log.info("📡 [RealtimePublisher/Wallet] Événement diffusé vers {} ({})", destination, type);
                        }
                    });
        // Interception de l'exception Exception e
        } catch (Exception e) {
            // Écrit un message de débogage dans les journaux : "Erreur broadcast: {}", e.getMessage());
            log.debug("Erreur broadcast: {}", e.getMessage());
        }
    }
}
