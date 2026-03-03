package frontoffice.controllers.views;

import frontoffice.models.UserDocument;
import frontoffice.services.UserDocumentService;
import frontoffice.utils.HttpErrorParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class DocumentsViewController {
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private DatePicker dobPicker;
    @FXML
    private TextField nationalityField;
    @FXML
    private TextField passportNumberField;
    @FXML
    private DatePicker passportExpiryPicker;
    @FXML
    private TextField cinNumberField;
    @FXML
    private DatePicker cinCreationPicker;
    @FXML
    private Label statusLabel;

    private final UserDocumentService userDocumentService = new UserDocumentService();

    @FXML
    public void initialize() {
        onRefresh();
    }

    @FXML
    private void onRefresh() {
        statusLabel.setText("Loading...");
        userDocumentService.getMy()
                .thenAccept(doc -> Platform.runLater(() -> {
                    setFields(doc);
                    statusLabel.setText(doc == null ? "No document yet." : "");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Load error: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void onSave() {
        UserDocument doc = new UserDocument();
        doc.setFirstName(trim(firstNameField.getText()));
        doc.setLastName(trim(lastNameField.getText()));
        doc.setDateOfBirth(dobPicker.getValue());
        doc.setNationality(trim(nationalityField.getText()));
        doc.setPassportNumber(trim(passportNumberField.getText()));
        doc.setPassportExpiryDate(passportExpiryPicker.getValue());
        doc.setCinNumber(trim(cinNumberField.getText()));
        doc.setCinCreationDate(cinCreationPicker.getValue());

        statusLabel.setText("Saving...");
        userDocumentService.save(doc)
                .thenAccept(resp -> Platform.runLater(() -> {
                    if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                        statusLabel.setText("Saved.");
                        onRefresh();
                    } else {
                        String details = HttpErrorParser.bestEffortMessage(resp.body());
                        statusLabel.setText("Save failed (" + resp.statusCode() + ")" + (details.isBlank() ? "" : ": " + details));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Save error: " + ex.getMessage()));
                    return null;
                });
    }

    private void setFields(UserDocument doc) {
        if (doc == null) {
            firstNameField.setText("");
            lastNameField.setText("");
            dobPicker.setValue(null);
            nationalityField.setText("");
            passportNumberField.setText("");
            passportExpiryPicker.setValue(null);
            cinNumberField.setText("");
            cinCreationPicker.setValue(null);
            return;
        }

        firstNameField.setText(doc.getFirstName() == null ? "" : doc.getFirstName());
        lastNameField.setText(doc.getLastName() == null ? "" : doc.getLastName());
        dobPicker.setValue(doc.getDateOfBirth());
        nationalityField.setText(doc.getNationality() == null ? "" : doc.getNationality());
        passportNumberField.setText(doc.getPassportNumber() == null ? "" : doc.getPassportNumber());
        passportExpiryPicker.setValue(doc.getPassportExpiryDate());
        cinNumberField.setText(doc.getCinNumber() == null ? "" : doc.getCinNumber());
        cinCreationPicker.setValue(doc.getCinCreationDate());
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
