package java_project.controllers.promocode;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java_project.models.PromoCode;
import java_project.services.PromoCodeService;
import javafx.application.Platform;

public class UpdatePromoCodeController {
    @FXML private TextField codeField, maxUsageField;
    @FXML private DatePicker expiryDatePicker;
    @FXML private CheckBox activeCheckBox;

    private int promoId;
    private final PromoCodeService promoService = new PromoCodeService();

    public void setPromoData(PromoCode promo) {
        this.promoId = promo.getId();
        this.codeField.setText(promo.getCode());
        this.maxUsageField.setText(String.valueOf(promo.getMaxUsage()));
        this.expiryDatePicker.setValue(promo.getExpiryDate());
        this.activeCheckBox.setSelected(promo.isActive());
    }

    @FXML
    private void handleUpdate() {
        String jsonBody = String.format(
            "{\"id\":%d, \"code\":\"%s\", \"maxUsage\":%s, \"expiryDate\":\"%s\", \"active\":%b}",
            promoId,
            codeField.getText(),
            maxUsageField.getText(),
            expiryDatePicker.getValue(),
            activeCheckBox.isSelected()
        );

        promoService.updatePromoCode(promoId, jsonBody).thenAccept(response -> {
            if (response.statusCode() == 200) {
                Platform.runLater(() -> {
                    showInformation("Success", "Promo code updated successfully.");
                    closeWindow();
                });
            } else {
                Platform.runLater(() -> showError("Error", "Update failed: " + response.body()));
            }
        });
    }

    private void closeWindow() {
        Stage stage = (Stage) codeField.getScene().getWindow();
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

    @FXML private void handleCancel() { closeWindow(); }
}
