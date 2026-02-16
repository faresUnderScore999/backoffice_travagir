package java_project.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java_project.models.Activity;
import java_project.services.ActivityService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class ActivityController {
    @FXML private TableView<Activity> activityTable;
    @FXML private TableColumn<Activity, Integer> colId;
    @FXML private TableColumn<Activity, String> colName;
    @FXML private TableColumn<Activity, String> colLocation;
    @FXML private TableColumn<Activity, Double> colPrice;
    @FXML private Label statusLabel; // Indicateur que tout fonctionne

    private final ActivityService activityService = new ActivityService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("pricePerPerson"));
        loadActivities();
    }

    @FXML
    private void loadActivities() {
        if (statusLabel != null) statusLabel.setText("⌛ Chargement...");

        activityService.getAllActivities().thenAccept(response -> {
            Platform.runLater(() -> { // Toujours mettre à jour l'UI dans Platform.runLater
                if (response.statusCode() == 200) {
                    try {
                        List<Activity> activities = objectMapper.readValue(response.body(), new TypeReference<List<Activity>>() {});
                        activityTable.setItems(FXCollections.observableArrayList(activities));
                        if (statusLabel != null) statusLabel.setText("✅ " + activities.size() + " activités chargées.");
                    } catch (Exception e) {
                        if (statusLabel != null) statusLabel.setText("❌ Erreur de données.");
                        e.printStackTrace();
                    }
                } else {
                    if (statusLabel != null) statusLabel.setText("❌ Erreur serveur: " + response.statusCode());
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                if (statusLabel != null) statusLabel.setText("❌ Serveur injoignable.");
            });
            return null;
        });
    }
}