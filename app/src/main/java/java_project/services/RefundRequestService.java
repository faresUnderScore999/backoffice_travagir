package java_project.services;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class RefundRequestService {

    private final ApiClient apiClient = new ApiClient();

    // ADMIN: get all refunds
    public CompletableFuture<HttpResponse<String>> getAllRefunds() {
        return apiClient.sendWithRetry("/api/v1/refunds/all", "GET", null);
    }

    // USER: get my refunds
    public CompletableFuture<HttpResponse<String>> getMyRefunds() {
        return apiClient.sendWithRetry("/api/v1/refunds/my", "GET", null);
    }

    // USER: create refund (jsonBody contains reclamationId, amount, reason)
    public CompletableFuture<HttpResponse<String>> addRefund(String jsonBody) {
        return apiClient.sendWithRetry("/api/v1/refunds", "POST", jsonBody);
    }

    // ADMIN: approve
    public CompletableFuture<HttpResponse<String>> approveRefund(int id) {
        String endpoint = "/api/v1/refunds/" + id + "/approve";
        return apiClient.sendWithRetry(endpoint, "PATCH", null);
    }

    // ADMIN: reject
    public CompletableFuture<HttpResponse<String>> rejectRefund(int id) {
        String endpoint = "/api/v1/refunds/" + id + "/reject";
        return apiClient.sendWithRetry(endpoint, "PATCH", null);
    }

    // USER: delete pending (owner only)
    public CompletableFuture<HttpResponse<String>> deleteRefund(int id) {
        String endpoint = "/api/v1/refunds/" + id;
        return apiClient.sendWithRetry(endpoint, "DELETE", null);
    }
}
