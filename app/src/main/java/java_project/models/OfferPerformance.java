package java_project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OfferPerformance {
    private double discountPercentage;
    private double ctr;
    private int offerId;
    private int clicks;
    private String title;
    private int views;

    public OfferPerformance() {}

    public double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }
    public double getCtr() { return ctr; }
    public void setCtr(double ctr) { this.ctr = ctr; }
    public int getOfferId() { return offerId; }
    public void setOfferId(int offerId) { this.offerId = offerId; }
    public int getClicks() { return clicks; }
    public void setClicks(int clicks) { this.clicks = clicks; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }
}