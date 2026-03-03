package frontoffice.controllers;

import frontoffice.utils.SceneNavigator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

public class MainLayoutController {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    public void initialize() {
        SceneNavigator.setMainLayoutController(this);
        showVoyages();
    }

    public void showVoyages() {
        setCenter("/frontoffice/views/voyagesView.fxml");
    }

    public void showActivities() {
        setCenter("/frontoffice/views/activitiesView.fxml");
    }

    public void showOffers() {
        setCenter("/frontoffice/views/offersView.fxml");
    }

    public void showMyOffers() {
        setCenter("/frontoffice/views/myOffersView.fxml");
    }

    public void showReservations() {
        setCenter("/frontoffice/views/reservationsView.fxml");
    }

    public void showReclamations() {
        setCenter("/frontoffice/views/reclamationsView.fxml");
    }

    public void showRefunds() {
        setCenter("/frontoffice/views/refundsView.fxml");
    }

    public void showDocuments() {
        setCenter("/frontoffice/views/documentsView.fxml");
    }

    public void showAssociations() {
        setCenter("/frontoffice/views/associationsView.fxml");
    }

    public void showProfile() {
        setCenter("/frontoffice/views/profileView.fxml");
    }

    public void setCenter(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = loader.load();
            mainBorderPane.setCenter(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
