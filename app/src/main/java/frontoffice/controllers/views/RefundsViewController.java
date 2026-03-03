package frontoffice.controllers.views;

import frontoffice.models.RefundRequest;
import frontoffice.services.RefundService;
import frontoffice.utils.HttpErrorParser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class RefundsViewController {
    @FXML
    private TextField reclamationIdField;
    @FXML
    private TextField amountField;
    @FXML
    private TextField reasonField;
    @FXML
    private TableView<RefundRequest> refundsTable;
    @FXML
    private TableColumn<RefundRequest, Integer> colId;
    @FXML
    private TableColumn<RefundRequest, Integer> colReclamationId;
    @FXML
    private TableColumn<RefundRequest, Double> colAmount;
    @FXML
    private TableColumn<RefundRequest, String> colStatus;
    @FXML
    private TableColumn<RefundRequest, Object> colCreatedAt;
    @FXML
    private Label statusLabel;

    private final RefundService refundService = new RefundService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colReclamationId.setCellValueFactory(new PropertyValueFactory<>("reclamationId"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        onRefresh();
    }

    @FXML
    private void onRefresh() {
        statusLabel.setText("Loading...");
        refundService.getMy()
                .thenAccept(list -> Platform.runLater(() -> {
                    refundsTable.setItems(FXCollections.observableArrayList(list));
                    statusLabel.setText("");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Load error: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void onSubmit() {
        int reclamationId;
        try {
            reclamationId = Integer.parseInt(reclamationIdField.getText().trim());
        } catch (Exception e) {
            statusLabel.setText("Reclamation ID must be a number.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
        } catch (Exception e) {
            statusLabel.setText("Amount must be a number.");
            return;
        }
        if (amount <= 0) {
            statusLabel.setText("Amount must be > 0.");
            return;
        }

        String reason = reasonField.getText() == null ? "" : reasonField.getText().trim();
        if (reason.isBlank()) {
            statusLabel.setText("Reason is required.");
            return;
        }

        RefundRequest req = new RefundRequest();
        req.setReclamationId(reclamationId);
        req.setAmount(amount);
        req.setReason(reason);

        statusLabel.setText("Submitting...");
        refundService.submit(req)
                .thenAccept(resp -> Platform.runLater(() -> {
                    if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                        statusLabel.setText("Submitted.");
                        reasonField.setText("");
                        onRefresh();
                    } else {
                        String details = HttpErrorParser.bestEffortMessage(resp.body());
                        statusLabel.setText("Submit failed (" + resp.statusCode() + ")" + (details.isBlank() ? "" : ": " + details));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Submit error: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void onDeleteSelected() {
        RefundRequest selected = refundsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a refund request first.");
            return;
        }

        statusLabel.setText("Deleting...");
        refundService.delete(selected.getId())
                .thenAccept(resp -> Platform.runLater(() -> {
                    if (resp.statusCode() == 204 || (resp.statusCode() >= 200 && resp.statusCode() < 300)) {
                        statusLabel.setText("Deleted.");
                        onRefresh();
                    } else {
                        String details = HttpErrorParser.bestEffortMessage(resp.body());
                        statusLabel.setText("Delete failed (" + resp.statusCode() + ")" + (details.isBlank() ? "" : ": " + details));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Delete error: " + ex.getMessage()));
                    return null;
                });
    }
}
