package java_project.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.ObservableList;

import java_project.models.RefundRequest;
import java_project.services.RefundService;

public class RefundRequestController {

    @FXML
    private TextField amountField;

    @FXML
    private TextArea reasonArea;

    @FXML
    private TableView<RefundRequest> refundTable;

    @FXML
    private TableColumn<RefundRequest, Integer> idCol;

    @FXML
    private TableColumn<RefundRequest, Double> amountCol;

    @FXML
    private TableColumn<RefundRequest, String> statusCol;

    private final RefundService refundService = new RefundService();

    @FXML
    private void initialize() {
        // Configure table columns
        if (idCol != null) {
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        }
        if (amountCol != null) {
            amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        }
        if (statusCol != null) {
            statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        }

        // Load test data into table
        if (refundTable != null) {
            ObservableList<RefundRequest> data = refundService.getAllForTest();
            refundTable.setItems(data);
        }
    }

    @FXML
    private void handleAdd() {
        if (amountField == null || reasonArea == null) {
            return;
        }

        String amountText = amountField.getText();
        String reason = reasonArea.getText();

        if (amountText == null || amountText.isBlank() || reason == null || reason.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Refund Request", "Please fill in both amount and reason.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Refund Request", "Amount must be a number.");
            return;
        }

        refundService.addTestRefund(amount, reason);

        showAlert(Alert.AlertType.INFORMATION, "Refund Request", "Refund request added to test list.");

        amountField.clear();
        reasonArea.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
