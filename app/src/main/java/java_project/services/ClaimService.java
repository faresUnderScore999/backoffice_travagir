package java_project.services;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ClaimService {
    private final ApiClient apiClient;

    public ClaimService() {
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
     * Get all claims for the logged-in user.
     * Endpoint: GET /api/v1/user-offers/my-claims
     * Requires JWT token
     */
    public CompletableFuture<HttpResponse<String>> getMyClams() {
        return apiClient.sendWithRetry("/api/v1/user-offers/my-claims", "GET", null);
    }

    /**
     * Update claim status. Admin only.
     * Endpoint: PATCH /api/v1/user-offers/{id}/status?status={status}
     * Example: PATCH /api/v1/user-offers/2/status?status=USED
     */
    public CompletableFuture<HttpResponse<String>> updateClaimStatus(int claimId, String status) {
        return apiClient.sendWithRetry("/api/v1/user-offers/" + claimId + "/status?status=" + status, "PATCH", null);
    }

    /**
     * Delete a claim. Allowed for Owner or Admin.
     * Endpoint: DELETE /api/v1/user-offers/{id}
     */
    public CompletableFuture<HttpResponse<String>> deleteClaim(int claimId) {
        return apiClient.sendWithRetry("/api/v1/user-offers/" + claimId, "DELETE", null);
    }
}
