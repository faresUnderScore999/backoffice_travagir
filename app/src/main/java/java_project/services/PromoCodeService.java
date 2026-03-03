package java_project.services;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class PromoCodeService {

    private final ApiClient apiClient;

    public PromoCodeService() {
        this.apiClient = new ApiClient();
    }

    /**
     * Fetches all promo codes.
     * Endpoint: GET /api/v1/promo-codes
     */
    public CompletableFuture<HttpResponse<String>> getAllPromoCodes() {
        return apiClient.sendWithRetry("/api/v1/promo-codes", "GET", null);
    }

    /**
     * Fetches promo codes by offer ID.
     * Endpoint: GET /api/v1/promo-codes/offer/{offerId}
     */
    public CompletableFuture<HttpResponse<String>> getPromoCodesByOfferId(int offerId) {
        return apiClient.sendWithRetry("/api/v1/promo-codes/offer/" + offerId, "GET", null);
    }

    /**
     * Adds a new promo code.
     * Endpoint: POST /api/v1/promo-codes
     */
    public CompletableFuture<HttpResponse<String>> addPromoCode(String jsonBody) {
        return apiClient.sendWithRetry("/api/v1/promo-codes", "POST", jsonBody);
    }

    /**
     * Updates an existing promo code by ID.
     * Endpoint: PUT /api/v1/promo-codes/{id}
     */
    public CompletableFuture<HttpResponse<String>> updatePromoCode(int promoCodeId, String jsonBody) {
        return apiClient.sendWithRetry("/api/v1/promo-codes/" + promoCodeId, "PUT", jsonBody);
    }

    /**
     * Deletes a promo code by ID.
     * Endpoint: DELETE /api/v1/promo-codes/{id}
     */
    public CompletableFuture<HttpResponse<String>> deletePromoCode(int promoCodeId) {
        return apiClient.sendWithRetry("/api/v1/promo-codes/" + promoCodeId, "DELETE", null);
    }

    /**
     * Validates a promo code.
     * Endpoint: POST /api/v1/promo-codes/validate?code={code}
     */
    public CompletableFuture<HttpResponse<String>> validatePromoCode(String code) {
        String encodedCode = URLEncoder.encode(code, StandardCharsets.UTF_8);
        return apiClient.sendWithRetry("/api/v1/promo-codes/validate?code=" + encodedCode, "POST", null);
    }
}
