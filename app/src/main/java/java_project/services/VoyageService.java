package java_project.services;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class VoyageService {

    private final ApiClient apiClient;

    public VoyageService() {
        this.apiClient = new ApiClient();
    }

    /**
     * Fetch all voyages.
     * Endpoint: GET /api/v1/voyages
     */
    public CompletableFuture<HttpResponse<String>> getAllVoyages() {
        return apiClient.sendWithRetry("/api/v1/voyages", "GET", null);
    }

    /**
     * Create a new voyage.
     * Endpoint: POST /api/v1/voyages
     */
    public CompletableFuture<HttpResponse<String>> createVoyage(String jsonBody) {
        return apiClient.sendWithRetry("/api/v1/voyages", "POST", jsonBody);
    }

    /**
     * Update an existing voyage.
     * Endpoint: PUT /api/v1/voyages/{id}
     */
    public CompletableFuture<HttpResponse<String>> updateVoyage(int voyageId, String jsonBody) {
        return apiClient.sendWithRetry("/api/v1/voyages/" + voyageId, "PUT", jsonBody);
    }

    /**
     * Delete a voyage by ID.
     * Endpoint: DELETE /api/v1/voyages/{id}
     */
    public CompletableFuture<HttpResponse<String>> deleteVoyage(int voyageId) {
        return apiClient.sendWithRetry("/api/v1/voyages/" + voyageId, "DELETE", null);
    }

    /**
     * Fetch a single voyage by ID (if needed).
     * Endpoint: GET /api/v1/voyages/{id}
     */
    public CompletableFuture<HttpResponse<String>> getVoyageById(int voyageId) {
        return apiClient.sendWithRetry("/api/v1/voyages/" + voyageId, "GET", null);
    }
}