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
     * Endpoint: GET /api/v1/promocodes
     */
    public CompletableFuture<HttpResponse<String>> getAllPromoCodes() {
        return apiClient.sendWithRetry("/api/v1/promocodes", "GET", null);
    }

    /**
     * Adds a new promo code.
     * Endpoint: POST /api/v1/promocodes
     */
    public CompletableFuture<HttpResponse<String>> addPromoCode(String jsonBody) {
        return apiClient.sendWithRetry("/api/v1/promocodes", "POST", jsonBody);
    }

    /**
     * Updates an existing promo code by ID.
     * Endpoint: PUT /api/v1/promocodes/{id}
     */
    public CompletableFuture<HttpResponse<String>> updatePromoCode(int id, String jsonBody) {
        return apiClient.sendWithRetry("/api/v1/promocodes/" + id, "PUT", jsonBody);
    }

    /**
     * Deletes a promo code by ID.
     * Endpoint: DELETE /api/v1/promocodes/{id}
     */
    public CompletableFuture<HttpResponse<String>> deletePromoCode(int id) {
        return apiClient.sendWithRetry("/api/v1/promocodes/" + id, "DELETE", null);
    }
}
