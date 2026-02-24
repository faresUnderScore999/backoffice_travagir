package java_project.services;

import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class VoyageImageService {
    private final ApiClient apiClient;

    public VoyageImageService() {
        this.apiClient = new ApiClient();
    }

    public CompletableFuture<HttpResponse<String>> uploadImage(int voyageId, Path filePath) {
        return apiClient.sendMultipartWithRetry("/api/v1/voyage-images/" + voyageId + "/upload", "file", filePath);
    }

    public CompletableFuture<HttpResponse<String>> getImagesForVoyage(int voyageId) {
        return apiClient.sendWithRetry("/api/v1/voyage-images/" + voyageId, "GET", null);
    }

    public CompletableFuture<HttpResponse<String>> getImageById(int imageId) {
        return apiClient.sendWithRetry("/api/v1/voyage-images/image/" + imageId, "GET", null);
    }

    public CompletableFuture<HttpResponse<String>> deleteImage(int imageId) {
        return apiClient.sendWithRetry("/api/v1/voyage-images/" + imageId, "DELETE", null);
    }

    public CompletableFuture<HttpResponse<String>> getAllImages() {
        return apiClient.sendWithRetry("/api/v1/voyage-images/all", "GET", null);
    }
}
