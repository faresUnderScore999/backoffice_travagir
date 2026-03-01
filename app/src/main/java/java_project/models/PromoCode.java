package java_project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PromoCode {
    private int id;
    private int offerId;
    private String code;
    private int usageLimit;
    private int usageCount;
    private LocalDate expiryDate;
    private boolean active;

    // default constructor for Jackson
    public PromoCode() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOfferId() { return offerId; }
    public void setOfferId(int offerId) { this.offerId = offerId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public int getUsageLimit() { return usageLimit; }
    public void setUsageLimit(int usageLimit) { this.usageLimit = usageLimit; }

    public int getUsageCount() { return usageCount; }
    public void setUsageCount(int usageCount) { this.usageCount = usageCount; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}