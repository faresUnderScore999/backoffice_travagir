package java_project.controllers.promocode;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java_project.services.PromoCodeService;
import javafx.application.Platform;

public class AddPromoCodeController {
    @FXML private TextField codeField, maxUsageField;
    @FXML private DatePicker expiryDatePicker;
    @FXML private CheckBox activeCheckBox;

    private final PromoCodeService promoService = new PromoCodeService();

    @FXML
    private void handleSave() {
        if (!isInputValid()) return;

        String jsonBody = String.format(
            "{\"code\":\"%s\", \"maxUsage\":%s, \"expiryDate\":\"%s\", \"active\":%b}",
            codeField.getText(),
            maxUsageField.getText(),
            expiryDatePicker.getValue(),
            activeCheckBox.isSelected()
        );

        promoService.addPromoCode(jsonBody).thenAccept(response -> {
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                Platform.runLater(() -> ((Stage) codeField.getScene().getWindow()).close());
            } else {
                Platform.runLater(() -> showError("Server Error", "Backend returned: " + response.body()));
            }
        });
    }

    private boolean isInputValid() {
        String errorMessage = "";
        if (codeField.getText() == null || codeField.getText().isEmpty()) {
            errorMessage += "Code is required!\n";
        }
        if (maxUsageField.getText() == null || maxUsageField.getText().isEmpty()) {
            errorMessage += "Max usage is required!\n";
        } else {
            try {
                Integer.parseInt(maxUsageField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "Max usage must be a valid integer!\n";
            }
        }
        if (expiryDatePicker.getValue() == null) {
            errorMessage += "Expiry date is required!\n";
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
        ((Stage) codeField.getScene().getWindow()).close();
    }
}
