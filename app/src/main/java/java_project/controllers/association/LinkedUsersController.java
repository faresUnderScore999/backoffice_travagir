package java_project.controllers.association;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.application.Platform;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

import java_project.models.Association;
import java_project.models.User;
import java_project.services.AssociationService;

public class LinkedUsersController {
    @FXML private Label titleLabel;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colTel;
    @FXML private TableColumn<User, Void> colActions;
    @FXML private Label statusLabel;

    private final AssociationService associationService = new AssociationService();
    private final ObjectMapper mapper = new ObjectMapper();
    private Association association;

    public void setAssociation(Association a) {
        this.association = a;
        titleLabel.setText("Users Linked to: " + a.getName());
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("tel"));
        setupActionsColumn();
        loadLinkedUsers();
    }

    private void loadLinkedUsers() {
        statusLabel.setText("Fetching linked users...");
        associationService.getLinkedUsers(association.getId()).thenAccept(response -> {
            if (response.statusCode() == 200) {
                try {
                    List<User> users = mapper.readValue(response.body(), new TypeReference<List<User>>(){});
                    Platform.runLater(() -> {
                        usersTable.getItems().setAll(users);
                        statusLabel.setText("✅ Loaded " + users.size() + " users");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> statusLabel.setText("Failed to parse users"));
                }
            } else {
                Platform.runLater(() -> statusLabel.setText("Error: " + response.statusCode()));
            }
        }).exceptionally(ex -> {
            Platform.runLater(() -> statusLabel.setText("Connection failed"));
            return null;
        });
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button unlinkBtn = new Button("Unlink");
            {
                unlinkBtn.getStyleClass().add("delete-btn");
                unlinkBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    unlinkUser(user.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : unlinkBtn);
            }
        });
    }

    private void unlinkUser(int userId) {
        statusLabel.setText("Unlinking user...");
        associationService.unlinkUser(userId, association.getId()).thenAccept(resp -> {
            Platform.runLater(() -> {
                if (resp.statusCode() == 200 || resp.statusCode() == 204) {
                    statusLabel.setText("✅ User unlinked");
                    loadLinkedUsers();
                } else {
                    statusLabel.setText("Error: " + resp.statusCode());
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> statusLabel.setText("Connection failed"));
            return null;
        });
    }

    @FXML
    private void handleClose() {
        ((Stage) titleLabel.getScene().getWindow()).close();
    }
}
