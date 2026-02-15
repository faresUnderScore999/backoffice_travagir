package java_project.services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java_project.models.RefundRequest;

/**
 * Simple in-memory service only for testing the Refund UI.
 */
public class RefundService {

    private final ObservableList<RefundRequest> testData = FXCollections.observableArrayList();
    private int nextId = 1;

    public RefundService() {
        // Seed a few demo rows
        testData.add(new RefundRequest(nextId++, 120.0, "PENDING", "Flight delay"));
        testData.add(new RefundRequest(nextId++, 75.5, "APPROVED", "Hotel issue"));
    }

    public ObservableList<RefundRequest> getAllForTest() {
        return testData;
    }

    public RefundRequest addTestRefund(double amount, String reason) {
        RefundRequest r = new RefundRequest(nextId++, amount, "PENDING", reason);
        testData.add(r);
        return r;
    }
}