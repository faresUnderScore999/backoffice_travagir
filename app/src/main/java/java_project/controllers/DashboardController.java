package java_project.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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

    @FXML private TableView<TrendingSearch> trendingTable;
    @FXML private TableColumn<TrendingSearch, String> colTrendQuery;
    @FXML private TableColumn<TrendingSearch, Integer> colTrendCount;

    @FXML private TableView<TimelineEntry> timelineTable;
    @FXML private TableColumn<TimelineEntry, String> colTimelineDate;
    @FXML private TableColumn<TimelineEntry, Integer> colTimelineLogins;

    @FXML private TableView<OfferPerformance> offerPerfTable;
    @FXML private TableColumn<OfferPerformance, String> colOfferTitle;
    @FXML private TableColumn<OfferPerformance, Double> colOfferDiscount;
    @FXML private TableColumn<OfferPerformance, Integer> colOfferClicks;
    @FXML private TableColumn<OfferPerformance, Integer> colOfferViews;

    private final DashboardService service = new DashboardService();

    @FXML
    public void initialize() {
        // configure table columns
        colLoginTime.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLoginTime()));
        colLoginUser.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getUserName()));
        colLoginMethod.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLoginMethod()));
        colLoginIP.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getIpAddress()));

        colVoyageTitle.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitle()));
        colVoyageDest.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDestination()));
        colVoyageCount.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getVisitCount()).asObject());

        colTrendQuery.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getQuery()));
        colTrendCount.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getCount()).asObject());

        colTimelineDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDate()));
        colTimelineLogins.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getLogins()).asObject());

        colOfferTitle.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitle()));
        colOfferDiscount.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getDiscountPercentage()).asObject());
        colOfferClicks.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getClicks()).asObject());
        colOfferViews.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getViews()).asObject());

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
            ObservableList<TrendingSearch> items = FXCollections.observableArrayList(list);
            Platform.runLater(() -> trendingTable.setItems(items));
        });
    }

    private void loadTimeline() {
        service.getActivityTimeline().thenAccept(list -> {
            ObservableList<TimelineEntry> items = FXCollections.observableArrayList(list);
            Platform.runLater(() -> timelineTable.setItems(items));
        });
    }

    private void loadOfferPerformance() {
        service.getOfferPerformance().thenAccept(list -> {
            ObservableList<OfferPerformance> items = FXCollections.observableArrayList(list);
            Platform.runLater(() -> offerPerfTable.setItems(items));
        });
    }
}
