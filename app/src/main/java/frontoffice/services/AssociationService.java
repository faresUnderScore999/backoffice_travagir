package frontoffice.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import frontoffice.models.Association;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AssociationService {
    private final ApiClient apiClient = new ApiClient();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public CompletableFuture<List<Association>> getAll() {
        return apiClient.sendWithRetry("/api/v1/associations/all", "GET", null)
                .thenApply(this::parseList);
    }

    public CompletableFuture<Association> getMyAssociation() {
        return apiClient.sendWithRetry("/api/v1/associations/my", "GET", null)
                .thenApply(this::parseObject);
    }

    private List<Association> parseList(HttpResponse<String> response) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            try {
                return mapper.readValue(response.body(), new TypeReference<>() {
                });
            } catch (Exception ignored) {
            }
        }
        return List.of();
    }

    private Association parseObject(HttpResponse<String> response) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            try {
                return mapper.readValue(response.body(), Association.class);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
