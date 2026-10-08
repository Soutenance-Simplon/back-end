// Déclaration du package Java : `com.diamyaraam.auth.service`
package com.diamyaraam.auth.service;

// Import de la classe `Logger` (paquet org.slf4j)
import org.slf4j.Logger;
// Import de la classe `LoggerFactory` (paquet org.slf4j)
import org.slf4j.LoggerFactory;
// Import de la classe `Value` (paquet org.springframework.beans.factory.annotation)
import org.springframework.beans.factory.annotation.Value;
// Import de la classe `HttpEntity` (paquet org.springframework.http)
import org.springframework.http.HttpEntity;
// Import de la classe `HttpHeaders` (paquet org.springframework.http)
import org.springframework.http.HttpHeaders;
// Import de la classe `MediaType` (paquet org.springframework.http)
import org.springframework.http.MediaType;
// Import de la classe `ResponseEntity` (paquet org.springframework.http)
import org.springframework.http.ResponseEntity;
// Import de la classe `Service` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Service;
// Import de la classe `RestTemplate` (paquet org.springframework.web.client)
import org.springframework.web.client.RestTemplate;

// Import de la classe `HashMap` (paquet java.util)
import java.util.HashMap;
// Import de la classe `Map` (paquet java.util)
import java.util.Map;

/**
 * ====================================================================================================
 * SERVICE D'INTÉGRATION TIERS : EXPÉDITION WHATSAPP CLOUD API (META GRAPH API)
 * ====================================================================================================
 * 
 * 🎓 JUSTIFICATION TECHNIQUE POUR LA SOUTENANCE :
 * Pourquoi intégrer WhatsApp plutôt qu'une passerelle SMS classique (ex: Twilio / Orange SMS) ?
 * 
 * 1. Contexte Local Sénégalais & Ouest-Africain :
 *    WhatsApp est l'application de communication la plus utilisée au Sénégal.
 *    Les messages WhatsApp sont gratuits pour les utilisateurs receveurs et ne dépendent pas
 *    de la couverture réseau GSM SMS (fonctionne sur Wi-Fi même sans crédit téléphonique).
 * 
 * 2. API Officielle Meta Graph Cloud (v19.0) :
 *    Communication sécurisée en HTTPS via `POST https://graph.facebook.com/v19.0/{PHONE_NUMBER_ID}/messages`
 *    avec authentification par Bearer Token longue durée.
 * 
 * 3. Normalisation E.164 Intelligente :
 *    Prise en compte automatique de l'indicatif téléphonique sénégalais (+221) :
 *    si un utilisateur saisit "77 123 45 67", le service normalise en "221771234567" requis par l'API Meta.
 * 
 * 4. Mode Hybride Résilient (Simulation / Production) :
 *    Si les clés API Meta ne sont pas configurées en environnement de développement ou de soutenance,
 *    le service simule l'envoi avec succès dans les logs pour ne jamais bloquer le jury ou les tests.
 * ====================================================================================================
 */
@Service
// Déclaration de la classe `WhatsAppCloudApiService` (rôle : porte la logique métier)
public class WhatsAppCloudApiService {

    // Constante `log` de type Logger [privée] ; valeur initiale : `LoggerFactory.getLogger(WhatsAppCloudApiService.class)`
    private static final Logger log = LoggerFactory.getLogger(WhatsAppCloudApiService.class);

    // Attribut `restTemplate` de type RestTemplate — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final RestTemplate restTemplate;

    /** Active ou désactive l'envoi de messages */
    @Value("${whatsapp.api.enabled:true}")
    // Attribut `enabled` de type booléen [privée]
    private boolean enabled;

    /** URL de base de l'API Graph Meta */
    @Value("${whatsapp.api.url:https://graph.facebook.com/v19.0}")
    // Attribut `apiUrl` de type chaîne de caractères [privée]
    private String apiUrl;

    /** Identifiant de ligne téléphonique professionnelle WhatsApp Business */
    @Value("${whatsapp.api.phone-number-id:}")
    // Attribut `phoneNumberId` de type chaîne de caractères [privée]
    private String phoneNumberId;

    /** Jeton d'accès Bearer fourni par Meta Business Manager */
    @Value("${whatsapp.api.token:}")
    // Attribut `apiToken` de type chaîne de caractères [privée]
    private String apiToken;

    /**
     * Initialisation du client HTTP RestTemplate.
     */
    public WhatsAppCloudApiService() {
        // Initialise l'attribut `restTemplate` avec une nouvelle instance de RestTemplate
        this.restTemplate = new RestTemplate();
    }


    /**
     * Envoie un code OTP par message WhatsApp au numéro indiqué.
     *
     * @param rawPhone        Numéro du destinataire (ex: +221 77 123 45 67)
     * @param code            Code OTP à 6 chiffres
     * @param validityMinutes Durée de validité en minutes
     * @return true si l'envoi (ou la simulation) s'est déroulé sans erreur
     */
    public boolean sendOtpMessage(String rawPhone, String code, int validityMinutes) {
        // Condition : exécute le bloc suivant seulement si `!enabled`
        if (!enabled) {
            // Écrit un message informatif dans les journaux : "[WHATSAPP DESACTIVE] Message OTP non envoyé pour {}", rawPhone);
            log.info("[WHATSAPP DESACTIVE] Message OTP non envoyé pour {}", rawPhone);
            // Retourne le booléen vrai
            return true;
        }

        // 1. Normaliser le numéro au format international E.164 sans '+' ni espaces
        String cleanPhone = normalizePhoneNumber(rawPhone);

        // 2. Si les identifiants Meta ne sont pas encore renseignés, effectuer une simulation structurée
        if (phoneNumberId == null || phoneNumberId.isBlank() || apiToken == null || apiToken.isBlank()) {
            // Écrit un message informatif dans les journaux : "====================================================================…
            log.info("================================================================================");
            // Écrit un message informatif dans les journaux : "[WHATSAPP CLOUD API - SIMULATION MODE]");
            log.info("[WHATSAPP CLOUD API - SIMULATION MODE]");
            // Écrit un message informatif dans les journaux : "Destinataire WhatsApp : +{}", cleanPhone);
            log.info("Destinataire WhatsApp : +{}", cleanPhone);
            // Écrit un message informatif dans les journaux : "Message : 🟢 Diam-Yaraam Santé : Votre code de vérification est : {} …
            log.info("Message : 🟢 Diam-Yaraam Santé : Votre code de vérification est : {} (valable {} min).", code, validityMinutes);
            // Écrit un message informatif dans les journaux : "Note : Renseignez 'whatsapp.api.phone-number-id' et 'whatsapp.api.to…
            log.info("Note : Renseignez 'whatsapp.api.phone-number-id' et 'whatsapp.api.token' dans application.properties pour l'envoi réel en direct.");
            // Écrit un message informatif dans les journaux : "====================================================================…
            log.info("================================================================================");
            // Retourne le booléen vrai
            return true;
        }

        // 3. Appel réel à Meta Graph API
        try {
            // Déclare la variable `endpoint` (chaîne de caractères) initialisée avec `String.format("%s/%s/messages", apiUrl.trim(), phoneNumberId.trim())`
            String endpoint = String.format("%s/%s/messages", apiUrl.trim(), phoneNumberId.trim());

            // Déclare la variable `headers` (HttpHeaders) initialisée avec une nouvelle instance de HttpHeaders
            HttpHeaders headers = new HttpHeaders();
            // Renseigne la propriété ContentType de `headers` avec la valeur de MediaType.APPLICATION_JSON
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Renseigne la propriété BearerAuth de `headers` avec `apiToken.trim()`
            headers.setBearerAuth(apiToken.trim());

            // Déclare la variable `messageText` (chaîne de caractères) initialisée avec `String.format(`
            String messageText = String.format(
                    // Paire clé/valeur : clé « 🟢 *Diam-Yaraam Santé*nnVotre code de vé… » associée à 
                    "🟢 *Diam-Yaraam Santé*\n\nVotre code de vérification WhatsApp pour activer votre compte est :\n👉 *%s*\n\nCe code est valable pendant %d minutes. Ne le partagez avec personne pour votre sécurité.",
                    // Suite de l'instruction précédente : code, validityMinutes
                    code, validityMinutes
            );

            // Déclare la variable `textBody` (dictionnaire clé/valeur) initialisée avec une nouvelle instance de HashMap<>
            Map<String, Object> textBody = new HashMap<>();
            // Appelle la méthode `put` sur `textBody` : textBody.put("preview_url", false);
            textBody.put("preview_url", false);
            // Appelle la méthode `put` sur `textBody` : textBody.put("body", messageText);
            textBody.put("body", messageText);

            // Déclare la variable `payload` (dictionnaire clé/valeur) initialisée avec une nouvelle instance de HashMap<>
            Map<String, Object> payload = new HashMap<>();
            // Appelle la méthode `put` sur `payload` : payload.put("messaging_product", "whatsapp");
            payload.put("messaging_product", "whatsapp");
            // Appelle la méthode `put` sur `payload` : payload.put("recipient_type", "individual");
            payload.put("recipient_type", "individual");
            // Appelle la méthode `put` sur `payload` : payload.put("to", cleanPhone);
            payload.put("to", cleanPhone);
            // Appelle la méthode `put` sur `payload` : payload.put("type", "text");
            payload.put("type", "text");
            // Appelle la méthode `put` sur `payload` : payload.put("text", textBody);
            payload.put("text", textBody);

            // Déclare la variable `request` (HttpEntity<Map<String, Object>>) initialisée avec une nouvelle instance de HttpEntity<>
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            // Écrit un message informatif dans les journaux : "[WHATSAPP CLOUD API] Envoi OTP vers +{} via {}", cleanPhone, endpoin…
            log.info("[WHATSAPP CLOUD API] Envoi OTP vers +{} via {}", cleanPhone, endpoint);
            // Déclare la variable `response` (réponse HTTP contenant chaîne de caractères) initialisée avec `restTemplate.postForEntity(endpoint, request, String.class)`
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);

            // Condition : exécute le bloc suivant seulement si `response.getStatusCode().is2xxSuccessful()`
            if (response.getStatusCode().is2xxSuccessful()) {
                // Écrit un message informatif dans les journaux : "[WHATSAPP CLOUD API] Message envoyé avec succès à +{} (HTTP {})", cl…
                log.info("[WHATSAPP CLOUD API] Message envoyé avec succès à +{} (HTTP {})", cleanPhone, response.getStatusCode());
                // Retourne le booléen vrai
                return true;
            // Sinon (cas contraire de la condition précédente)
            } else {
                // Écrit un message d'avertissement dans les journaux : "[WHATSAPP CLOUD API] Réponse inattendue de Meta pour +{} : HTTP {}",…
                log.warn("[WHATSAPP CLOUD API] Réponse inattendue de Meta pour +{} : HTTP {}", cleanPhone, response.getStatusCode());
                // Retourne le booléen faux
                return false;
            }

        // Interception de l'exception org.springframework.web.client.HttpStatusCodeException http…
        } catch (org.springframework.web.client.HttpStatusCodeException httpEx) {
            // Écrit un message d'erreur dans les journaux : "[WHATSAPP CLOUD API] Erreur HTTP Meta {} : {}", httpEx.getStatusCode…
            log.error("[WHATSAPP CLOUD API] Erreur HTTP Meta {} : {}", httpEx.getStatusCode(), httpEx.getResponseBodyAsString());
            // Retourne le booléen faux
            return false;
        // Interception de l'exception Exception e
        } catch (Exception e) {
            // Écrit un message d'erreur dans les journaux : "[WHATSAPP CLOUD API] Erreur inattendue lors de l'appel Meta Graph AP…
            log.error("[WHATSAPP CLOUD API] Erreur inattendue lors de l'appel Meta Graph API pour +{} : {}", cleanPhone, e.getMessage());
            // Retourne le booléen faux
            return false;
        }
    }

    /**
     * Normalise un numéro en format chiffres purs sans préfixe '+' (ex: '+221 77 123 45 67' -> '221771234567')
     */
    private String normalizePhoneNumber(String phone) {
        // Condition : exécute le bloc suivant seulement si `phone == null) return "";`
        if (phone == null) return "";
        // Déclare la variable `clean` (chaîne de caractères) initialisée avec `phone.replaceAll("[^0-9]", "")`
        String clean = phone.replaceAll("[^0-9]", "");
        // Condition : exécute le bloc suivant seulement si `clean.startsWith("00")`
        if (clean.startsWith("00")) {
            // Affecte à `clean` `clean.substring(2)`
            clean = clean.substring(2);
        }
        // Si numéro sénégalais local à 9 chiffres sans 221 (ex: 771234567)
        if (clean.length() == 9 && (clean.startsWith("7") || clean.startsWith("3"))) {
            // Affecte à `clean` `"221" + clean`
            clean = "221" + clean;
        }
        // Retourne la valeur de clean
        return clean;
    }
}
