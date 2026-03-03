package frontoffice.controllers.views;

import frontoffice.models.Reservation;
import frontoffice.models.Voyage;
import frontoffice.services.ReservationService;
import frontoffice.services.VoyageService;
import frontoffice.utils.HttpErrorParser;
import frontoffice.utils.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class VoyagesViewController {
    @FXML
    private TextField searchField;
    @FXML
    private TableView<Voyage> voyagesTable;
    @FXML
    private TableColumn<Voyage, Integer> colId;
    @FXML
    private TableColumn<Voyage, String> colTitle;
    @FXML
    private TableColumn<Voyage, String> colDestination;
    @FXML
    private TableColumn<Voyage, Double> colPrice;
    @FXML
    private TextField peopleField;
    @FXML
    private Label statusLabel;

    private final VoyageService voyageService = new VoyageService();
    private final ReservationService reservationService = new ReservationService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDestination.setCellValueFactory(new PropertyValueFactory<>("destination"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        onRefresh();
    }

    @FXML
    private void onSearch() {
        String q = searchField.getText();
        statusLabel.setText("Searching...");
        voyageService.searchVoyages(q)
                .thenAccept(list -> Platform.runLater(() -> {
                    voyagesTable.setItems(FXCollections.observableArrayList(list));
                    statusLabel.setText("");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Search error: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void onRefresh() {
        statusLabel.setText("Loading...");
        voyageService.getAllVoyages()
                .thenAccept(list -> Platform.runLater(() -> {
                    voyagesTable.setItems(FXCollections.observableArrayList(list));
                    statusLabel.setText("");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Load error: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void onBookSelected() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            statusLabel.setText("Please login first.");
            return;
        }

        Voyage selected = voyagesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a voyage first.");
            return;
        }

        int people;
        try {
            String raw = peopleField.getText();
            people = raw == null || raw.isBlank() ? 1 : Integer.parseInt(raw.trim());
        } catch (Exception e) {
            statusLabel.setText("People must be a number.");
            return;
        }
        if (people < 1) {
            statusLabel.setText("People must be at least 1.");
            return;
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(user.id());
        reservation.setVoyageId(selected.getId());
        reservation.setNumberOfPeople(people);

        statusLabel.setText("Booking...");
        reservationService.createReservation(reservation)
                .thenAccept(resp -> Platform.runLater(() -> {
                    if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                        statusLabel.setText("Reservation created.");
                    } else {
                        String details = HttpErrorParser.bestEffortMessage(resp.body());
                        statusLabel.setText("Booking failed (" + resp.statusCode() + ")" + (details.isBlank() ? "" : ": " + details));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Booking error: " + ex.getMessage()));
                    return null;
                });
    }
}
