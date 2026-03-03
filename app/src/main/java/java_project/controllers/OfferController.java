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
import java_project.services.GoogleCalendarService;
import java_project.controllers.offer.UpdateOfferController;
import java_project.controllers.offer.GoogleCalendarDialogController;

public class OfferController {
    @FXML private TableView<Offer> offerTable;
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
        // Get columns by index since we removed fx:ids (ID column removed)
        TableColumn<Offer, String> colTitle = (TableColumn<Offer, String>) offerTable.getColumns().get(0);
        TableColumn<Offer, Double> colDiscount = (TableColumn<Offer, Double>) offerTable.getColumns().get(1);
        TableColumn<Offer, Void> colActions = (TableColumn<Offer, Void>) offerTable.getColumns().get(2);
        
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discountPercentage"));
        setupActionColumn(colActions);
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

    private void setupActionColumn(TableColumn<Offer, Void> colActions) {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final Button calendarBtn = new Button("📅");
            private final HBox pane = new HBox(5, editBtn, calendarBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("update-btn");
                deleteBtn.getStyleClass().add("delete-btn");
                calendarBtn.getStyleClass().add("calendar-btn");
                calendarBtn.setTooltip(new Tooltip("Ajouter au Google Calendar"));
                
                editBtn.setOnAction(e -> openUpdateModal(getTableView().getItems().get(getIndex())));
                calendarBtn.setOnAction(e -> openGoogleCalendarDialog(getTableView().getItems().get(getIndex())));
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
    
    private void openGoogleCalendarDialog(Offer offer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/java_project/views/offer/googleCalendarDialog.fxml"));
            Parent root = loader.load();
            
            GoogleCalendarDialogController controller = loader.getController();
            controller.setOffer(offer);
            
            Stage stage = new Stage();
            stage.setTitle("📅 Ajouter au Google Calendar");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);
            stage.showAndWait();
            
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de l'ouverture de la boîte de dialogue Google Calendar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}