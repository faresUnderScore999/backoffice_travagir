package java_project.services;

import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Generic file upload service.  In the current project this is primarily used
 * for avatar images, but it wraps the {@link ApiClient} multipart helper so the
 * controller code stays thin and testable.
 */
public class UploadService {
    private final ApiClient apiClient;

    public UploadService() {
        this.apiClient = new ApiClient();
    }

    /**
     * Upload a single file using the backend's &quot;upload/image&quot; endpoint.
     * The caller is responsible for parsing the response JSON.
     *
     * @param filePath path to the file to send
     * @return future holding the http response body as a string
     */
    public CompletableFuture<HttpResponse<String>> uploadImage(Path filePath) {
        return apiClient.sendMultipartWithRetry("/api/v1/upload/image?file", "file", filePath);
    }
}
