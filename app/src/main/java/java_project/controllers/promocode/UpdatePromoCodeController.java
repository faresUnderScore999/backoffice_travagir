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

public class UpdatePromoCodeController {
    @FXML private ComboBox<Offer> offerComboBox;
    @FXML private TextField codeField, usageLimitField;
    @FXML private DatePicker expiryDatePicker;
    @FXML private CheckBox activeCheckBox;

    private Integer initialOfferId;

    private final PromoCodeService promoService = new PromoCodeService();
    private final OfferService offerService = new OfferService();
    private final ObjectMapper mapper = new ObjectMapper();

    private int promoId;

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
                    Platform.runLater(() -> {
                        offerComboBox.getItems().setAll(offers);
                        if (initialOfferId != null) {
                            for (Offer o : offers) {
                                if (o.getId() == initialOfferId) {
                                    offerComboBox.setValue(o);
                                    break;
                                }
                            }
                        }
                    });
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    public void setPromoCodeData(PromoCode promo) {
        this.promoId = promo.getId();
        this.initialOfferId = promo.getOfferId();
        this.codeField.setText(promo.getCode());
        this.usageLimitField.setText(String.valueOf(promo.getUsageLimit()));
        this.expiryDatePicker.setValue(promo.getExpiryDate());
        this.activeCheckBox.setSelected(promo.isActive());
    }

    @FXML
    private void handleUpdate() {
        if (offerComboBox.getValue() == null) {
            showError("Missing Information", "Offer selection is required!");
            return;
        }
        Offer selected = offerComboBox.getValue();
        int offerId = selected.getId();

        String jsonBody = String.format(
            "{\"id\":%d, \"offerId\":%d, \"code\":\"%s\", \"usageLimit\":%s, \"expiryDate\":\"%s\", \"active\":%b}",
            promoId,
            offerId,
            codeField.getText(),
            usageLimitField.getText(),
            expiryDatePicker.getValue(),
            activeCheckBox.isSelected()
        );

        promoService.updatePromoCode(promoId, jsonBody).thenAccept(response -> {
            if (response.statusCode() == 200) {
                Platform.runLater(() -> {
                    showInformation("Success", "Promo code updated successfully.");
                    closeWindow();
                });
            } else {
                Platform.runLater(() -> showError("Error", "Update failed: " + response.body()));
            }
        });
    }

    @FXML private void handleCancel() { closeWindow(); }

    private void closeWindow() {
        Stage stage = (Stage) codeField.getScene().getWindow();
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