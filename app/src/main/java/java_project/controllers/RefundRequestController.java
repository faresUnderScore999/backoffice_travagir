package java_project.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java_project.models.RefundRequest;
import java_project.services.RefundRequestService;

import java.util.List;
import java.util.Optional;

public class RefundRequestController {

    @FXML private TextField amountField;
    @FXML private TextArea reasonArea;

    @FXML private TableView<RefundRequest> refundTable;
    @FXML private TableColumn<RefundRequest, Integer> idCol;
    @FXML private TableColumn<RefundRequest, Double> amountCol;
    @FXML private TableColumn<RefundRequest, String> statusCol;

    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button deleteButton;

    private final RefundRequestService refundService = new RefundRequestService();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    private void initialize() {
        mapper.registerModule(new JavaTimeModule());

        // Match RefundRequest getters: getId(), getAmount(), getStatus()
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        if (approveButton != null) {
            approveButton.disableProperty().bind(refundTable.getSelectionModel().selectedItemProperty().isNull());
        }
        if (rejectButton != null) {
            rejectButton.disableProperty().bind(refundTable.getSelectionModel().selectedItemProperty().isNull());
        }
        if (deleteButton != null) {
            deleteButton.disableProperty().bind(refundTable.getSelectionModel().selectedItemProperty().isNull());
        }

        loadMyRefunds(); // USER view
        // if you want ADMIN view instead, use: loadAllRefunds();
    }

    @FXML
    private void handleApproveSelected() {
        RefundRequest selected = refundTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (!confirmAction("Approve refund", "Approve refund #" + selected.getId() + "?")) return;

        refundService.approveRefund(selected.getId()).thenAccept(response -> {
            if (response.statusCode() == 200 || response.statusCode() == 204) {
                Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Refund", "Approved ✅"));
                loadMyRefunds();
            } else {
                Platform.runLater(() -> showAlert(
                        Alert.AlertType.ERROR,
                        "Refund",
                        "Approve failed (" + response.statusCode() + ")\n" + response.body()
                ));
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Refund", "Error: " + ex.getMessage()));
            return null;
        });
    }

    @FXML
    private void handleRejectSelected() {
        RefundRequest selected = refundTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (!confirmAction("Reject refund", "Reject refund #" + selected.getId() + "?")) return;

        refundService.rejectRefund(selected.getId()).thenAccept(response -> {
            if (response.statusCode() == 200 || response.statusCode() == 204) {
                Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Refund", "Rejected ✅"));
                loadMyRefunds();
            } else {
                Platform.runLater(() -> showAlert(
                        Alert.AlertType.ERROR,
                        "Refund",
                        "Reject failed (" + response.statusCode() + ")\n" + response.body()
                ));
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Refund", "Error: " + ex.getMessage()));
            return null;
        });
    }

    @FXML
    private void handleDeleteSelected() {
        RefundRequest selected = refundTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String status = selected.getStatus();
        if (status != null && !status.isBlank() && !"PENDING".equalsIgnoreCase(status.trim())) {
            showAlert(Alert.AlertType.WARNING, "Refund", "Only pending refunds are typically deletable.\nCurrent status: " + status);
            return;
        }

        if (!confirmAction("Delete refund", "Delete refund #" + selected.getId() + "?")) return;

        refundService.deleteRefund(selected.getId()).thenAccept(response -> {
            if (response.statusCode() == 200 || response.statusCode() == 204) {
                Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Refund", "Deleted ✅"));
                loadMyRefunds();
            } else {
                Platform.runLater(() -> showAlert(
                        Alert.AlertType.ERROR,
                        "Refund",
                        "Delete failed (" + response.statusCode() + ")\n" + response.body()
                ));
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Refund", "Error: " + ex.getMessage()));
            return null;
        });
    }

    private boolean confirmAction(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void loadMyRefunds() {
        refundService.getMyRefunds().thenAccept(response -> {
            try {
                if (response.statusCode() == 200) {
                    List<RefundRequest> list = mapper.readValue(
                            response.body(),
                            new TypeReference<List<RefundRequest>>() {}
                    );

                    Platform.runLater(() ->
                            refundTable.setItems(FXCollections.observableArrayList(list))
                    );
                } else {
                    Platform.runLater(() ->
                            showAlert(Alert.AlertType.ERROR, "Refunds",
                                    "Load failed (" + response.statusCode() + ")\n" + response.body())
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Refunds", "Parsing error: " + e.getMessage())
                );
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(() ->
                    showAlert(Alert.AlertType.ERROR, "Refunds", "Request error: " + ex.getMessage())
            );
            return null;
        });
    }

    // Optional for ADMIN:
    @SuppressWarnings("unused")
    private void loadAllRefunds() {
        refundService.getAllRefunds().thenAccept(response -> {
            try {
                if (response.statusCode() == 200) {
                    List<RefundRequest> list = mapper.readValue(
                            response.body(),
                            new TypeReference<List<RefundRequest>>() {}
                    );
                    Platform.runLater(() ->
                            refundTable.setItems(FXCollections.observableArrayList(list))
                    );
                } else {
                    Platform.runLater(() ->
                            showAlert(Alert.AlertType.ERROR, "Refunds",
                                    "Load failed (" + response.statusCode() + ")\n" + response.body())
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Refunds", "Parsing error: " + e.getMessage())
                );
            }
        });
    }

    @FXML
    private void handleAdd() {
        String amountText = amountField.getText();
        String reason = reasonArea.getText();

        if (amountText == null || amountText.isBlank() || reason == null || reason.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Refund Request", "Fill amount and reason.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText.trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Refund Request", "Amount must be a number.");
            return;
        }

        // Because your FXML doesn't have reclamationId field,
        // we ask it with a small popup:
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reclamation ID");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter Reclamation ID:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) return;

        int reclamationId;
        try {
            reclamationId = Integer.parseInt(result.get().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Refund Request", "Reclamation ID must be a number.");
            return;
        }

        String jsonBody =
                "{\"reclamationId\":" + reclamationId +
                ",\"amount\":" + amount +
                ",\"reason\":\"" + escapeJson(reason.trim()) + "\"}";

        refundService.addRefund(jsonBody).thenAccept(response -> {
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Refund Request", "Created ✅");
                    amountField.clear();
                    reasonArea.clear();
                });
                loadMyRefunds();
            } else {
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Refund Request",
                                "Create failed (" + response.statusCode() + ")\n" + response.body())
                );
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(() ->
                    showAlert(Alert.AlertType.ERROR, "Refund Request", "Error: " + ex.getMessage())
            );
            return null;
        });
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
