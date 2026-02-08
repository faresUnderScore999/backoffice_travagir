package java_project.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java_project.models.User;
import java_project.models.User;
import java_project.services.ApiClient; // Import ApiClient
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
    private final ApiClient apiClient = new ApiClient();
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

        // Use the ApiClient to handle authentication and refresh automatically
        apiClient.sendWithRetry("/api/v1/users/all", "GET", null)
                .thenAccept(response -> {
                    try {
                        if (response.statusCode() == 200) {
                            // Parse JSON into List
                            List<User> users = mapper.readValue(response.body(), new TypeReference<List<User>>() {
                            });

                            javafx.application.Platform.runLater(() -> {
                                userTable.getItems().setAll(users);
                                statusLabel.setText("Users loaded successfully.");
                            });
                        } else {
                            javafx.application.Platform.runLater(
                                    () -> statusLabel.setText("Error: Server returned " + response.statusCode()));
                        }
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> statusLabel.setText("Error parsing user data"));
                        e.printStackTrace();
                    }
                })
                .exceptionally(ex -> {
                    javafx.application.Platform
                            .runLater(() -> statusLabel.setText("Network error: " + ex.getMessage()));
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
        System.out.println("Delete User: " + user.getName());
        statusLabel.setText("Delete User: " + user.getName());
        apiClient.sendWithRetry("/api/v1/admins/manage/users/" + user.getId(), "DELETE", null)
                .thenAccept(response -> {
                    try {
                        if (response.statusCode() == 200) {

                            statusLabel.setText("Users deleted successfully.");
                            ;
                        } else {

                            statusLabel.setText("Error: Server returned " + response.statusCode());
                        }
                    } catch (Exception e) {
                        statusLabel.setText("Error parsing user data");
                        e.printStackTrace();
                    }
                })
                .exceptionally(ex -> {
                    statusLabel.setText("Network error: " + ex.getMessage());
                    return null;
                });
        // Remove from table + database
        userTable.getItems().remove(user);
    }

    @FXML
private void handleMenuClick(MouseEvent event) {
    HBox clickedItem = (HBox) event.getSource();
    String fxmlPath = "/java_project/views/addUserView.fxml";


    try {
        BorderPane root = (BorderPane) clickedItem.getScene().getRoot();
        
        // 1. Load and Swap Content
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        root.setCenter(loader.load());

        

    } catch (IOException e) {
        e.printStackTrace();
    }
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