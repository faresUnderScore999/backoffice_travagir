package frontoffice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import frontoffice.models.UserRegistration;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class UserService {
    private final ApiClient apiClient = new ApiClient();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public CompletableFuture<HttpResponse<String>> register(UserRegistration registration) {
        try {
            String json = mapper.writeValueAsString(registration);
            return apiClient.sendWithRetry("/api/v1/users/create", "POST", json);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
