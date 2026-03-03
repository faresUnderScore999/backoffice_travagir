package frontoffice.controllers.views;

import frontoffice.models.Reclamation;
import frontoffice.services.ReclamationService;
import frontoffice.utils.HttpErrorParser;
import frontoffice.utils.ViewContext;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ReclamationsViewController {
    @FXML
    private TextField reservationIdField;
    @FXML
    private TextField titleField;
    @FXML
    private TextField descriptionField;
    @FXML
    private TableView<Reclamation> reclamationsTable;
    @FXML
    private TableColumn<Reclamation, Integer> colId;
    @FXML
    private TableColumn<Reclamation, Integer> colReservationId;
    @FXML
    private TableColumn<Reclamation, String> colTitle;
    @FXML
    private TableColumn<Reclamation, String> colStatus;
    @FXML
    private TableColumn<Reclamation, String> colPriority;
    @FXML
    private Label statusLabel;

    private final ReclamationService reclamationService = new ReclamationService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colReservationId.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));

        Integer prefill = ViewContext.getInstance().getReclamationReservationId();
        if (prefill != null) {
            reservationIdField.setText(String.valueOf(prefill));
            ViewContext.getInstance().setReclamationReservationId(null);
        }

        onRefresh();
    }

    @FXML
    private void onRefresh() {
        statusLabel.setText("Loading...");
        reclamationService.getMy()
                .thenAccept(list -> Platform.runLater(() -> {
                    reclamationsTable.setItems(FXCollections.observableArrayList(list));
                    statusLabel.setText("");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Load error: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void onSubmit() {
        int reservationId;
        try {
            reservationId = Integer.parseInt(reservationIdField.getText().trim());
        } catch (Exception e) {
            statusLabel.setText("Reservation ID must be a number.");
            return;
        }

        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        String desc = descriptionField.getText() == null ? "" : descriptionField.getText().trim();
        if (title.length() < 5) {
            statusLabel.setText("Title must be at least 5 chars.");
            return;
        }
        if (desc.isBlank()) {
            statusLabel.setText("Description is required.");
            return;
        }

        Reclamation rec = new Reclamation();
        rec.setReservationId(reservationId);
        rec.setTitle(title);
        rec.setDescription(desc);

        statusLabel.setText("Submitting...");
        reclamationService.submit(rec)
                .thenAccept(resp -> Platform.runLater(() -> {
                    if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                        statusLabel.setText("Submitted.");
                        titleField.setText("");
                        descriptionField.setText("");
                        onRefresh();
                    } else {
                        String details = HttpErrorParser.bestEffortMessage(resp.body());
                        statusLabel.setText("Submit failed (" + resp.statusCode() + ")" + (details.isBlank() ? "" : ": " + details));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Submit error: " + ex.getMessage()));
                    return null;
                });
    }
}
