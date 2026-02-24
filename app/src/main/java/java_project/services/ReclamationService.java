package java_project.services;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

import java.util.concurrent.CompletionException;

public class ReclamationService {
    private final ApiClient apiClient = new ApiClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final String PDFMONKEY_URL = "https://api.pdfmonkey.io/api/v1/documents";
    private static final String PDFMONKEY_TOKEN = "zwAfCwQwXHnwZusdRAnt";
    // PDFMonkey document template id (provided)
    private static final String PDFMONKEY_TEMPLATE_ID = "085664AC-1FBB-40A5-A506-E7B13FC4AA24";

    public CompletableFuture<HttpResponse<String>> getAllReclamations() {
        return apiClient.sendWithRetry("/api/v1/reclamations/all", "GET", null);
    }

    public CompletableFuture<HttpResponse<String>> addReclamation(String jsonBody) {
        return apiClient.sendWithRetry("/api/v1/reclamations", "POST", jsonBody);
    }

    public CompletableFuture<HttpResponse<String>> resolveReclamation(int id, String responseText) {
        // Encoding the response string for the URL query parameter
        String encodedResponse = URLEncoder.encode(responseText, StandardCharsets.UTF_8);
        String endpoint = "/api/v1/reclamations/" + id + "/resolve?response=" + encodedResponse;
        return apiClient.sendWithRetry(endpoint, "PATCH", null);
    }

    /**
     * Fetches reclamation details (handles auth/refresh via ApiClient) and forwards
     * a document creation request to PDFMonkey. Returns the PDFMonkey HTTP response.
     */
    public CompletableFuture<HttpResponse<String>> exportReclamationAsPdf(int id) {
        return apiClient.sendWithRetry("/api/v1/reclamations/" + id + "/details", "GET", null)
                .thenCompose(getResp -> {
                    if (getResp.statusCode() != 200) {
                        return CompletableFuture.failedFuture(new RuntimeException("Details fetch failed: " + getResp.statusCode()));
                    }

                    try {
                        Object jsonObj = mapper.readValue(getResp.body(), Object.class);
                        String payloadString = mapper.writeValueAsString(jsonObj);

                        Map<String, Object> documentBody = Map.of(
                            "payload", payloadString,
                            "document_template_id", PDFMONKEY_TEMPLATE_ID,
                            "output_type", "pdf"
                        );
                        Map<String, Object> doc = Map.of("document", documentBody);
                        String postBody = mapper.writeValueAsString(doc);

                        HttpClient client = HttpClient.newHttpClient();
                        HttpRequest postReq = HttpRequest.newBuilder()
                                .uri(URI.create(PDFMONKEY_URL))
                                .header("Authorization", "Bearer " + PDFMONKEY_TOKEN)
                                .header("Content-Type", "application/json")
                                .POST(BodyPublishers.ofString(postBody, StandardCharsets.UTF_8))
                                .build();

                        return client.sendAsync(postReq, BodyHandlers.ofString(StandardCharsets.UTF_8));
                    } catch (Exception e) {
                        return CompletableFuture.failedFuture(new CompletionException(e));
                    }
                });
    }
}