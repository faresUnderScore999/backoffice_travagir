package frontoffice.controllers.views;

import frontoffice.models.Association;
import frontoffice.services.AssociationService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AssociationsViewController {
    @FXML
    private TableView<Association> associationsTable;
    @FXML
    private TableColumn<Association, Integer> colId;
    @FXML
    private TableColumn<Association, String> colName;
    @FXML
    private TableColumn<Association, String> colCompanyCode;
    @FXML
    private TableColumn<Association, Double> colDiscount;
    @FXML
    private Label myAssociationLabel;
    @FXML
    private Label statusLabel;

    private final AssociationService associationService = new AssociationService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCompanyCode.setCellValueFactory(new PropertyValueFactory<>("companyCode"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discountRate"));
        onRefresh();
    }

    @FXML
    private void onRefresh() {
        statusLabel.setText("Loading...");
        associationService.getAll()
                .thenAccept(list -> Platform.runLater(() -> {
                    associationsTable.setItems(FXCollections.observableArrayList(list));
                    statusLabel.setText("");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Load error: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void onLoadMyAssociation() {
        myAssociationLabel.setText("Loading...");
        associationService.getMyAssociation()
                .thenAccept(assoc -> Platform.runLater(() -> {
                    if (assoc == null) {
                        myAssociationLabel.setText("No association.");
                    } else {
                        myAssociationLabel.setText(assoc.getName() + " (" + assoc.getCompanyCode() + ")");
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> myAssociationLabel.setText("Error: " + ex.getMessage()));
                    return null;
                });
    }
}
