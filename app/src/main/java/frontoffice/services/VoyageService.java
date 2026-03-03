package frontoffice.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import frontoffice.models.Voyage;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class VoyageService {
    private final ApiClient apiClient = new ApiClient();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public CompletableFuture<List<Voyage>> getAllVoyages() {
        return apiClient.sendWithRetry("/api/v1/voyages", "GET", null)
                .thenApply(this::parseList);
    }

    public CompletableFuture<List<Voyage>> searchVoyages(String title) {
        String encoded = URLEncoder.encode(title == null ? "" : title, StandardCharsets.UTF_8);
        return apiClient.sendWithRetry("/api/v1/voyages/search?title=" + encoded, "GET", null)
                .thenApply(this::parseList);
    }

    private List<Voyage> parseList(HttpResponse<String> response) {
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
