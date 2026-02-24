package java_project.services;

import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
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
        String encodedStatus = URLEncoder.encode(status, StandardCharsets.UTF_8);
        String endpoint = "/api/v1/reservations/" + id + "/status?status=" + encodedStatus;
        return apiClient.sendWithRetry(endpoint, "PATCH", null);
    }
}