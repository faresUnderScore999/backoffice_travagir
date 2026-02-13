package java_project.services;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class OfferService {

    private final ApiClient apiClient;

    public OfferService() {
        this.apiClient = new ApiClient();
    }

    /**
     * Fetches all offers.
     * Endpoint: GET /api/v1/offers
     */
    public CompletableFuture<HttpResponse<String>> getAllOffers() {
        return apiClient.sendWithRetry("/api/v1/offers", "GET", null);
    }

    /**
     * Searches for offers by title.
     * Endpoint: GET /api/v1/offers/search?title={title}
     */
    public CompletableFuture<HttpResponse<String>> searchOffersByTitle(String title) {
        // Encode the title to handle spaces and special characters in the URL
        String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
        return apiClient.sendWithRetry("/api/v1/offers/search?title=" + encodedTitle, "GET", null);
    }

    /**
     * Adds a new offer.
     * Endpoint: POST /api/v1/offers
     */
    public CompletableFuture<HttpResponse<String>> addOffer(String jsonBody) {
        return apiClient.sendWithRetry("/api/v1/offers", "POST", jsonBody);
    }

    /**
     * Updates an existing offer by ID.
     * Endpoint: PUT /api/v1/offers/{id}
     */
    public CompletableFuture<HttpResponse<String>> updateOffer(int offerId, String jsonBody) {
        return apiClient.sendWithRetry("/api/v1/offers/" + offerId, "PUT", jsonBody);
    }

    /**
     * Deletes an offer by ID.
     * Endpoint: DELETE /api/v1/offers/{id}
     */
    public CompletableFuture<HttpResponse<String>> deleteOffer(int offerId) {
        return apiClient.sendWithRetry("/api/v1/offers/" + offerId, "DELETE", null);
    }
}