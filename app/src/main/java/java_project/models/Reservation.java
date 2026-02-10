package java_project.models;

import java.time.LocalDateTime;

public class Reservation {
    private Integer id;
    private Integer userId;
    private Integer voyageId;
    private Integer offerId;
    private String reservationDate;
    private int numberOfPeople;
    private double totalPrice;
    private String status;
    private String specialRequests;
    private String paymentStatus;
    private String paymentDate;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getVoyageId() { return voyageId; }
    public void setVoyageId(Integer voyageId) { this.voyageId = voyageId; }
    public int getNumberOfPeople() { return numberOfPeople; }
    public void setNumberOfPeople(int numberOfPeople) { this.numberOfPeople = numberOfPeople; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}