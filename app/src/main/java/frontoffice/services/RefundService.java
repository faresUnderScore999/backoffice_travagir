package frontoffice.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import frontoffice.models.RefundRequest;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RefundService {
    private final ApiClient apiClient = new ApiClient();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public CompletableFuture<HttpResponse<String>> submit(RefundRequest request) {
        try {
            String body = mapper.writeValueAsString(request);
            return apiClient.sendWithRetry("/api/v1/refunds", "POST", body);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<List<RefundRequest>> getMy() {
        return apiClient.sendWithRetry("/api/v1/refunds/my", "GET", null)
                .thenApply(this::parseList);
    }

    public CompletableFuture<HttpResponse<String>> delete(int id) {
        return apiClient.sendWithRetry("/api/v1/refunds/" + id, "DELETE", null);
    }

    private List<RefundRequest> parseList(HttpResponse<String> response) {
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
