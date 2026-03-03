package frontoffice.controllers.views;

import frontoffice.models.Activity;
import frontoffice.services.ActivityService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ActivitiesViewController {
    @FXML
    private TextField voyageIdField;
    @FXML
    private TableView<Activity> activitiesTable;
    @FXML
    private TableColumn<Activity, Integer> colId;
    @FXML
    private TableColumn<Activity, String> colName;
    @FXML
    private TableColumn<Activity, String> colLocation;
    @FXML
    private TableColumn<Activity, Integer> colDuration;
    @FXML
    private TableColumn<Activity, Double> colPrice;
    @FXML
    private Label statusLabel;

    private final ActivityService activityService = new ActivityService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("durationHours"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("pricePerPerson"));
    }

    @FXML
    private void onLoad() {
        int voyageId;
        try {
            voyageId = Integer.parseInt(voyageIdField.getText().trim());
        } catch (Exception e) {
            statusLabel.setText("Voyage ID must be a number.");
            return;
        }

        statusLabel.setText("Loading...");
        activityService.getByVoyage(voyageId)
                .thenAccept(list -> Platform.runLater(() -> {
                    activitiesTable.setItems(FXCollections.observableArrayList(list));
                    statusLabel.setText(list.isEmpty() ? "No activities." : "");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Load error: " + ex.getMessage()));
                    return null;
                });
    }
}
