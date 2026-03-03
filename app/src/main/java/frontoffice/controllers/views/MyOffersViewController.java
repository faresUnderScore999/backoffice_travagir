package frontoffice.controllers.views;

import frontoffice.models.UserOffer;
import frontoffice.services.UserOfferService;
import frontoffice.utils.HttpErrorParser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class MyOffersViewController {
    @FXML
    private TableView<UserOffer> claimsTable;
    @FXML
    private TableColumn<UserOffer, Integer> colId;
    @FXML
    private TableColumn<UserOffer, Integer> colOfferId;
    @FXML
    private TableColumn<UserOffer, String> colStatus;
    @FXML
    private TableColumn<UserOffer, Object> colClaimedAt;
    @FXML
    private Label statusLabel;

    private final UserOfferService userOfferService = new UserOfferService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colOfferId.setCellValueFactory(new PropertyValueFactory<>("offerId"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colClaimedAt.setCellValueFactory(new PropertyValueFactory<>("claimedAt"));
        onRefresh();
    }

    @FXML
    private void onRefresh() {
        statusLabel.setText("Loading...");
        userOfferService.getMyClaims()
                .thenAccept(list -> Platform.runLater(() -> {
                    claimsTable.setItems(FXCollections.observableArrayList(list));
                    statusLabel.setText("");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Load error: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void onDeleteSelected() {
        UserOffer selected = claimsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a claim first.");
            return;
        }

        statusLabel.setText("Deleting...");
        userOfferService.deleteClaim(selected.getId())
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
