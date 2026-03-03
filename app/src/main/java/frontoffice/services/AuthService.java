package frontoffice.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import frontoffice.config.EnvVariable;
import frontoffice.models.LoginRequest;
import frontoffice.models.LoginResponse;
import frontoffice.utils.SessionManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AuthService {
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public HttpResponse<String> login(String email, String password) throws Exception {
        LoginRequest loginData = new LoginRequest(email, password);
        String jsonBody = objectMapper.writeValueAsString(loginData);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(EnvVariable.baseUrl + "/api/v1/users/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            LoginResponse data = objectMapper.readValue(response.body(), LoginResponse.class);
            SessionManager.getInstance().setSession(
                    data.tokens().accessToken(),
                    data.tokens().refreshToken(),
                    data.user());
        }

        return response;
    }

    public CompletableFuture<Boolean> refreshAccessToken() {
        String refreshToken = SessionManager.getInstance().getRefreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            return CompletableFuture.completedFuture(false);
        }

        try {
            String jsonBody = objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(EnvVariable.baseUrl + "/api/v1/users/refresh"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                Map<String, String> data = objectMapper.readValue(response.body(),
                                        new TypeReference<>() {
                                        });

                                String newAccess = data.get("accessToken");
                                String newRefresh = data.get("refreshToken");

                                if (newAccess != null && newRefresh != null) {
                                    SessionManager.getInstance().setSession(
                                            newAccess,
                                            newRefresh,
                                            SessionManager.getInstance().getCurrentUser());
                                    return true;
                                }
                            } catch (Exception ignored) {
                            }
                        }
                        return false;
                    });
        } catch (Exception e) {
            return CompletableFuture.completedFuture(false);
        }
    }
}
