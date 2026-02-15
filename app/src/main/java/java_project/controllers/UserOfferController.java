package java_project.controllers;

import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import java_project.models.UserOffer;
import java_project.services.UserOfferService;
import javafx.stage.Stage;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.stage.Modality;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.List;

public class UserOfferController {

    @FXML
    private TableView<UserOffer> userOfferTable;
    @FXML
    private TableColumn<UserOffer, Integer> colId;
    @FXML
    private TableColumn<UserOffer, Integer> colUserId;
    @FXML
    private TableColumn<UserOffer, Integer> colOfferId;
    @FXML
    private TableColumn<UserOffer, String> colStatus;
    @FXML
    private TableColumn<UserOffer, String> colClaimedDate;
    @FXML
    private TableColumn<UserOffer, String> colUsedDate;
    @FXML
    private Label statusLabel;
    @FXML
    private TableColumn<UserOffer, Void> colActions;

    private final UserOfferService userOfferService = new UserOfferService();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @FXML
    public void initialize() {
        // Link columns to UserOffer properties
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colOfferId.setCellValueFactory(new PropertyValueFactory<>("offerId"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colClaimedDate.setCellValueFactory(new PropertyValueFactory<>("claimedDate"));
        colUsedDate.setCellValueFactory(new PropertyValueFactory<>("usedDate"));

        loadUserOffers();
        addButtonToTable();
    }

    @FXML
    public void loadUserOffers() {
        statusLabel.setText("Fetching data...");

        userOfferService.getMyUserOffers()
                .thenAccept(response -> {
                    try {
                        if (response.statusCode() == 200) {
                            // Parse JSON into List
                            List<UserOffer> userOffers = mapper.readValue(response.body(),
                                    new TypeReference<List<UserOffer>>() {
                                    });

                            // Update UI on the JavaFX Thread
                            javafx.application.Platform.runLater(() -> {
                                userOfferTable.setItems(FXCollections.observableArrayList(userOffers));
                                statusLabel.setText("✅ Data Loaded Successfully");
                            });
                        } else if (response.statusCode() == 403) {
                            javafx.application.Platform.runLater(() -> {
                                statusLabel.setText("Session Expired. Please login again.");
                            });
                        } else {
                            javafx.application.Platform.runLater(
                                    () -> statusLabel.setText("Error: Server returned " + response.statusCode()));
                        }
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> statusLabel.setText("Error parsing data"));
                        e.printStackTrace();
                    }
                })
                .exceptionally(ex -> {
                    javafx.application.Platform.runLater(() -> statusLabel.setText("Connection failed"));
                    ex.printStackTrace();
                    return null;
                });
    }

    private void addButtonToTable() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button useButton = new Button("Mark Used");
            private final Button deleteButton = new Button("Delete");
            private final HBox pane = new HBox(10, useButton, deleteButton);

            {
                useButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                useButton.setOnAction(event -> {
                    UserOffer userOffer = getTableView().getItems().get(getIndex());
                    handleMarkUsed(userOffer);
                });

                deleteButton.setOnAction(event -> {
                    UserOffer userOffer = getTableView().getItems().get(getIndex());
                    handleDelete(userOffer);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void handleMarkUsed(UserOffer userOffer) {
        statusLabel.setText("Updating status for offer " + userOffer.getOfferId() + "...");
        
        userOfferService.updateUserOfferStatus(userOffer.getId(), "USED")
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        javafx.application.Platform.runLater(() -> {
                            statusLabel.setText("✅ Status updated to USED");
                            loadUserOffers();
                        });
                    } else {
                        javafx.application.Platform.runLater(
                                () -> statusLabel.setText("Error: " + response.statusCode()));
                    }
                })
                .exceptionally(ex -> {
                    javafx.application.Platform.runLater(() -> statusLabel.setText("Failed to update status"));
                    return null;
                });
    }

    private void handleDelete(UserOffer userOffer) {
        statusLabel.setText("Deleting user offer...");
        
        userOfferService.deleteUserOffer(userOffer.getId())
                .thenAccept(response -> {
                    if (response.statusCode() == 200 || response.statusCode() == 204) {
                        javafx.application.Platform.runLater(() -> {
                            statusLabel.setText("✅ User offer deleted");
                            loadUserOffers();
                        });
                    } else {
                        javafx.application.Platform.runLater(
                                () -> statusLabel.setText("Error: " + response.statusCode()));
                    }
                })
                .exceptionally(ex -> {
                    javafx.application.Platform.runLater(() -> statusLabel.setText("Failed to delete"));
                    return null;
                });
    }

    @FXML
    private void openClaimOfferModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/java_project/views/user-offer/addUserOfferView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Claim New Offer");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh the table after the modal closes
            loadUserOffers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
