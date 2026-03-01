package java_project.controllers.offer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java_project.models.Offer;
import java_project.models.Voyage;
import java_project.services.ApiClient;
import java_project.services.OfferService;
import javafx.application.Platform;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
public class UpdateOfferController {
    // FXML Fields matching all backend requirements
    @FXML private TextField titleField, discountField;
    @FXML private ComboBox<Voyage> voyageComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker startDatePicker, endDatePicker;
    @FXML private CheckBox activeCheckBox;

    private Integer initialVoyageId; // store until voyages have loaded
    private final ObjectMapper mapper = new ObjectMapper();
    private final ApiClient apiClient = new ApiClient();

    private int offerId;
    private final OfferService offerService = new OfferService();

    @FXML
    public void initialize() {
        mapper.registerModule(new JavaTimeModule());
        setupVoyageCombo();
        loadVoyages();
    }

    /**
     * Pre-fills all fields with existing data from the selected Offer
     */
    public void setOfferData(Offer offer) {
        this.offerId = offer.getId();
        this.initialVoyageId = offer.getVoyageId();
        this.titleField.setText(offer.getTitle());
        this.descriptionArea.setText(offer.getDescription());
        this.discountField.setText(String.valueOf(offer.getDiscountPercentage()));
        this.startDatePicker.setValue(offer.getStartDate());
        this.endDatePicker.setValue(offer.getEndDate());
        this.activeCheckBox.setSelected(offer.isActive());
    }

    @FXML
    private void handleUpdate() {
        // Validate voyage selection
        Voyage selected = voyageComboBox.getValue();
        if (selected == null) {
            showError("Missing Information", "Voyage selection is required!");
            return;
        }
        int voyageId = selected.getId();

        // Construct FULL JSON payload required by your backend
        String jsonBody = String.format(
            "{\"id\":%d, \"voyageId\":%d, \"title\":\"%s\", \"description\":\"%s\", \"discountPercentage\":%s, \"startDate\":\"%s\", \"endDate\":\"%s\", \"active\":%b}",
            offerId,
            voyageId,
            titleField.getText(),
            descriptionArea.getText().replace("\"", "\\\""), // Escape quotes for JSON
            discountField.getText(),
            startDatePicker.getValue(),
            endDatePicker.getValue(),
            activeCheckBox.isSelected()
        );

        offerService.updateOffer(offerId, jsonBody).thenAccept(response -> {
            if (response.statusCode() == 200) {
                Platform.runLater(() -> {
                    showInformation("Success", "Offer updated successfully.");
                    closeWindow();
                });
            } else {
                Platform.runLater(() -> showError("Error", "Update failed: " + response.body()));
            }
        });
    }

    @FXML private void handleCancel() { closeWindow(); }

    /************ voyage helpers ************/
    private void setupVoyageCombo() {
        voyageComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Voyage item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle() + " - " + item.getDestination());
                }
            }
        });
        voyageComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Voyage item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle() + " - " + item.getDestination());
                }
            }
        });
    }

    private void loadVoyages() {
        apiClient.sendWithRetry("/api/v1/offers/voyages", "GET", null)
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            List<Voyage> voyages = mapper.readValue(
                                    response.body(),
                                    new com.fasterxml.jackson.core.type.TypeReference<List<Voyage>>() {}
                            );
                            Platform.runLater(() -> {
                                voyageComboBox.getItems().setAll(voyages);
                                if (initialVoyageId != null) {
                                    for (Voyage v : voyages) {
                                        if (v.getId() == initialVoyageId) {
                                            voyageComboBox.setValue(v);
                                            break;
                                        }
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInformation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}