package java_project.services;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class UserService {

    private final ApiClient apiClient;

    public UserService() {
        this.apiClient = new ApiClient();
    }

    /**
     * Adds a new user to the system.
     * Uses the public creation endpoint defined in SecurityConfig.
     */
    public CompletableFuture<HttpResponse<String>> addUser(String jsonBody) {
        // Endpoint matched from: .requestMatchers("/api/v1/users/create").permitAll()
        return apiClient.sendWithRetry("/api/v1/users/create", "POST", jsonBody);
    }

    /**
     * Updates an existing user's information.
     * Requires authentication as per SecurityConfig.
     */
    public CompletableFuture<HttpResponse<String>> updateUser(String userId, String jsonBody) {
        // Endpoint matched from: .requestMatchers(HttpMethod.PUT,
        // "/api/v1/users/{id}").authenticated()
        return apiClient.sendWithRetry("/api/v1/users/" + userId, "PUT", jsonBody);
    }

    /**
     * Deletes a user by ID.
     * Requires ADMIN role as per SecurityConfig.
     */
    public CompletableFuture<HttpResponse<String>> deleteUser(String userId) {
        // Endpoint matched from: .requestMatchers(HttpMethod.DELETE,
        // "/api/v1/users/{id}").hasRole("ADMIN")
        return apiClient.sendWithRetry("/api/v1/users/" + userId, "DELETE", null);
    }

    /**
     * Fetches all users.
     * Requires ADMIN role as per SecurityConfig.
     */
    public CompletableFuture<HttpResponse<String>> getAllUsers() {
        // Endpoint matched from: .requestMatchers("/api/v1/users/all").hasRole("ADMIN")
        return apiClient.sendWithRetry("/api/v1/users/all", "GET", null);
    }

    public CompletableFuture<HttpResponse<String>> getUserDocument(int userId) {
        return apiClient.sendWithRetry("/api/v1/user-documents/" + userId, "GET", null);
    }

    public CompletableFuture<HttpResponse<String>> saveUserDocument(String jsonBody) {
        return apiClient.sendWithRetry("/api/v1/user-documents/save", "POST", jsonBody);
    }

    /**
     * Fetches a specific user's details by email.
     * Requires appropriate admin permissions as per backend security.
     */
    public CompletableFuture<HttpResponse<String>> getUserByEmail(String email) {
        // Endpoint: /api/v1/users/{email}
        return apiClient.sendWithRetry("/api/v1/users/" + email, "GET", null);
    }
}