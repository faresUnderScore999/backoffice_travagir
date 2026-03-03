package java_project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public class RefundRequest {
    private int id;
    private double amount;
    private String status;
    private String reason;
    private String translatedReason;

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
}