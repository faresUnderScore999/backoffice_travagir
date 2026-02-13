package java_project.controllers.offer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java_project.models.Offer;
import java_project.services.OfferService;
import javafx.application.Platform;
import java.time.LocalDate;

public class UpdateOfferController {
    // FXML Fields matching all backend requirements
    @FXML private TextField titleField, voyageIdField, discountField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker startDatePicker, endDatePicker;
    @FXML private CheckBox activeCheckBox;

    private int offerId;
    private final OfferService offerService = new OfferService();

    /**
     * Pre-fills all fields with existing data from the selected Offer
     */
    public void setOfferData(Offer offer) {
        this.offerId = offer.getId();
        this.voyageIdField.setText(String.valueOf(offer.getVoyageId()));
        this.titleField.setText(offer.getTitle());
        this.descriptionArea.setText(offer.getDescription());
        this.discountField.setText(String.valueOf(offer.getDiscountPercentage()));
        this.startDatePicker.setValue(offer.getStartDate());
        this.endDatePicker.setValue(offer.getEndDate());
        this.activeCheckBox.setSelected(offer.isActive());
    }

    @FXML
    private void handleUpdate() {
        // Construct FULL JSON payload required by your backend
        String jsonBody = String.format(
            "{\"id\":%d, \"voyageId\":%s, \"title\":\"%s\", \"description\":\"%s\", \"discountPercentage\":%s, \"startDate\":\"%s\", \"endDate\":\"%s\", \"active\":%b}",
            offerId,
            voyageIdField.getText(),
            titleField.getText(),
            descriptionArea.getText().replace("\"", "\\\""), // Escape quotes for JSON
            discountField.getText(),
            startDatePicker.getValue(),
            endDatePicker.getValue(),
            activeCheckBox.isSelected()
        );

        offerService.updateOffer(offerId, jsonBody).thenAccept(response -> {
            if (response.statusCode() == 200) {
                Platform.runLater(() -> {
                    showInformation("Success", "Offer updated successfully.");
                    closeWindow();
                });
            } else {
                Platform.runLater(() -> showError("Error", "Update failed: " + response.body()));
            }
        });
    }

    @FXML private void handleCancel() { closeWindow(); }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInformation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}