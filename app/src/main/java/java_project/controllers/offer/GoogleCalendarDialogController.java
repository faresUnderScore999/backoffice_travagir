package java_project.controllers.offer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java_project.models.Offer;
import java_project.services.GoogleCalendarService;

public class GoogleCalendarDialogController {
    
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label discountLabel;
    @FXML private Label startDateLabel;
    @FXML private Label endDateLabel;
    @FXML private CheckBox addReminderCheckBox;
    @FXML private ComboBox<String> reminderTimeComboBox;
    @FXML private Button addToCalendarButton;
    @FXML private Button cancelButton;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;
    
    private Offer offer;
    private GoogleCalendarService calendarService;
    private Stage dialogStage;
    
    @FXML
    public void initialize() {
        calendarService = new GoogleCalendarService();
        
        // Initialiser les options de rappel
        reminderTimeComboBox.getItems().addAll(
            "15 minutes avant",
            "30 minutes avant", 
            "1 heure avant",
            "2 heures avant",
            "1 jour avant",
            "2 jours avant"
        );
        reminderTimeComboBox.setValue("1 jour avant");
        
        // Cacher la barre de progression au début
        progressBar.setVisible(false);
        statusLabel.setVisible(false);
    }
    
    public void setOffer(Offer offer) {
        this.offer = offer;
        updateOfferDisplay();
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    private void updateOfferDisplay() {
        if (offer != null) {
            titleLabel.setText("🔥 " + offer.getTitle());
            descriptionLabel.setText(offer.getDescription() != null ? offer.getDescription() : "Aucune description");
            discountLabel.setText(String.format("%.1f%%", offer.getDiscountPercentage()));
            
            if (offer.getStartDate() != null) {
                startDateLabel.setText(offer.getStartDate().toString());
            }
            if (offer.getEndDate() != null) {
                endDateLabel.setText(offer.getEndDate().toString());
            }
        }
    }
    
    @FXML
    private void handleAddToCalendar() {
        if (offer == null) {
            showError("Erreur", "Aucune offre sélectionnée");
            return;
        }
        
        // Afficher la progression
        progressBar.setVisible(true);
        statusLabel.setVisible(true);
        statusLabel.setText("📅 Ajout de l'offre au Google Calendar...");
        addToCalendarButton.setDisable(true);
        cancelButton.setDisable(true);
        
        // Exécuter dans un thread séparé pour ne pas bloquer l'UI
        new Thread(() -> {
            try {
                // Ajouter l'événement principal
                boolean success = calendarService.addOfferAndOpenCalendar(offer);
                
                // Si demandé, ajouter un rappel
                if (success && addReminderCheckBox.isSelected()) {
                    String reminderTime = reminderTimeComboBox.getValue();
                    int minutesBefore = getMinutesFromReminderTime(reminderTime);
                    
                    String reminderUrl = calendarService.createOfferReminder(offer, minutesBefore);
                    if (reminderUrl != null) {
                        calendarService.openCalendarInBrowser(reminderUrl);
                    }
                }
                
                // Mettre à jour l'UI
                javafx.application.Platform.runLater(() -> {
                    if (success) {
                        statusLabel.setText("✅ Offre ajoutée avec succès au Google Calendar!");
                        progressBar.setProgress(1.0);
                        
                        // Fermer la fenêtre après 2 secondes
                        new Thread(() -> {
                            try {
                                Thread.sleep(2000);
                                javafx.application.Platform.runLater(() -> dialogStage.close());
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();
                    } else {
                        statusLabel.setText("❌ Erreur lors de l'ajout au calendar");
                        progressBar.setVisible(false);
                        addToCalendarButton.setDisable(false);
                        cancelButton.setDisable(false);
                    }
                });
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("❌ Erreur: " + e.getMessage());
                    progressBar.setVisible(false);
                    addToCalendarButton.setDisable(false);
                    cancelButton.setDisable(false);
                });
            }
        }).start();
    }
    
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
    private int getMinutesFromReminderTime(String reminderTime) {
        return switch (reminderTime) {
            case "15 minutes avant" -> 15;
            case "30 minutes avant" -> 30;
            case "1 heure avant" -> 60;
            case "2 heures avant" -> 120;
            case "1 jour avant" -> 1440; // 24 * 60
            case "2 jours avant" -> 2880; // 48 * 60
            default -> 1440;
        };
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
