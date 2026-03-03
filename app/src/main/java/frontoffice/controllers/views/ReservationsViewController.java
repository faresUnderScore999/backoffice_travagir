package frontoffice.controllers.views;

import frontoffice.models.Reservation;
import frontoffice.services.ReservationService;
import frontoffice.utils.HttpErrorParser;
import frontoffice.utils.SceneNavigator;
import frontoffice.utils.SessionManager;
import frontoffice.utils.ViewContext;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ReservationsViewController {
    @FXML
    private TableView<Reservation> reservationsTable;
    @FXML
    private TableColumn<Reservation, Integer> colId;
    @FXML
    private TableColumn<Reservation, Integer> colVoyageId;
    @FXML
    private TableColumn<Reservation, Integer> colPeople;
    @FXML
    private TableColumn<Reservation, String> colStatus;
    @FXML
    private TextField peopleUpdateField;
    @FXML
    private Label statusLabel;

    private final ReservationService reservationService = new ReservationService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colVoyageId.setCellValueFactory(new PropertyValueFactory<>("voyageId"));
        colPeople.setCellValueFactory(new PropertyValueFactory<>("numberOfPeople"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        onRefresh();
    }

    @FXML
    private void onRefresh() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            reservationsTable.setItems(FXCollections.observableArrayList());
            statusLabel.setText("Please login.");
            return;
        }

        statusLabel.setText("Loading...");
        reservationService.getMyReservations(user.id())
                .thenAccept(list -> Platform.runLater(() -> {
                    reservationsTable.setItems(FXCollections.observableArrayList(list));
                    statusLabel.setText("");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Load error: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void onCancelSelected() {
        Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a reservation first.");
            return;
        }

        statusLabel.setText("Cancelling...");
        reservationService.cancelReservation(selected.getId())
                .thenAccept(resp -> Platform.runLater(() -> {
                    if (resp.statusCode() == 204 || (resp.statusCode() >= 200 && resp.statusCode() < 300)) {
                        statusLabel.setText("Cancelled.");
                        onRefresh();
                    } else {
                        String details = HttpErrorParser.bestEffortMessage(resp.body());
                        statusLabel.setText("Cancel failed (" + resp.statusCode() + ")" + (details.isBlank() ? "" : ": " + details));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Cancel error: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void onUpdatePeople() {
        Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a reservation first.");
            return;
        }

        int count;
        try {
            count = Integer.parseInt(peopleUpdateField.getText().trim());
        } catch (Exception e) {
            statusLabel.setText("People must be a number.");
            return;
        }
        if (count < 1) {
            statusLabel.setText("People must be at least 1.");
            return;
        }

        statusLabel.setText("Updating...");
        reservationService.updatePeopleCount(selected.getId(), count)
                .thenAccept(resp -> Platform.runLater(() -> {
                    if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                        statusLabel.setText("Updated.");
                        onRefresh();
                    } else {
                        String details = HttpErrorParser.bestEffortMessage(resp.body());
                        statusLabel.setText("Update failed (" + resp.statusCode() + ")" + (details.isBlank() ? "" : ": " + details));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Update error: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void onCreateReclamationForSelected() {
        Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a reservation first.");
            return;
        }

        ViewContext.getInstance().setReclamationReservationId(selected.getId());
        var main = SceneNavigator.getMainLayoutController();
        if (main != null) {
            main.showReclamations();
        }
    }
}
