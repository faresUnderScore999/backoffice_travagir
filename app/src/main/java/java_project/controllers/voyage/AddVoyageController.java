package java_project.controllers.voyage;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java_project.services.ApiClient;
import java.util.concurrent.CompletableFuture;
import java.net.http.HttpResponse;
import javafx.application.Platform;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class AddVoyageController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField destinationField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField priceField;

    private final ApiClient apiClient = new ApiClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Handles the Save button action.
     * Maps voyage data to JSON and sends a POST request.
     */
    @FXML
    private void handleSave() {
        if (isInputValid()) {
            // We'll create the voyage first, then upload any attached files to the created voyage id.

            String jsonBody;
            try {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("title", titleField.getText());
                payload.put("description", descriptionArea.getText());
                payload.put("destination", destinationField.getText());
                payload.put("startDate", startDatePicker.getValue().format(DateTimeFormatter.ISO_DATE));
                payload.put("endDate", endDatePicker.getValue().format(DateTimeFormatter.ISO_DATE));
                payload.put("price", Double.parseDouble(priceField.getText()));
                // image URLs are replaced by file attachments; uploads happen after creation
                jsonBody = mapper.writeValueAsString(payload);
            } catch (Exception e) {
                Platform.runLater(() -> showError("Payload Error", "Could not build request payload."));
                return;
            }

            // 2. Send the async request
            sendRequest("/api/v1/voyages", "POST", jsonBody)
                .thenAccept(response -> {
                    if (response.statusCode() == 201 || response.statusCode() == 200) {
                        Platform.runLater(() -> {
                            showInformation("Success", "Voyage has been created successfully.");
                            closeWindow();
                        });
                    } else {
                        Platform.runLater(() -> 
                            showError("Creation Failed", "Server returned: " + response.body())
                        );
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> 
                        showError("Connection Error", "Could not reach the server.")
                    );
                    return null;
                });
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    /**
     * Validation logic for Voyage fields.
     */
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
        } else {
            showError("Invalid Fields", errorMessage.toString());
            return false;
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    // --- Helper UI Methods (Mirrored from AddUserController) ---

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
    
}