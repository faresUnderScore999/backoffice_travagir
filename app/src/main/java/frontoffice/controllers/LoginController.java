package frontoffice.controllers;

import frontoffice.services.AuthService;
import frontoffice.utils.SceneNavigator;
import frontoffice.utils.HttpErrorParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void onLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        messageLabel.setText("Logging in...");

        new Thread(() -> {
            try {
                var response = authService.login(email, password);
                Platform.runLater(() -> {
                    if (response.statusCode() == 200) {
                        SceneNavigator.goToDashboard();
                    } else {
                        String details = HttpErrorParser.bestEffortMessage(response.body());
                        messageLabel.setText("Login failed (" + response.statusCode() + ")" + (details.isBlank() ? "" : ": " + details));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> messageLabel.setText("Login error: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void onGoToRegister() {
        SceneNavigator.goToRegister();
    }
}
