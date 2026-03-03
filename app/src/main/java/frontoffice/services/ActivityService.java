package frontoffice.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import frontoffice.models.Activity;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ActivityService {
    private final ApiClient apiClient = new ApiClient();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public CompletableFuture<List<Activity>> getByVoyage(int voyageId) {
        return apiClient.sendWithRetry("/api/v1/activities/voyage/" + voyageId, "GET", null)
                .thenApply(this::parseList);
    }

    private List<Activity> parseList(HttpResponse<String> response) {
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
