package java_project.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.net.http.HttpResponse;
import java_project.models.Reservation;
import java_project.models.User;          // import User model
import java_project.models.Voyage;        // import Voyage model
import java_project.services.ReservationService;
import java_project.services.UserService; // new
import java_project.services.VoyageService; // new

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ReservationController {

    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, Integer> colId;
    @FXML private TableColumn<Reservation, Integer> colUserId;
    @FXML private TableColumn<Reservation, Integer> colVoyageId;
    @FXML private TableColumn<Reservation, String> colDate;
    @FXML private TableColumn<Reservation, Integer> colPeople;
    @FXML private TableColumn<Reservation, Double> colPrice;
    @FXML private TableColumn<Reservation, String> colStatus;
    @FXML private TableColumn<Reservation, String> colPayment;
    @FXML private TableColumn<Reservation, Void> colActions;

    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private Label totalRevenueLabel;

    private final ReservationService reservationService = new ReservationService();
    private final UserService userService = new UserService();       // new
    private final VoyageService voyageService = new VoyageService(); // new
    private final ObjectMapper mapper = new ObjectMapper();
    private ObservableList<Reservation> allReservations = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        mapper.registerModule(new JavaTimeModule());
        reservationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupColumns();
        loadReservations();
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colVoyageId.setCellValueFactory(new PropertyValueFactory<>("voyageId"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("reservationDate"));
        colPeople.setCellValueFactory(new PropertyValueFactory<>("numberOfPeople"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPayment.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        setupActionColumn();
    }

    @FXML
    public void loadReservations() {
        statusLabel.setText("Status: Loading...");
        reservationService.getAllReservations()
            .thenAccept(response -> {
                if (response.statusCode() == 200) {
                    try {
                        List<Reservation> reservations = mapper.readValue(response.body(), 
                            new TypeReference<List<Reservation>>() {});
                        Platform.runLater(() -> {
                            allReservations.setAll(reservations);
                            reservationTable.setItems(allReservations);
                            statusLabel.setText("Status: " + reservations.size() + " loaded.");
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> statusLabel.setText("Status: Data Error."));
                    }
                } else {
                    Platform.runLater(() -> statusLabel.setText("Status: Server Error " + response.statusCode()));
                }
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> statusLabel.setText("Status: Network Error."));
                return null;
            });
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            reservationTable.setItems(allReservations);
            return;
        }
        List<Reservation> filtered = allReservations.stream()
            .filter(r -> String.valueOf(r.getUserId()).contains(query) || 
                         r.getStatus().toLowerCase().contains(query))
            .collect(Collectors.toList());
        reservationTable.setItems(FXCollections.observableArrayList(filtered));
        statusLabel.setText("Status: Found " + filtered.size());
    }

    // ---------- Action Column with Confirm, Cancel, Detail ----------
    private void setupActionColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button confirmBtn = new Button("Confirm");
            private final Button cancelBtn = new Button("Cancel");
            private final Button detailBtn = new Button("Detail");
            private final HBox pane = new HBox(5, confirmBtn, cancelBtn, detailBtn);

            {
                confirmBtn.setStyle("-fx-background-color: #F9B729; -fx-text-fill: black; -fx-font-weight: bold;");
                cancelBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold;");
                detailBtn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold;");
                
                confirmBtn.setOnAction(e -> {
                    Reservation res = getTableView().getItems().get(getIndex());
                    updateReservationStatus(res, "CONFIRMED");
                });
                
                cancelBtn.setOnAction(e -> {
                    Reservation res = getTableView().getItems().get(getIndex());
                    updateReservationStatus(res, "CANCELLED");
                });

                detailBtn.setOnAction(e -> {
                    Reservation res = getTableView().getItems().get(getIndex());
                    showDetailDialog(res);
                });
                
                pane.setStyle("-fx-alignment: CENTER;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void updateReservationStatus(Reservation res, String newStatus) {
        reservationService.updateStatus(res.getId(), newStatus)
            .thenAccept(response -> {
                if (response.statusCode() == 200) {
                    Platform.runLater(() -> {
                        res.setStatus(newStatus);
                        reservationTable.refresh();
                        statusLabel.setText("Status: Updated ID " + res.getId());
                    });
                }
            });
    }

    // ---------- Detail Dialog ----------
    private void showDetailDialog(Reservation reservation) {
        // Fetch user and voyage in parallel
        CompletableFuture<HttpResponse<String>> userFuture = 
            userService.getUserById(reservation.getUserId());
        CompletableFuture<HttpResponse<String>> voyageFuture = 
            voyageService.getVoyageById(reservation.getVoyageId());

        CompletableFuture.allOf(userFuture, voyageFuture).thenRun(() -> {
            try {
                String userJson = userFuture.get().body();
                String voyageJson = voyageFuture.get().body();

                User user = mapper.readValue(userJson, User.class);
                Voyage voyage = mapper.readValue(voyageJson, Voyage.class);

                Platform.runLater(() -> {
                    StringBuilder content = new StringBuilder();
                    content.append("Reservation ID: ").append(reservation.getId()).append("\n\n");
                    content.append("--- User Details ---\n");
                    content.append("Name: ").append(user.getName()).append("\n");
                    content.append("Email: ").append(user.getEmail()).append("\n");
                    content.append("Phone: ").append(user.getTel() == null ? "N/A" : user.getTel()).append("\n\n");
                    content.append("--- Voyage Details ---\n");
                    content.append("Title: ").append(voyage.getTitle()).append("\n");
                    content.append("Destination: ").append(voyage.getDestination()).append("\n");
                    content.append("Dates: ").append(voyage.getStartDate()).append(" to ").append(voyage.getEndDate()).append("\n");
                    content.append("Price per person: ").append(voyage.getPrice()).append("\n");

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Reservation Details");
                    alert.setHeaderText("Details for Reservation #" + reservation.getId());
                    alert.setContentText(content.toString());
                    alert.showAndWait();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText("Could not load details: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Failed to fetch details: " + ex.getMessage());
                alert.showAndWait();
            });
            return null;
        });
    }
}