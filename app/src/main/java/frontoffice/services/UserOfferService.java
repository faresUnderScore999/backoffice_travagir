package frontoffice.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import frontoffice.models.UserOffer;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class UserOfferService {
    private final ApiClient apiClient = new ApiClient();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public CompletableFuture<HttpResponse<String>> claimOffer(int offerId) {
        try {
            String body = mapper.writeValueAsString(Map.of("offerId", offerId));
            return apiClient.sendWithRetry("/api/v1/user-offers/claim", "POST", body);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<List<UserOffer>> getMyClaims() {
        return apiClient.sendWithRetry("/api/v1/user-offers/my-claims", "GET", null)
                .thenApply(this::parseList);
    }

    public CompletableFuture<HttpResponse<String>> deleteClaim(int id) {
        return apiClient.sendWithRetry("/api/v1/user-offers/" + id, "DELETE", null);
    }

    private List<UserOffer> parseList(HttpResponse<String> response) {
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
