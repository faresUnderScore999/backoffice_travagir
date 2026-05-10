package java_project.controllers;

import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import java_project.controllers.voyage.UpdateVoyageController;
import java_project.models.Voyage;
import java_project.services.VoyageService;   // new import
import javafx.stage.Stage;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.stage.Modality;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.List;

public class VoyageController {

    @FXML private TableView<Voyage> voyageTable;
    @FXML private TableColumn<Voyage, Integer> colId;
    @FXML private TableColumn<Voyage, String> colTitle;
    @FXML private TableColumn<Voyage, String> colDestination;
    @FXML private TableColumn<Voyage, Double> colPrice;
    @FXML private TableColumn<Voyage, String> colStartDate;
    @FXML private TableColumn<Voyage, String> colEndDate;
    @FXML private Label statusLabel;
    @FXML private TableColumn<Voyage, Void> colActions;

    private final VoyageService voyageService = new VoyageService();   // use service
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @FXML
    public void initialize() {
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

        voyageService.getAllVoyages()
                .thenAccept(response -> {
                    try {
                        if (response.statusCode() == 200) {
                            List<Voyage> voyages = mapper.readValue(response.body(),
                                    new TypeReference<List<Voyage>>() {});
                            javafx.application.Platform.runLater(() -> {
                                voyageTable.setItems(FXCollections.observableArrayList(voyages));
                                statusLabel.setText("Data Loaded Successfully");
                            });
                        } else if (response.statusCode() == 403) {
                            javafx.application.Platform.runLater(() ->
                                    statusLabel.setText("Session Expired. Please login again."));
                        } else {
                            javafx.application.Platform.runLater(() ->
                                    statusLabel.setText("Error: Server returned " + response.statusCode()));
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
            private final Button updateButton = new Button("Update");
            private final Button deleteButton = new Button("Delete");
            private final Button viewActivitiesButton = new Button("View Activities");
            private final Button newOfferButton = new Button("New Offer");
            private final HBox pane = new HBox(10, updateButton, deleteButton, viewActivitiesButton, newOfferButton);

            {
                updateButton.setStyle("-fx-background-color: #F9B729; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                viewActivitiesButton.setStyle("-fx-background-color: #131a22; -fx-text-fill: white;");
                newOfferButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");

                updateButton.setOnAction(event -> {
                    Voyage voyage = getTableView().getItems().get(getIndex());
                    handleUpdate(voyage);
                });

                deleteButton.setOnAction(event -> {
                    Voyage voyage = getTableView().getItems().get(getIndex());
                    handleDelete(voyage);
                });

                viewActivitiesButton.setOnAction(event -> {
                    Voyage voyage = getTableView().getItems().get(getIndex());
                    openActivityView(voyage);
                });

                newOfferButton.setOnAction(event -> {
                    Voyage voyage = getTableView().getItems().get(getIndex());
                    openAddOfferForVoyage(voyage.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void openActivityView(Voyage voyage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/java_project/views/activityView.fxml"));
            Parent root = loader.load();
            ActivityController controller = loader.getController();
            if (controller != null) {
                controller.setVoyageId(voyage.getId());
            }
            Stage stage = new Stage();
            stage.setTitle("Activities - " + voyage.getTitle());
            stage.initModality(Modality.NONE);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Failed to open activity view");
        }
    }

    private void handleUpdate(Voyage voyage) {
        if (voyage == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/java_project/views/voyage/updateVoyageView.fxml"));
            Parent root = loader.load();
            UpdateVoyageController controller = loader.getController();
            if (controller != null) {
                controller.setVoyageData(voyage);
            }
            Stage stage = new Stage();
            stage.setTitle("Update Voyage: " + voyage.getTitle());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadVoyages(); // refresh after closing
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Failed to open update voyage view");
        }
    }

    private void openAddOfferForVoyage(int voyageId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/java_project/views/offer/addOfferView.fxml"));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof java_project.controllers.offer.AddOfferController) {
                ((java_project.controllers.offer.AddOfferController) controller).setVoyageId(voyageId);
            }
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Voyage voyage) {
        if (voyage == null) return;
        int voyageId = voyage.getId();
        statusLabel.setText("Deleting voyage id=" + voyageId + " ...");

        voyageService.deleteVoyage(voyageId)
                .thenAccept(response -> {
                    int code = response.statusCode();
                    if (code == 200 || code == 204) {
                        javafx.application.Platform.runLater(() -> {
                            voyageTable.getItems().remove(voyage);
                            statusLabel.setText("Voyage deleted successfully");
                        });
                    } else if (code == 401 || code == 403) {
                        javafx.application.Platform.runLater(() ->
                                statusLabel.setText("Session expired. Please login again."));
                    } else {
                        javafx.application.Platform.runLater(() ->
                                statusLabel.setText("Delete failed: Server returned " + code));
                    }
                })
                .exceptionally(ex -> {
                    javafx.application.Platform.runLater(() -> statusLabel.setText("Delete failed: Connection error"));
                    ex.printStackTrace();
                    return null;
                });
    }

    @FXML
    private void openAddVoyageModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/java_project/views/voyage/addVoyageView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Register New Voyage");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadVoyages();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}