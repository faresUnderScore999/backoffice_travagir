package frontoffice.controllers;

import frontoffice.models.UserRegistration;
import frontoffice.services.UserService;
import frontoffice.utils.HttpErrorParser;
import frontoffice.utils.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField telField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    private void onRegister() {
        UserRegistration registration = new UserRegistration();
        registration.setName(nameField.getText());
        registration.setEmail(emailField.getText());
        registration.setTel(telField.getText());
        registration.setPassword(passwordField.getText());

        messageLabel.setText("Creating account...");

        userService.register(registration)
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response.statusCode() == 201) {
                        messageLabel.setText("Account created. Please login.");
                        SceneNavigator.goToLogin();
                    } else {
                        String details = HttpErrorParser.bestEffortMessage(response.body());
                        messageLabel.setText("Register failed (" + response.statusCode() + ")" + (details.isBlank() ? "" : ": " + details));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> messageLabel.setText("Register error: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void onBackToLogin() {
        SceneNavigator.goToLogin();
    }
}
