package frontoffice.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import frontoffice.models.Reservation;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ReservationService {
    private final ApiClient apiClient = new ApiClient();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public CompletableFuture<List<Reservation>> getMyReservations(int userId) {
        String encoded = URLEncoder.encode(String.valueOf(userId), StandardCharsets.UTF_8);
        return apiClient.sendWithRetry("/api/v1/reservations/my?userId=" + encoded, "GET", null)
                .thenApply(this::parseList);
    }

    public CompletableFuture<HttpResponse<String>> createReservation(Reservation reservation) {
        try {
            String body = mapper.writeValueAsString(reservation);
            return apiClient.sendWithRetry("/api/v1/reservations", "POST", body);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<HttpResponse<String>> cancelReservation(int reservationId) {
        return apiClient.sendWithRetry("/api/v1/reservations/" + reservationId, "DELETE", null);
    }

    public CompletableFuture<HttpResponse<String>> updatePeopleCount(int reservationId, int count) {
        return apiClient.sendWithRetry("/api/v1/reservations/" + reservationId + "/people?count=" + count, "PATCH", null);
    }

    private List<Reservation> parseList(HttpResponse<String> response) {
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
