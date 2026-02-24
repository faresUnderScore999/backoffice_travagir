package java_project.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Map;
import java.awt.Desktop;
import java.net.URI;
import java.io.IOException;

import java_project.models.Reclamation;
import java_project.services.ReclamationService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;

public class ReclamationController {

    @FXML private TableView<Reclamation> reclamationTable;
    @FXML private TableColumn<Reclamation, Integer> colId;
    @FXML private TableColumn<Reclamation, Integer> colReservationId;
    @FXML private TableColumn<Reclamation, String> colTitle;
    @FXML private TableColumn<Reclamation, String> colPriority;
    @FXML private TableColumn<Reclamation, String> colStatus;
    @FXML private TableColumn<Reclamation, Void> colActions;

    private final ReclamationService reclamationService = new ReclamationService();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final String BACKEND_BASE_URL = "http://localhost:8080/api"; // backend base URL

    @FXML
    public void initialize() {
        // Register the JavaTimeModule to handle LocalDateTime from JSON
        mapper.registerModule(new JavaTimeModule());

        // Setup columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colReservationId.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        setupActionButtons();
        loadReclamations();
    }

    @FXML
    private void loadReclamations() {
        reclamationService.getAllReclamations().thenAccept(response -> {
            try {
                if (response.statusCode() == 200) {
                    List<Reclamation> list = mapper.readValue(response.body(), new TypeReference<List<Reclamation>>() {});
                    Platform.runLater(() -> reclamationTable.getItems().setAll(list));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setupActionButtons() {
        Callback<TableColumn<Reclamation, Void>, TableCell<Reclamation, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Reclamation, Void> call(final TableColumn<Reclamation, Void> param) {
                return new TableCell<>() {
                    private final Button resolveBtn = new Button("Resolve");
                    private final Button exportBtn = new Button("Export as PDF");
                    private final HBox pane = new HBox(10, resolveBtn);

                    {
                        resolveBtn.getStyleClass().add("submit-button"); // Uses your CSS
                        exportBtn.getStyleClass().add("secondary-button");
                        pane.setStyle("-fx-alignment: CENTER;");
                        
                        resolveBtn.setOnAction(e -> {
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            handleResolveAction(rec);
                        });
                        exportBtn.setOnAction(e -> {
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            exportAsPdf(rec);
                        });
                        pane.getChildren().add(exportBtn);
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            // Disable button if already resolved
                            resolveBtn.setDisable("RESOLVED".equals(rec.getStatus()));
                            setGraphic(pane);
                        }
                    }
                };
            }
        };
        colActions.setCellFactory(cellFactory);
    }

    private void handleResolveAction(Reclamation rec) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Resolve Reclamation");
        dialog.setHeaderText("Responding to: " + rec.getTitle());
        dialog.setContentText("Enter admin response:");

        // Add your Cloudinary icon to the dialog window
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("https://res.cloudinary.com/dzxxigjkk/image/upload/v1770949791/images_qlsaxx.png"));

        dialog.showAndWait().ifPresent(responseText -> {
            if (responseText.isEmpty()) {
                showError("Input Required", "You must provide a response to resolve the issue.");
                return;
            }

            reclamationService.resolveReclamation(rec.getId(), responseText)
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        Platform.runLater(() -> {
                            loadReclamations(); // Refresh the table
                            showInfo("Success", "Reclamation #" + rec.getId() + " has been resolved.");
                        });
                    } else {
                        Platform.runLater(() -> showError("Error", "Failed to update: " + response.body()));
                    }
                });
        });
    }

    private void exportAsPdf(Reclamation rec) {
        if (rec == null) {
            showError("No selection", "Please select a reclamation to export.");
            return;
        }

        reclamationService.exportReclamationAsPdf(rec.getId())
                .thenAccept(postResp -> {
                    if (postResp.statusCode() >= 200 && postResp.statusCode() < 300) {
                        try {
                            Map<String, Object> respMap = mapper.readValue(postResp.body(), Map.class);
                            Object document = respMap.get("document");
                            if (document instanceof Map) {
                                Object preview = ((Map) document).get("preview_url");
                                if (preview != null) {
                                    String previewUrl = preview.toString();
                                    Platform.runLater(() -> {
                                        try {
                                            if (Desktop.isDesktopSupported()) {
                                                Desktop.getDesktop().browse(new URI(previewUrl));
                                            } else {
                                                showInfo("PDF Created", "Preview URL: " + previewUrl);
                                            }
                                        } catch (IOException | java.net.URISyntaxException ex) {
                                            showError("Error", "Failed to open preview: " + ex.getMessage());
                                        }
                                    });
                                    return;
                                }
                            }
                        } catch (Exception e) {
                            // fallthrough to generic success
                        }
                        Platform.runLater(() -> showInfo("PDF Created", "Document created successfully."));
                    } else {
                        Platform.runLater(() -> showError("Error", "Failed to create PDF: " + postResp.body()));
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> showError("Error", "Export failed: " + ex.getMessage()));
                    return null;
                });
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}