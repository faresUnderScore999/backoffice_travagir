package java_project.services;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ActivityService {
    private final ApiClient apiClient = new ApiClient();

    public CompletableFuture<HttpResponse<String>> getAllActivities() {
        return apiClient.sendWithRetry("/api/v1/activities", "GET", null);
    }

    public CompletableFuture<HttpResponse<String>> getActivitiesByVoyage(int voyageId) {
        return apiClient.sendWithRetry("/api/v1/activities/voyage/" + voyageId, "GET", null);
    }

    public CompletableFuture<HttpResponse<String>> addActivity(String jsonBody) {
        return apiClient.sendWithRetry("/api/v1/activities", "POST", jsonBody);
    }

    public CompletableFuture<HttpResponse<String>> updateActivity(int id, String jsonBody) {
        return apiClient.sendWithRetry("/api/v1/activities/" + id, "PUT", jsonBody);
    }

    public CompletableFuture<HttpResponse<String>> deleteActivity(int id) {
        return apiClient.sendWithRetry("/api/v1/activities/" + id, "DELETE", null);
    }
}