package java_project.services;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class UserOfferService {
    private final ApiClient apiClient;

    public UserOfferService() {
        this.apiClient = new ApiClient();
    }

    /**
     * Claim an offer. User ID is pulled from JWT.
     * Endpoint: POST /api/v1/user-offers/claim
     * Body: { "offerId": int }
     */
    public CompletableFuture<HttpResponse<String>> claimOffer(int offerId) {
        String json = "{\"offerId\":" + offerId + "}";
        return apiClient.sendWithRetry("/api/v1/user-offers/claim", "POST", json);
    }

    /**
     * Get all user offers/claims for the logged-in user.
     * Endpoint: GET /api/v1/user-offers/my-claims
     * Requires JWT token
     */
    public CompletableFuture<HttpResponse<String>> getMyUserOffers() {
        return apiClient.sendWithRetry("/api/v1/user-offers/my-claims", "GET", null);
    }

    /**
     * Update user offer status. Admin only.
     * Endpoint: PATCH /api/v1/user-offers/{id}/status?status={status}
     * Example: PATCH /api/v1/user-offers/2/status?status=USED
     */
    public CompletableFuture<HttpResponse<String>> updateUserOfferStatus(int userOfferId, String status) {
        return apiClient.sendWithRetry("/api/v1/user-offers/" + userOfferId + "/status?status=" + status, "PATCH", null);
    }

    /**
     * Delete a user offer. Allowed for Owner or Admin.
     * Endpoint: DELETE /api/v1/user-offers/{id}
     */
    public CompletableFuture<HttpResponse<String>> deleteUserOffer(int userOfferId) {
        return apiClient.sendWithRetry("/api/v1/user-offers/" + userOfferId, "DELETE", null);
    }
}
