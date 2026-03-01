package java_project.controllers.offer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java_project.models.Voyage;
import java_project.services.OfferService;
import java_project.services.ApiClient;
import javafx.application.Platform;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;

public class AddOfferController {
    @FXML private TextField titleField, discountField;
    @FXML private ComboBox<Voyage> voyageComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker startDatePicker, endDatePicker;
    @FXML private CheckBox activeCheckBox;

    // mapper used for parsing voyage list
    private final ObjectMapper mapper = new ObjectMapper();
    private final ApiClient apiClient = new ApiClient();

    private final OfferService offerService = new OfferService();

    @FXML
    public void initialize() {
        // prepare jackson mapper for LocalDate
        mapper.registerModule(new JavaTimeModule());
        setupVoyageCombo();
        loadVoyages();
    }

    @FXML
    private void handleSave() {
        // 1. Perform validation
        if (!isInputValid()) {
            return; // Stop if something is missing
        }

        // 2. Determine selected voyage id
        Voyage selectedVoyage = voyageComboBox.getValue();
        int voyageId = selectedVoyage.getId();

        // 3. Construct the JSON if valid
        String jsonBody = String.format(
            "{\"voyageId\":%d, \"title\":\"%s\", \"description\":\"%s\", \"discountPercentage\":%s, \"startDate\":\"%s\", \"endDate\":\"%s\", \"active\":%b}",
            voyageId,
            titleField.getText(), 
            descriptionArea.getText().replace("\"", "\\\""), 
            discountField.getText(),
            startDatePicker.getValue(), 
            endDatePicker.getValue(), 
            activeCheckBox.isSelected()
        );

        offerService.addOffer(jsonBody).thenAccept(response -> {
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                Platform.runLater(() -> ((Stage) titleField.getScene().getWindow()).close());
            } else {
                Platform.runLater(() -> showError("Server Error", "Backend returned: " + response.body()));
            }
        });
    }

    /**
     * Validates user input and shows an alert if fields are missing.
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (voyageComboBox.getValue() == null) {
            errorMessage += "Voyage selection is required!\n";
        }
        if (titleField.getText() == null || titleField.getText().isEmpty()) {
            errorMessage += "Offer Title is required!\n";
        }
        if (discountField.getText() == null || discountField.getText().isEmpty()) {
            errorMessage += "Discount Percentage is required!\n";
        } else {
            try {
                Double.parseDouble(discountField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "Discount must be a valid number (e.g., 15.5)!\n";
            }
        }
        if (startDatePicker.getValue() == null) {
            errorMessage += "Start Date is missing!\n";
        }
        if (endDatePicker.getValue() == null) {
            errorMessage += "End Date is missing!\n";
        }
        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
                errorMessage += "End Date cannot be before Start Date!\n";
            }
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showError("Missing Information", errorMessage);
            return false;
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML
    private void handleCancel() {
        ((Stage) titleField.getScene().getWindow()).close();
    }

    /***************************************************
     * helper methods for voyages
     ***************************************************/
    private void setupVoyageCombo() {
        // display title + destination
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
                             Platform.runLater(() -> voyageComboBox.getItems().setAll(voyages));
                         } catch (Exception e) {
                             e.printStackTrace();
                         }
                     }
                 });
    }
}