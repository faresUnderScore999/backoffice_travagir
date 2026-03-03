package frontoffice.services;

import frontoffice.config.EnvVariable;
import frontoffice.utils.SessionManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ApiClient {
    private final HttpClient client = HttpClient.newHttpClient();
    private final AuthService authService = new AuthService();

    public CompletableFuture<HttpResponse<String>> sendWithRetry(String endpoint, String method, String body) {
        return sendRequest(endpoint, method, body).thenCompose(response -> {
            if (response.statusCode() == 401 && SessionManager.getInstance().getRefreshToken() != null) {
                return authService.refreshAccessToken().thenCompose(success -> {
                    if (success) {
                        return sendRequest(endpoint, method, body);
                    }
                    return CompletableFuture.completedFuture(response);
                });
            }
            return CompletableFuture.completedFuture(response);
        });
    }

    public CompletableFuture<HttpResponse<String>> sendMultipartWithRetry(String endpoint, String fieldName, java.nio.file.Path filePath) {
        return sendMultipartRequest(endpoint, fieldName, filePath).thenCompose(response -> {
            if (response.statusCode() == 401 && SessionManager.getInstance().getRefreshToken() != null) {
                return authService.refreshAccessToken().thenCompose(success -> {
                    if (success) {
                        return sendMultipartRequest(endpoint, fieldName, filePath);
                    }
                    return CompletableFuture.completedFuture(response);
                });
            }
            return CompletableFuture.completedFuture(response);
        });
    }

    private CompletableFuture<HttpResponse<String>> sendMultipartRequest(String endpoint, String fieldName, java.nio.file.Path filePath) {
        try {
            String boundary = "----JavaBoundary" + java.util.UUID.randomUUID().toString();
            byte[] fileBytes = java.nio.file.Files.readAllBytes(filePath);
            String filename = filePath.getFileName().toString();

            String partHeader = "--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + filename + "\"\r\n"
                    + "Content-Type: application/octet-stream\r\n\r\n";
            String end = "\r\n--" + boundary + "--\r\n";

            byte[] headerBytes = partHeader.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] endBytes = end.getBytes(java.nio.charset.StandardCharsets.UTF_8);

            byte[] all = new byte[headerBytes.length + fileBytes.length + endBytes.length];
            System.arraycopy(headerBytes, 0, all, 0, headerBytes.length);
            System.arraycopy(fileBytes, 0, all, headerBytes.length, fileBytes.length);
            System.arraycopy(endBytes, 0, all, headerBytes.length + fileBytes.length, endBytes.length);

            String token = SessionManager.getInstance().getAccessToken();

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(EnvVariable.baseUrl + endpoint))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(all));

            if (token != null && !token.isBlank()) {
                builder.header("Authorization", "Bearer " + token);
            }

            HttpRequest request = builder.build();
            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            CompletableFuture<HttpResponse<String>> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    private CompletableFuture<HttpResponse<String>> sendRequest(String endpoint, String method, String body) {
        String token = SessionManager.getInstance().getAccessToken();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(EnvVariable.baseUrl + endpoint))
                .header("Content-Type", "application/json");

        if (token != null && !token.isBlank()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        HttpRequest.BodyPublisher bodyPublisher = (body == null || body.isEmpty())
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);

        switch (method.toUpperCase()) {
            case "GET" -> requestBuilder.GET();
            case "POST" -> requestBuilder.POST(bodyPublisher);
            case "PUT" -> requestBuilder.PUT(bodyPublisher);
            case "DELETE" -> requestBuilder.method("DELETE", bodyPublisher);
            case "PATCH" -> requestBuilder.method("PATCH", bodyPublisher);
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        return client.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
