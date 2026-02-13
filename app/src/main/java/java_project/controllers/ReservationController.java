package java_project.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java_project.models.Reservation;
import java_project.services.ReservationService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;
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
    private final ObjectMapper mapper = new ObjectMapper();
    private ObservableList<Reservation> allReservations = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        mapper.registerModule(new JavaTimeModule());

        // Fix for LoadException: Set the resize policy in Java code
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
                           // updateTotalRevenue(reservations);
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

    private void setupActionColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button confirmBtn = new Button("Confirm");
            private final HBox pane = new HBox(confirmBtn);

            {
                confirmBtn.setStyle("-fx-background-color: #F9B729; -fx-text-fill: black; -fx-font-weight: bold;");
                pane.setStyle("-fx-alignment: CENTER;");
                confirmBtn.setOnAction(e -> {
                    Reservation res = getTableView().getItems().get(getIndex());
                    updateReservationStatus(res, "CONFIRMED");
                });
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

    private void updateTotalRevenue(List<Reservation> list) {
        double total = list.stream()
                .filter(r -> "CONFIRMED".equalsIgnoreCase(r.getStatus()))
                .mapToDouble(Reservation::getTotalPrice)
                .sum();
        totalRevenueLabel.setText(String.format("$%.2f", total));
    }
}