package java_project.services;

import java_project.utils.SessionManager;
import java_project.config.EnvVariable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ApiClient {
    private final HttpClient client = HttpClient.newHttpClient();
    private final AuthService authService = new AuthService(); // For refresh logic



public CompletableFuture<HttpResponse<String>> sendWithRetry(String endpoint, String method, String body) {
    return sendRequest(endpoint, method, body).thenCompose(response -> {
        // 1. If unauthorized AND we aren't already trying to refresh
        if (response.statusCode() == 401 ) {
            
            // 2. Call the async refresh
            return authService.refreshAccessToken().thenCompose(success -> {
                if (success) {
                    // 3. If refresh worked, retry the original request
                    return sendRequest(endpoint, method, body);
                } else {
                    // 4. If refresh failed, send the user back to Login
                   
                    return CompletableFuture.completedFuture(response);
                }
            });
        }
        return CompletableFuture.completedFuture(response);
    });
}

private CompletableFuture<HttpResponse<String>> sendRequest(String endpoint, String method, String body) {
    String token = SessionManager.getInstance().getAccessToken();
    
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(EnvVariable.baseUrl + endpoint))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json");

    // Handle the body: if it's null, we use an empty publisher
    HttpRequest.BodyPublisher bodyPublisher = (body == null || body.isEmpty()) 
            ? HttpRequest.BodyPublishers.noBody() 
            : HttpRequest.BodyPublishers.ofString(body);

    switch (method.toUpperCase()) {
        case "GET":
            requestBuilder.GET();
            break;
        case "POST":
            requestBuilder.POST(bodyPublisher);
            break;
        case "PUT":
            requestBuilder.PUT(bodyPublisher);
            break;
        case "DELETE":
            requestBuilder.method("DELETE", bodyPublisher);
            break;
        case "PATCH":
            // Java's HttpClient supports PATCH via the generic .method() call
            requestBuilder.method("PATCH", bodyPublisher);
            break;
        default:
            throw new IllegalArgumentException("Unsupported HTTP method: " + method);
    }

    return client.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
}}