package java_project.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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
}
