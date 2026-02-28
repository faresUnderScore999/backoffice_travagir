package java_project.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.io.IOException;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java_project.models.PromoCode;
import java_project.services.PromoCodeService;
import java_project.controllers.promocode.UpdatePromoCodeController;

public class PromoCodeController {
    @FXML private TableView<PromoCode> promoTable;
    @FXML private TableColumn<PromoCode, String> colCode;
    @FXML private TableColumn<PromoCode, Integer> colMaxUsage;
    @FXML private TableColumn<PromoCode, Integer> colUsageCount;
    @FXML private TableColumn<PromoCode, String> colExpiry;
    @FXML private TableColumn<PromoCode, Boolean> colActive;
    @FXML private TableColumn<PromoCode, Void> colActions;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private final PromoCodeService promoService = new PromoCodeService();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        mapper.registerModule(new JavaTimeModule());
        setupColumns();
        loadPromoCodes();
    }

    private void setupColumns() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colMaxUsage.setCellValueFactory(new PropertyValueFactory<>("maxUsage"));
        colUsageCount.setCellValueFactory(new PropertyValueFactory<>("usageCount"));
        colExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        setupActionColumn();
    }

    @FXML
    public void loadPromoCodes() {
        promoService.getAllPromoCodes().thenAccept(response -> {
            if (response.statusCode() == 200) {
                try {
                    List<PromoCode> codes = mapper.readValue(response.body(), new TypeReference<List<PromoCode>>() {});
                    Platform.runLater(() -> promoTable.getItems().setAll(codes));
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    private void setupActionColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("update-btn");
                deleteBtn.getStyleClass().add("delete-btn");
                editBtn.setOnAction(e -> openUpdateModal(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> {
                    PromoCode code = getTableView().getItems().get(getIndex());
                    promoService.deletePromoCode(code.getId()).thenAccept(res -> {
                        if (res.statusCode() == 200) Platform.runLater(() -> loadPromoCodes());
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    @FXML
    private void openAddModal() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/java_project/views/promoCode/addPromoCodeView.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(loader.load()));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        loadPromoCodes();
    }

    private void openUpdateModal(PromoCode code) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/java_project/views/promoCode/updatePromoCodeView.fxml"));
            Parent root = loader.load();
            UpdatePromoCodeController controller = loader.getController();
            controller.setPromoData(code);
            Stage stage = new Stage();
            stage.setTitle("Update Promo Code");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadPromoCodes();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
