package frontoffice.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import frontoffice.models.Reclamation;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ReclamationService {
    private final ApiClient apiClient = new ApiClient();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public CompletableFuture<HttpResponse<String>> submit(Reclamation reclamation) {
        try {
            String body = mapper.writeValueAsString(reclamation);
            return apiClient.sendWithRetry("/api/v1/reclamations", "POST", body);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<List<Reclamation>> getMy() {
        return apiClient.sendWithRetry("/api/v1/reclamations/my", "GET", null)
                .thenApply(this::parseList);
    }

    private List<Reclamation> parseList(HttpResponse<String> response) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            try {
                return mapper.readValue(response.body(), new TypeReference<>() {
                });
            } catch (Exception ignored) {
            }
        }
        return List.of();
    }
}
