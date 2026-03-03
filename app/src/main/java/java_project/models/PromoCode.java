package java_project.models;

import javafx.beans.property.*;
import java.time.LocalDate;

public class PromoCode {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty code = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final IntegerProperty offerId = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDate> validFrom = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> validTo = new SimpleObjectProperty<>();
    private final IntegerProperty usageLimit = new SimpleIntegerProperty();
    private final IntegerProperty usedCount = new SimpleIntegerProperty();
    private final BooleanProperty isActive = new SimpleBooleanProperty();

    public PromoCode() {}

    public PromoCode(int id, String code, String description, int offerId,
                    LocalDate validFrom, LocalDate validTo, int usageLimit,
                    int usedCount, boolean active) {
        setId(id);
        setCode(code);
        setDescription(description);
        setOfferId(offerId);
        setValidFrom(validFrom);
        setValidTo(validTo);
        setUsageLimit(usageLimit);
        setUsedCount(usedCount);
        setActive(active);
    }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty codeProperty() { return code; }
    public StringProperty descriptionProperty() { return description; }
    public IntegerProperty offerIdProperty() { return offerId; }
    public ObjectProperty<LocalDate> validFromProperty() { return validFrom; }
    public ObjectProperty<LocalDate> validToProperty() { return validTo; }
    public IntegerProperty usageLimitProperty() { return usageLimit; }
    public IntegerProperty usedCountProperty() { return usedCount; }
    public BooleanProperty isActiveProperty() { return isActive; }

    // Getters and Setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    
    public String getCode() { return code.get(); }
    public void setCode(String code) { this.code.set(code); }
    
    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    
    public int getOfferId() { return offerId.get(); }
    public void setOfferId(int offerId) { this.offerId.set(offerId); }
    
    public LocalDate getValidFrom() { return validFrom.get(); }
    public void setValidFrom(LocalDate validFrom) { this.validFrom.set(validFrom); }
    
    public LocalDate getValidTo() { return validTo.get(); }
    public void setValidTo(LocalDate validTo) { this.validTo.set(validTo); }
    
    public int getUsageLimit() { return usageLimit.get(); }
    public void setUsageLimit(int usageLimit) { this.usageLimit.set(usageLimit); }
    
    public int getUsedCount() { return usedCount.get(); }
    public void setUsedCount(int usedCount) { this.usedCount.set(usedCount); }
    
    public boolean isActive() { return isActive.get(); }
    public void setActive(boolean active) { this.isActive.set(active); }

    @Override
    public String toString() {
        return "PromoCode{" +
                "id=" + getId() +
                ", code='" + getCode() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", offerId=" + getOfferId() +
                ", validFrom=" + getValidFrom() +
                ", validTo=" + getValidTo() +
                ", usageLimit=" + getUsageLimit() +
                ", usedCount=" + getUsedCount() +
                ", isActive=" + isActive() +
                '}';
    }
}
