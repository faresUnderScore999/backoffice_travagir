package java_project.controllers.offer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java_project.services.OfferService;
import javafx.application.Platform;

public class AddOfferController {
    @FXML private TextField titleField, voyageIdField, discountField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker startDatePicker, endDatePicker;
    @FXML private CheckBox activeCheckBox;

    private final OfferService offerService = new OfferService();
    private Integer initialVoyageId = null;

    /**
     * Called by parent controllers to prefill the voyage ID.
     */
    public void setVoyageId(int voyageId) {
        this.initialVoyageId = voyageId;
        if (voyageIdField != null) {
            voyageIdField.setText(String.valueOf(voyageId));
            voyageIdField.setEditable(false);
            voyageIdField.getStyleClass().add("readonly-field");
        }
    }

    @FXML
    private void initialize() {
        if (initialVoyageId != null && voyageIdField != null) {
            voyageIdField.setText(String.valueOf(initialVoyageId));
            voyageIdField.setEditable(false);
            voyageIdField.getStyleClass().add("readonly-field");
        }
    }

    @FXML
    private void handleSave() {
        // 1. Perform validation
        if (!isInputValid()) {
            return; // Stop if something is missing
        }

        // 2. Get destination name from voyage ID
        String voyageId = voyageIdField.getText();
        offerService.getVoyageById(Integer.parseInt(voyageId)).thenAccept(voyageResponse -> {
            if (voyageResponse.statusCode() == 200) {
                // Parse voyage to get destination name
                String destinationName = extractDestinationFromVoyage(voyageResponse.body());
                
                // 3. Construct the JSON with destinationName
                String jsonBody = String.format(
                    "{\"destinationName\":\"%s\", \"title\":\"%s\", \"description\":\"%s\", \"discountPercentage\":%s, \"startDate\":\"%s\", \"endDate\":\"%s\", \"active\":%b}",
                    destinationName,
                    titleField.getText(), 
                    descriptionArea.getText().replace("\"", "\\\""), 
                    discountField.getText(),
                    startDatePicker.getValue(), 
                    endDatePicker.getValue(), 
                    activeCheckBox.isSelected()
                );

                offerService.addOffer(jsonBody).thenAccept(response -> {
                    if (response.statusCode() == 201 || response.statusCode() == 200) {
                        Platform.runLater(() -> ((Stage) titleField.getScene().getWindow()).close());
                    } else {
                        Platform.runLater(() -> showError("Server Error", "Backend returned: " + response.body()));
                    }
                });
            } else {
                Platform.runLater(() -> showError("Voyage Error", "Voyage with ID " + voyageId + " not found"));
            }
        });
    }

    /**
     * Validates user input and shows an alert if fields are missing.
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (voyageIdField.getText() == null || voyageIdField.getText().isEmpty()) {
            errorMessage += "Voyage ID is required!\n";
        }
        if (titleField.getText() == null || titleField.getText().isEmpty()) {
            errorMessage += "Offer Title is required!\n";
        }
        if (discountField.getText() == null || discountField.getText().isEmpty()) {
            errorMessage += "Discount Percentage is required!\n";
        } else {
            try {
                Double.parseDouble(discountField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "Discount must be a valid number (e.g., 15.5)!\n";
            }
        }
        if (startDatePicker.getValue() == null) {
            errorMessage += "Start Date is missing!\n";
        }
        if (endDatePicker.getValue() == null) {
            errorMessage += "End Date is missing!\n";
        }
        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
                errorMessage += "End Date cannot be before Start Date!\n";
            }
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showError("Missing Information", errorMessage);
            return false;
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML
    private void handleCancel() {
        ((Stage) titleField.getScene().getWindow()).close();
    }

    /**
     * Extracts destination name from voyage JSON response.
     */
    private String extractDestinationFromVoyage(String voyageJson) {
        try {
            // Simple JSON parsing to extract "destination" field
            // Expected format: {"id":28,"title":"tounesss","destination":"tunis",...}
            int destinationIndex = voyageJson.indexOf("\"destination\":");
            if (destinationIndex != -1) {
                int startQuote = voyageJson.indexOf("\"", destinationIndex + 14);
                int endQuote = voyageJson.indexOf("\"", startQuote + 1);
                if (startQuote != -1 && endQuote != -1) {
                    return voyageJson.substring(startQuote + 1, endQuote);
                }
            }
            return "";
        } catch (Exception e) {
            System.err.println("Error parsing destination from voyage: " + e.getMessage());
            return "";
        }
    }
}