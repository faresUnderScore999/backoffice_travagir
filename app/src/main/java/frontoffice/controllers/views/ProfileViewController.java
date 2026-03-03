package frontoffice.controllers.views;

import frontoffice.models.UserProfile;
import frontoffice.services.UserProfileService;
import frontoffice.utils.HttpErrorParser;
import frontoffice.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.Map;

public class ProfileViewController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField imageUrlField;
    @FXML
    private Label statusLabel;

    private final UserProfileService userProfileService = new UserProfileService();

    @FXML
    public void initialize() {
        onRefresh();
    }

    @FXML
    private void onRefresh() {
        statusLabel.setText("Loading...");
        userProfileService.getMe()
                .thenAccept(profile -> Platform.runLater(() -> {
                    setFields(profile);
                    statusLabel.setText(profile == null ? "Failed to load profile." : "");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Load error: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void onSave() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            statusLabel.setText("Please login.");
            return;
        }

        Map<String, Object> update = new HashMap<>();
        update.put("name", trim(nameField.getText()));
        update.put("email", trim(emailField.getText()));
        update.put("tel", trim(phoneField.getText()));
        update.put("imageUrl", trim(imageUrlField.getText()));

        statusLabel.setText("Saving...");
        userProfileService.updateProfile(user.id(), update)
                .thenAccept(resp -> Platform.runLater(() -> {
                    if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                        statusLabel.setText("Saved.");
                        onRefresh();
                    } else {
                        String details = HttpErrorParser.bestEffortMessage(resp.body());
                        statusLabel.setText("Save failed (" + resp.statusCode() + ")" + (details.isBlank() ? "" : ": " + details));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Save error: " + ex.getMessage()));
                    return null;
                });
    }

    private void setFields(UserProfile profile) {
        if (profile == null) {
            nameField.setText("");
            emailField.setText("");
            phoneField.setText("");
            imageUrlField.setText("");
            return;
        }

        nameField.setText(profile.getName() == null ? "" : profile.getName());
        emailField.setText(profile.getEmail() == null ? "" : profile.getEmail());
        phoneField.setText(profile.getTel() == null ? "" : profile.getTel());
        imageUrlField.setText(profile.getImageUrl() == null ? "" : profile.getImageUrl());
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
