package java_project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)

public class RefundRequest {
    private int id;
    private int userId;
    private int reservationId;
    private double amount;
    private String status;
    private String reason;
    private String translatedReason;
    private LocalDateTime createdAt;

    public RefundRequest() {}

    public RefundRequest(int id, double amount, String status, String reason) {
        this.id = id;
        this.amount = amount;
        this.status = status;
        this.reason = reason;
    }

    public RefundRequest(int id, double amount, String status, String reason, String translatedReason) {
        this.id = id;
        this.amount = amount;
        this.status = status;
        this.reason = reason;
        this.translatedReason = translatedReason;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTranslatedReason() {
        return translatedReason;
    }

    public void setTranslatedReason(String translatedReason) {
        this.translatedReason = translatedReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}