package java_project.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import java_project.services.AuthService;
import javafx.scene.control.Label;
import java.io.IOException;
import java.net.http.HttpResponse;

public class LoginController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    private final AuthService authService = new AuthService();

    @FXML
    public void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String pass = passwordField.getText();

        try {
            HttpResponse<String> response = authService.login(email, pass);

            if (response.statusCode() == 200) {
                // SUCCESS: Switch to the Home Page (Trips View)
                Platform.runLater(() -> {
                    try {
                        // Inside Platform.runLater
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Get the current
                                                                                                 // stage
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/java_project/views/MainLayout.fxml"));
                        BorderPane mainLayout = loader.load();
                        // ... rest of your logic

                        // Load default center content (Voyage)
                        FXMLLoader contentLoader = new FXMLLoader(
                                getClass().getResource("/java_project/views/voyageView.fxml"));
                        mainLayout.setCenter(contentLoader.load());

                        Scene scene = new Scene(mainLayout);
                        stage.setScene(scene);
                        stage.setTitle("Trip Manager - Home");
                        stage.show();

                    } catch (IOException e) {
                        System.err.println("Error loading Home Page: " + e.getMessage());
                    }
                });
            } else {
                errorLabel.setText("Invalid credentials. Try again."+ " Status: " + response.statusCode());
            }
        } catch (Exception e) {
            errorLabel.setText("Server error: " + e.getMessage());
        }
    }
}