package java_project.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import java_project.models.Offer;

public class GoogleCalendarService {
    
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    
    // Google Calendar API endpoint pour créer des événements
    private static final String GOOGLE_CALENDAR_URL = "https://calendar.google.com/calendar/render";
    
    public GoogleCalendarService() {
        // Constructeur par défaut
    }
    
    /**
     * Ajoute une offre au Google Calendar de l'administrateur
     * @param offer L'offre à ajouter
     * @return URL du Google Calendar avec l'événement pré-rempli
     */
    public String addOfferToGoogleCalendar(Offer offer) {
        try {
            // Construire les paramètres pour l'événement Google Calendar
            Map<String, String> params = new HashMap<>();
            
            // Titre de l'événement
            String title = "🔥 OFFRE SPÉCIALE: " + offer.getTitle();
            params.put("text", URLEncoder.encode(title, StandardCharsets.UTF_8));
            
            // Description détaillée
            String description = createOfferDescription(offer);
            params.put("details", URLEncoder.encode(description, StandardCharsets.UTF_8));
            
            // Dates de début et de fin
            if (offer.getStartDate() != null && offer.getEndDate() != null) {
                String startDate = offer.getStartDate().atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME);
                String endDate = offer.getEndDate().plusDays(1).atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME); // +1 jour pour inclure le dernier jour
                
                params.put("dates", startDate + "/" + endDate);
            }
            
            // Rendre l'événement public
            params.put("action", "TEMPLATE");
            params.put("pli", "1"); // Ouvrir dans un nouvel onglet
            
            // Construire l'URL complète
            String calendarUrl = buildCalendarUrl(params);
            
            System.out.println("📅 Google Calendar URL générée: " + calendarUrl);
            return calendarUrl;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création de l'événement Google Calendar: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Crée une description détaillée pour l'événement
     */
    private String createOfferDescription(Offer offer) {
        StringBuilder description = new StringBuilder();
        
        description.append("🎯 DÉTAILS DE L'OFFRE\n\n");
        
        // Titre
        description.append("📋 Titre: ").append(offer.getTitle()).append("\n");
        
        // Description
        if (offer.getDescription() != null && !offer.getDescription().trim().isEmpty()) {
            description.append("📝 Description: ").append(offer.getDescription()).append("\n");
        }
        
        // Réduction
        description.append("💰 Réduction: ").append(offer.getDiscountPercentage()).append("%\n");
        
        // Dates
        if (offer.getStartDate() != null) {
            description.append("📅 Date de début: ").append(offer.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        }
        if (offer.getEndDate() != null) {
            description.append("📅 Date de fin: ").append(offer.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        }
        
        // Statut
        description.append("✅ Statut: ").append(offer.isActive() ? "Active" : "Inactive").append("\n");
        
        // Informations système
        description.append("\n---\n");
        description.append("🖥️ Ajouté via le système Travagir\n");
        description.append("📅 Date d'ajout: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        description.append("🔔 N'oubliez pas de promouvoir cette offre!");
        
        return description.toString();
    }
    
    /**
     * Construit l'URL complète avec les paramètres
     */
    private String buildCalendarUrl(Map<String, String> params) {
        StringBuilder url = new StringBuilder(GOOGLE_CALENDAR_URL);
        url.append("?");
        
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                url.append("&");
            }
            url.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        
        return url.toString();
    }
    
    /**
     * Ouvre le navigateur avec l'URL Google Calendar
     */
    public boolean openCalendarInBrowser(String calendarUrl) {
        try {
            if (calendarUrl == null || calendarUrl.trim().isEmpty()) {
                System.err.println("❌ URL Google Calendar invalide");
                return false;
            }
            
            // Utiliser java.awt.Desktop pour ouvrir le navigateur
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(calendarUrl));
                    System.out.println("🌐 Navigateur ouvert avec Google Calendar");
                    return true;
                }
            }
            
            // Alternative: utiliser ProcessBuilder pour différentes plateformes
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            
            if (os.contains("win")) {
                pb = new ProcessBuilder("cmd", "/c", "start", calendarUrl);
            } else if (os.contains("mac")) {
                pb = new ProcessBuilder("open", calendarUrl);
            } else {
                pb = new ProcessBuilder("xdg-open", calendarUrl);
            }
            
            pb.start();
            System.out.println("🌐 Navigateur ouvert avec Google Calendar (alternative)");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'ouverture du navigateur: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Méthode complète pour ajouter une offre et ouvrir le calendar
     */
    public boolean addOfferAndOpenCalendar(Offer offer) {
        System.out.println("📅 Ajout de l'offre au Google Calendar...");
        
        String calendarUrl = addOfferToGoogleCalendar(offer);
        if (calendarUrl != null) {
            return openCalendarInBrowser(calendarUrl);
        }
        
        return false;
    }
    
    /**
     * Crée un rappel pour une offre (optionnel)
     */
    public String createOfferReminder(Offer offer, int minutesBefore) {
        try {
            String reminderText = "🔔 Rappel: L'offre \"" + offer.getTitle() + "\" commence bientôt!";
            
            Map<String, String> params = new HashMap<>();
            params.put("text", URLEncoder.encode(reminderText, StandardCharsets.UTF_8));
            params.put("details", URLEncoder.encode(
                "💰 Réduction: " + offer.getDiscountPercentage() + "%\n" +
                "📅 Début: " + offer.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n" +
                "📅 Fin: " + offer.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 
                StandardCharsets.UTF_8));
            
            // Créer un rappel 15 minutes avant le début
            if (offer.getStartDate() != null) {
                String reminderTime = offer.getStartDate().atStartOfDay()
                    .minusMinutes(minutesBefore)
                    .format(DateTimeFormatter.ISO_DATE_TIME);
                params.put("dates", reminderTime + "/" + reminderTime);
            }
            
            params.put("action", "TEMPLATE");
            params.put("pli", "1");
            
            return buildCalendarUrl(params);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création du rappel: " + e.getMessage());
            return null;
        }
    }
}
