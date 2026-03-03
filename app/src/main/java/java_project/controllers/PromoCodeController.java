package java_project.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.stage.Stage;
import java_project.models.PromoCode;
import java_project.models.Offer;
import java_project.services.PromoCodeService;
import java_project.services.OfferService;
import java_project.services.PromoCodePDFService;
import java_project.services.EmailService;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class PromoCodeController implements Initializable {

    @FXML private TableView<PromoCode> promoCodeTable;
    @FXML private ComboBox<Offer> offerComboBox;
    @FXML private TextField searchField;
    @FXML private CheckBox activeFilterCheckBox;
    @FXML private CheckBox inactiveFilterCheckBox;
    @FXML private VBox statisticsView;

    private final PromoCodeService promoCodeService = new PromoCodeService();
    private final OfferService offerService = new OfferService();
    private final PromoCodePDFService pdfService = new PromoCodePDFService();
    private final EmailService emailService = new EmailService();
    private final ObservableList<PromoCode> promoCodeList = FXCollections.observableArrayList();
    private final ObservableList<Offer> offerList = FXCollections.observableArrayList();
    private final ObservableList<PromoCode> filteredPromoCodeList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("? DEBUG: PromoCodeController initialize() called!");
        System.out.println("? DEBUG: Location: " + location);
        System.out.println("? DEBUG: ResourceBundle: " + resources);
        
        System.out.println("? DEBUG: Starting to load offers...");
        loadOffers();
        System.out.println("? DEBUG: PromoCodeController initialization completed!");
        
        setupTableColumns();
        setupEventListeners();
        loadPromoCodes();
    }

    private void setupEventListeners() {
        // Real-time search
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            applyFilters();
        });

        // Offer filter
        offerComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            applyFilters();
        });

        // Status filters
        activeFilterCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
            applyFilters();
        });

        inactiveFilterCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
            applyFilters();
        });
    }

    private void applyFilters() {
        filteredPromoCodeList.clear();
        
        String searchText = searchField.getText().toLowerCase();
        Offer selectedOffer = offerComboBox.getValue();
        boolean showActive = activeFilterCheckBox.isSelected();
        boolean showInactive = inactiveFilterCheckBox.isSelected();

        for (PromoCode promoCode : promoCodeList) {
            // Text search filter
            boolean matchesSearch = searchText.isEmpty() || 
                promoCode.getCode().toLowerCase().contains(searchText) ||
                promoCode.getDescription().toLowerCase().contains(searchText);

            // Offer filter
            boolean matchesOffer = selectedOffer == null || 
                promoCode.getOfferId() == selectedOffer.getId();

            // Status filter
            boolean matchesStatus = true;
            if (showActive && !showInactive) {
                matchesStatus = promoCode.isActive();
            } else if (!showActive && showInactive) {
                matchesStatus = !promoCode.isActive();
            }

            if (matchesSearch && matchesOffer && matchesStatus) {
                filteredPromoCodeList.add(promoCode);
            }
        }

        promoCodeTable.setItems(filteredPromoCodeList);
    }

    private void setupTableColumns() {
        // Get columns by index since we removed fx:ids (ID column removed)
        TableColumn<PromoCode, String> codeColumn = (TableColumn<PromoCode, String>) promoCodeTable.getColumns().get(0);
        TableColumn<PromoCode, String> descriptionColumn = (TableColumn<PromoCode, String>) promoCodeTable.getColumns().get(1);
        TableColumn<PromoCode, String> offerColumn = (TableColumn<PromoCode, String>) promoCodeTable.getColumns().get(2);
        TableColumn<PromoCode, String> validFromColumn = (TableColumn<PromoCode, String>) promoCodeTable.getColumns().get(3);
        TableColumn<PromoCode, String> validToColumn = (TableColumn<PromoCode, String>) promoCodeTable.getColumns().get(4);
        TableColumn<PromoCode, String> usageColumn = (TableColumn<PromoCode, String>) promoCodeTable.getColumns().get(5);
        TableColumn<PromoCode, Boolean> activeColumn = (TableColumn<PromoCode, Boolean>) promoCodeTable.getColumns().get(6);
        TableColumn<PromoCode, Void> actionsColumn = (TableColumn<PromoCode, Void>) promoCodeTable.getColumns().get(7);
        
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // Custom cell value factory for offer column to show offer titles
        offerColumn.setCellValueFactory(cellData -> {
            int offerId = cellData.getValue().getOfferId();
            String offerTitle = offerList.stream()
                .filter(offer -> offer != null && offer.getId() == offerId)
                .map(Offer::getTitle)
                .findFirst()
                .orElse("Unknown Offer");
            return new SimpleStringProperty(offerTitle);
        });
        
        validFromColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getValidFrom().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                cellData.getValue().validFromProperty()
            )
        );
        
        validToColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getValidTo().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                cellData.getValue().validToProperty()
            )
        );
        
        usageColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getUsedCount() + "/" + cellData.getValue().getUsageLimit(),
                cellData.getValue().usedCountProperty(),
                cellData.getValue().usageLimitProperty()
            )
        );
        
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        
        // Add custom cell factory for actions
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button(" Edit");
            private final Button deleteButton = new Button(" Delete");
            private final Button pdfButton = new Button("📄 PDF");
            private final HBox buttons = new HBox(5, editButton, pdfButton, deleteButton);

            {
                editButton.getStyleClass().addAll("table-button", "edit-btn");
                deleteButton.getStyleClass().addAll("table-button", "delete-btn");
                pdfButton.getStyleClass().addAll("table-button", "pdf-btn");

                editButton.setOnAction(e -> {
                    PromoCode promoCode = getTableView().getItems().get(getIndex());
                    handleEditPromoCode(promoCode);
                });

                deleteButton.setOnAction(e -> {
                    PromoCode promoCode = getTableView().getItems().get(getIndex());
                    handleDeletePromoCode(promoCode);
                });

                pdfButton.setOnAction(e -> {
                    PromoCode promoCode = getTableView().getItems().get(getIndex());
                    handleDownloadPDF(promoCode);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });

        promoCodeTable.setItems(filteredPromoCodeList);
    }

    private void loadOffers() {
        System.out.println("🔍 DEBUG: Starting to load offers...");
        
        offerService.getAllOffers().thenAccept(response -> {
            System.out.println("🔍 DEBUG: Offers API response status: " + response.statusCode());
            System.out.println("🔍 DEBUG: Offers API response body: " + response.body());
            
            if (response.statusCode() == 200) {
                List<Offer> offers = parseOffersFromJson(response.body());
                System.out.println("🔍 DEBUG: Parsed " + offers.size() + " offers from JSON");
                
                offerList.clear();
                offerList.addAll(offers);
                System.out.println("🔍 DEBUG: Added offers to offerList, size: " + offerList.size());
                
                javafx.application.Platform.runLater(() -> {
                    System.out.println("🔍 DEBUG: Setting up combo box on UI thread...");
                    
                    offerComboBox.setItems(offerList);
                    System.out.println("🔍 DEBUG: Set items to combo box, combo box size: " + offerComboBox.getItems().size());
                    
                    // Use simple string converter instead of custom cell factory
                    offerComboBox.setConverter(new javafx.util.StringConverter<Offer>() {
                        @Override
                        public String toString(Offer offer) {
                            if (offer == null) return "All Offers";
                            return offer.getId() + " - " + offer.getTitle();
                        }
                        
                        @Override
                        public Offer fromString(String string) {
                            // Simple parsing - not needed for display only
                            if (string.equals("All Offers") || string.isEmpty()) {
                                return null;
                            }
                            try {
                                int id = Integer.parseInt(string.split(" - ")[0]);
                                for (Offer offer : offerList) {
                                    if (offer.getId() == id) {
                                        return offer;
                                    }
                                }
                            } catch (Exception e) {
                                // Ignore parsing errors
                            }
                            return null;
                        }
                    });
                    
                    // Add "All Offers" option
                    offerComboBox.getItems().add(0, null);
                    System.out.println("🔍 DEBUG: Added 'All Offers' option, final combo size: " + offerComboBox.getItems().size());
                    
                    offerComboBox.getSelectionModel().selectFirst();
                    System.out.println("🔍 DEBUG: Selected first item in combo box");
                    
                    // Debug: Print all items in combo box
                    for (int i = 0; i < offerComboBox.getItems().size(); i++) {
                        Offer item = offerComboBox.getItems().get(i);
                        System.out.println("🔍 DEBUG: Combo box item " + i + ": " + 
                            (item == null ? "All Offers" : item.getId() + " - " + item.getTitle()));
                    }
                });
            } else {
                System.err.println("🔍 DEBUG: Failed to load offers, status: " + response.statusCode());
            }
        }).exceptionally(e -> {
            System.err.println("🔍 DEBUG: Exception loading offers: " + e.getMessage());
            e.printStackTrace();
            return null;
        });
    }

    private void loadPromoCodes() {
        promoCodeService.getAllPromoCodes().thenAccept(response -> {
            if (response.statusCode() == 200) {
                List<PromoCode> promoCodes = parsePromoCodesFromJson(response.body());
                javafx.application.Platform.runLater(() -> {
                    promoCodeList.clear();
                    promoCodeList.addAll(promoCodes);
                    applyFilters(); // Apply initial filters
                });
            }
        });
    }

    @FXML
    private void handleAddPromoCode() {
        try {
            Stage stage = new Stage();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/java_project/views/promoCode/addPromoCodeView.fxml"));
            javafx.scene.Parent root = loader.load();
            stage.setTitle("Add Promo Code");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
            
            // Refresh after closing
            stage.setOnHidden(e -> loadPromoCodes());
        } catch (Exception e) {
            showError("Error", "Failed to open add promo code window: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadPromoCodes();
    }

    @FXML
    private void handleTestCombo() {
        System.out.println("🧪 DEBUG: Testing combo box...");
        System.out.println("🧪 DEBUG: Combo box items count: " + offerComboBox.getItems().size());
        System.out.println("🧪 DEBUG: Selected item: " + offerComboBox.getValue());
        
        for (int i = 0; i < offerComboBox.getItems().size(); i++) {
            Offer item = offerComboBox.getItems().get(i);
            System.out.println("🧪 DEBUG: Item " + i + ": " + 
                (item == null ? "All Offers" : item.getId() + " - " + item.getTitle()));
        }
        
        // Try to manually set a selection
        if (offerComboBox.getItems().size() > 1) {
            offerComboBox.getSelectionModel().select(1);
            System.out.println("🧪 DEBUG: Manually selected item 1: " + offerComboBox.getValue());
        }
    }

    @FXML
    public void showStatisticsView() {
        if (statisticsView.isVisible()) {
            // Hide statistics, show table
            statisticsView.setVisible(false);
            promoCodeTable.setVisible(true);
        } else {
            // Show statistics, hide table
            promoCodeTable.setVisible(false);
            statisticsView.setVisible(true);
            
            // Initialize statistics view if empty
            if (statisticsView.getChildren().isEmpty()) {
                initializeStatisticsView();
            }
        }
    }

    @FXML
    private void exportToCSV() {
        try {
            // Get all offers from the backend
            offerService.getAllOffers().thenAccept(response -> {
                if (response.statusCode() == 200) {
                    List<Offer> offers = parseOffersFromJson(response.body());
                    javafx.application.Platform.runLater(() -> {
                        try {
                            exportOffersToCSV(offers);
                            showInfo("Export Successful", "Successfully exported " + offers.size() + " offers to CSV file!");
                        } catch (Exception e) {
                            showError("Export Error", "Failed to export offers: " + e.getMessage());
                        }
                    });
                } else {
                    javafx.application.Platform.runLater(() -> 
                        showError("Export Error", "Failed to fetch offers from server"));
                }
            }).exceptionally(e -> {
                javafx.application.Platform.runLater(() -> 
                    showError("Export Error", "Error connecting to server: " + e.getMessage()));
                return null;
            });
        } catch (Exception e) {
            showError("Export Error", "Unexpected error: " + e.getMessage());
        }
    }

    private void exportOffersToCSV(List<Offer> offers) throws Exception {
        // Create file chooser
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Save Offers CSV");
        fileChooser.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("offers_export.csv");
        
        // Show save dialog
        java.io.File file = fileChooser.showSaveDialog(promoCodeTable.getScene().getWindow());
        if (file == null) return; // User cancelled
        
        // Write CSV data
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(file))) {
            // Write header
            writer.println("ID,Voyage ID,Title,Description,Discount Percentage,Start Date,End Date,Is Active");
            
            // Write data rows
            for (Offer offer : offers) {
                String csvRow = String.format("%d,%d,\"%s\",\"%s\",\"%.2f\",\"%s\",\"%s\",\"%s\"",
                    offer.getId(),
                    offer.getVoyageId(),
                    escapeCSV(offer.getTitle()),
                    escapeCSV(offer.getDescription()),
                    offer.getDiscountPercentage(),
                    offer.getStartDate() != null ? offer.getStartDate().toString() : "",
                    offer.getEndDate() != null ? offer.getEndDate().toString() : "",
                    offer.isActive()
                );
                writer.println(csvRow);
            }
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        // Escape quotes and wrap in quotes if contains comma or quote
        if (value.contains(",") || value.contains("\"")) {
            return value.replace("\"", "\"\"");
        }
        return value;
    }

    private void initializeStatisticsView() {
        // Title
        Label title = new Label("📊 Statistics Overview");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Stats Cards
        HBox cardsBox = new HBox(20);
        cardsBox.getChildren().addAll(
            createStatCard("Total Promo Codes", String.valueOf(promoCodeList.size()), "#3498db"),
            createStatCard("Active Codes", String.valueOf(promoCodeList.stream().mapToInt(pc -> pc.isActive() ? 1 : 0).sum()), "#2ecc71"),
            createStatCard("Inactive Codes", String.valueOf(promoCodeList.stream().mapToInt(pc -> !pc.isActive() ? 1 : 0).sum()), "#e74c3c"),
            createStatCard("Total Offers", String.valueOf(offerList.size()), "#f39c12")
        );

        // Charts Section
        HBox chartsBox = new HBox(20);
        
        // Pie Chart for Promo Code Status
        PieChart statusPieChart = new PieChart();
        statusPieChart.setTitle("Promo Code Status");
        int activeCount = (int) promoCodeList.stream().filter(PromoCode::isActive).count();
        int inactiveCount = promoCodeList.size() - activeCount;
        statusPieChart.getData().addAll(
            new PieChart.Data("Active", activeCount),
            new PieChart.Data("Inactive", inactiveCount)
        );
        statusPieChart.setPrefSize(400, 300);

        // Line Chart for Promo Code Usage
        LineChart<String, Number> usageLineChart = new LineChart<>(new CategoryAxis(), new NumberAxis());
        usageLineChart.setTitle("Promo Code Usage Trend");
        usageLineChart.setPrefSize(400, 300);
        
        XYChart.Series<String, Number> usageSeries = new XYChart.Series<>();
        usageSeries.setName("Redemptions");
        // Sample data - replace with real data from your backend
        usageSeries.getData().addAll(
            new XYChart.Data<>("Mon", 5),
            new XYChart.Data<>("Tue", 8),
            new XYChart.Data<>("Wed", 12),
            new XYChart.Data<>("Thu", 7),
            new XYChart.Data<>("Fri", 15),
            new XYChart.Data<>("Sat", 22),
            new XYChart.Data<>("Sun", 18)
        );
        usageLineChart.getData().add(usageSeries);

        chartsBox.getChildren().addAll(statusPieChart, usageLineChart);

        // Bar Chart for Offer Distribution
        BarChart<String, Number> offerBarChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        offerBarChart.setTitle("Promo Codes per Offer");
        offerBarChart.setPrefSize(820, 250);
        
        XYChart.Series<String, Number> offerSeries = new XYChart.Series<>();
        offerSeries.setName("Number of Promo Codes");
        
        // Count promo codes per offer
        java.util.Map<Integer, Long> offerCounts = promoCodeList.stream()
            .collect(java.util.stream.Collectors.groupingBy(PromoCode::getOfferId, java.util.stream.Collectors.counting()));
        
        for (java.util.Map.Entry<Integer, Long> entry : offerCounts.entrySet()) {
            String offerName = offerList.stream()
                .filter(o -> o != null && o.getId() == entry.getKey())
                .map(Offer::getTitle)
                .findFirst()
                .orElse("Offer " + entry.getKey());
            offerSeries.getData().add(new XYChart.Data<>(offerName, entry.getValue().intValue()));
        }
        
        offerBarChart.getData().add(offerSeries);

        // Back button
        Button backButton = new Button("🔙 Back to Table");
        backButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        backButton.setOnAction(e -> showStatisticsView()); // Toggle back

        statisticsView.getChildren().addAll(title, cardsBox, chartsBox, offerBarChart, backButton);
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0); -fx-border-color: " + color + "; -fx-border-width: 2; -fx-border-radius: 10;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private void handleEditPromoCode(PromoCode promoCode) {
        try {
            Stage stage = new Stage();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/java_project/views/promoCode/updatePromoCodeView.fxml"));
            javafx.scene.Parent root = loader.load();
            
            // Pass promo code data to update controller
            java_project.controllers.promoCode.UpdatePromoCodeController controller = loader.getController();
            controller.setPromoCodeData(promoCode);
            
            stage.setTitle("Update Promo Code");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
            
            // Refresh after closing
            stage.setOnHidden(e -> loadPromoCodes());
        } catch (Exception e) {
            showError("Error", "Failed to open update promo code window: " + e.getMessage());
        }
    }

    private void handleDeletePromoCode(PromoCode promoCode) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Promo Code");
        alert.setHeaderText("Are you sure you want to delete this promo code?");
        alert.setContentText("Code: " + promoCode.getCode());

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            promoCodeService.deletePromoCode(promoCode.getId()).thenAccept(response -> {
                if (response.statusCode() == 200) {
                    javafx.application.Platform.runLater(() -> {
                        promoCodeList.remove(promoCode);
                        showInfo("Success", "Promo code deleted successfully");
                    });
                } else {
                    javafx.application.Platform.runLater(() -> 
                        showError("Error", "Failed to delete promo code"));
                }
            });
        }
    }

    @FXML
    public void handleSendPromoCodeEmail() {
        PromoCode selectedPromoCode = promoCodeTable.getSelectionModel().getSelectedItem();
        if (selectedPromoCode == null) {
            showError("No Selection", "Please select a promo code to email.");
            return;
        }
        
        // Get offer title for the email
        String offerTitle = offerList.stream()
            .filter(offer -> offer != null && offer.getId() == selectedPromoCode.getOfferId())
            .map(Offer::getTitle)
            .findFirst()
            .orElse("Unknown Offer");
        
        // Send email to all users
        emailService.sendPromoCodeToAllUsers(
            selectedPromoCode.getCode(),
            selectedPromoCode.getDescription(),
            selectedPromoCode.getValidTo().toString()
        ).thenAccept(success -> {
            javafx.application.Platform.runLater(() -> {
                if (success) {
                    showSuccess("Email Sent", "Promo code emailed to all users successfully!");
                } else {
                    showError("Email Failed", "Failed to send promo code emails. Check console for details.");
                }
            });
        });
    }

    private void handleDownloadPDF(PromoCode promoCode) {
        // Find the offer title with null check
        String offerTitle = offerList.stream()
            .filter(offer -> offer != null && offer.getId() == promoCode.getOfferId())
            .map(Offer::getTitle)
            .findFirst()
            .orElse("Unknown Offer");
        
        // Generate PDF
        pdfService.generatePromoCodePDF(promoCode, offerTitle, promoCodeTable.getScene().getWindow());
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Helper methods to parse JSON
    private List<Offer> parseOffersFromJson(String json) {
        List<Offer> offers = new java.util.ArrayList<>();
        System.out.println("🔍 DEBUG: Starting JSON parsing for offers...");
        System.out.println("🔍 DEBUG: Input JSON: " + json);
        
        try {
            // Simple JSON parsing for offers array
            if (json != null && json.startsWith("[") && json.endsWith("]")) {
                String[] items = json.substring(1, json.length() - 1).split("\\},\\{");
                System.out.println("🔍 DEBUG: Split JSON into " + items.length + " items");
                
                for (int i = 0; i < items.length; i++) {
                    String item = items[i];
                    if (item.trim().isEmpty()) continue;
                    
                    System.out.println("🔍 DEBUG: Processing item " + i + ": " + item);
                    
                    // Clean up the item
                    String cleanItem = item.replace("[", "").replace("]", "");
                    if (!cleanItem.endsWith("}")) cleanItem += "}";
                    
                    // Parse ID
                    int id = extractIntFromJson(cleanItem, "id");
                    System.out.println("🔍 DEBUG: Extracted ID: " + id);
                    
                    // Parse title
                    String title = extractStringFromJson(cleanItem, "title");
                    System.out.println("🔍 DEBUG: Extracted title: " + title);
                    
                    if (id > 0 && title != null && !title.isEmpty()) {
                        Offer offer = new Offer();
                        offer.setId(id);
                        offer.setTitle(title);
                        offers.add(offer);
                        System.out.println("🔍 DEBUG: Successfully created offer: " + id + " - " + title);
                    } else {
                        System.out.println("🔍 DEBUG: Skipping offer - invalid ID or title");
                    }
                }
            } else {
                System.out.println("🔍 DEBUG: Invalid JSON format - not starting with [ or ending with ]");
            }
        } catch (Exception e) {
            System.err.println("🔍 DEBUG: Error parsing offers JSON: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("🔍 DEBUG: Final offers list size: " + offers.size());
        return offers;
    }

    private List<PromoCode> parsePromoCodesFromJson(String json) {
        List<PromoCode> promoCodes = new java.util.ArrayList<>();
        try {
            if (json != null && json.startsWith("[") && json.endsWith("]")) {
                String[] items = json.substring(1, json.length() - 1).split("\\},\\{");
                for (String item : items) {
                    if (item.trim().isEmpty()) continue;
                    
                    String cleanItem = item.replace("[", "").replace("]", "");
                    if (!cleanItem.endsWith("}")) cleanItem += "}";
                    
                    int id = extractIntFromJson(cleanItem, "id");
                    String code = extractStringFromJson(cleanItem, "code");
                    String description = extractStringFromJson(cleanItem, "description");
                    int offerId = extractIntFromJson(cleanItem, "offerId");
                    String validFrom = extractStringFromJson(cleanItem, "validFrom");
                    String validTo = extractStringFromJson(cleanItem, "validTo");
                    int usageLimit = extractIntFromJson(cleanItem, "usageLimit");
                    int usedCount = extractIntFromJson(cleanItem, "usedCount");
                    boolean isActive = extractBooleanFromJson(cleanItem, "isActive");
                    
                    if (id > 0 && code != null && !code.isEmpty()) {
                        PromoCode promoCode = new PromoCode();
                        promoCode.setId(id);
                        promoCode.setCode(code);
                        promoCode.setDescription(description != null ? description : "");
                        promoCode.setOfferId(offerId);
                        try {
                            promoCode.setValidFrom(java.time.LocalDate.parse(validFrom));
                            promoCode.setValidTo(java.time.LocalDate.parse(validTo));
                        } catch (Exception e) {
                            promoCode.setValidFrom(java.time.LocalDate.now());
                            promoCode.setValidTo(java.time.LocalDate.now().plusMonths(1));
                        }
                        promoCode.setUsageLimit(usageLimit > 0 ? usageLimit : 1);
                        promoCode.setUsedCount(usedCount);
                        promoCode.setActive(isActive);
                        promoCodes.add(promoCode);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing promo codes JSON: " + e.getMessage());
        }
        return promoCodes;
    }

    private int extractIntFromJson(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int index = json.indexOf(pattern);
            if (index != -1) {
                int start = index + pattern.length();
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (end != -1) {
                    String value = json.substring(start, end).trim();
                    int result = Integer.parseInt(value);
                    System.out.println("🔍 DEBUG: Extracted int for key '" + key + "': " + result);
                    return result;
                }
            }
        } catch (Exception e) {
            System.err.println("🔍 DEBUG: Error extracting int for key '" + key + "': " + e.getMessage());
        }
        System.out.println("🔍 DEBUG: Could not extract int for key '" + key + "', returning 0");
        return 0;
    }

    private String extractStringFromJson(String json, String key) {
        try {
            String pattern = "\"" + key + "\":\"";
            int index = json.indexOf(pattern);
            if (index != -1) {
                int start = index + pattern.length();
                int end = json.indexOf("\"", start);
                if (end != -1) {
                    String value = json.substring(start, end);
                    System.out.println("🔍 DEBUG: Extracted string for key '" + key + "': " + value);
                    return value;
                }
            }
        } catch (Exception e) {
            System.err.println("🔍 DEBUG: Error extracting string for key '" + key + "': " + e.getMessage());
        }
        System.out.println("🔍 DEBUG: Could not extract string for key '" + key + "', returning null");
        return null;
    }

    private boolean extractBooleanFromJson(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int index = json.indexOf(pattern);
            if (index != -1) {
                int start = index + pattern.length();
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                if (end != -1) {
                    String value = json.substring(start, end).trim();
                    return Boolean.parseBoolean(value);
                }
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return true;
    }
}
