package java_project.controllers.association;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import java_project.models.Association;
import java_project.services.AssociationService;

public class LinkAssociationController {
    @FXML private TextField userIdField;
    @FXML private Label assocLabel;
    @FXML private Label statusLabel;

    private final AssociationService associationService = new AssociationService();
    private Association association;

    public void setAssociation(Association a) {
        this.association = a;
        assocLabel.setText(a.getName() + " (id=" + a.getId() + ")");
    }

    @FXML
    private void handleLink() {
        Integer userId = parseUserId();
        if (userId == null) return;
        statusLabel.setText("Linking user...");
        associationService.linkUser(userId, association.getId()).thenAccept(resp -> {
            Platform.runLater(() -> {
                if (resp.statusCode() == 200 || resp.statusCode() == 201) {
                    statusLabel.setText("✅ Linked successfully");
                    close();
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
    private void handleUnlink() {
        Integer userId = parseUserId();
        if (userId == null) return;
        statusLabel.setText("Unlinking user...");
        associationService.unlinkUser(userId, association.getId()).thenAccept(resp -> {
            Platform.runLater(() -> {
                if (resp.statusCode() == 200 || resp.statusCode() == 204) {
                    statusLabel.setText("✅ Unlinked successfully");
                    close();
                } else {
                    statusLabel.setText("Error: " + resp.statusCode());
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> statusLabel.setText("Connection failed"));
            return null;
        });
    }

    private Integer parseUserId() {
        String raw = userIdField.getText();
        if (raw == null || raw.trim().isEmpty()) {
            statusLabel.setText("User ID is required");
            return null;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            statusLabel.setText("User ID must be a number");
            return null;
        }
    }

    @FXML
    private void handleCancel() { close(); }

    private void close() {
        ((Stage) userIdField.getScene().getWindow()).close();
    }
}