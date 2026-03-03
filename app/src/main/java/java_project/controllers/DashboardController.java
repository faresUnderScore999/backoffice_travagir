package java_project.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java_project.services.DashboardService;
import java_project.models.*;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DashboardController {
    @FXML private HBox overviewBox;

    @FXML private TableView<RecentLogin> recentLoginsTable;
    @FXML private TableColumn<RecentLogin, String> colLoginTime;
    @FXML private TableColumn<RecentLogin, String> colLoginUser;
    @FXML private TableColumn<RecentLogin, String> colLoginMethod;
    @FXML private TableColumn<RecentLogin, String> colLoginIP;

    @FXML private TableView<PopularVoyage> popularVoyagesTable;
    @FXML private TableColumn<PopularVoyage, String> colVoyageTitle;
    @FXML private TableColumn<PopularVoyage, String> colVoyageDest;
    @FXML private TableColumn<PopularVoyage, Integer> colVoyageCount;

    @FXML private LineChart<String, Number> timelineChart;
    @FXML private BarChart<String, Number> trendingChart;
    @FXML private BarChart<String, Number> offerChart;

    private final DashboardService service = new DashboardService();
    @FXML private Button btnSuggest;
    @FXML private TextArea suggestionArea;

    @FXML
    public void initialize() {
        // configure table columns for remaining tables
        colLoginTime.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLoginTime()));
        colLoginUser.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getUserName()));
        colLoginMethod.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLoginMethod()));
        colLoginIP.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getIpAddress()));

        colVoyageTitle.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitle()));
        colVoyageDest.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDestination()));
        colVoyageCount.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getVisitCount()).asObject());

        // fetch data
        loadOverview();
        loadRecentLogins();
        loadPopularVoyages();
        loadTrending();
        loadTimeline();
        loadOfferPerformance();

        // wire suggestion UI
        if (suggestionArea != null) {
            suggestionArea.setEditable(false);
        }
    }

    @FXML
    public void onSuggestVoyage() {
        // disable button while waiting
        btnSuggest.setDisable(true);
        suggestionArea.setText("Requesting suggestion...");

        String body = "{\"message\":\"Please propose the best new voyage to create.\"}";
        service.suggestVoyage(body).thenAccept(s -> {
            Platform.runLater(() -> {
                suggestionArea.setText(s == null || s.isEmpty() ? "No suggestion returned." : s);
                btnSuggest.setDisable(false);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                suggestionArea.setText("Error: " + ex.getMessage());
                btnSuggest.setDisable(false);
            });
            return null;
        });
    }

    private void loadOverview() {
        service.getOverview().thenAccept(ov -> {
            if (ov != null) {
                Platform.runLater(() -> {
                    overviewBox.getChildren().clear();
                    overviewBox.getChildren().addAll(
                        createMetricLabel("Offers", ov.getTotalOffers()),
                        createMetricLabel("Users", ov.getTotalUsers()),
                        createMetricLabel("Voyages", ov.getTotalVoyages()),
                        createMetricLabel("Reservations", ov.getTotalReservations()),
                        createMetricLabel("Today Res", ov.getTodayReservations()),
                        createMetricLabel("Revenue", ov.getTotalRevenue()),
                        createMetricLabel("Today Logins", ov.getTodayLogins()),
                        createMetricLabel("Active Last Week", ov.getActiveUsersLastWeek())
                    );
                });
            }
        });
    }

    private Label createMetricLabel(String name, Object value) {
        Label lbl = new Label(name + ": " + value);
        lbl.getStyleClass().add("metric-label");
        return lbl;
    }

    private void loadRecentLogins() {
        service.getRecentLogins(5).thenAccept(list -> {
            ObservableList<RecentLogin> items = FXCollections.observableArrayList(list);
            Platform.runLater(() -> recentLoginsTable.setItems(items));
        });
    }

    private void loadPopularVoyages() {
        service.getPopularVoyages(5).thenAccept(list -> {
            ObservableList<PopularVoyage> items = FXCollections.observableArrayList(list);
            Platform.runLater(() -> popularVoyagesTable.setItems(items));
        });
    }

    private void loadTrending() {
        service.getTrendingSearches(5).thenAccept(list -> {
            Platform.runLater(() -> {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Search Count");
                for (TrendingSearch trend : list) {
                    series.getData().add(new XYChart.Data<>(trend.getQuery(), trend.getCount()));
                }
                trendingChart.getData().clear();
                trendingChart.getData().add(series);
            });
        });
    }

    private void loadTimeline() {
        service.getActivityTimeline().thenAccept(list -> {
            Platform.runLater(() -> {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Logins");
                for (TimelineEntry entry : list) {
                    series.getData().add(new XYChart.Data<>(entry.getDate(), entry.getLogins()));
                }
                timelineChart.getData().clear();
                timelineChart.getData().add(series);
            });
        });
    }

    private void loadOfferPerformance() {
        service.getOfferPerformance().thenAccept(list -> {
            Platform.runLater(() -> {
                XYChart.Series<String, Number> clicksSeries = new XYChart.Series<>();
                clicksSeries.setName("Clicks");
                XYChart.Series<String, Number> viewsSeries = new XYChart.Series<>();
                viewsSeries.setName("Views");
                
                for (OfferPerformance offer : list) {
                    clicksSeries.getData().add(new XYChart.Data<>(offer.getTitle(), offer.getClicks()));
                    viewsSeries.getData().add(new XYChart.Data<>(offer.getTitle(), offer.getViews()));
                }
                offerChart.getData().clear();
                offerChart.getData().addAll(clicksSeries, viewsSeries);
            });
        });
    }

    @FXML
    public void showStatisticsPopup() {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("📊 Statistics Dashboard");
        popup.setWidth(900);
        popup.setHeight(700);

        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 20; -fx-background-color: #f5f5f5;");

        // Title
        Label title = new Label("📊 Statistics Overview");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Stats Cards
        HBox cardsBox = new HBox(20);
        cardsBox.getChildren().addAll(
            createStatCard("Total Offers", "142", "#3498db"),
            createStatCard("Active Promo Codes", "28", "#2ecc71"),
            createStatCard("Total Users", "1,247", "#e74c3c"),
            createStatCard("New Users Today", "15", "#f39c12")
        );

        // Charts Section
        HBox chartsBox = new HBox(20);
        
        // Pie Chart for Offers
        PieChart offerPieChart = new PieChart();
        offerPieChart.setTitle("Offers Distribution");
        offerPieChart.getData().addAll(
            new PieChart.Data("Active", 85),
            new PieChart.Data("Inactive", 25),
            new PieChart.Data("Expired", 32)
        );
        offerPieChart.setPrefSize(400, 300);

        // Line Chart for Promo Codes
        LineChart<String, Number> promoLineChart = new LineChart<>(new CategoryAxis(), new NumberAxis());
        promoLineChart.setTitle("Promo Codes Usage (Last 7 Days)");
        promoLineChart.setPrefSize(400, 300);
        
        XYChart.Series<String, Number> promoSeries = new XYChart.Series<>();
        promoSeries.setName("Redemptions");
        promoSeries.getData().addAll(
            new XYChart.Data<>("Mon", 5),
            new XYChart.Data<>("Tue", 8),
            new XYChart.Data<>("Wed", 12),
            new XYChart.Data<>("Thu", 7),
            new XYChart.Data<>("Fri", 15),
            new XYChart.Data<>("Sat", 22),
            new XYChart.Data<>("Sun", 18)
        );
        promoLineChart.getData().add(promoSeries);

        chartsBox.getChildren().addAll(offerPieChart, promoLineChart);

        // Bar Chart for User Activity
        BarChart<String, Number> userBarChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        userBarChart.setTitle("User Registration (Last 6 Months)");
        userBarChart.setPrefSize(820, 250);
        
        XYChart.Series<String, Number> userSeries = new XYChart.Series<>();
        userSeries.setName("New Users");
        userSeries.getData().addAll(
            new XYChart.Data<>("Jan", 120),
            new XYChart.Data<>("Feb", 150),
            new XYChart.Data<>("Mar", 180),
            new XYChart.Data<>("Apr", 165),
            new XYChart.Data<>("May", 210),
            new XYChart.Data<>("Jun", 195)
        );
        userBarChart.getData().add(userSeries);

        // Close Button
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        closeButton.setOnAction(e -> popup.close());

        root.getChildren().addAll(title, cardsBox, chartsBox, userBarChart, closeButton);

        Scene scene = new Scene(root);
        popup.setScene(scene);
        popup.showAndWait();
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
}
