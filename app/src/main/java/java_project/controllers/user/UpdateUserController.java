package java_project.controllers.user;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.http.HttpResponse;
import javafx.application.Platform;
import java_project.services.UserService;
import java_project.models.User;

public class UpdateUserController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField imageUrlField;
    @FXML private PasswordField passField;
    @FXML private ComboBox<String> roleCombo;

    private final UserService userService = new UserService();
    private int userId; // Stores the ID of the user being updated

    @FXML
    public void initialize() {
        roleCombo.getItems().addAll("ADMIN", "USER");
    }

    /**
     * Pre-fills the form with existing user data.
     */
    public void setUserData(User user) {
        this.userId = user.getId();
        nameField.setText(user.getName());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getTel());
    
        // roleCombo.setValue(user.getRole()); // Set if role is in User model
    }

    @FXML
    private void handleUpdate() {
        if (isInputValid()) {
            // 1. Construct JSON payload based on your request
            String jsonBody = String.format(
                "{\"name\":\"%s\", \"email\":\"%s\", \"password\":\"%s\", \"imageUrl\":\"%s\", \"tel\":\"%s\"}",
                nameField.getText(),
                emailField.getText(),
                passField.getText(),
                "imageUrlField.getText()",
                phoneField.getText()
                // roleCombo.getValue().toLowerCase()
            );

            // 2. Call UserService.updateUser
            userService.updateUser(String.valueOf(userId), jsonBody)
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        Platform.runLater(() -> {
                            showInformation("Success", "User updated successfully.");
                            closeWindow();
                        });
                    } else {
                        Platform.runLater(() -> 
                            showError("Update Failed", "Server returned: " + response.statusCode())
                        );
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> showError("Error", "Connection failed."));
                    return null;
                });
        }
    }

    @FXML private void handleCancel() { closeWindow(); }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private boolean isInputValid() {
        // reuse validation logic from AddUserController
        return !nameField.getText().isEmpty() && emailField.getText().contains("@");
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInformation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}