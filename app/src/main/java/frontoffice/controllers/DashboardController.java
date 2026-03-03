package frontoffice.controllers;

import frontoffice.models.Offer;
import frontoffice.models.Reservation;
import frontoffice.models.UserProfile;
import frontoffice.models.Voyage;
import frontoffice.services.OfferService;
import frontoffice.services.ReservationService;
import frontoffice.services.UserProfileService;
import frontoffice.services.VoyageService;
import frontoffice.utils.SceneNavigator;
import frontoffice.utils.SessionManager;
import frontoffice.utils.HttpErrorParser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.HashMap;
import java.util.Map;

public class DashboardController {
    @FXML
    private Label userLabel;

    @FXML
    private TextField voyageSearchField;
    @FXML
    private TableView<Voyage> voyagesTable;
    @FXML
    private TableColumn<Voyage, Integer> voyageIdCol;
    @FXML
    private TableColumn<Voyage, String> voyageTitleCol;
    @FXML
    private TableColumn<Voyage, String> voyageDestinationCol;
    @FXML
    private TableColumn<Voyage, Double> voyagePriceCol;

    @FXML
    private TextField peopleCountField;
    @FXML
    private Label bookingMessageLabel;

    @FXML
    private TextField offerSearchField;
    @FXML
    private TableView<Offer> offersTable;
    @FXML
    private TableColumn<Offer, Integer> offerIdCol;
    @FXML
    private TableColumn<Offer, String> offerTitleCol;
    @FXML
    private TableColumn<Offer, Double> offerDiscountCol;

    @FXML
    private TableView<Reservation> reservationsTable;
    @FXML
    private TableColumn<Reservation, Integer> reservationIdCol;
    @FXML
    private TableColumn<Reservation, Integer> reservationVoyageIdCol;
    @FXML
    private TableColumn<Reservation, Integer> reservationPeopleCol;
    @FXML
    private TableColumn<Reservation, String> reservationStatusCol;

    @FXML
    private Label profileStatusLabel;
    @FXML
    private TextField profileNameField;
    @FXML
    private TextField profileEmailField;
    @FXML
    private TextField profileTelField;
    @FXML
    private TextField profileImageUrlField;

    @FXML
    private TextField reservationPeopleUpdateField;
    @FXML
    private Label reservationActionLabel;

    private final VoyageService voyageService = new VoyageService();
    private final OfferService offerService = new OfferService();
    private final ReservationService reservationService = new ReservationService();
    private final UserProfileService userProfileService = new UserProfileService();

    @FXML
    public void initialize() {
        var user = SessionManager.getInstance().getCurrentUser();
        userLabel.setText(user != null ? ("Signed in as: " + user.name() + " (" + user.email() + ")") : "Not signed in");

        voyageIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        voyageTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        voyageDestinationCol.setCellValueFactory(new PropertyValueFactory<>("destination"));
        voyagePriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        offerIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        offerTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        offerDiscountCol.setCellValueFactory(new PropertyValueFactory<>("discountPercentage"));

        reservationIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        reservationVoyageIdCol.setCellValueFactory(new PropertyValueFactory<>("voyageId"));
        reservationPeopleCol.setCellValueFactory(new PropertyValueFactory<>("numberOfPeople"));
        reservationStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        refreshVoyages();
        refreshOffers();
        refreshReservations();
        refreshProfile();
    }

    @FXML
    private void onLogout() {
        SessionManager.getInstance().logout();
        SceneNavigator.goToLogin();
    }

    @FXML
    private void onSearchVoyages() {
        String q = voyageSearchField.getText();
        voyageService.searchVoyages(q).thenAccept(list -> Platform.runLater(() ->
                voyagesTable.setItems(FXCollections.observableArrayList(list))
        ));
    }

    @FXML
    private void onRefreshVoyages() {
        refreshVoyages();
    }

    private void refreshVoyages() {
        voyageService.getAllVoyages().thenAccept(list -> Platform.runLater(() ->
                voyagesTable.setItems(FXCollections.observableArrayList(list))
        ));
    }

    @FXML
    private void onSearchOffers() {
        String q = offerSearchField.getText();
        offerService.searchOffers(q).thenAccept(list -> Platform.runLater(() ->
                offersTable.setItems(FXCollections.observableArrayList(list))
        ));
    }

    @FXML
    private void onRefreshOffers() {
        refreshOffers();
    }

    private void refreshOffers() {
        offerService.getAllOffers().thenAccept(list -> Platform.runLater(() ->
                offersTable.setItems(FXCollections.observableArrayList(list))
        ));
    }

    @FXML
    private void onRefreshReservations() {
        refreshReservations();
    }

    private void refreshReservations() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            reservationsTable.setItems(FXCollections.observableArrayList());
            return;
        }
        reservationService.getMyReservations(user.id()).thenAccept(list -> Platform.runLater(() ->
                reservationsTable.setItems(FXCollections.observableArrayList(list))
        ));
    }

    @FXML
    private void onBookSelectedVoyage() {
        Voyage selected = voyagesTable.getSelectionModel().getSelectedItem();
        var user = SessionManager.getInstance().getCurrentUser();

        if (user == null) {
            bookingMessageLabel.setText("Please login first.");
            return;
        }
        if (selected == null) {
            bookingMessageLabel.setText("Select a voyage first.");
            return;
        }

        int people;
        try {
            people = Integer.parseInt(peopleCountField.getText());
        } catch (Exception e) {
            bookingMessageLabel.setText("People count must be a number.");
            return;
        }
        if (people < 1) {
            bookingMessageLabel.setText("People count must be at least 1.");
            return;
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(user.id());
        reservation.setVoyageId(selected.getId());
        reservation.setNumberOfPeople(people);

        bookingMessageLabel.setText("Booking...");
        reservationService.createReservation(reservation)
                .thenAccept(resp -> Platform.runLater(() -> {
                    if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                        bookingMessageLabel.setText("Reservation created.");
                        refreshReservations();
                    } else {
                        String details = HttpErrorParser.bestEffortMessage(resp.body());
                        bookingMessageLabel.setText("Booking failed (" + resp.statusCode() + ")" + (details.isBlank() ? "" : ": " + details));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> bookingMessageLabel.setText("Booking error: " + ex.getMessage()));
                    return null;
                });
    }

    private void refreshProfile() {
        var current = SessionManager.getInstance().getCurrentUser();
        if (current == null) {
            setProfileFields(null);
            return;
        }

        profileStatusLabel.setText("Loading profile...");
        userProfileService.getMe().thenAccept(profile -> Platform.runLater(() -> {
            setProfileFields(profile);
            profileStatusLabel.setText(profile == null ? "Failed to load profile" : "");
        })).exceptionally(ex -> {
            Platform.runLater(() -> profileStatusLabel.setText("Profile error: " + ex.getMessage()));
            return null;
        });
    }

    private void setProfileFields(UserProfile profile) {
        if (profile == null) {
            profileNameField.setText("");
            profileEmailField.setText("");
            profileTelField.setText("");
            profileImageUrlField.setText("");
            return;
        }
        profileNameField.setText(profile.getName() == null ? "" : profile.getName());
        profileEmailField.setText(profile.getEmail() == null ? "" : profile.getEmail());
        profileTelField.setText(profile.getTel() == null ? "" : profile.getTel());
        profileImageUrlField.setText(profile.getImageUrl() == null ? "" : profile.getImageUrl());
    }

    @FXML
    private void onRefreshProfile() {
        refreshProfile();
    }

    @FXML
    private void onSaveProfile() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            profileStatusLabel.setText("Please login first.");
            return;
        }

        Map<String, Object> update = new HashMap<>();
        update.put("name", profileNameField.getText());
        update.put("email", profileEmailField.getText());
        update.put("tel", profileTelField.getText());
        update.put("imageUrl", profileImageUrlField.getText());

        profileStatusLabel.setText("Saving...");
        userProfileService.updateProfile(user.id(), update)
                .thenAccept(resp -> Platform.runLater(() -> {
                    if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                        profileStatusLabel.setText("Profile updated.");
                        refreshProfile();
                    } else {
                        String details = HttpErrorParser.bestEffortMessage(resp.body());
                        profileStatusLabel.setText("Update failed (" + resp.statusCode() + ")" + (details.isBlank() ? "" : ": " + details));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> profileStatusLabel.setText("Update error: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void onCancelSelectedReservation() {
        Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            reservationActionLabel.setText("Select a reservation first.");
            return;
        }

        reservationActionLabel.setText("Cancelling...");
        reservationService.cancelReservation(selected.getId())
                .thenAccept(resp -> Platform.runLater(() -> {
                    if (resp.statusCode() == 204 || (resp.statusCode() >= 200 && resp.statusCode() < 300)) {
                        reservationActionLabel.setText("Reservation cancelled.");
                        refreshReservations();
                    } else {
                        String details = HttpErrorParser.bestEffortMessage(resp.body());
                        reservationActionLabel.setText("Cancel failed (" + resp.statusCode() + ")" + (details.isBlank() ? "" : ": " + details));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> reservationActionLabel.setText("Cancel error: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void onUpdateSelectedReservationPeople() {
        Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            reservationActionLabel.setText("Select a reservation first.");
            return;
        }

        int count;
        try {
            count = Integer.parseInt(reservationPeopleUpdateField.getText());
        } catch (Exception e) {
            reservationActionLabel.setText("People count must be a number.");
            return;
        }
        if (count < 1) {
            reservationActionLabel.setText("People count must be at least 1.");
            return;
        }

        reservationActionLabel.setText("Updating...");
        reservationService.updatePeopleCount(selected.getId(), count)
                .thenAccept(resp -> Platform.runLater(() -> {
                    if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                        reservationActionLabel.setText("Reservation updated.");
                        refreshReservations();
                    } else {
                        String details = HttpErrorParser.bestEffortMessage(resp.body());
                        reservationActionLabel.setText("Update failed (" + resp.statusCode() + ")" + (details.isBlank() ? "" : ": " + details));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> reservationActionLabel.setText("Update error: " + ex.getMessage()));
                    return null;
                });
    }
}
