package java_project.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MenuController {

@FXML
private void handleMenuClick(MouseEvent event) {
    HBox clickedItem = (HBox) event.getSource();
    String fxmlPath = "";

    // Determine path based on ID
    if (clickedItem.getId().equals("menuVoyage")) fxmlPath = "/java_project/views/voyageView.fxml";
    else if (clickedItem.getId().equals("menuUser")) fxmlPath = "/java_project/views/gestionuserView.fxml";
    else if (clickedItem.getId().equals("menuReservation")) fxmlPath = "/java_project/views/reservationView.fxml";
      else if (clickedItem.getId().equals("menuOffer")) fxmlPath = "/java_project/views/offerManagementView.fxml";
        else if (clickedItem.getId().equals("menuReclamation")) fxmlPath = "/java_project/views/reclamationView.fxml";
    // else if (clickedItem.getId().equals("menuDashboard")) fxmlPath = "/java_project/views/dashboardView.fxml";

    try {
        BorderPane root = (BorderPane) clickedItem.getScene().getRoot();
        
        // 1. Load and Swap Content
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        root.setCenter(loader.load());

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


}