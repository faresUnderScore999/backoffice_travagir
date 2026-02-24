package java_project.services;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class AssociationService {
    private final ApiClient apiClient;

    public AssociationService() {
        this.apiClient = new ApiClient();
    }

    // Create association
    public CompletableFuture<HttpResponse<String>> createAssociation(String jsonBody) {
        return apiClient.sendWithRetry("/api/v1/associations/create", "POST", jsonBody);
    }

    // Get all associations (admin)
    public CompletableFuture<HttpResponse<String>> getAllAssociations() {
        return apiClient.sendWithRetry("/api/v1/associations/all", "GET", null);
    }

    // Get association for current user
    public CompletableFuture<HttpResponse<String>> getMyAssociation() {
        return apiClient.sendWithRetry("/api/v1/associations/my", "GET", null);
    }

    // Get association by id
    public CompletableFuture<HttpResponse<String>> getAssociationById(int id) {
        return apiClient.sendWithRetry("/api/v1/associations/" + id, "GET", null);
    }

    // Update association
    public CompletableFuture<HttpResponse<String>> updateAssociation(int id, String jsonBody) {
        return apiClient.sendWithRetry("/api/v1/associations/" + id, "PUT", jsonBody);
    }

    // Link user to association
    public CompletableFuture<HttpResponse<String>> linkUser(int userId, int associationId) {
        String body = String.format("{\"userId\":%d,\"associationId\":%d}", userId, associationId);
        return apiClient.sendWithRetry("/api/v1/associations/link", "POST", body);
    }

    // Unlink user from association
    public CompletableFuture<HttpResponse<String>> unlinkUser(int userId, int associationId) {
        String body = String.format("{\"userId\":%d,\"associationId\":%d}", userId, associationId);
        return apiClient.sendWithRetry("/api/v1/associations/unlink", "POST", body);
    }

    // Delete association (optional)
    public CompletableFuture<HttpResponse<String>> deleteAssociation(int id) {
        return apiClient.sendWithRetry("/api/v1/associations/" + id, "DELETE", null);
    }

    // Get users linked to an association
    public CompletableFuture<HttpResponse<String>> getLinkedUsers(int associationId) {
        return apiClient.sendWithRetry("/api/v1/associations/" + associationId + "/users", "GET", null);
    }
}