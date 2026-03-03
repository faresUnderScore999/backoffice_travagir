package java_project.controllers.promoCode;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java_project.models.Offer;
import java_project.services.PromoCodeService;
import java_project.services.OfferService;
import javafx.application.Platform;
import org.controlsfx.control.Notifications;

import java.util.List;

public class AddPromoCodeController {
    @FXML private TextField codeField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<Offer> offerComboBox;
    @FXML private DatePicker validFromPicker;
    @FXML private DatePicker validToPicker;
    @FXML private TextField usageLimitField;
    @FXML private CheckBox activeCheckBox;

    private final PromoCodeService promoCodeService = new PromoCodeService();
    private final OfferService offerService = new OfferService();

    @FXML
    public void initialize() {
        loadOffers();
        
        // Set default dates
        validFromPicker.setValue(java.time.LocalDate.now());
        validToPicker.setValue(java.time.LocalDate.now().plusMonths(1));
    }

    private void loadOffers() {
        System.out.println(" DEBUG: AddPromoCode - Starting to load offers...");
        
        offerService.getAllOffers().thenAccept(response -> {
            System.out.println(" DEBUG: AddPromoCode - API response status: " + response.statusCode());
            
            if (response.statusCode() == 200) {
                List<Offer> offers = parseOffersFromJson(response.body());
                System.out.println(" DEBUG: AddPromoCode - Parsed " + offers.size() + " offers");
                
                Platform.runLater(() -> {
                    offerComboBox.getItems().clear();
                    offerComboBox.getItems().addAll(offers);
                    System.out.println(" DEBUG: AddPromoCode - Added offers to combo, size: " + offerComboBox.getItems().size());
                    
                    // Show success notification
                    showInfoNotification("Offers Loaded", "Successfully loaded " + offers.size() + " offers");
                    
                    // Use StringConverter instead of custom cell factory
                    offerComboBox.setConverter(new javafx.util.StringConverter<Offer>() {
                        @Override
                        public String toString(Offer offer) {
                            if (offer == null) return "";
                            return offer.getId() + " - " + offer.getTitle();
                        }
                        
                        @Override
                        public Offer fromString(String string) {
                            if (string == null || string.isEmpty()) return null;
                            try {
                                int id = Integer.parseInt(string.split(" - ")[0]);
                                for (Offer offer : offers) {
                                    if (offer.getId() == id) {
                                        return offer;
                                    }
                                }
                            } catch (Exception e) {
                                // Ignore parsing errors
                            }
                            return null;
                        }
                    });
                    
                    System.out.println(" DEBUG: AddPromoCode - Combo box setup completed");
                });
            } else {
                Platform.runLater(() -> {
                    showErrorNotification("Loading Failed", "Failed to load offers: " + response.body());
                });
            }
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                showErrorNotification("Network Error", "Error connecting to server: " + e.getMessage());
            });
            return null;
        });
    }

    @FXML
    private void handleSave() {
        if (!isInputValid()) {
            return;
        }

        // Get selected offer
        Offer selectedOffer = offerComboBox.getValue();
        if (selectedOffer == null) {
            showWarningNotification("Validation Error", "Please select an offer");
            return;
        }

        // Construct JSON
        String jsonBody = String.format(
            "{\"code\":\"%s\", \"description\":\"%s\", \"offerId\":%d, \"validFrom\":\"%s\", \"validTo\":\"%s\", \"usageLimit\":%s, \"usedCount\":0, \"active\":%b}",
            codeField.getText().trim(),
            descriptionArea.getText().replace("\"", "\\\""),
            selectedOffer.getId(),
            validFromPicker.getValue(),
            validToPicker.getValue(),
            usageLimitField.getText(),
            activeCheckBox.isSelected()
        );

        promoCodeService.addPromoCode(jsonBody).thenAccept(response -> {
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                Platform.runLater(() -> {
                    showSuccessNotification("Promo Code Created", "Promo code '" + codeField.getText().trim() + "' created successfully!");
                    ((Stage) codeField.getScene().getWindow()).close();
                });
            } else {
                Platform.runLater(() -> 
                    showErrorNotification("Creation Failed", "Failed to create promo code: " + response.body()));
            }
        });
    }

    @FXML
    private void handleCancel() {
        ((Stage) codeField.getScene().getWindow()).close();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (codeField.getText() == null || codeField.getText().trim().isEmpty()) {
            errorMessage += "Promo code is required!\n";
        }

        if (offerComboBox.getValue() == null) {
            errorMessage += "Offer selection is required!\n";
        }

        if (validFromPicker.getValue() == null) {
            errorMessage += "Valid from date is required!\n";
        }

        if (validToPicker.getValue() == null) {
            errorMessage += "Valid to date is required!\n";
        }

        if (validFromPicker.getValue() != null && validToPicker.getValue() != null) {
            if (validToPicker.getValue().isBefore(validFromPicker.getValue())) {
                errorMessage += "Valid to date cannot be before valid from date!\n";
            }
        }

        if (usageLimitField.getText() == null || usageLimitField.getText().trim().isEmpty()) {
            errorMessage += "Usage limit is required!\n";
        } else {
            try {
                int limit = Integer.parseInt(usageLimitField.getText());
                if (limit <= 0) {
                    errorMessage += "Usage limit must be greater than 0!\n";
                }
            } catch (NumberFormatException e) {
                errorMessage += "Usage limit must be a valid number!\n";
            }
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showWarningNotification("Validation Error", errorMessage);
            return false;
        }
    }

    private void showErrorNotification(String title, String message) {
        Notifications.create()
            .title(title)
            .text(message)
            .darkStyle()
            .showError();
    }

    private void showWarningNotification(String title, String message) {
        Notifications.create()
            .title(title)
            .text(message)
            .darkStyle()
            .showWarning();
    }

    private void showSuccessNotification(String title, String message) {
        Notifications.create()
            .title(title)
            .text(message)
            .darkStyle()
            .showConfirm();
    }

    private void showInfoNotification(String title, String message) {
        Notifications.create()
            .title(title)
            .text(message)
            .darkStyle()
            .showInformation();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Helper methods to parse JSON (same as main controller)
    private List<Offer> parseOffersFromJson(String json) {
        List<Offer> offers = new java.util.ArrayList<>();
        System.out.println("🔍 DEBUG: AddPromoCode - Starting JSON parsing for offers...");
        System.out.println("🔍 DEBUG: AddPromoCode - Input JSON: " + json);
        
        try {
            if (json != null && json.startsWith("[") && json.endsWith("]")) {
                String[] items = json.substring(1, json.length() - 1).split("\\},\\{");
                System.out.println("🔍 DEBUG: AddPromoCode - Split JSON into " + items.length + " items");
                
                for (int i = 0; i < items.length; i++) {
                    String item = items[i];
                    if (item.trim().isEmpty()) continue;
                    
                    System.out.println("🔍 DEBUG: AddPromoCode - Processing item " + i + ": " + item);
                    
                    String cleanItem = item.replace("[", "").replace("]", "");
                    if (!cleanItem.endsWith("}")) cleanItem += "}";
                    
                    int id = extractIntFromJson(cleanItem, "id");
                    String title = extractStringFromJson(cleanItem, "title");
                    
                    System.out.println("🔍 DEBUG: AddPromoCode - Extracted ID: " + id);
                    System.out.println("🔍 DEBUG: AddPromoCode - Extracted title: " + title);
                    
                    if (id > 0 && title != null && !title.isEmpty()) {
                        Offer offer = new Offer();
                        offer.setId(id);
                        offer.setTitle(title);
                        offers.add(offer);
                        System.out.println("🔍 DEBUG: AddPromoCode - Successfully created offer: " + id + " - " + title);
                    } else {
                        System.out.println("🔍 DEBUG: AddPromoCode - Skipping offer - invalid ID or title");
                    }
                }
            } else {
                System.out.println("🔍 DEBUG: AddPromoCode - Invalid JSON format - not starting with [ or ending with ]");
            }
        } catch (Exception e) {
            System.err.println("🔍 DEBUG: AddPromoCode - Error parsing offers JSON: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("🔍 DEBUG: AddPromoCode - Final offers list size: " + offers.size());
        return offers;
    }

    private int extractIntFromJson(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int index = json.indexOf(pattern);
            if (index != -1) {
                int start = index + pattern.length();
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (end != -1) {
                    String value = json.substring(start, end).trim();
                    int result = Integer.parseInt(value);
                    System.out.println("🔍 DEBUG: AddPromoCode - Extracted int for key '" + key + "': " + result);
                    return result;
                }
            }
        } catch (Exception e) {
            System.err.println("🔍 DEBUG: AddPromoCode - Error extracting int for key '" + key + "': " + e.getMessage());
        }
        System.out.println("🔍 DEBUG: AddPromoCode - Could not extract int for key '" + key + "', returning 0");
        return 0;
    }

    private String extractStringFromJson(String json, String key) {
        try {
            String pattern = "\"" + key + "\":\"";
            int index = json.indexOf(pattern);
            if (index != -1) {
                int start = index + pattern.length();
                int end = json.indexOf("\"", start);
                if (end != -1) {
                    String value = json.substring(start, end);
                    System.out.println("🔍 DEBUG: AddPromoCode - Extracted string for key '" + key + "': " + value);
                    return value;
                }
            }
        } catch (Exception e) {
            System.err.println("🔍 DEBUG: AddPromoCode - Error extracting string for key '" + key + "': " + e.getMessage());
        }
        System.out.println("🔍 DEBUG: AddPromoCode - Could not extract string for key '" + key + "', returning null");
        return null;
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
