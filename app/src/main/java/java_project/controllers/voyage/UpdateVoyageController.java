package java_project.controllers.voyage;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

import java_project.models.Voyage;
import java_project.services.ApiClient;
import java_project.models.VoyageImage;
import java_project.services.VoyageImageService;

public class UpdateVoyageController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField destinationField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField priceField;
    @FXML private FlowPane imageGallery;
    @FXML private Button uploadImageBtn;

    private final ApiClient apiClient = new ApiClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private int voyageId;

    private final VoyageImageService voyageImageService = new VoyageImageService();

    /**
     * Pre-fills the form with the selected voyage data.
     */
    public void setVoyageData(Voyage voyage) {
        if (voyage == null) {
            return;
        }

        this.voyageId = voyage.getId();
        titleField.setText(voyage.getTitle());
        descriptionArea.setText(voyage.getDescription());
        destinationField.setText(voyage.getDestination());

        if (voyage.getStartDate() != null) {
            startDatePicker.setValue(voyage.getStartDate());
        }
        if (voyage.getEndDate() != null) {
            endDatePicker.setValue(voyage.getEndDate());
        }

        priceField.setText(String.valueOf(voyage.getPrice()));

        loadImagesAsync();
    }


    @FXML
    private void initialize() {
        
        // ensure gallery spacing and wrap
        if (imageGallery != null) {
            imageGallery.setHgap(10);
            imageGallery.setVgap(10);
        }
    }

    @FXML
    private void handleUploadImage() {
        if (voyageId <= 0) {
            showError("Upload Error", "Voyage ID is not set.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Image(s) to Upload");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"   )
        );
        List<File> selected = chooser.showOpenMultipleDialog(titleField.getScene().getWindow());
        if (selected == null || selected.isEmpty()) return;

        uploadImageBtn.setDisable(true);
        List<CompletableFuture<HttpResponse<String>>> futures = new ArrayList<>();
        for (File f : selected) {
            Path p = f.toPath();
            futures.add(voyageImageService.uploadImage(voyageId, p));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .whenComplete((v, ex) -> {
                    Platform.runLater(() -> uploadImageBtn.setDisable(false));
                    if (ex != null) {
                        Platform.runLater(() -> showError("Upload Error", "One or more uploads failed."));
                    }
                    // refresh gallery regardless
                    loadImagesAsync();
                });
    }

    private void loadImagesAsync() {
        if (voyageId <= 0) return;
        voyageImageService.getImagesForVoyage(voyageId)
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            VoyageImage[] imgs = mapper.readValue(response.body(), VoyageImage[].class);
                            Platform.runLater(() -> renderGallery(imgs));
                        } catch (Exception e) {
                            Platform.runLater(() -> showError("Parse Error", "Could not parse images response."));
                        }
                    } else {
                        Platform.runLater(() -> showError("Load Error", "Server returned: " + response.body()));
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> showError("Connection Error", "Could not reach server to load images."));
                    return null;
                });
    }

    private void renderGallery(VoyageImage[] imgs) {
        imageGallery.getChildren().clear();
        if (imgs == null || imgs.length == 0) return;

        for (VoyageImage img : imgs) {
            VBox card = new VBox(6);
            ImageView iv = new ImageView();
            iv.setFitWidth(140);
            iv.setFitHeight(90);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            try {
                Image image = new Image(img.getImageUrl(), 160, 100, true, true, true);
                iv.setImage(image);
            } catch (Exception ignored) {
            }

            Button del = new Button("Delete");
            del.setOnAction(ae -> {
                if (img.getId() == null) return;
                del.setDisable(true);
                voyageImageService.deleteImage(img.getId())
                        .thenAccept(resp -> {
                            if (resp.statusCode() == 200 || resp.statusCode() == 204) {
                                Platform.runLater(() -> loadImagesAsync());
                            } else {
                                Platform.runLater(() -> showError("Delete Failed", resp.body()));
                                Platform.runLater(() -> del.setDisable(false));
                            }
                        })
                        .exceptionally(ex -> {
                            Platform.runLater(() -> showError("Connection Error", "Could not delete image."));
                            Platform.runLater(() -> del.setDisable(false));
                            return null;
                        });
            });

            card.getChildren().addAll(iv, del);
            imageGallery.getChildren().add(card);
        }
    }
    @FXML
    private void handleUpdate() {
        if (!isInputValid()) {
            return;
        }
        String jsonBody;
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", voyageId);
            payload.put("title", titleField.getText());
            payload.put("description", descriptionArea.getText());
            payload.put("destination", destinationField.getText());
            payload.put("startDate", startDatePicker.getValue().format(DateTimeFormatter.ISO_DATE));
            payload.put("endDate", endDatePicker.getValue().format(DateTimeFormatter.ISO_DATE));
            payload.put("price", Double.parseDouble(priceField.getText()));
            jsonBody = mapper.writeValueAsString(payload);
        } catch (Exception e) {
            Platform.runLater(() -> showError("Payload Error", "Could not build request payload."));
            return;
        }

        sendRequest("/api/v1/voyages/" + voyageId, "PUT", jsonBody)
                .thenAccept(response -> {
                    int code = response.statusCode();
                    if (code == 200 || code == 204) {
                        Platform.runLater(() -> {
                            showInformation("Success", "Voyage updated successfully.");
                            closeWindow();
                        });
                    } else {
                        Platform.runLater(() -> showError("Update Failed", "Server returned: " + response.body()));
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> showError("Connection Error", "Could not reach the server."));
                    return null;
                });
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();

        if (titleField.getText() == null || titleField.getText().isEmpty()) errorMessage.append("Title is required!\n");
        if (destinationField.getText() == null || destinationField.getText().isEmpty()) errorMessage.append("Destination is required!\n");
        if (startDatePicker.getValue() == null) errorMessage.append("Start date is required!\n");
        if (endDatePicker.getValue() == null) errorMessage.append("End date is required!\n");

        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
                errorMessage.append("End date cannot be before start date!\n");
            }
        }

        try {
            Double.parseDouble(priceField.getText());
        } catch (NumberFormatException e) {
            errorMessage.append("Invalid price format!\n");
        }

        if (errorMessage.length() == 0) {
            return true;
        }

        showError("Invalid Fields", errorMessage.toString());
        return false;
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInformation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private CompletableFuture<HttpResponse<String>> sendRequest(String endpoint, String method, String body) {
        return apiClient.sendWithRetry(endpoint, method, body);
    }


}
