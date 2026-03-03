package frontoffice.controllers;

import frontoffice.utils.SceneNavigator;
import frontoffice.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.util.List;

public class MenuController {
    @FXML
    private HBox menuVoyages;
    @FXML
    private HBox menuActivities;
    @FXML
    private HBox menuOffers;
    @FXML
    private HBox menuMyOffers;
    @FXML
    private HBox menuReservations;
    @FXML
    private HBox menuReclamations;
    @FXML
    private HBox menuRefunds;
    @FXML
    private HBox menuDocuments;
    @FXML
    private HBox menuAssociations;
    @FXML
    private HBox menuProfile;
    @FXML
    private HBox menuLogout;

    private List<HBox> allMenuItems;

    @FXML
    public void initialize() {
        allMenuItems = List.of(
                menuVoyages,
                menuActivities,
                menuOffers,
                menuMyOffers,
                menuReservations,
                menuReclamations,
                menuRefunds,
                menuDocuments,
                menuAssociations,
                menuProfile,
                menuLogout
        );
        setActive(menuVoyages);
    }

    @FXML
    private void handleMenuClick(MouseEvent event) {
        Object src = event.getSource();
        if (!(src instanceof HBox clicked)) {
            return;
        }

        if (clicked == menuLogout) {
            SessionManager.getInstance().logout();
            SceneNavigator.goToLogin();
            return;
        }

        setActive(clicked);

        MainLayoutController main = SceneNavigator.getMainLayoutController();
        if (main == null) {
            return;
        }

        if (clicked == menuVoyages) main.showVoyages();
        else if (clicked == menuActivities) main.showActivities();
        else if (clicked == menuOffers) main.showOffers();
        else if (clicked == menuMyOffers) main.showMyOffers();
        else if (clicked == menuReservations) main.showReservations();
        else if (clicked == menuReclamations) main.showReclamations();
        else if (clicked == menuRefunds) main.showRefunds();
        else if (clicked == menuDocuments) main.showDocuments();
        else if (clicked == menuAssociations) main.showAssociations();
        else if (clicked == menuProfile) main.showProfile();
    }

    private void setActive(HBox active) {
        for (HBox item : allMenuItems) {
            item.getStyleClass().remove("active");
        }
        if (!active.getStyleClass().contains("active")) {
            active.getStyleClass().add("active");
        }
    }
}
