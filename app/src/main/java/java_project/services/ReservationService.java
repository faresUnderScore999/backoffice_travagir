package java_project.services;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ReservationService {
    private final ApiClient apiClient;

    public ReservationService() {
        this.apiClient = new ApiClient();
    }

    public CompletableFuture<HttpResponse<String>> getAllReservations() {
        return apiClient.sendWithRetry("/api/v1/reservations/all", "GET", null);
    }

    public CompletableFuture<HttpResponse<String>> updateStatus(int id, String status) {
        String json = "{\"status\":\"" + status + "\"}";
        return apiClient.sendWithRetry("/api/v1/reservations/" + id + "/status", "PATCH", json);
    }
}