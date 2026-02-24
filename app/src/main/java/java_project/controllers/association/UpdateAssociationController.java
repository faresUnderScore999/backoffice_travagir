package java_project.controllers.association;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import java_project.models.Association;
import java_project.services.AssociationService;

public class UpdateAssociationController {
    @FXML private TextField nameField;
    @FXML private TextField companyCodeField;
    @FXML private TextField discountRateField;
    @FXML private Label idLabel;

    private final AssociationService associationService = new AssociationService();
    private Association association;

    public void setAssociationData(Association a) {
        this.association = a;
        idLabel.setText(a.getId() == null ? "-" : a.getId().toString());
        nameField.setText(a.getName());
        companyCodeField.setText(a.getCompanyCode());
        discountRateField.setText(a.getDiscountRate() == null ? "" : a.getDiscountRate().toString());
    }

    @FXML
    private void handleUpdate() {
        if (association == null) return;
        if (!isInputValid()) return;

        String json = String.format("{\"name\":\"%s\",\"companyCode\":\"%s\",\"discountRate\":%s}",
                nameField.getText().trim().replace("\"", "\\\""),
                companyCodeField.getText().trim(),
                discountRateField.getText().trim());

        associationService.updateAssociation(association.getId(), json).thenAccept(response -> {
            Platform.runLater(() -> {
                int code = response.statusCode();
                if (code == 200 || code == 204) {
                    ((Stage) nameField.getScene().getWindow()).close();
                } else {
                    showError("Server Error", "Backend returned: " + code + "\n" + response.body());
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> showError("Connection Error", ex.getMessage()));
            return null;
        });
    }

    @FXML
    private void handleCancel() { ((Stage) nameField.getScene().getWindow()).close(); }

    private boolean isInputValid() {
        String err = "";
        if (nameField.getText() == null || nameField.getText().isBlank()) err += "Name is required\n";
        if (companyCodeField.getText() == null || companyCodeField.getText().isBlank()) err += "Company code is required\n";
        if (discountRateField.getText() == null || discountRateField.getText().isBlank()) err += "Discount rate is required\n";
        else {
            try { Double.parseDouble(discountRateField.getText().trim()); } catch (NumberFormatException e) { err += "Discount must be a number (e.g. 15.5)\n"; }
        }

        if (!err.isEmpty()) { showError("Invalid input", err); return false; }
        return true;
    }

    private void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}