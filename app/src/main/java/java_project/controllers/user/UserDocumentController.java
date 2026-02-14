package java_project.controllers.user;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java_project.models.UserDocument;
import java_project.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import java.time.LocalDate;

public class UserDocumentController {
    @FXML
    private TextField firstNameField, lastNameField, nationalityField, passportNumField, cinNumField;
    @FXML
    private DatePicker dobPicker, passportExpiryPicker, cinCreationPicker;

    private final UserService userService = new UserService();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private int userId;

    public void initData(int userId) {
        this.userId = userId;
        loadExistingDocument();
    }

    private void loadExistingDocument() {
        userService.getUserDocument(userId).thenAccept(response -> {
            if (response.statusCode() == 200) {
                try {
                    UserDocument doc = mapper.readValue(response.body(), UserDocument.class);
                    Platform.runLater(() -> fillFields(doc));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void fillFields(UserDocument doc) {
        if (doc == null)
            return;
        firstNameField.setText(doc.getFirstName());
        lastNameField.setText(doc.getLastName());
        nationalityField.setText(doc.getNationality());
        dobPicker.setValue(doc.getDateOfBirth());
        passportNumField.setText(doc.getPassportNumber());
        passportExpiryPicker.setValue(doc.getPassportExpiryDate());
        cinNumField.setText(doc.getCinNumber());
        cinCreationPicker.setValue(doc.getCinCreationDate());
    }

    @FXML
    private void handleSave() {
        if (!isInputValid()) {
            return; // Stop if validation fails
        }

        try {
            String dob = (dobPicker.getValue() != null) ? "\"" + dobPicker.getValue() + "\"" : "null";
            String passportExp = (passportExpiryPicker.getValue() != null)
                    ? "\"" + passportExpiryPicker.getValue() + "\""
                    : "null";
            String cinCreate = (cinCreationPicker.getValue() != null) ? "\"" + cinCreationPicker.getValue() + "\""
                    : "null";

            String json = String.format(
                    "{\"userId\":%d, \"firstName\":\"%s\", \"lastName\":\"%s\", \"dateOfBirth\":%s, \"nationality\":\"%s\", "
                            +
                            "\"passportNumber\":\"%s\", \"passportExpiryDate\":%s, \"cinNumber\":\"%s\", \"cinCreationDate\":%s}",
                    userId,
                    getSafeText(firstNameField),
                    getSafeText(lastNameField),
                    dob,
                    getSafeText(nationalityField),
                    getSafeText(passportNumField),
                    passportExp,
                    getSafeText(cinNumField),
                    cinCreate);

            userService.saveUserDocument(json).thenAccept(response -> {
                Platform.runLater(() -> {
                    if (response.statusCode() == 201 || response.statusCode() == 200) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Document Saved Successfully!");
                        closeWindow();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Server Error", "Failed to save: " + response.statusCode());
                    }
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Network Error", ex.getMessage()));
                return null;
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getSafeText(TextField field) {
        return (field.getText() == null) ? "" : field.getText().trim();
    }

    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();

        // 1. Mandatory Names
        if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) {
            errorMessage.append("First Name is required.\n");
        }
        if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
            errorMessage.append("Last Name is required.\n");
        }

        // 2. Date of Birth Validation
        if (dobPicker.getValue() == null) {
            errorMessage.append("Date of Birth is required.\n");
        } else if (dobPicker.getValue().isAfter(LocalDate.now().minusYears(12))) {
            errorMessage.append("User must be at least 12 years old.\n");
        }

        // 3. Nationality
        if (nationalityField.getText() == null || nationalityField.getText().trim().isEmpty()) {
            errorMessage.append("Nationality is required.\n");
        }

        // 4. ID Check (Must have either Passport or CIN)
        boolean hasPassport = passportNumField.getText() != null && !passportNumField.getText().trim().isEmpty();
        boolean hasCIN = cinNumField.getText() != null && !cinNumField.getText().trim().isEmpty();

        if (!hasPassport && !hasCIN) {
            errorMessage.append("Please provide either a Passport Number or a CIN Number.\n");
        }

        // 5. Passport Expiry Validation (If passport is provided)
        if (hasPassport && passportExpiryPicker.getValue() == null) {
            errorMessage.append("Please provide the Passport Expiry Date.\n");
        } else if (hasPassport && passportExpiryPicker.getValue().isBefore(LocalDate.now())) {
            errorMessage.append("The provided Passport has expired.\n");
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            showAlert(Alert.AlertType.WARNING, "Validation Error", errorMessage.toString());
            return false;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}