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

    @FXML
    private void handleSave() {
        // 1. Perform validation
        if (!isInputValid()) {
            return; // Stop if something is missing
        }

        // 2. Construct the JSON if valid
        String jsonBody = String.format(
            "{\"voyageId\":%s, \"title\":\"%s\", \"description\":\"%s\", \"discountPercentage\":%s, \"startDate\":\"%s\", \"endDate\":\"%s\", \"active\":%b}",
            voyageIdField.getText(), 
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
}