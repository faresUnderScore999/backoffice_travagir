package java_project.controllers.voyage;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.concurrent.CompletableFuture;
import java.net.http.HttpResponse;
import javafx.application.Platform;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AddVoyageController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField destinationField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField priceField;
    @FXML private TextField imageUrlField; // For simplicity, taking a comma-separated list or single URL

    /**
     * Handles the Save button action.
     * Maps voyage data to JSON and sends a POST request.
     */
    @FXML
    private void handleSave() {
        if (isInputValid()) {
            // Process the image URL field into a JSON array format
            List<String> images = Arrays.stream(imageUrlField.getText().split(","))
                                       .map(String::trim)
                                       .collect(Collectors.toList());
            String imagesJson = images.stream()
                                     .map(s -> "\"" + s + "\"")
                                     .collect(Collectors.joining(", ", "[", "]"));

            // 1. Construct the JSON payload
            String jsonBody = String.format(
                "{\"title\":\"%s\", \"description\":\"%s\", \"destination\":\"%s\", \"startDate\":\"%s\", \"endDate\":\"%s\", \"price\":%s, \"imageUrl\":%s}",
                titleField.getText(),
                descriptionArea.getText().replace("\n", "\\n"), // Escape newlines for JSON
                destinationField.getText(),
                startDatePicker.getValue().format(DateTimeFormatter.ISO_DATE),
                endDatePicker.getValue().format(DateTimeFormatter.ISO_DATE),
                priceField.getText(),
                imagesJson
            );

            // 2. Send the async request using the logic from AddUserController
            sendRequest("/voyages/add", "POST", jsonBody)
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
        // Implementation should match your project's ApiService or BaseController logic
        return null; 
    }
}