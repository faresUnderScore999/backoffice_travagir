package java_project.controllers.user;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import javafx.application.Platform;
import java_project.services.UploadService;
import java_project.services.UserService;
import java_project.models.User;

public class UpdateUserController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField imageUrlField;
    @FXML private ImageView avatarPreview;
    @FXML private PasswordField passField;
    @FXML private ComboBox<String> roleCombo;

    private final UserService userService = new UserService();
    private final UploadService uploadService = new UploadService();

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
        imageUrlField.setText(user.getImageUrl());
        updateAvatarPreview(user.getImageUrl());
    
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
                imageUrlField.getText(),
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

    /**
     * Opens a file chooser and uploads the selected image.  
     * The server returns a JSON payload containing a `secure_url` field;
     * we parse it and display the value in the `imageUrlField`.
     */
    @FXML
    private void handleChooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Avatar Image");
        // optionally filter by image types
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = chooser.showOpenDialog(nameField.getScene().getWindow());
        if (file != null) {
            Path path = file.toPath();
            uploadService.uploadImage(path)
                .thenAccept(response -> {
                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        String body = response.body();
                        String url = extractSecureUrl(body);
                        if (url != null) {
                            Platform.runLater(() -> {
                                imageUrlField.setText(url);
                                updateAvatarPreview(url);
                            });
                        } else {
                            Platform.runLater(() -> showError("Upload failed", "No secure_url in response."));
                        }
                    } else {
                        Platform.runLater(() -> showError("Upload failed", "Server returned: " + response.statusCode()));
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> showError("Upload error", ex.getMessage()));
                    return null;
                });
        }
    }

    /**
     * Simple parser to find the value of `secure_url` in a JSON response.
     * This keeps us free of additional JSON dependencies in the project.
     */
    private String extractSecureUrl(String json) {
        int idx = json.indexOf("\"secure_url\"");
        if (idx != -1) {
            int colon = json.indexOf(':', idx);
            int firstQuote = json.indexOf('"', colon + 1);
            int secondQuote = json.indexOf('"', firstQuote + 1);
            if (firstQuote != -1 && secondQuote != -1) {
                return json.substring(firstQuote + 1, secondQuote);
            }
        }
        return null;
    }

    private void updateAvatarPreview(String url) {
        if (url == null || url.isEmpty()) {
            avatarPreview.setImage(null);
            return;
        }
        try {
            Image img = new Image(url, 50, 50, true, true);
            avatarPreview.setImage(img);
        } catch (Exception e) {
            // silently ignore invalid URLs
            avatarPreview.setImage(null);
        }
    }

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