package java_project.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java_project.models.RefundRequest;
import java_project.services.RefundRequestService;
import java_project.utils.CsvUtils;

import java.io.File;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class RefundRequestController {

    @FXML private TextField amountField;
    @FXML private TextArea reasonArea;
    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> searchTypeChoice;
    @FXML private ChoiceBox<String> searchValueChoice;

    @FXML private ChoiceBox<String> sortFieldChoice;
    @FXML private ChoiceBox<String> sortOrderChoice;

    @FXML private TableView<RefundRequest> refundTable;
    @FXML private TableColumn<RefundRequest, Integer> idCol;
    @FXML private TableColumn<RefundRequest, Double> amountCol;
    @FXML private TableColumn<RefundRequest, String> statusCol;
    @FXML private TableColumn<RefundRequest, String> reasonCol;

    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button deleteButton;

    @FXML private Label statusTextLabel;
    @FXML private Label refundTotalLabel;
    @FXML private Label refundPendingLabel;
    @FXML private Label refundApprovedLabel;
    @FXML private Label refundRejectedLabel;
    @FXML private Label refundApprovedSumLabel;

    private final ObservableList<RefundRequest> refundItems = FXCollections.observableArrayList();
    private FilteredList<RefundRequest> filteredRefunds;

    private static final String SEARCH_ALL = "All";
    private static final String SEARCH_ID = "ID";
    private static final String SEARCH_AMOUNT = "Amount";
    private static final String SEARCH_STATUS = "Status";
    private static final String SEARCH_REASON = "Reason";

    private static final String ANY_VALUE = "(Any)";

    private static final String SORT_ID = "ID";
    private static final String SORT_AMOUNT = "Amount";
    private static final String SORT_STATUS = "Status";
    private static final String SORT_DESCRIPTION = "Description";
    private static final String ORDER_ASC = "Ascending";
    private static final String ORDER_DESC = "Descending";

    private final RefundRequestService refundService = new RefundRequestService();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final DecimalFormat MONEY_FMT = new DecimalFormat("0.##");

    @FXML
    private void initialize() {
        mapper.registerModule(new JavaTimeModule());

        // Match RefundRequest getters: getId(), getAmount(), getStatus()
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (reasonCol != null) {
            // Show translatedReason when available (fallback to original reason)
            reasonCol.setCellValueFactory(cell -> {
                RefundRequest r = cell == null ? null : cell.getValue();
                String translated = r == null ? null : r.getTranslatedReason();
                String original = r == null ? null : r.getReason();
                String display = (translated != null && !translated.isBlank()) ? translated : original;
                return new ReadOnlyStringWrapper(display == null ? "" : display);
            });
        }

        // Table data is stored in refundItems.
        // We wrap it with FilteredList + SortedList so:
        // - filtering updates the table instantly
        // - sorting still works when you click table headers
        filteredRefunds = new FilteredList<>(refundItems, item -> true);
        SortedList<RefundRequest> sorted = new SortedList<>(filteredRefunds);
        sorted.comparatorProperty().bind(refundTable.comparatorProperty());
        refundTable.setItems(sorted);

        // "Tri" (sorting) controls
        setupSortControls();

        if (searchTypeChoice != null) {
            searchTypeChoice.setItems(FXCollections.observableArrayList(
                SEARCH_ALL, SEARCH_ID, SEARCH_AMOUNT, SEARCH_STATUS, SEARCH_REASON
            ));
            searchTypeChoice.setValue(SEARCH_ALL);
            searchTypeChoice.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
                updateSearchInputMode();
                populateSearchValueChoices();
                runSearchFromUi();
            });
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applySearchFilter(newVal));
        }

        if (searchValueChoice != null) {
            searchValueChoice.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> runSearchFromUi());
        }

        updateSearchInputMode();

        if (approveButton != null) {
            approveButton.disableProperty().bind(refundTable.getSelectionModel().selectedItemProperty().isNull());
        approveButton.setStyle("-fx-background-color: #1aff00; -fx-text-fill: white;");
             
        }
        if (rejectButton != null) {
            rejectButton.disableProperty().bind(refundTable.getSelectionModel().selectedItemProperty().isNull());
            rejectButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        }
        if (deleteButton != null) {
            deleteButton.disableProperty().bind(refundTable.getSelectionModel().selectedItemProperty().isNull());
            deleteButton.setStyle("-fx-background-color: #ff0000; -fx-text-fill: white;");
        }

        loadMyRefunds(); // USER view
        // if you want ADMIN view instead, use: loadAllRefunds();

        // Initialize stats once UI is loaded
        updateRefundStats();
    }

    /**
     * Initializes and wires the sort dropdowns.
     * Uses TableView native sorting (same as clicking column headers).
     */
    private void setupSortControls() {
        if (refundTable == null) return;

        if (sortFieldChoice != null) {
            sortFieldChoice.setItems(FXCollections.observableArrayList(
                    SORT_ID, SORT_AMOUNT, SORT_STATUS, SORT_DESCRIPTION
            ));
            sortFieldChoice.setValue(SORT_ID);
            sortFieldChoice.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> applySortFromUi());
        }

        if (sortOrderChoice != null) {
            sortOrderChoice.setItems(FXCollections.observableArrayList(ORDER_ASC, ORDER_DESC));
            sortOrderChoice.setValue(ORDER_ASC);
            sortOrderChoice.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> applySortFromUi());
        }

        applySortFromUi();
    }

    /** Applies the currently selected sort field + order to the table. */
    private void applySortFromUi() {
        if (refundTable == null) return;

        String field = sortFieldChoice == null || sortFieldChoice.getValue() == null ? SORT_ID : sortFieldChoice.getValue();
        String order = sortOrderChoice == null || sortOrderChoice.getValue() == null ? ORDER_ASC : sortOrderChoice.getValue();

        TableColumn<RefundRequest, ?> col = switch (field) {
            case SORT_AMOUNT -> amountCol;
            case SORT_STATUS -> statusCol;
            case SORT_DESCRIPTION -> reasonCol;
            case SORT_ID -> idCol;
            default -> idCol;
        };
        if (col == null) return;

        col.setSortType(ORDER_DESC.equals(order) ? TableColumn.SortType.DESCENDING : TableColumn.SortType.ASCENDING);
        refundTable.getSortOrder().setAll(col);
        refundTable.sort();
    }

    /**
     * Shows either the value dropdown (no typing) or the text field depending on search type.
     * - ID / Amount / Status => dropdown only
     * - Reason / All => text field
     */
    private void updateSearchInputMode() {
        if (searchTypeChoice == null) return;
        String type = searchTypeChoice.getValue();

        boolean useValueDropdown = SEARCH_ID.equals(type) || SEARCH_AMOUNT.equals(type) || SEARCH_STATUS.equals(type);
        boolean useText = SEARCH_ALL.equals(type) || SEARCH_REASON.equals(type);

        if (searchValueChoice != null) {
            searchValueChoice.setManaged(useValueDropdown);
            searchValueChoice.setVisible(useValueDropdown);
        }
        if (searchField != null) {
            searchField.setManaged(useText);
            searchField.setVisible(useText);
        }
    }

    /**
     * Populates the value dropdown based on current table data.
     * Values are taken from refundItems so it always matches what is loaded.
     */
    private void populateSearchValueChoices() {
        if (searchValueChoice == null || searchTypeChoice == null) return;

        String type = searchTypeChoice.getValue();
        if (!(SEARCH_ID.equals(type) || SEARCH_AMOUNT.equals(type) || SEARCH_STATUS.equals(type))) return;

        List<String> values = new ArrayList<>();
        values.add(ANY_VALUE);

        if (SEARCH_STATUS.equals(type)) {
            refundItems.stream()
                    .map(r -> r == null ? null : r.getStatus())
                    .filter(s -> s != null && !s.isBlank())
                    .map(String::trim)
                    .distinct()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .forEach(values::add);
        } else if (SEARCH_ID.equals(type)) {
            refundItems.stream()
                    .map(r -> r == null ? null : r.getId())
                    .distinct()
                    .sorted()
                    .map(String::valueOf)
                    .forEach(values::add);
        } else if (SEARCH_AMOUNT.equals(type)) {
            refundItems.stream()
                    .map(r -> r == null ? null : r.getAmount())
                    .distinct()
                    .sorted()
                    .map(a -> {
                        // Keep formatting stable for parsing ("50" instead of "50.0" if possible)
                        if (a == null) return null;
                        double d = a;
                        if (Math.floor(d) == d) return String.valueOf((long) d);
                        return String.valueOf(d);
                    })
                    .filter(s -> s != null && !s.isBlank())
                    .forEach(values::add);
        }

        searchValueChoice.setItems(FXCollections.observableArrayList(values));
        if (searchValueChoice.getValue() == null) {
            searchValueChoice.setValue(ANY_VALUE);
        }
    }

    /**
     * Runs search based on the current UI controls.
     * - For dropdown modes (ID/Amount/Status): build a query like "status:pending".
     * - For text modes (All/Reason): use the searchField text.
     */
    private void runSearchFromUi() {
        if (searchTypeChoice == null) return;
        String type = searchTypeChoice.getValue();

        if (SEARCH_ID.equals(type) || SEARCH_AMOUNT.equals(type) || SEARCH_STATUS.equals(type)) {
            String selected = searchValueChoice == null ? null : searchValueChoice.getValue();
            if (selected == null || ANY_VALUE.equals(selected)) {
                applySearchFilter("");
                return;
            }

            String key = getDefaultSearchField(); // id/amount/status
            if (key == null || key.isBlank()) {
                applySearchFilter("");
                return;
            }
            applySearchFilter(key + ":" + selected);
            return;
        }

        // All / Reason => typing
        applySearchFilter(searchField == null ? "" : searchField.getText());
    }

    /**
     * Professional search filter for the refunds table.
     *
     * Supported query syntax (case-insensitive):
     * - Free text: matches Status/Description (contains)
     *     Example: pending
     * - Field filters with ':'
     *     id:12
     *     amount:50
     *     status:pend
     *     reason:"flight delayed"   (quotes keep spaces)
     * - Numeric operators / ranges:
     *     amount:>50   amount:>=50   amount:<100
     *     amount:10-20 (inclusive range)
     * - Exclude terms using '-':
     *     -rejected    -status:rejected
     * - OR groups using '|':
     *     status:pending | status:approved
     *
     * Dropdown behavior:
     * - If ChoiceBox is not "All", unprefixed terms are treated as that field.
     */
    private void applySearchFilter(String query) {
        if (filteredRefunds == null) return;

        final String raw = query == null ? "" : query.trim();
        if (raw.isEmpty()) {
            filteredRefunds.setPredicate(item -> true);
            updateRefundStats();
            return;
        }

        final String defaultField = getDefaultSearchField(); // null when "All"
        final List<String> orGroups = splitByUnquotedPipe(raw);
        final List<Predicate<RefundRequest>> groupPredicates = new ArrayList<>();

        for (String group : orGroups) {
            String g = group == null ? "" : group.trim();
            if (g.isEmpty()) continue;

            List<String> tokens = tokenizeQuery(g);
            List<Predicate<RefundRequest>> includes = new ArrayList<>();
            List<Predicate<RefundRequest>> excludes = new ArrayList<>();

            for (String tokenRaw : tokens) {
                if (tokenRaw == null) continue;
                String token = tokenRaw.trim();
                if (token.isEmpty()) continue;

                // Exclusion token: starts with '-' but NOT a negative number like -10
                boolean isExclude = token.startsWith("-")
                        && token.length() > 1
                        && !Character.isDigit(token.charAt(1));
                if (isExclude) token = token.substring(1).trim();
                if (token.isEmpty()) continue;

                Predicate<RefundRequest> p = buildTokenPredicate(token, defaultField);
                if (p == null) continue;

                if (isExclude) {
                    excludes.add(p);
                } else {
                    includes.add(p);
                }
            }

            Predicate<RefundRequest> groupPredicate = item -> {
                if (item == null) return false;
                for (Predicate<RefundRequest> inc : includes) {
                    if (!inc.test(item)) return false;
                }
                for (Predicate<RefundRequest> exc : excludes) {
                    if (exc.test(item)) return false;
                }
                return true;
            };
            groupPredicates.add(groupPredicate);
        }

        // If parsing produced no predicates, default to "show all" rather than hide everything.
        if (groupPredicates.isEmpty()) {
            filteredRefunds.setPredicate(item -> true);
            updateRefundStats();
            return;
        }

        filteredRefunds.setPredicate(item -> {
            for (Predicate<RefundRequest> gp : groupPredicates) {
                if (gp.test(item)) return true;
            }
            return false;
        });

        updateRefundStats();
    }

    private void updateRefundStats() {
        if (refundTable == null) return;

        List<RefundRequest> view = new ArrayList<>(refundTable.getItems());
        int total = view.size();

        Map<String, Integer> statusCounts = new LinkedHashMap<>();
        double approvedSum = 0.0;

        for (RefundRequest r : view) {
            if (r == null) continue;
            String status = r.getStatus() == null ? "" : r.getStatus().trim().toUpperCase();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
            if ("APPROVED".equals(status)) {
                approvedSum += r.getAmount();
            }
        }

        int pending = statusCounts.getOrDefault("PENDING", 0);
        int approved = statusCounts.getOrDefault("APPROVED", 0);
        int rejected = statusCounts.getOrDefault("REJECTED", 0);

        if (refundTotalLabel != null) refundTotalLabel.setText(String.valueOf(total));
        if (refundPendingLabel != null) refundPendingLabel.setText(String.valueOf(pending));
        if (refundApprovedLabel != null) refundApprovedLabel.setText(String.valueOf(approved));
        if (refundRejectedLabel != null) refundRejectedLabel.setText(String.valueOf(rejected));
        if (refundApprovedSumLabel != null) refundApprovedSumLabel.setText(MONEY_FMT.format(approvedSum));

        if (statusTextLabel != null) {
            statusTextLabel.setText("Showing " + total + " refund(s)");
        }
    }

    @FXML
    private void handleExportCsv() {
        if (refundTable == null) return;

        List<RefundRequest> rows = new ArrayList<>(refundTable.getItems());
        if (rows.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Export CSV", "No refunds to export (current view is empty).");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Refunds to CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        chooser.setInitialFileName("refunds.csv");
        File file = chooser.showSaveDialog(refundTable.getScene() == null ? null : refundTable.getScene().getWindow());
        if (file == null) return;

        try {
            Path path = file.toPath();
            List<String> headers = List.of(
                    "id",
                    "userId",
                    "reservationId",
                    "amount",
                    "status",
                    "reason",
                    "translatedReason",
                    "createdAt"
            );

            List<List<String>> data = new ArrayList<>();
            for (RefundRequest r : rows) {
                if (r == null) continue;
                data.add(List.of(
                        String.valueOf(r.getId()),
                        String.valueOf(r.getUserId()),
                        String.valueOf(r.getReservationId()),
                        MONEY_FMT.format(r.getAmount()),
                        r.getStatus(),
                        r.getReason(),
                        r.getTranslatedReason(),
                        r.getCreatedAt() == null ? null : r.getCreatedAt().toString()
                ));
            }

            CsvUtils.write(path, headers, data);
            showAlert(Alert.AlertType.INFORMATION, "Export CSV", "Exported " + data.size() + " row(s) to:\n" + path);
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Export CSV", "Export failed: " + ex.getMessage());
        }
    }

    /**
     * Maps the ChoiceBox selection into a default field for unprefixed tokens.
     * Returns null when search is "All".
     */
    private String getDefaultSearchField() {
        if (searchTypeChoice == null || searchTypeChoice.getValue() == null) return null;
        return switch (searchTypeChoice.getValue()) {
            case SEARCH_ID -> "id";
            case SEARCH_AMOUNT -> "amount";
            case SEARCH_STATUS -> "status";
            case SEARCH_REASON -> "reason";
            default -> null; // SEARCH_ALL
        };
    }

    /**
     * Turns one token into a predicate. Supports key:value filters.
     * If no key is provided and defaultField is set, token is treated as defaultField:token.
     */
    private Predicate<RefundRequest> buildTokenPredicate(String token, String defaultField) {
        String t = token.trim();
        if (t.isEmpty()) return null;

        String key = null;
        String value = null;

        int colonIdx = t.indexOf(':');
        if (colonIdx > 0) {
            key = normalize(t.substring(0, colonIdx));
            value = t.substring(colonIdx + 1).trim();
        } else if (defaultField != null) {
            key = defaultField;
            value = t;
        }

        if (value != null) {
            value = unquote(value.trim());
        }

        // No key: free text token across all fields (and numeric tokens match ID/Amount too)
        if (key == null) {
            final String term = normalize(unquote(t));
            if (term.isEmpty()) return null;
            return item -> matchesFreeText(item, term);
        }

        final String v = value == null ? "" : value;
        final String vNorm = normalize(v);

        return switch (key) {
            case "id" -> buildIdPredicate(vNorm);
            case "amount" -> buildAmountPredicate(vNorm);
            case "status" -> item -> normalize(item.getStatus()).contains(vNorm);
            case "reason", "desc", "description" -> item -> {
                String original = normalize(item.getReason());
                String translated = normalize(item.getTranslatedReason());
                return original.contains(vNorm) || translated.contains(vNorm);
            };
            default -> {
                // Unknown field: treat as free text fallback
                final String fallback = normalize(v);
                yield item -> matchesFreeText(item, fallback);
            }
        };
    }

    private boolean matchesFreeText(RefundRequest item, String term) {
        if (item == null) return false;
        if (term == null || term.isEmpty()) return true;

        Integer maybeInt = tryParseInt(term);
        if (maybeInt != null && maybeInt == item.getId()) return true;

        Double maybeDouble = tryParseDouble(term);
        if (maybeDouble != null && nearlyEqual(maybeDouble, item.getAmount())) return true;

        String status = normalize(item.getStatus());
        String reason = normalize(item.getReason());
        String translatedReason = normalize(item.getTranslatedReason());
        return status.contains(term) || reason.contains(term) || translatedReason.contains(term);
    }

    private Predicate<RefundRequest> buildIdPredicate(String expr) {
        if (expr == null || expr.isEmpty()) return item -> true;

        OpNumber on = parseOpNumber(expr);
        if (on == null || on.number == null) {
            Integer idExact = tryParseInt(expr);
            if (idExact == null) return item -> false;
            return item -> item != null && item.getId() == idExact;
        }

        int n = on.number.intValue();
        return switch (on.op) {
            case GT -> item -> item != null && item.getId() > n;
            case GTE -> item -> item != null && item.getId() >= n;
            case LT -> item -> item != null && item.getId() < n;
            case LTE -> item -> item != null && item.getId() <= n;
            case EQ -> item -> item != null && item.getId() == n;
        };
    }

    private Predicate<RefundRequest> buildAmountPredicate(String expr) {
        if (expr == null || expr.isEmpty()) return item -> true;

        RangeNumber range = parseRange(expr);
        if (range != null) {
            return item -> item != null && item.getAmount() >= range.min && item.getAmount() <= range.max;
        }

        OpNumber on = parseOpNumber(expr);
        if (on == null || on.number == null) {
            Double amountExact = tryParseDouble(expr);
            if (amountExact == null) return item -> false;
            return item -> item != null && nearlyEqual(item.getAmount(), amountExact);
        }

        double n = on.number;
        return switch (on.op) {
            case GT -> item -> item != null && item.getAmount() > n;
            case GTE -> item -> item != null && item.getAmount() >= n;
            case LT -> item -> item != null && item.getAmount() < n;
            case LTE -> item -> item != null && item.getAmount() <= n;
            case EQ -> item -> item != null && nearlyEqual(item.getAmount(), n);
        };
    }

    private enum Op { GT, GTE, LT, LTE, EQ }

    private static final class OpNumber {
        final Op op;
        final Double number;

        private OpNumber(Op op, Double number) {
            this.op = op;
            this.number = number;
        }
    }

    private static final class RangeNumber {
        final double min;
        final double max;

        private RangeNumber(double min, double max) {
            this.min = min;
            this.max = max;
        }
    }

    /** Parses expressions like ">=50", "<10", "=12", "50" into (op, number). */
    private static OpNumber parseOpNumber(String expr) {
        if (expr == null) return null;
        String s = expr.trim();
        if (s.isEmpty()) return null;

        Op op = Op.EQ;
        String numPart = s;

        if (s.startsWith(">=")) {
            op = Op.GTE;
            numPart = s.substring(2).trim();
        } else if (s.startsWith("<=")) {
            op = Op.LTE;
            numPart = s.substring(2).trim();
        } else if (s.startsWith(">")) {
            op = Op.GT;
            numPart = s.substring(1).trim();
        } else if (s.startsWith("<")) {
            op = Op.LT;
            numPart = s.substring(1).trim();
        } else if (s.startsWith("==")) {
            op = Op.EQ;
            numPart = s.substring(2).trim();
        } else if (s.startsWith("=")) {
            op = Op.EQ;
            numPart = s.substring(1).trim();
        }

        Double n = tryParseDouble(numPart);
        if (n == null) return null;
        return new OpNumber(op, n);
    }

    /** Parses inclusive ranges like "10-20" or "10..20". Returns null if not a range. */
    private static RangeNumber parseRange(String expr) {
        if (expr == null) return null;
        String s = expr.trim();
        if (s.isEmpty()) return null;

        String[] parts;
        if (s.contains("..")) {
            parts = s.split("\\.\\.", 2);
        } else {
            int dashIdx = s.indexOf('-');
            // Avoid treating negative numbers as a range
            if (dashIdx <= 0) return null;
            parts = new String[] { s.substring(0, dashIdx), s.substring(dashIdx + 1) };
        }

        if (parts.length != 2) return null;
        Double a = tryParseDouble(parts[0].trim());
        Double b = tryParseDouble(parts[1].trim());
        if (a == null || b == null) return null;

        double min = Math.min(a, b);
        double max = Math.max(a, b);
        return new RangeNumber(min, max);
    }

    /** Splits query by '|' not inside quotes. */
    private static List<String> splitByUnquotedPipe(String raw) {
        List<String> parts = new ArrayList<>();
        if (raw == null || raw.isEmpty()) {
            parts.add("");
            return parts;
        }

        StringBuilder current = new StringBuilder();
        char quote = 0;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if ((c == '"' || c == '\'') ) {
                if (quote == 0) quote = c;
                else if (quote == c) quote = 0;
                current.append(c);
                continue;
            }
            if (c == '|' && quote == 0) {
                parts.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        parts.add(current.toString());
        return parts;
    }

    /** Tokenizes by spaces, but keeps quoted phrases together (supports "..." and '...'). */
    private static List<String> tokenizeQuery(String raw) {
        List<String> tokens = new ArrayList<>();
        if (raw == null) return tokens;

        StringBuilder current = new StringBuilder();
        char quote = 0;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);

            if ((c == '"' || c == '\'') ) {
                if (quote == 0) {
                    quote = c;
                } else if (quote == c) {
                    quote = 0;
                }
                current.append(c);
                continue;
            }

            if (Character.isWhitespace(c) && quote == 0) {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
                continue;
            }

            current.append(c);
        }

        if (!current.isEmpty()) tokens.add(current.toString());
        return tokens;
    }

    private static String unquote(String s) {
        if (s == null) return "";
        String t = s.trim();
        if (t.length() >= 2) {
            char first = t.charAt(0);
            char last = t.charAt(t.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return t.substring(1, t.length() - 1);
            }
        }
        return t;
    }

    // --- Small helpers to keep filtering logic readable ---

    private static String normalize(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private static Integer tryParseInt(String s) {
        if (s == null) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Double tryParseDouble(String s) {
        if (s == null) return null;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static boolean nearlyEqual(double a, double b) {
        return Math.abs(a - b) < 1e-9;
    }

    @FXML
    private void handleApproveSelected() {
        RefundRequest selected = refundTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (!confirmAction("Approve refund", "Approve refund #" + selected.getId() + "?")) return;

        refundService.approveRefund(selected.getId()).thenAccept(response -> {
            if (response.statusCode() == 200 || response.statusCode() == 204) {
                Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Refund", "Approved ✅"));
                loadMyRefunds();
            } else {
                Platform.runLater(() -> showAlert(
                        Alert.AlertType.ERROR,
                        "Refund",
                        "Approve failed (" + response.statusCode() + ")\n" + response.body()
                ));
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Refund", "Error: " + ex.getMessage()));
            return null;
        });
    }

    @FXML
    private void handleRejectSelected() {
        RefundRequest selected = refundTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (!confirmAction("Reject refund", "Reject refund #" + selected.getId() + "?")) return;

        refundService.rejectRefund(selected.getId()).thenAccept(response -> {
            if (response.statusCode() == 200 || response.statusCode() == 204) {
                Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Refund", "Rejected ✅"));
                loadMyRefunds();
            } else {
                Platform.runLater(() -> showAlert(
                        Alert.AlertType.ERROR,
                        "Refund",
                        "Reject failed (" + response.statusCode() + ")\n" + response.body()
                ));
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Refund", "Error: " + ex.getMessage()));
            return null;
        });
    }

    @FXML
    private void handleDeleteSelected() {
        RefundRequest selected = refundTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String status = selected.getStatus();
        if (status != null && !status.isBlank() && !"PENDING".equalsIgnoreCase(status.trim())) {
            showAlert(Alert.AlertType.WARNING, "Refund", "Only pending refunds are typically deletable.\nCurrent status: " + status);
            return;
        }

        if (!confirmAction("Delete refund", "Delete refund #" + selected.getId() + "?")) return;

        refundService.deleteRefund(selected.getId()).thenAccept(response -> {
            if (response.statusCode() == 200 || response.statusCode() == 204) {
                Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Refund", "Deleted ✅"));
                loadMyRefunds();
            } else {
                Platform.runLater(() -> showAlert(
                        Alert.AlertType.ERROR,
                        "Refund",
                        "Delete failed (" + response.statusCode() + ")\n" + response.body()
                ));
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Refund", "Error: " + ex.getMessage()));
            return null;
        });
    }

    private boolean confirmAction(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void loadMyRefunds() {
        refundService.getMyRefunds().thenAccept(response -> {
            try {
                if (response.statusCode() == 200) {
                    List<RefundRequest> list = mapper.readValue(
                            response.body(),
                            new TypeReference<List<RefundRequest>>() {}
                    );

                    Platform.runLater(() -> {
                        refundItems.setAll(list);
                        populateSearchValueChoices();
                        runSearchFromUi();
                        applySortFromUi();
                        updateRefundStats();
                    });
                } else {
                    Platform.runLater(() ->
                            showAlert(Alert.AlertType.ERROR, "Refunds",
                                    "Load failed (" + response.statusCode() + ")\n" + response.body())
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Refunds", "Parsing error: " + e.getMessage())
                );
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(() ->
                    showAlert(Alert.AlertType.ERROR, "Refunds", "Request error: " + ex.getMessage())
            );
            return null;
        });
    }

    // Optional for ADMIN:
    @SuppressWarnings("unused")
    private void loadAllRefunds() {
        refundService.getAllRefunds().thenAccept(response -> {
            try {
                if (response.statusCode() == 200) {
                    List<RefundRequest> list = mapper.readValue(
                            response.body(),
                            new TypeReference<List<RefundRequest>>() {}
                    );
                    Platform.runLater(() -> {
                        refundItems.setAll(list);
                        populateSearchValueChoices();
                        runSearchFromUi();
                        applySortFromUi();
                        updateRefundStats();
                    });
                } else {
                    Platform.runLater(() ->
                            showAlert(Alert.AlertType.ERROR, "Refunds",
                                    "Load failed (" + response.statusCode() + ")\n" + response.body())
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Refunds", "Parsing error: " + e.getMessage())
                );
            }
        });
    }

    @FXML
    private void handleAdd() {
        String amountText = amountField.getText();
        String reason = reasonArea.getText();

        if (amountText == null || amountText.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Refund Request", "Amount is required.");
            return;
        }
        if (reason == null || reason.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Refund Request", "Description (reason) is required.");
            return;
        }

        String reasonTrimmed = reason.trim();
        if (reasonTrimmed.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Refund Request", "Description must be at least 3 characters.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText.trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Refund Request", "Amount must be a number.");
            return;
        }

        if (amount <= 0) {
            showAlert(Alert.AlertType.WARNING, "Refund Request", "Amount must be greater than 0.");
            return;
        }

        // Because your FXML doesn't have reclamationId field,
        // we ask it with a small popup:
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reclamation ID");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter Reclamation ID:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) return;

        int reclamationId;
        try {
            reclamationId = Integer.parseInt(result.get().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Refund Request", "Reclamation ID must be a number.");
            return;
        }

        if (reclamationId <= 0) {
            showAlert(Alert.AlertType.WARNING, "Refund Request", "Reclamation ID must be greater than 0.");
            return;
        }

        String jsonBody =
                "{\"reclamationId\":" + reclamationId +
                ",\"amount\":" + amount +
            ",\"reason\":\"" + escapeJson(reasonTrimmed) + "\"}";

        refundService.addRefund(jsonBody).thenAccept(response -> {
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Refund Request", "Created ✅");
                    amountField.clear();
                    reasonArea.clear();
                });
                loadMyRefunds();
            } else {
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Refund Request",
                                "Create failed (" + response.statusCode() + ")\n" + response.body())
                );
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(() ->
                    showAlert(Alert.AlertType.ERROR, "Refund Request", "Error: " + ex.getMessage())
            );
            return null;
        });
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
