package java_project.controllers.voyage;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URI;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import java_project.models.Voyage;
import java_project.services.ApiClient;

public class UpdateVoyageController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField destinationField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField priceField;
    @FXML private TextField imageUrlField;

    private final ApiClient apiClient = new ApiClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private int voyageId;

    /**
     * Pre-fills the form with the selected voyage data.
     */
    public void setVoyageData(Voyage voyage) {
        if (voyage == null) {
            return;
        }

        this.voyageId = voyage.getId();
        titleField.setText(voyage.getTitle());
        descriptionArea.setText(voyage.getDescription());
        destinationField.setText(voyage.getDestination());

        if (voyage.getStartDate() != null) {
            startDatePicker.setValue(voyage.getStartDate());
        }
        if (voyage.getEndDate() != null) {
            endDatePicker.setValue(voyage.getEndDate());
        }

        priceField.setText(String.valueOf(voyage.getPrice()));

        // Image URLs: if API/model provides a list, join it; else fall back to single string.
        if (voyage.getImageUrlList() != null && !voyage.getImageUrlList().isEmpty()) {
            imageUrlField.setText(String.join(", ", voyage.getImageUrlList()));
        } else if (voyage.getImageUrl() != null) {
            imageUrlField.setText(voyage.getImageUrl());
        }
    }

    @FXML
    private void handleUpdate() {
        if (!isInputValid()) {
            return;
        }

        List<String> validImages = parseValidImageUrls(imageUrlField != null ? imageUrlField.getText() : null);

        String jsonBody;
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", voyageId);
            payload.put("title", titleField.getText());
            payload.put("description", descriptionArea.getText());
            payload.put("destination", destinationField.getText());
            payload.put("startDate", startDatePicker.getValue().format(DateTimeFormatter.ISO_DATE));
            payload.put("endDate", endDatePicker.getValue().format(DateTimeFormatter.ISO_DATE));
            payload.put("price", Double.parseDouble(priceField.getText()));
            if (!validImages.isEmpty()) {
                payload.put("imageUrl", validImages);
            }
            jsonBody = mapper.writeValueAsString(payload);
        } catch (Exception e) {
            Platform.runLater(() -> showError("Payload Error", "Could not build request payload."));
            return;
        }

        sendRequest("/api/v1/voyages/" + voyageId, "PUT", jsonBody)
                .thenAccept(response -> {
                    int code = response.statusCode();
                    if (code == 200 || code == 204) {
                        Platform.runLater(() -> {
                            showInformation("Success", "Voyage updated successfully.");
                            closeWindow();
                        });
                    } else {
                        Platform.runLater(() -> showError("Update Failed", "Server returned: " + response.body()));
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> showError("Connection Error", "Could not reach the server."));
                    return null;
                });
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();

        if (titleField.getText() == null || titleField.getText().isEmpty()) errorMessage.append("Title is required!\n");
        if (destinationField.getText() == null || destinationField.getText().isEmpty()) errorMessage.append("Destination is required!\n");
        if (startDatePicker.getValue() == null) errorMessage.append("Start date is required!\n");
        if (endDatePicker.getValue() == null) errorMessage.append("End date is required!\n");

        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
                errorMessage.append("End date cannot be before start date!\n");
            }
        }

        try {
            Double.parseDouble(priceField.getText());
        } catch (NumberFormatException e) {
            errorMessage.append("Invalid price format!\n");
        }

        if (errorMessage.length() == 0) {
            return true;
        }

        showError("Invalid Fields", errorMessage.toString());
        return false;
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInformation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private CompletableFuture<HttpResponse<String>> sendRequest(String endpoint, String method, String body) {
        return apiClient.sendWithRetry(endpoint, method, body);
    }

    private List<String> parseValidImageUrls(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return List.of();
        }

        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .filter(this::isValidHttpUrl)
                .collect(Collectors.toList());
    }

    private boolean isValidHttpUrl(String value) {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            if (scheme == null) {
                return false;
            }
            if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                return false;
            }
            return uri.getHost() != null && !uri.getHost().isBlank();
        } catch (Exception e) {
            return false;
        }
    }
}
