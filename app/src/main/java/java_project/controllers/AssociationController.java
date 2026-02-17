package java_project.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Platform;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.util.List;

import java_project.models.Association;
import java_project.services.AssociationService;

public class AssociationController {

    @FXML private TableView<Association> associationTable;
    @FXML private TableColumn<Association, Integer> colId;
    @FXML private TableColumn<Association, String> colName;
    @FXML private TableColumn<Association, String> colCompanyCode;
    @FXML private TableColumn<Association, Double> colDiscountRate;
    @FXML private TableColumn<Association, Void> colActions;
    @FXML private Label statusLabel;

    private final AssociationService associationService = new AssociationService();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        mapper.registerModule(new JavaTimeModule());
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCompanyCode.setCellValueFactory(new PropertyValueFactory<>("companyCode"));
        colDiscountRate.setCellValueFactory(new PropertyValueFactory<>("discountRate"));

        setupActionsColumn();
        loadAssociations();
    }

    public void loadAssociations() {
        statusLabel.setText("Fetching associations...");
        associationService.getAllAssociations().thenAccept(response -> {
            if (response.statusCode() == 200) {
                try {
                    List<Association> list = mapper.readValue(response.body(), new TypeReference<List<Association>>(){});
                    Platform.runLater(() -> {
                        associationTable.getItems().setAll(list);
                        statusLabel.setText("✅ Associations loaded");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> statusLabel.setText("Failed to parse associations"));
                }
            } else if (response.statusCode() == 403) {
                Platform.runLater(() -> statusLabel.setText("Session expired. Please login again."));
            } else {
                Platform.runLater(() -> statusLabel.setText("Error: server returned " + response.statusCode()));
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(() -> statusLabel.setText("Connection failed"));
            return null;
        });
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final Button linkBtn = new Button("Link User");
            private final Button viewUsersBtn = new Button("View Users");
            private final HBox pane = new HBox(8, editBtn, linkBtn, viewUsersBtn, deleteBtn);
            {
                editBtn.getStyleClass().add("update-btn");
                deleteBtn.getStyleClass().add("delete-btn");
                linkBtn.getStyleClass().add("docs-btn");
                viewUsersBtn.getStyleClass().add("docs-btn");

                editBtn.setOnAction(e -> openUpdateModal(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> {
                    Association a = getTableView().getItems().get(getIndex());
                    associationService.deleteAssociation(a.getId()).thenAccept(res -> {
                        if (res.statusCode() == 200 || res.statusCode() == 204) Platform.runLater(() -> refreshTable());
                    });
                });

                linkBtn.setOnAction(e -> openLinkModal(getTableView().getItems().get(getIndex())));
                viewUsersBtn.setOnAction(e -> openLinkedUsersModal(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void refreshTable() { loadAssociations(); }

    @FXML
    private void openAddModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/java_project/views/association/addAssociationView.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadAssociations();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openUpdateModal(Association association) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/java_project/views/association/updateAssociationView.fxml"));
            Parent root = loader.load();
            java_project.controllers.association.UpdateAssociationController ctrl = loader.getController();
            ctrl.setAssociationData(association);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadAssociations();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openLinkModal(Association association) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/java_project/views/association/linkAssociationView.fxml"));
            Parent root = loader.load();
            java_project.controllers.association.LinkAssociationController ctrl = loader.getController();
            ctrl.setAssociation(association);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadAssociations();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openLinkedUsersModal(Association association) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/java_project/views/association/linkedUsersView.fxml"));
            Parent root = loader.load();
            java_project.controllers.association.LinkedUsersController ctrl = loader.getController();
            ctrl.setAssociation(association);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadAssociations();
        } catch (IOException e) { e.printStackTrace(); }
    }
}