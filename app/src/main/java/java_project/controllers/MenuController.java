package java_project.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import java_project.utils.SessionManager;

public class MenuController implements Initializable {

    @FXML private ImageView profileImage;
    @FXML private Label profileName;
    @FXML private Label profileRole;

    @FXML
    private void handleMenuClick(MouseEvent event) {
        HBox clickedItem = (HBox) event.getSource();
        String fxmlPath = "";

        // Determine path based on ID
        if (clickedItem.getId().equals("menuVoyage"))
            fxmlPath = "/java_project/views/voyageView.fxml";
        else if (clickedItem.getId().equals("menuUser"))
            fxmlPath = "/java_project/views/gestionuserView.fxml";
        else if (clickedItem.getId().equals("menuReservation"))
            fxmlPath = "/java_project/views/reservationView.fxml";
        else if (clickedItem.getId().equals("menuOffer"))
            fxmlPath = "/java_project/views/offerManagementView.fxml";
        else if (clickedItem.getId().equals("menuPromoCode"))
            fxmlPath = "/java_project/views/promoCodeView.fxml";
        else if (clickedItem.getId().equals("menuAssociation"))
            fxmlPath = "/java_project/views/association/associationManagementView.fxml";
        else if (clickedItem.getId().equals("menuUserOffer"))
            fxmlPath = "/java_project/views/userOfferView.fxml";
        else if (clickedItem.getId().equals("menuReclamation"))
            fxmlPath = "/java_project/views/reclamationView.fxml";
        else if (clickedItem.getId().equals("menuRefund"))
            fxmlPath = "/java_project/views/refundView.fxml";
        else if (clickedItem.getId().equals("menuActivity"))
            fxmlPath = "/java_project/views/activityView.fxml";
        else if (clickedItem.getId().equals("menuDashboard"))
            fxmlPath = "/java_project/views/dashboard.fxml";

        try {
            BorderPane root = (BorderPane) clickedItem.getScene().getRoot();

            // 1. Load and Swap Content
            var resUrl = getClass().getResource(fxmlPath);
            if (resUrl == null) {
                System.err.println("FXML resource not found: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resUrl);
            try {
                root.setCenter(loader.load());
            } catch (IOException ioe) {
                // Common problem: stray BOM or invalid chars before XML prolog
                String msg = ioe.getMessage() != null ? ioe.getMessage() : "";
                if (msg.contains("Content is not allowed in prolog") || msg.contains("XMLStreamException")) {
                    try (var is = resUrl.openStream()) {
                        byte[] raw = is.readAllBytes();
                        byte[] cleaned = raw;
                        // strip UTF-8 BOM if present
                        if (raw.length >= 3 && (raw[0] & 0xFF) == 0xEF && (raw[1] & 0xFF) == 0xBB
                                && (raw[2] & 0xFF) == 0xBF) {
                            cleaned = java.util.Arrays.copyOfRange(raw, 3, raw.length);
                        }
                        try (var bais = new java.io.ByteArrayInputStream(cleaned)) {
                            FXMLLoader fallback = new FXMLLoader();
                            root.setCenter(fallback.load(bais));
                            System.out.println("Loaded FXML after stripping BOM: " + fxmlPath);
                        }
                    } catch (Exception ex2) {
                        System.err.println("Failed fallback load for " + fxmlPath + ": " + ex2.getMessage());
                        ioe.printStackTrace();
                    }
                } else {
                    throw ioe;
                }
            }

            // 2. CSS Logic: Reset all, then set clicked one
            // We look inside the Sidebar (left side of BorderPane)
            VBox sidebar = (VBox) root.getLeft();
            sidebar.lookupAll(".menu-item").forEach(node -> {
                node.getStyleClass().remove("active");
            });

            clickedItem.getStyleClass().add("active");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            try {
                profileName.setText(user.name());
                if (user.imageUrl() != null && !user.imageUrl().isEmpty()) {
                    try {
                        // try synchronous load to be able to detect failure immediately
                        Image img = new Image(user.imageUrl(), 48, 48, true, true, false);
                        if (!img.isError()) {
                            profileImage.setImage(img);
                        } else {
                            // fallback to bundled icon
                            var is = getClass().getResourceAsStream("/java_project/icons/users.png");
                            if (is != null) profileImage.setImage(new Image(is));
                        }
                    } catch (Exception ex) {
                        var is = getClass().getResourceAsStream("/java_project/icons/users.png");
                        if (is != null) profileImage.setImage(new Image(is));
                    }
                } else {
                    var is = getClass().getResourceAsStream("/java_project/icons/users.png");
                    if (is != null) profileImage.setImage(new Image(is));
                }
            } catch (Exception ignored) {
            }
        }
    }

}