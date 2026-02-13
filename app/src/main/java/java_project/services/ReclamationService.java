package java_project.services;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ReclamationService {
    private final ApiClient apiClient = new ApiClient();

    public CompletableFuture<HttpResponse<String>> getAllReclamations() {
        return apiClient.sendWithRetry("/api/v1/reclamations/all", "GET", null);
    }

    public CompletableFuture<HttpResponse<String>> addReclamation(String jsonBody) {
        return apiClient.sendWithRetry("/api/v1/reclamations", "POST", jsonBody);
    }

    public CompletableFuture<HttpResponse<String>> resolveReclamation(int id, String responseText) {
        // Encoding the response string for the URL query parameter
        String encodedResponse = URLEncoder.encode(responseText, StandardCharsets.UTF_8);
        String endpoint = "/api/v1/reclamations/" + id + "/resolve?response=" + encodedResponse;
        return apiClient.sendWithRetry(endpoint, "PATCH", null);
    }
}