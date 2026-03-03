package frontoffice.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import frontoffice.models.Offer;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OfferService {
    private final ApiClient apiClient = new ApiClient();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public CompletableFuture<List<Offer>> getAllOffers() {
        System.out.println("🔍 DEBUG: OfferService.getAllOffers() called");
        return apiClient.sendWithRetry("/api/v1/offers", "GET", null)
                .thenApply(response -> {
                    System.out.println("🔍 DEBUG: API response status: " + response.statusCode());
                    System.out.println("🔍 DEBUG: API response body: " + response.body());
                    return parseList(response);
                });
    }

    public CompletableFuture<List<Offer>> searchOffers(String title) {
        String encoded = URLEncoder.encode(title == null ? "" : title, StandardCharsets.UTF_8);
        return apiClient.sendWithRetry("/api/v1/offers/search?title=" + encoded, "GET", null)
                .thenApply(this::parseList);
    }

    private List<Offer> parseList(HttpResponse<String> response) {
        System.out.println("🔍 DEBUG: parseList called with status: " + response.statusCode());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            try {
                System.out.println("🔍 DEBUG: Attempting to parse JSON: " + response.body());
                List<Offer> offers = mapper.readValue(response.body(), new TypeReference<>() {
                });
                System.out.println("🔍 DEBUG: Successfully parsed " + offers.size() + " offers");
                return offers;
            } catch (Exception e) {
                System.out.println("🔍 DEBUG: JSON parsing error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("🔍 DEBUG: API returned error status: " + response.statusCode());
        }
        System.out.println("🔍 DEBUG: Returning empty list");
        return List.of();
    }
}
