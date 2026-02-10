package java_project.controllers;

import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import java_project.models.Voyage; // Reusing your Voyage model
import java_project.services.ApiClient;
import javafx.stage.Stage;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.stage.Modality;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.List;

public class VoyageController {

    @FXML
    private TableView<Voyage> voyageTable;
    @FXML
    private TableColumn<Voyage, Integer> colId;
    @FXML
    private TableColumn<Voyage, String> colTitle;
    @FXML
    private TableColumn<Voyage, String> colDestination;
    @FXML
    private TableColumn<Voyage, Double> colPrice;
    @FXML
    private TableColumn<Voyage, String> colStartDate;
    @FXML
    private TableColumn<Voyage, String> colEndDate;
    @FXML
    private Label statusLabel;
    private final ApiClient apiClient = new ApiClient();
    @FXML
    private TableColumn<Voyage, Void> colActions;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            // This line prevents the "Error parsing" if API has extra fields
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @FXML
    public void initialize() {
        // Link columns to Voyage properties
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDestination.setCellValueFactory(new PropertyValueFactory<>("destination"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        loadVoyages();
        addButtonToTable();
    }

    @FXML
    public void loadVoyages() {
        statusLabel.setText("Fetching data...");

        // 1. Use the ApiClient (which acts as your interceptor)
        // It will automatically catch 403, refresh the token, and retry this call
        apiClient.sendWithRetry("/api/v1/voyages", "GET", null)
                .thenAccept(response -> {
                    try {
                        if (response.statusCode() == 200) {
                            // 2. Parse JSON into List
                            List<Voyage> voyages = mapper.readValue(response.body(),
                                    new TypeReference<List<Voyage>>() {
                                    });

                            // 3. Update UI on the JavaFX Thread
                            javafx.application.Platform.runLater(() -> {
                                voyageTable.setItems(FXCollections.observableArrayList(voyages));
                                statusLabel.setText("Data Loaded Successfully");
                            });
                        } else if (response.statusCode() == 403) {
                            // This only happens if BOTH access and refresh tokens are dead
                            javafx.application.Platform.runLater(() -> {
                                statusLabel.setText("Session Expired. Please login again.");
                                // Optional: trigger logout redirect here
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
                    // Handle Network Errors (Server offline, etc.)
                    javafx.application.Platform.runLater(() -> statusLabel.setText("Connection failed"));
                    ex.printStackTrace();
                    return null;
                });
    }

    private void addButtonToTable() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button updateButton = new Button("Update");
            private final Button deleteButton = new Button("Delete");
            private final HBox pane = new HBox(10, updateButton, deleteButton);

            {
                updateButton.setStyle("-fx-background-color: #F9B729; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                updateButton.setOnAction(event -> {
                    Voyage voyage = getTableView().getItems().get(getIndex());
                    // Call your update logic
                    handleUpdate(voyage);
                });

                deleteButton.setOnAction(event -> {
                    Voyage voyage = getTableView().getItems().get(getIndex());
                    // Call your delete logic
                    handleDelete(voyage);
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

    private void handleUpdate(Voyage voyage) {
        System.out.println("Update voyage: " + voyage.getTitle());
        statusLabel.setText("Update voyage: " + voyage.getTitle());
        // Open update dialog or modify fields
    }

    private void handleDelete(Voyage voyage) {
        System.out.println("Delete voyage: " + voyage.getTitle());
        statusLabel.setText("Delete voyage: " + voyage.getTitle());
        // Remove from table + database
        voyageTable.getItems().remove(voyage);
    }

        @FXML
    private void openAddVoyageModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/java_project/views/addVoyageView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Register New Voyage");
            stage.initModality(Modality.APPLICATION_MODAL); // Blocks the main window until closed
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh the table after the modal closes
            loadVoyages();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}