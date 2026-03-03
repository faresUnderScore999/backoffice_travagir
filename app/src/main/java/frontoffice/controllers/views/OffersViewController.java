package frontoffice.controllers.views;

import frontoffice.models.Offer;
import frontoffice.models.PromoCode;
import frontoffice.services.EmailService;
import frontoffice.services.OfferService;
import frontoffice.services.PromoCodeService;
import frontoffice.services.UserOfferService;
import frontoffice.utils.HttpErrorParser;
import frontoffice.utils.SessionManager;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class OffersViewController {

    @FXML
    private TextField searchField;
    @FXML
    private FlowPane offersCardsContainer;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Label statusLabel;

    private final OfferService offerService = new OfferService();
    private final UserOfferService userOfferService = new UserOfferService();
    private final PromoCodeService promoCodeService = new PromoCodeService();
    private final EmailService emailService = new EmailService();

    private ObservableList<Offer> allOffers = FXCollections.observableArrayList();
    private Offer selectedOffer = null;

    // ──────────────────────────────────────────────────────────
    // Initialization
    // ──────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        System.out.println("🔍 DEBUG: OffersViewController initialize() called!");
        setupRealTimeSearch();
        onRefresh();
        System.out.println("🔍 DEBUG: OffersViewController initialization completed!");
    }

    private void setupRealTimeSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterOffers(newVal));
    }

    private void filterOffers(String searchText) {
        Platform.runLater(() -> {
            List<Offer> filtered;
            if (searchText == null || searchText.trim().isEmpty()) {
                filtered = new ArrayList<>(allOffers);
            } else {
                String q = searchText.toLowerCase().trim();
                filtered = allOffers.stream()
                        .filter(o -> (o.getTitle() != null && o.getTitle().toLowerCase().contains(q))
                                || (o.getDestinationName() != null && o.getDestinationName().toLowerCase().contains(q))
                                || (o.getDescription() != null && o.getDescription().toLowerCase().contains(q)))
                        .collect(Collectors.toList());
            }
            displayOffersAsCards(filtered);
            if (filtered.isEmpty())
                showNoResultsMessage();
            else
                clearNoResultsMessage();
        });
    }

    @FXML
    private void onRefresh() {
        statusLabel.setText("Chargement des offres…");
        offerService.getAllOffers()
                .thenAccept(list -> {
                    allOffers.clear();
                    allOffers.addAll(list);
                    Platform.runLater(() -> {
                        displayOffersAsCards(list);
                        statusLabel.setText(list.size() + " offre(s) chargée(s)");
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Erreur : " + ex.getMessage()));
                    return null;
                });
    }

    // ──────────────────────────────────────────────────────────
    // Card display
    // ──────────────────────────────────────────────────────────

    private void displayOffersAsCards(List<Offer> offers) {
        offersCardsContainer.getChildren().clear();
        for (Offer offer : offers) {
            offersCardsContainer.getChildren().add(createOfferCard(offer));
        }
    }

    private VBox createOfferCard(Offer offer) {
        // ── Main card ──
        VBox card = new VBox();
        card.getStyleClass().add("offer-card");
        card.setPrefWidth(350);
        card.setPrefHeight(420);

        // ── Header with destination ──
        HBox header = new HBox();
        header.getStyleClass().add("card-header");
        header.setPrefHeight(80);
        header.setAlignment(Pos.CENTER);
        Label destinationLabel = new Label(
                offer.getDestinationName() != null ? offer.getDestinationName() : "Destination inconnue");
        destinationLabel.getStyleClass().add("destination-label");
        header.getChildren().add(destinationLabel);

        // ── Content ──
        VBox content = new VBox(12);
        content.getStyleClass().add("card-content");
        content.setPadding(new Insets(20));

        Label titleLabel = new Label(offer.getTitle());
        titleLabel.getStyleClass().add("offer-title");
        titleLabel.setWrapText(true);

        Label descriptionLabel = new Label(offer.getDescription());
        descriptionLabel.getStyleClass().add("offer-description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setPrefHeight(60);

        HBox discountContainer = new HBox();
        discountContainer.getStyleClass().add("discount-badge");
        Label discountLabel = new Label(String.format("%.0f%% OFF", offer.getDiscountPercentage()));
        discountLabel.getStyleClass().add("discount-text");
        discountContainer.getChildren().add(discountLabel);

        HBox dateContainer = new HBox();
        dateContainer.getStyleClass().add("date-info");
        Label dateLabel = new Label(String.format("📅 %s → %s", offer.getStartDate(), offer.getEndDate()));
        dateLabel.getStyleClass().add("date-text");
        dateContainer.getChildren().add(dateLabel);

        HBox statusContainer = new HBox();
        if (offer.isActive()) {
            statusContainer.getStyleClass().add("active-status");
            Label lbl = new Label("✨ ACTIVE");
            lbl.getStyleClass().add("active-status-text");
            statusContainer.getChildren().add(lbl);
        } else {
            statusContainer.getStyleClass().add("inactive-status");
            Label lbl = new Label("❌ INACTIVE");
            lbl.getStyleClass().add("inactive-status-text");
            statusContainer.getChildren().add(lbl);
        }

        // ── Usage row (fetched async from promo codes) ──
        HBox usageContainer = new HBox(8);
        usageContainer.setAlignment(Pos.CENTER_LEFT);
        usageContainer.setStyle(
                "-fx-background-color: #f3f0fd; -fx-background-radius: 10; -fx-padding: 8 12;");
        Label usageLabel = new Label("🔄 Chargement des utilisations…");
        usageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        usageContainer.getChildren().add(usageLabel);

        // Fetch promo codes asynchronously to populate usage info
        promoCodeService.getPromoCodesByOfferId(offer.getId())
                .thenAccept(codes -> Platform.runLater(() -> {
                    if (codes == null || codes.isEmpty()) {
                        usageLabel.setText("🚧 Aucun code promo");
                        usageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #bbb;");
                    } else {
                        // Use the first active promo code's stats
                        PromoCode pc = codes.stream()
                                .filter(PromoCode::isActive)
                                .findFirst()
                                .orElse(codes.get(0));
                        updateUsageLabel(usageLabel, pc.getUsedCount(), pc.getUsageLimit());
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> usageLabel.setText("❌ Erreur de chargement"));
                    return null;
                });

        // ── "Obtenir le code promo" button ──
        Button promoBtn = new Button("🎁 Obtenir le code promo");
        promoBtn.getStyleClass().add("promo-btn");
        promoBtn.setStyle(
                "-fx-background-color: linear-gradient(135deg, #667eea, #764ba2);" +
                        "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 14; -fx-border-radius: 14; -fx-cursor: hand;" +
                        "-fx-padding: 10 16; -fx-effect: dropshadow(gaussian, rgba(118,75,162,0.35), 8, 0, 0, 4);");
        promoBtn.setMaxWidth(Double.MAX_VALUE);
        promoBtn.setOnAction(e -> handleGetPromoCode(offer, discountLabel, promoBtn, usageLabel));

        content.getChildren().addAll(titleLabel, descriptionLabel, discountContainer, dateContainer,
                statusContainer, usageContainer, promoBtn);

        card.setOnMouseClicked(event -> selectOffer(offer, card));
        card.getChildren().addAll(header, content);
        return card;
    }

    /** Renders the usage badge text and colour based on used/limit values. */
    private void updateUsageLabel(Label usageLabel, int usedCount, int usageLimit) {
        String icon;
        String colour;
        String text;
        if (usageLimit <= 0) {
            // Unlimited
            text = "♾️ Utilisations illimitées  (" + usedCount + " utilisé(s))";
            icon = "";
            colour = "#5c6bc0";
        } else {
            int remaining = usageLimit - usedCount;
            double pct = (double) usedCount / usageLimit;
            if (pct >= 1.0) {
                icon = "⛔";
                colour = "#e53935";
                text = icon + " Limit atteinte  " + usedCount + "/" + usageLimit;
            } else if (pct >= 0.7) {
                icon = "⚠️";
                colour = "#f57c00";
                text = icon + " " + usedCount + "/" + usageLimit + "  (" + remaining + " restant(s))";
            } else {
                icon = "✅";
                colour = "#2e7d32";
                text = icon + " " + usedCount + "/" + usageLimit + "  (" + remaining + " restant(s))";
            }
        }
        usageLabel.setText(text);
        usageLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + colour + ";");
    }

    // ──────────────────────────────────────────────────────────
    // Promo code flow
    // ──────────────────────────────────────────────────────────

    /**
     * Step 1 – Fetch promo code(s) for the offer, send one by email, then open the
     * popup.
     */
    private void handleGetPromoCode(Offer offer, Label discountLabel, Button promoBtn, Label usageLabel) {
        statusLabel.setText("Récupération du code promo…");

        promoCodeService.getPromoCodesByOfferId(offer.getId())
                .thenAccept(codes -> Platform.runLater(() -> {
                    if (codes == null || codes.isEmpty()) {
                        statusLabel.setText("Aucun code promo disponible pour cette offre.");
                        showErrorAlert("Aucun code promo", "Aucun code promo n'est disponible pour cette offre.");
                        return;
                    }

                    // Pick first available (active, within valid dates, usage not exhausted)
                    PromoCode chosen = codes.stream()
                            .filter(pc -> pc.isActive()
                                    && (pc.getUsageLimit() <= 0 || pc.getUsedCount() < pc.getUsageLimit()))
                            .findFirst()
                            .orElse(codes.get(0));

                    // Send email asynchronously
                    String userEmail = getUserEmail();
                    if (userEmail == null) {
                        statusLabel.setText("Impossible d'envoyer l'e-mail : utilisateur non connecté.");
                        return;
                    }

                    statusLabel.setText("Envoi du code promo par e-mail…");
                    final String codeValue = chosen.getCode();
                    final int chosenUsed = chosen.getUsedCount();
                    final int chosenLimit = chosen.getUsageLimit();
                    new Thread(() -> {
                        try {
                            emailService.sendPromoCodeEmail(userEmail, codeValue, offer.getTitle());
                            Platform.runLater(() -> {
                                statusLabel.setText("Code promo envoyé à " + userEmail);
                                showPromoCodeDialog(offer, codeValue, discountLabel, promoBtn, usageLabel, chosenUsed,
                                        chosenLimit);
                            });
                        } catch (Exception ex) {
                            System.err.println("Email send failed: " + ex.getMessage());
                            Platform.runLater(() -> {
                                statusLabel.setText("Erreur d'envoi de l'e-mail.");
                                showPromoCodeDialog(offer, codeValue, discountLabel, promoBtn, usageLabel, chosenUsed,
                                        chosenLimit);
                            });
                        }
                    }, "email-sender").start();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Erreur : " + ex.getMessage()));
                    return null;
                });
    }

    /**
     * Step 2 – Show the promo code entry popup.
     * On success, updates the card's discount label, disables the promo button and
     * refreshes the usage row.
     */
    private void showPromoCodeDialog(Offer offer, String sentCode,
            Label discountLabel, Button promoBtn,
            Label usageLabel, int currentUsed, int usageLimit) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle("Code Promo");

        // ── Layout ──
        VBox root = new VBox(18);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(32, 36, 32, 36));
        root.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 24, 0, 0, 6);");
        root.setPrefWidth(440);

        // Icon & title
        Label iconLbl = new Label("🎟️");
        iconLbl.setStyle("-fx-font-size: 48px;");

        Label titleLbl = new Label("Entrez votre code promo");
        titleLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2d2d2d;");

        // Info
        Label infoLbl = new Label(
                "Un code promo a été envoyé à votre adresse e-mail.\nSaisissez-le ci-dessous pour l'activer.");
        infoLbl.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");
        infoLbl.setWrapText(true);
        infoLbl.setAlignment(Pos.CENTER);
        infoLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Code input
        TextField codeField = new TextField();
        codeField.setPromptText("Ex: PROMO2025");
        codeField.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: bold; -fx-letter-spacing: 3;" +
                        "-fx-text-fill: #764ba2; -fx-border-color: #ddd; -fx-border-radius: 10;" +
                        "-fx-background-radius: 10; -fx-padding: 12 16; -fx-pref-width: 280;");
        codeField.setMaxWidth(280);

        // Error label (hidden by default)
        Label errorLbl = new Label();
        errorLbl.setStyle(
                "-fx-text-fill: #e53935; -fx-font-size: 13px; -fx-font-weight: bold;" +
                        "-fx-background-color: #fce4ec; -fx-background-radius: 8; -fx-padding: 8 12;");
        errorLbl.setWrapText(true);
        errorLbl.setMaxWidth(360);
        errorLbl.setVisible(false);
        errorLbl.setManaged(false);

        // Buttons row
        Button validateBtn = new Button("✅ Valider");
        validateBtn.setStyle(
                "-fx-background-color: linear-gradient(135deg, #667eea, #764ba2);" +
                        "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 10; -fx-padding: 11 28; -fx-cursor: hand;");

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle(
                "-fx-background-color: #f0f0f0; -fx-text-fill: #555; -fx-font-size: 13px;" +
                        "-fx-background-radius: 10; -fx-padding: 11 24; -fx-cursor: hand;");

        HBox btnRow = new HBox(14, validateBtn, cancelBtn);
        btnRow.setAlignment(Pos.CENTER);

        root.getChildren().addAll(iconLbl, titleLbl, infoLbl, codeField, errorLbl, btnRow);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);

        // ── Pop-in animation ──
        root.setScaleX(0.8);
        root.setScaleY(0.8);
        root.setOpacity(0);
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(280), root);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(280), root);
        fadeIn.setToValue(1.0);
        new ParallelTransition(scaleIn, fadeIn).play();

        // ── Cancel ──
        cancelBtn.setOnAction(e -> dialog.close());

        // ── Validate ──
        validateBtn.setOnAction(e -> {
            String inputCode = codeField.getText().trim();
            if (inputCode.isEmpty()) {
                showInlineError(errorLbl, "Veuillez saisir un code promo.");
                return;
            }
            validateBtn.setDisable(true);
            validateBtn.setText("Vérification…");

            promoCodeService.validateCode(inputCode)
                    .thenAccept(response -> Platform.runLater(() -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            // ── Valid: mark as used ──
                            promoCodeService.useCode(inputCode);
                            dialog.close();
                            statusLabel.setText("✅ Code promo appliqué avec succès !");

                            // ── Update card discount badge ──
                            double newDiscount = offer.getDiscountPercentage();
                            discountLabel.setText(String.format("%.0f%% OFF  🎁 CODE APPLIQUÉ", newDiscount));
                            discountLabel.setStyle(
                                    "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");

                            // ── Disable the promo button with a confirmation style ──
                            promoBtn.setText("✅ Code promo appliqué !");
                            promoBtn.setDisable(true);
                            promoBtn.setStyle(
                                    "-fx-background-color: linear-gradient(135deg, #43a047, #2e7d32);" +
                                            "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;" +
                                            "-fx-background-radius: 14; -fx-border-radius: 14; -fx-cursor: default;" +
                                            "-fx-padding: 10 16; -fx-opacity: 0.85;");

                            // ── Update usage counter on the card (local +1) ──
                            updateUsageLabel(usageLabel, currentUsed + 1, usageLimit);

                            playCongratsAnimation();
                            showSuccessNotification("Code promo activé !",
                                    "Félicitations ! Votre réduction a bien été appliquée. 🎉");
                        } else {
                            String body = response.body() != null ? response.body().toLowerCase() : "";
                            String msg;
                            if (body.contains("usage") || body.contains("limit")) {
                                msg = "⚠️ Limite d'utilisation atteinte. Ce code a déjà été utilisé le nombre maximum de fois.";
                            } else if (body.contains("expir")) {
                                msg = "⏰ Ce code promo est expiré.";
                            } else {
                                msg = "❌ Code invalide ou expiré. Veuillez vérifier et réessayer.";
                            }
                            showInlineError(errorLbl, msg);
                            validateBtn.setDisable(false);
                            validateBtn.setText("✅ Valider");
                        }
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            showInlineError(errorLbl, "Erreur de connexion. Réessayez.");
                            validateBtn.setDisable(false);
                            validateBtn.setText("✅ Valider");
                        });
                        return null;
                    });
        });

        dialog.show();

        // Centre the dialog over the main window
        Stage owner = getOwnerStage();
        if (owner != null) {
            dialog.setX(owner.getX() + (owner.getWidth() - root.getPrefWidth()) / 2.0);
            dialog.setY(owner.getY() + (owner.getHeight() - 400) / 2.0);
        }
    }

    // ──────────────────────────────────────────────────────────
    // Confetti / congrats animation
    // ──────────────────────────────────────────────────────────

    private void playCongratsAnimation() {
        if (offersCardsContainer.getScene() == null)
            return;

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.0);");
        overlay.setMouseTransparent(true);
        overlay.setPrefSize(
                offersCardsContainer.getScene().getWidth(),
                offersCardsContainer.getScene().getHeight());

        // Congratulations label
        Label congratsLbl = new Label("🎉 Félicitations ! 🎉");
        congratsLbl.setStyle(
                "-fx-font-size: 36px; -fx-font-weight: bold;" +
                        "-fx-text-fill: white;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 12, 0, 0, 4);");
        congratsLbl.setTranslateY(-60);

        StackPane.setAlignment(congratsLbl, Pos.CENTER);
        overlay.getChildren().add(congratsLbl);

        // Pane behind label for confetti visibility
        Pane confettiPane = new Pane();
        confettiPane.setMouseTransparent(true);
        confettiPane.setPrefSize(overlay.getPrefWidth(), overlay.getPrefHeight());
        overlay.getChildren().add(0, confettiPane);

        // Add overlay to scene root
        Pane sceneRoot = (Pane) offersCardsContainer.getScene().getRoot();
        sceneRoot.getChildren().add(overlay);

        // Generate confetti particles
        Random rnd = new Random();
        Color[] palette = {
                Color.web("#667eea"), Color.web("#764ba2"), Color.web("#f6c90e"),
                Color.web("#ff6b6b"), Color.web("#48cfad"), Color.web("#ff9ff3"),
                Color.web("#54a0ff"), Color.web("#ffeaa7")
        };

        double sceneW = offersCardsContainer.getScene().getWidth();

        for (int i = 0; i < 90; i++) {
            Rectangle piece = new Rectangle(
                    8 + rnd.nextInt(10),
                    8 + rnd.nextInt(10));
            piece.setFill(palette[rnd.nextInt(palette.length)]);
            piece.setArcWidth(rnd.nextBoolean() ? piece.getWidth() : 0);
            piece.setArcHeight(rnd.nextBoolean() ? piece.getHeight() : 0);
            piece.setX(rnd.nextDouble() * sceneW);
            piece.setY(-20);
            piece.setRotate(rnd.nextDouble() * 360);
            piece.setOpacity(0.9 + rnd.nextDouble() * 0.1);
            confettiPane.getChildren().add(piece);

            double duration = 1200 + rnd.nextInt(1400);
            double fallDistance = overlay.getPrefHeight() + 60;
            double horizontalDrift = (rnd.nextDouble() - 0.5) * 180;

            TranslateTransition fall = new TranslateTransition(Duration.millis(duration), piece);
            fall.setByY(fallDistance);
            fall.setByX(horizontalDrift);
            fall.setCycleCount(1);

            FadeTransition fade = new FadeTransition(Duration.millis(duration * 0.6), piece);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setDelay(Duration.millis(duration * 0.4));

            fall.setDelay(Duration.millis(rnd.nextInt(600)));
            new ParallelTransition(fall, fade).play();
        }

        // Fade-in the congrats label
        FadeTransition labelFade = new FadeTransition(Duration.millis(400), congratsLbl);
        labelFade.setFromValue(0);
        labelFade.setToValue(1);

        ScaleTransition labelScale = new ScaleTransition(Duration.millis(600), congratsLbl);
        labelScale.setFromX(0.5);
        labelScale.setFromY(0.5);
        labelScale.setToX(1.0);
        labelScale.setToY(1.0);
        new ParallelTransition(labelFade, labelScale).play();

        // Remove overlay after 3 s
        javafx.animation.PauseTransition cleanup = new javafx.animation.PauseTransition(Duration.seconds(3));
        cleanup.setOnFinished(e -> {
            FadeTransition overlayFade = new FadeTransition(Duration.millis(500), overlay);
            overlayFade.setToValue(0);
            overlayFade.setOnFinished(ev -> sceneRoot.getChildren().remove(overlay));
            overlayFade.play();
        });
        cleanup.play();
    }

    // ──────────────────────────────────────────────────────────
    // ControlsFX notification
    // ──────────────────────────────────────────────────────────

    private void showSuccessNotification(String title, String text) {
        // ControlsFX Notifications is NOT available in FrontOffice — use a styled
        // in-app toast instead
        Platform.runLater(() -> {
            if (offersCardsContainer.getScene() == null)
                return;
            Pane sceneRoot = (Pane) offersCardsContainer.getScene().getRoot();

            Label toast = new Label("✅  " + title + "  —  " + text);
            toast.setStyle(
                    "-fx-background-color: #43a047;" +
                            "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;" +
                            "-fx-padding: 14 24; -fx-background-radius: 14;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 12, 0, 0, 4);");
            toast.setWrapText(true);
            toast.setMaxWidth(480);

            StackPane toastWrapper = new StackPane(toast);
            toastWrapper.setMouseTransparent(true);
            StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
            toastWrapper.setTranslateY(-30);
            toastWrapper.setPrefSize(sceneRoot.getWidth(), sceneRoot.getHeight());

            sceneRoot.getChildren().add(toastWrapper);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(350), toastWrapper);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            javafx.animation.PauseTransition hold = new javafx.animation.PauseTransition(Duration.seconds(3));
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), toastWrapper);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> sceneRoot.getChildren().remove(toastWrapper));

            javafx.animation.SequentialTransition seq = new javafx.animation.SequentialTransition(fadeIn, hold,
                    fadeOut);
            seq.play();
        });
    }

    // ──────────────────────────────────────────────────────────
    // Offer selection (existing)
    // ──────────────────────────────────────────────────────────

    private void selectOffer(Offer offer, VBox card) {
        if (selectedOffer != null) {
            offersCardsContainer.getChildren().forEach(node -> node.getStyleClass().remove("selected"));
        }
        card.getStyleClass().add("selected");
        selectedOffer = offer;
        statusLabel.setText("Sélectionné : " + offer.getTitle());
    }

    @FXML
    private void onClaimSelected() {
        if (selectedOffer == null) {
            statusLabel.setText("Veuillez sélectionner une offre.");
            return;
        }
        statusLabel.setText("Réclamation en cours…");
        userOfferService.claimOffer(selectedOffer.getId())
                .thenAccept(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        Platform.runLater(() -> statusLabel.setText("✅ Offre réclamée : " + selectedOffer.getTitle()));
                    } else {
                        Platform.runLater(() -> statusLabel.setText("❌ Échec de la réclamation."));
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("❌ Erreur : " + ex.getMessage()));
                    return null;
                });
    }

    // ──────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────

    private String getUserEmail() {
        try {
            return SessionManager.getInstance().getCurrentUser().email();
        } catch (Exception e) {
            return null;
        }
    }

    private Stage getOwnerStage() {
        try {
            return (Stage) offersCardsContainer.getScene().getWindow();
        } catch (Exception e) {
            return null;
        }
    }

    private void showInlineError(Label errorLbl, String message) {
        errorLbl.setText(message);
        errorLbl.setVisible(true);
        errorLbl.setManaged(true);

        FadeTransition shake = new FadeTransition(Duration.millis(100), errorLbl);
        shake.setFromValue(0);
        shake.setToValue(1);
        shake.play();
    }

    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Code Promo");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showNoResultsMessage() {
        VBox box = new VBox();
        box.getStyleClass().add("no-results-container");
        Label lbl = new Label("🔍 Aucune offre ne correspond à votre recherche.");
        lbl.getStyleClass().add("no-results-label");
        box.getChildren().add(lbl);
        offersCardsContainer.getChildren().add(box);
    }

    private void clearNoResultsMessage() {
        offersCardsContainer.getChildren()
                .removeIf(n -> n.getStyleClass().contains("no-results-container"));
    }
}
