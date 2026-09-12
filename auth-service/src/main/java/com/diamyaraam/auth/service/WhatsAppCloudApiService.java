package com.diamyaraam.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Service d'envoi automatique d'OTP via l'API officielle Meta WhatsApp Cloud.
 * Endpoint officiel : https://graph.facebook.com/v19.0/{PHONE_NUMBER_ID}/messages
 */
@Service
public class WhatsAppCloudApiService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppCloudApiService.class);

    private final RestTemplate restTemplate;

    @Value("${whatsapp.api.enabled:true}")
    private boolean enabled;

    @Value("${whatsapp.api.url:https://graph.facebook.com/v19.0}")
    private String apiUrl;

    @Value("${whatsapp.api.phone-number-id:}")
    private String phoneNumberId;

    @Value("${whatsapp.api.token:}")
    private String apiToken;

    public WhatsAppCloudApiService() {
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
        if (!enabled) {
            log.info("[WHATSAPP DESACTIVE] Message OTP non envoyé pour {}", rawPhone);
            return true;
        }

        // 1. Normaliser le numéro au format international E.164 sans '+' ni espaces
        String cleanPhone = normalizePhoneNumber(rawPhone);

        // 2. Si les identifiants Meta ne sont pas encore renseignés, effectuer une simulation structurée
        if (phoneNumberId == null || phoneNumberId.isBlank() || apiToken == null || apiToken.isBlank()) {
            log.info("================================================================================");
            log.info("[WHATSAPP CLOUD API - SIMULATION MODE]");
            log.info("Destinataire WhatsApp : +{}", cleanPhone);
            log.info("Message : 🟢 Diam-Yaraam Santé : Votre code de vérification est : {} (valable {} min).", code, validityMinutes);
            log.info("Note : Renseignez 'whatsapp.api.phone-number-id' et 'whatsapp.api.token' dans application.properties pour l'envoi réel en direct.");
            log.info("================================================================================");
            return true;
        }

        // 3. Appel réel à Meta Graph API
        try {
            String endpoint = String.format("%s/%s/messages", apiUrl.trim(), phoneNumberId.trim());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiToken.trim());

            String messageText = String.format(
                    "🟢 *Diam-Yaraam Santé*\n\nVotre code de vérification WhatsApp pour activer votre compte est :\n👉 *%s*\n\nCe code est valable pendant %d minutes. Ne le partagez avec personne pour votre sécurité.",
                    code, validityMinutes
            );

            Map<String, Object> textBody = new HashMap<>();
            textBody.put("preview_url", false);
            textBody.put("body", messageText);

            Map<String, Object> payload = new HashMap<>();
            payload.put("messaging_product", "whatsapp");
            payload.put("recipient_type", "individual");
            payload.put("to", cleanPhone);
            payload.put("type", "text");
            payload.put("text", textBody);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            log.info("[WHATSAPP CLOUD API] Envoi OTP vers +{} via {}", cleanPhone, endpoint);
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[WHATSAPP CLOUD API] Message envoyé avec succès à +{} (HTTP {})", cleanPhone, response.getStatusCode());
                return true;
            } else {
                log.warn("[WHATSAPP CLOUD API] Réponse inattendue de Meta pour +{} : HTTP {}", cleanPhone, response.getStatusCode());
                return false;
            }

        } catch (org.springframework.web.client.HttpStatusCodeException httpEx) {
            log.error("[WHATSAPP CLOUD API] Erreur HTTP Meta {} : {}", httpEx.getStatusCode(), httpEx.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.error("[WHATSAPP CLOUD API] Erreur inattendue lors de l'appel Meta Graph API pour +{} : {}", cleanPhone, e.getMessage());
            return false;
        }
    }

    /**
     * Normalise un numéro en format chiffres purs sans préfixe '+' (ex: '+221 77 123 45 67' -> '221771234567')
     */
    private String normalizePhoneNumber(String phone) {
        if (phone == null) return "";
        String clean = phone.replaceAll("[^0-9]", "");
        if (clean.startsWith("00")) {
            clean = clean.substring(2);
        }
        // Si numéro sénégalais local à 9 chiffres sans 221 (ex: 771234567)
        if (clean.length() == 9 && (clean.startsWith("7") || clean.startsWith("3"))) {
            clean = "221" + clean;
        }
        return clean;
    }
}
