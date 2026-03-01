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
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java_project.models.Offer;
import java_project.models.Voyage;
import java_project.services.OfferService;
import java_project.services.ApiClient;
import java_project.controllers.offer.UpdateOfferController;

public class OfferController {
    @FXML private TableView<Offer> offerTable;
    // show human-friendly voyage name after fetching voyages
    @FXML private TableColumn<Offer, String> colVoyage;
    @FXML private TableColumn<Offer, String> colTitle;
    @FXML private TableColumn<Offer, Double> colDiscount;
    @FXML private TableColumn<Offer, Void> colActions;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private final OfferService offerService = new OfferService();
    private final ApiClient apiClient = new ApiClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        mapper.registerModule(new JavaTimeModule());
        setupColumns();
        loadOffers();
    }

    private void setupColumns() {
        colVoyage.setCellValueFactory(new PropertyValueFactory<>("voyageName"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discountPercentage"));
        setupActionColumn();
    }

    @FXML
    public void loadOffers() {
        offerService.getAllOffers().thenCompose(response -> {
            if (response.statusCode() == 200) {
                try {
                    List<Offer> offers = mapper.readValue(response.body(), new TypeReference<List<Offer>>() {});
                    return fetchVoyages().thenApply(voyages -> {
                        // map id to Voyage
                        for (Offer o : offers) {
                            for (Voyage v : voyages) {
                                if (v.getId() == o.getVoyageId()) {
                                    o.setVoyageName(v.getTitle() + " - " + v.getDestination());
                                    break;
                                }
                            }
                        }
                        return offers;
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return CompletableFuture.completedFuture(List.<Offer>of());
        }).thenAccept(offers -> {
            Platform.runLater(() -> offerTable.getItems().setAll(offers));
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

    // helper to fetch voyages from backend
    private CompletableFuture<List<Voyage>> fetchVoyages() {
        return apiClient.sendWithRetry("/api/v1/offers/voyages", "GET", null)
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return mapper.readValue(response.body(), new TypeReference<List<Voyage>>() {});
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                    return List.<Voyage>of();
                });
    }
}