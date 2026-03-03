package frontoffice.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import frontoffice.models.PromoCode;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for promo code operations in the FrontOffice.
 * Communicates with the backend REST API.
 */
public class PromoCodeService {

    private final ApiClient apiClient = new ApiClient();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    /**
     * Fetches all promo codes associated with a given offer.
     *
     * @param offerId The offer's ID
     * @return CompletableFuture containing a list of PromoCodes
     */
    public CompletableFuture<List<PromoCode>> getPromoCodesByOfferId(int offerId) {
        return apiClient.sendWithRetry("/api/v1/promo-codes/offer/" + offerId, "GET", null)
                .thenApply(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        try {
                            return mapper.readValue(response.body(), new TypeReference<List<PromoCode>>() {
                            });
                        } catch (Exception e) {
                            System.err.println("PromoCodeService: JSON parse error — " + e.getMessage());
                        }
                    }
                    return List.<PromoCode>of();
                });
    }

    /**
     * Validates a promo code against the backend.
     *
     * @param code The promo code string entered by the user
     * @return CompletableFuture containing the raw HttpResponse (200 = valid, 400 =
     *         invalid/expired)
     */
    public CompletableFuture<HttpResponse<String>> validateCode(String code) {
        String encoded = java.net.URLEncoder.encode(code, java.nio.charset.StandardCharsets.UTF_8);
        return apiClient.sendWithRetry("/api/v1/promo-codes/validate?code=" + encoded, "POST", null);
    }

    /**
     * Marks a promo code as used after successful validation.
     *
     * @param code The promo code string
     * @return CompletableFuture containing the raw HttpResponse
     */
    public CompletableFuture<HttpResponse<String>> useCode(String code) {
        String encoded = java.net.URLEncoder.encode(code, java.nio.charset.StandardCharsets.UTF_8);
        return apiClient.sendWithRetry("/api/v1/promo-codes/use?code=" + encoded, "POST", null);
    }
}
