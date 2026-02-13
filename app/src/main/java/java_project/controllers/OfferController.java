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
import java_project.models.Offer;
import java_project.services.OfferService;
import java_project.controllers.offer.UpdateOfferController;

public class OfferController {
    @FXML private TableView<Offer> offerTable;
    @FXML private TableColumn<Offer, Integer> colId;
    @FXML private TableColumn<Offer, String> colTitle;
    @FXML private TableColumn<Offer, Double> colDiscount;
    @FXML private TableColumn<Offer, Void> colActions;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private final OfferService offerService = new OfferService();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        mapper.registerModule(new JavaTimeModule());
        setupColumns();
        loadOffers();
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discountPercentage"));
        setupActionColumn();
    }

    @FXML
    public void loadOffers() {
        offerService.getAllOffers().thenAccept(response -> {
            if (response.statusCode() == 200) {
                try {
                    List<Offer> offers = mapper.readValue(response.body(), new TypeReference<List<Offer>>() {});
                    Platform.runLater(() -> offerTable.getItems().setAll(offers));
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
                    Offer offer = getTableView().getItems().get(getIndex());
                    offerService.deleteOffer(offer.getId()).thenAccept(res -> {
                        if (res.statusCode() == 200) Platform.runLater(() -> loadOffers());
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/java_project/views/offer/addOfferView.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(loader.load()));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        loadOffers();
    }

    private void openUpdateModal(Offer offer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/java_project/views/offer/updateOfferView.fxml"));
             Parent root = loader.load();
           
           
            UpdateOfferController controller = loader.getController();
            controller.setOfferData(offer);
             Stage stage = new Stage();
              stage.setTitle("Update    Offer");
          stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadOffers();
        } catch (IOException e) { e.printStackTrace(); }
    }
}