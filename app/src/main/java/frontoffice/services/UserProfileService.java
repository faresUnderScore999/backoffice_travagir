package frontoffice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import frontoffice.models.UserProfile;

import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class UserProfileService {
    private final ApiClient apiClient = new ApiClient();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public CompletableFuture<UserProfile> getMe() {
        return apiClient.sendWithRetry("/api/v1/users/me", "GET", null)
                .thenApply(resp -> {
                    if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                        try {
                            return mapper.readValue(resp.body(), UserProfile.class);
                        } catch (Exception ignored) {
                        }
                    }
                    return null;
                });
    }

    public CompletableFuture<HttpResponse<String>> updateProfile(int userId, Map<String, Object> updateFields) {
        try {
            String json = mapper.writeValueAsString(updateFields);
            return apiClient.sendWithRetry("/api/v1/users/" + userId, "PUT", json);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
