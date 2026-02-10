package java_project.controllers.user;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.concurrent.CompletableFuture;
import java.net.http.HttpResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import java_project.services.UserService;

public class AddUserController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passField;
    @FXML private ComboBox<String> roleCombo;

    private final UserService userService = new UserService(); // Use UserService for API calls

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    public void initialize() {
        // Populate the Role ComboBox with your defined roles
        roleCombo.setItems(FXCollections.observableArrayList(
            "ADMIN", 
            "USER"
        ));
    }

    /**
     * Handles the Save button action.
     * Validates input and sends a POST request to the API.
     */
@FXML
private void handleSave() {
    if (isInputValid()) {
        // 1. Construct the JSON payload [cite: 2]
        String jsonBody = String.format(
            "{\"name\":\"%s\", \"email\":\"%s\", \"tel\":\"%s\", \"password\":\"%s\", \"role\":\"%s\"}",
            nameField.getText(),
            emailField.getText(),
            phoneField.getText(),
            passField.getText(),
            roleCombo.getValue()
        );

        // 2. Use the UserService to send the request
        // This automatically handles authentication and retries via ApiClient [cite: 3, 4]
        userService.addUser(jsonBody)
            .thenAccept(response -> {
                // Status 201 is standard for successful creation [cite: 2]
                if (response.statusCode() == 201 || response.statusCode() == 200) {
                    Platform.runLater(() -> {
                        showInformation("Success", "User has been registered successfully.");
                        closeWindow();
                    });
                } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                    // Handle cases where even the refresh attempt failed 
                    Platform.runLater(() -> 
                        showError("Session Expired", "Please log in again to register users.")
                    );
                } else {
                    Platform.runLater(() -> 
                        showError("Registration Failed", "Server returned: " + response.body())
                    );
                }
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> 
                    showError("Connection Error", "Could not reach the server: " + ex.getMessage())
                );
                return null;
            });
    }
}

    /**
     * Handles the Cancel button action.
     */
    @FXML
    private void handleCancel() {
        closeWindow();
    }

    /**
     * Simple validation logic.
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (nameField.getText() == null || nameField.getText().isEmpty()) errorMessage += "Name is required!\n";
        if (emailField.getText() == null || !emailField.getText().contains("@")) errorMessage += "Valid email is required!\n";
        if (passField.getText() == null || passField.getText().length() < 6) errorMessage += "Password must be at least 6 characters!\n";
        if (roleCombo.getValue() == null) errorMessage += "Please select a role!\n";

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showError("Invalid Fields", errorMessage);
            return false;
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    // --- Helper UI Methods ---

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInformation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * This is the helper method we completed earlier. 
     * In a real project, move this to an 'ApiService' class to keep controllers clean.
     */
    private CompletableFuture<HttpResponse<String>> sendRequest(String endpoint, String method, String body) {
        // ... (Insert the full sendRequest logic provided in previous turns here)
        return null; // Placeholder for compilation
    }
}