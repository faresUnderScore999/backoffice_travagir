package java_project.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java_project.models.LoginRequest;
import java_project.models.LoginResponse;
import java_project.utils.SessionManager;
import java_project.config.EnvVariable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class AuthService {
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String API_URL = EnvVariable.baseUrl + "/api/v1/admins/login";

    public HttpResponse<String> login(String email, String password) throws Exception {
        LoginRequest loginData = new LoginRequest(email, password);
        String jsonBody = objectMapper.writeValueAsString(loginData);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // Parse the response into our LoginResponse model
            LoginResponse data = objectMapper.readValue(response.body(), LoginResponse.class);

            // Save to global session
            SessionManager.getInstance().setSession(
                    data.tokens().accessToken(),
                    data.tokens().refreshToken(),
                    data.user());
        }
        return response;
    }

    public CompletableFuture<Boolean> refreshAccessToken() {
        String refreshToken = SessionManager.getInstance().getRefreshToken();
        if (refreshToken == null)
            return CompletableFuture.completedFuture(false);

        try {
            String jsonBody = objectMapper.writeValueAsString(java.util.Map.of("refreshToken", refreshToken));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(EnvVariable.baseUrl + "/api/v1/admins/refresh"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // Use sendAsync instead of send
            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                java.util.Map<String, String> data = objectMapper.readValue(response.body(),
                                        new com.fasterxml.jackson.core.type.TypeReference<>() {
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
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        System.err.println("Refresh failed. Status: " + response.statusCode());
                        return false;
                    });
        } catch (Exception e) {
            return CompletableFuture.completedFuture(false);
        }
    }
}