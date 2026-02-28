package java_project.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java_project.models.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.net.http.HttpResponse;

public class DashboardService {
    private final ApiClient api = new ApiClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public CompletableFuture<DashboardOverview> getOverview() {
        return api.sendWithRetry("/api/v1/dashboard/overview", "GET", null)
                .thenApply(response -> {
                    try {
                        return mapper.readValue(response.body(), DashboardOverview.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                });
    }

    public CompletableFuture<List<RecentLogin>> getRecentLogins(int limit) {
        return api.sendWithRetry("/api/v1/dashboard/recent-logins?limit=" + limit, "GET", null)
                .thenApply(response -> {
                    try {
                        var node = mapper.readTree(response.body()).get("recentLogins");
                        return mapper.readerForListOf(RecentLogin.class).readValue(node);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return List.of();
                    }
                });
    }

    public CompletableFuture<List<PopularVoyage>> getPopularVoyages(int limit) {
        return api.sendWithRetry("/api/v1/dashboard/popular-voyages?limit=" + limit, "GET", null)
                .thenApply(response -> {
                    try {
                        var node = mapper.readTree(response.body()).get("popularVoyages");
                        return mapper.readerForListOf(PopularVoyage.class).readValue(node);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return List.of();
                    }
                });
    }

    public CompletableFuture<List<TrendingSearch>> getTrendingSearches(int limit) {
        return api.sendWithRetry("/api/v1/dashboard/search-trends?limit=" + limit, "GET", null)
                .thenApply(response -> {
                    try {
                        var node = mapper.readTree(response.body()).get("trendingSearches");
                        return mapper.readerForListOf(TrendingSearch.class).readValue(node);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return List.of();
                    }
                });
    }

    public CompletableFuture<List<TimelineEntry>> getActivityTimeline() {
        return api.sendWithRetry("/api/v1/dashboard/activity-timeline", "GET", null)
                .thenApply(response -> {
                    try {
                        var node = mapper.readTree(response.body()).get("activityTimeline");
                        return mapper.readerForListOf(TimelineEntry.class).readValue(node);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return List.of();
                    }
                });
    }

    public CompletableFuture<List<OfferPerformance>> getOfferPerformance() {
        return api.sendWithRetry("/api/v1/dashboard/offer-performance", "GET", null)
                .thenApply(response -> {
                    try {
                        var node = mapper.readTree(response.body()).get("offerPerformance");
                        return mapper.readerForListOf(OfferPerformance.class).readValue(node);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return List.of();
                    }
                });
    }
}
