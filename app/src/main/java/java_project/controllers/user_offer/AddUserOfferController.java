package java_project.controllers.user_offer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java_project.services.UserOfferService;

public class AddUserOfferController {

    @FXML
    private TextField offerIdField;

    @FXML
    private Label statusLabel;

    private final UserOfferService userOfferService = new UserOfferService();

    @FXML
    private void handleClaim() {
        Integer offerId = parseOfferId();
        if (offerId == null) {
            return;
        }

        setBusy(true);
        statusLabel.setText("Claiming offer...");

        userOfferService.claimOffer(offerId)
                .thenAccept(response -> Platform.runLater(() -> {
                    setBusy(false);

                    int code = response.statusCode();
                    if (code == 200 || code == 201) {
                        showInformation("Success", "Offer claimed successfully.");
                        closeWindow();
                    } else if (code == 401 || code == 403) {
                        showError("Session Expired", "Please log in again.");
                    } else {
                        showError("Claim Failed", "Server returned: " + code + "\n" + response.body());
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        setBusy(false);
                        showError("Connection Error", "Could not reach the server: " + ex.getMessage());
                    });
                    return null;
                });
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private Integer parseOfferId() {
        String raw = offerIdField.getText();
        if (raw == null || raw.trim().isEmpty()) {
            showError("Invalid Input", "Offer ID is required.");
            return null;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            if (value <= 0) {
                showError("Invalid Input", "Offer ID must be a positive number.");
                return null;
            }
            return value;
        } catch (NumberFormatException e) {
            showError("Invalid Input", "Offer ID must be a number.");
            return null;
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) offerIdField.getScene().getWindow();
        stage.close();
    }

    private void setBusy(boolean busy) {
        offerIdField.setDisable(busy);
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
}
