package java_project.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import java_project.models.Activity;
import java_project.services.ActivityService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Optional;

public class ActivityController {
    @FXML private TableView<Activity> activityTable;
    @FXML private TableColumn<Activity, Integer> colId;
    @FXML private TableColumn<Activity, String> colName;
    @FXML private TableColumn<Activity, String> colDescription;
    @FXML private TableColumn<Activity, String> colLocation;
    @FXML private TableColumn<Activity, Double> colPrice;
    @FXML private TableColumn<Activity, Void> colActions;
    @FXML private Label statusLabel; // Indicateur que tout fonctionne

    private final ActivityService activityService = new ActivityService();
        private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private Integer voyageId;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (colDescription != null) {
            colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        }
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("pricePerPerson"));
        addButtonsToTable();
        // If opened from Voyage view, VoyageController will call setVoyageId(...).
        // If opened from the menu, there is no backend endpoint for "get all activities",
        // so we show a hint message.
        loadActivities();
    }

    public void setVoyageId(int voyageId) {
        this.voyageId = voyageId;
        loadActivities();
    }

    @FXML
    private void openAddActivityModal() {
        if (voyageId == null) {
            if (statusLabel != null) statusLabel.setText("ℹ️ Open activities from a voyage first.");
            return;
        }

        Optional<Activity> result = showActivityDialog(null);
        result.ifPresent(activity -> {
            String jsonBody = buildActivityJson(activity, false);
            if (jsonBody == null) return;

            if (statusLabel != null) statusLabel.setText("⌛ Adding activity...");
            activityService.addActivity(jsonBody)
                    .thenAccept(response -> Platform.runLater(() -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            if (statusLabel != null) statusLabel.setText("✅ Activity added.");
                            loadActivities();
                        } else {
                            if (statusLabel != null) statusLabel.setText("❌ Add failed: " + response.statusCode());
                        }
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            if (statusLabel != null) statusLabel.setText("❌ Server unreachable.");
                        });
                        return null;
                    });
        });
    }

    @FXML
    private void loadActivities() {
        if (voyageId == null) {
            if (statusLabel != null) statusLabel.setText("ℹ️ Select a voyage and click 'View Activities'.");
            if (activityTable != null) activityTable.setItems(FXCollections.observableArrayList());
            return;
        }

        if (statusLabel != null) statusLabel.setText("⌛ Chargement...");

        activityService.getActivitiesByVoyage(voyageId).thenAccept(response -> {
            Platform.runLater(() -> { // Toujours mettre à jour l'UI dans Platform.runLater
                if (response.statusCode() == 200) {
                    try {
                        List<Activity> activities = objectMapper.readValue(response.body(), new TypeReference<List<Activity>>() {});
                        activityTable.setItems(FXCollections.observableArrayList(activities));
                        if (statusLabel != null) {
                            statusLabel.setText("✅ " + activities.size() + " activités chargées (voyage #" + voyageId + ").");
                        }
                    } catch (Exception e) {
                        if (statusLabel != null) statusLabel.setText("Erreur de données.");
                        e.printStackTrace();
                    }
                } else {
                    if (statusLabel != null) statusLabel.setText("Erreur serveur: " + response.statusCode());
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                if (statusLabel != null) statusLabel.setText("Erreur de connexion au serveur.");
            });
            return null;
        });
    }

    private void addButtonsToTable() {
        if (colActions == null) return;

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button updateButton = new Button("Update");
            private final Button deleteButton = new Button("Delete");
            private final HBox pane = new HBox(10, updateButton, deleteButton);

            {
                updateButton.setStyle("-fx-background-color: #F9B729; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                updateButton.setOnAction(event -> {
                    Activity activity = getTableView().getItems().get(getIndex());
                    openUpdateActivityModal(activity);
                });

                deleteButton.setOnAction(event -> {
                    Activity activity = getTableView().getItems().get(getIndex());
                    handleDeleteActivity(activity);
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

    private void openUpdateActivityModal(Activity existing) {
        if (existing == null) return;
        if (voyageId == null) {
            if (statusLabel != null) statusLabel.setText("ℹ️ Open activities from a voyage first.");
            return;
        }

        Optional<Activity> result = showActivityDialog(existing);
        result.ifPresent(updated -> {
            String jsonBody = buildActivityJson(updated, true);
            if (jsonBody == null) return;

            if (statusLabel != null) statusLabel.setText("⌛ Updating activity...");
            activityService.updateActivity(existing.getId(), jsonBody)
                    .thenAccept(response -> Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            if (statusLabel != null) statusLabel.setText("✅ Activity updated.");
                            loadActivities();
                        } else {
                            if (statusLabel != null) statusLabel.setText("❌ Update failed: " + response.statusCode());
                        }
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            if (statusLabel != null) statusLabel.setText("❌ Server unreachable.");
                        });
                        return null;
                    });
        });
    }

    private void handleDeleteActivity(Activity activity) {
        if (activity == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete activity: " + activity.getName());
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        if (statusLabel != null) statusLabel.setText("⌛ Deleting activity...");
        activityService.deleteActivity(activity.getId())
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.statusCode() == 200 || response.statusCode() == 204) {
                        if (statusLabel != null) statusLabel.setText("✅ Activity deleted.");
                        loadActivities();
                    } else {
                        if (statusLabel != null) statusLabel.setText("❌ Delete failed: " + response.statusCode());
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        if (statusLabel != null) statusLabel.setText("❌ Server unreachable.");
                    });
                    return null;
                });
    }

    private Optional<Activity> showActivityDialog(Activity existing) {
        Dialog<Activity> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Activity" : "Update Activity");
        dialog.setHeaderText(existing == null ? "Create a new activity" : "Update activity details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField locationField = new TextField();
        locationField.setPromptText("Location");
        TextField priceField = new TextField();
        priceField.setPromptText("Price per person");
        TextField durationField = new TextField();
        durationField.setPromptText("Duration hours");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");
        descriptionArea.setPrefRowCount(3);

        if (existing != null) {
            nameField.setText(existing.getName());
            locationField.setText(existing.getLocation());
            priceField.setText(String.valueOf(existing.getPricePerPerson()));
            durationField.setText(String.valueOf(existing.getDurationHours()));
            descriptionArea.setText(existing.getDescription());
        }

        grid.add(new Label("Name"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Location"), 0, 1);
        grid.add(locationField, 1, 1);
        grid.add(new Label("Price"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Duration (h)"), 0, 3);
        grid.add(durationField, 1, 3);
        grid.add(new Label("Description"), 0, 4);
        grid.add(descriptionArea, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(nameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton != saveButtonType) return null;

            String name = nameField.getText() == null ? "" : nameField.getText().trim();
            String location = locationField.getText() == null ? "" : locationField.getText().trim();
            String description = descriptionArea.getText() == null ? "" : descriptionArea.getText().trim();
            if (name.isEmpty()) return null;

            int duration;
            double price;
            try {
                duration = Integer.parseInt(durationField.getText().trim());
                price = Double.parseDouble(priceField.getText().trim());
            } catch (Exception e) {
                return null;
            }

            Activity activity = new Activity();
            activity.setVoyageId(voyageId == null ? 0 : voyageId);
            activity.setName(name);
            activity.setLocation(location);
            activity.setDescription(description);
            activity.setDurationHours(duration);
            activity.setPricePerPerson(price);
            return activity;
        });

        return dialog.showAndWait();
    }

    private String buildActivityJson(Activity activity, boolean isUpdate) {
        if (voyageId == null) return null;
        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("voyageId", voyageId);
            payload.put("name", activity.getName());
            payload.put("description", activity.getDescription());
            payload.put("durationHours", activity.getDurationHours());
            payload.put("pricePerPerson", activity.getPricePerPerson());
            payload.put("location", activity.getLocation());
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            e.printStackTrace();
            if (statusLabel != null) statusLabel.setText("❌ Failed to build request body");
            return null;
        }
    }
}