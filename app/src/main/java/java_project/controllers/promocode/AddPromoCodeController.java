package java_project.controllers.promocode;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java_project.models.PromoCode;
import java_project.models.Offer;
import java_project.services.PromoCodeService;
import java_project.services.OfferService;

public class AddPromoCodeController {
    @FXML private ComboBox<Offer> offerComboBox;
    @FXML private TextField codeField, usageLimitField;
    @FXML private DatePicker expiryDatePicker;
    @FXML private CheckBox activeCheckBox;

    private final PromoCodeService promoService = new PromoCodeService();
    private final OfferService offerService = new OfferService();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        mapper.registerModule(new JavaTimeModule());
        setupOfferCombo();
        loadOffers();
    }

    private void setupOfferCombo() {
        offerComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Offer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle());
                }
            }
        });
        offerComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Offer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle());
                }
            }
        });
    }

    private void loadOffers() {
        offerService.getAllOffers().thenAccept(response -> {
            if (response.statusCode() == 200) {
                try {
                    List<Offer> offers = mapper.readValue(response.body(), new TypeReference<List<Offer>>() {});
                    Platform.runLater(() -> offerComboBox.getItems().setAll(offers));
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    @FXML
    private void handleSave() {
        if (!isInputValid()) return;

        Offer selected = offerComboBox.getValue();
        int offerId = selected.getId();

        String jsonBody = String.format(
            "{\"offerId\":%d, \"code\":\"%s\", \"usageLimit\":%s, \"expiryDate\":\"%s\", \"active\":%b}",
            offerId,
            codeField.getText(),
            usageLimitField.getText(),
            expiryDatePicker.getValue(),
            activeCheckBox.isSelected()
        );

        promoService.addPromoCode(jsonBody).thenAccept(response -> {
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                Platform.runLater(() -> ((Stage) codeField.getScene().getWindow()).close());
            } else {
                Platform.runLater(() -> showError("Server Error", response.body()));
            }
        });
    }

    private boolean isInputValid() {
        String err = "";
        if (offerComboBox.getValue() == null) err += "Offer selection is required!\n";
        if (codeField.getText() == null || codeField.getText().isEmpty()) err += "Code is required!\n";
        if (usageLimitField.getText() == null || usageLimitField.getText().isEmpty()) err += "Usage limit is required!\n";
        else {
            try { Integer.parseInt(usageLimitField.getText()); }
            catch (NumberFormatException e) { err += "Usage limit must be a number!\n"; }
        }
        if (expiryDatePicker.getValue() == null) err += "Expiry date is required!\n";
        if (!err.isEmpty()) {
            showError("Missing Information", err);
            return false;
        }
        return true;
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
        ((Stage) codeField.getScene().getWindow()).close();
    }
}