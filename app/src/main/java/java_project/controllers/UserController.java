package java_project.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import javafx.stage.Modality;
import javafx.stage.Stage;
import java_project.models.User;
import java_project.services.UserService; 

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class UserController {
    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Integer> colId;
    @FXML
    private TableColumn<User, String> colName;
    @FXML
    private TableColumn<User, String> colEmail;
    @FXML
    private TableColumn<User, String> colTel;
    @FXML
    private TableColumn<User, Void> colActions;
    @FXML
    private Label statusLabel;

    // Use the centralized ApiClient

    private final UserService userService = new UserService(); // Use UserService for API calls
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("tel"));

        loadUsers();
        addButtonToTable();
    }

@FXML
public void loadUsers() {
    javafx.application.Platform.runLater(() -> statusLabel.setText("Fetching users..."));

    // 1. Use the UserService instead of raw ApiClient calls
    userService.getAllUsers()
            .thenAccept(response -> {
                try {
                    // Check for successful response (Status 200)
                    if (response.statusCode() == 200) {
                        // 2. Parse JSON into List
                        List<User> users = mapper.readValue(response.body(), new TypeReference<List<User>>() {});

                        // 3. Update UI on the JavaFX Thread
                        javafx.application.Platform.runLater(() -> {
                            userTable.getItems().setAll(users);
                            statusLabel.setText("Users loaded successfully.");
                        });
                    } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                        // This handles cases where both access and refresh tokens failed 
                        javafx.application.Platform.runLater(() -> 
                            statusLabel.setText("Session Expired. Please login again.")
                        );
                    } else {
                        javafx.application.Platform.runLater(() -> 
                            statusLabel.setText("Error: Server returned " + response.statusCode())
                        );
                    }
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> statusLabel.setText("Error parsing user data"));
                    e.printStackTrace();
                }
            })
            .exceptionally(ex -> {
                // 4. Handle Network Errors (e.g., Server Offline)
                javafx.application.Platform.runLater(() -> 
                    statusLabel.setText("Network error: " + ex.getMessage())
                );
                return null;
            });
}

    private void addButtonToTable() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button updateButton = new Button("Update");
            private final Button deleteButton = new Button("Delete");
            private final HBox pane = new HBox(10, updateButton, deleteButton);

            {
                updateButton.getStyleClass().add("update-btn");
                deleteButton.getStyleClass().add("delete-btn");

                updateButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    // Call your update logic
                    handleUpdate(user);
                });

                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    // Call your delete logic
                    handleDelete(user);
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

    private void handleUpdate(User user) {
        System.out.println("Update User: " + user.getName());
        statusLabel.setText("Update User: " + user.getName());
        // Open update dialog or modify fields
    }

 private void handleDelete(User user) {
    // 1. Initial UI feedback
    statusLabel.setText("Deleting User: " + user.getName());

    // 2. Call the UserService (which uses sendWithRetry internally)
    userService.deleteUser(String.valueOf(user.getId()))
            .thenAccept(response -> {
                if (response.statusCode() == 200 || response.statusCode() == 204) {
                    // 3. Success: Update UI on the JavaFX Thread
                    javafx.application.Platform.runLater(() -> {
                        userTable.getItems().remove(user);
                        statusLabel.setText("User '" + user.getName() + "' deleted successfully.");
                    });
                } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                    // Handle session expiration
                    javafx.application.Platform.runLater(() -> 
                        statusLabel.setText("Session Expired. Access denied.")
                    );
                } else {
                    // Handle server errors (e.g., 404 or 500)
                    javafx.application.Platform.runLater(() -> 
                        statusLabel.setText("Delete failed: Server returned " + response.statusCode())
                    );
                }
            })
            .exceptionally(ex -> {
                // 4. Handle Network Failures
                javafx.application.Platform.runLater(() -> 
                    statusLabel.setText("Network error: " + ex.getMessage())
                );
                return null;
            });
}


    @FXML
    private void openAddUserModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/java_project/views/addUserView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Register New User");
            stage.initModality(Modality.APPLICATION_MODAL); // Blocks the main window until closed
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh the table after the modal closes
            loadUsers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}