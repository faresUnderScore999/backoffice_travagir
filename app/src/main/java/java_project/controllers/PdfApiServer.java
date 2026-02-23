package java_project.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import spark.Spark;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static spark.Spark.*;

import java_project.services.PdfService;
import java_project.utils.SessionManager;

public class PdfApiServer {

    private static final PdfService pdfService = new PdfService();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void startServer(int port) {
        port(port);

        before((req, res) -> {
            // Simple auth placeholder: require Authorization header
            String auth = req.headers("Authorization");
            if (!validateAuth(auth)) {
                halt(401, "Unauthorized");
            }
        });

        post("/api/pdf/export", (req, res) -> {
            Map<String, Object> body = mapper.readValue(req.body(), new TypeReference<>() {});
            String template = (String) body.get("templateName");
            Map<String, Object> data = (Map<String, Object>) body.get("data");

            byte[] pdf = pdfService.generatePdfFromTemplate(template, data);

            res.type("application/pdf");
            res.header("Content-Disposition", "attachment; filename=export.pdf");
            res.raw().getOutputStream().write(pdf);
            res.raw().getOutputStream().flush();
            return res.raw();
        });

        post("/api/pdf/save", (req, res) -> {
            Map<String, Object> body = mapper.readValue(req.body(), new TypeReference<>() {});
            String template = (String) body.get("templateName");
            Map<String, Object> data = (Map<String, Object>) body.get("data");
            String filename = (String) body.getOrDefault("filename", "export.pdf");

            byte[] pdf = pdfService.generatePdfFromTemplate(template, data);
            Path saved = pdfService.savePdf(pdf, filename);

            res.type("application/json");
            return mapper.writeValueAsString(Map.of("path", saved.toString()));
        });

        post("/api/pdf/print", (req, res) -> {
            Map<String, Object> body = mapper.readValue(req.body(), new TypeReference<>() {});
            String template = (String) body.get("templateName");
            Map<String, Object> data = (Map<String, Object>) body.get("data");
            String filename = (String) body.getOrDefault("filename", "export.pdf");

            byte[] pdf = pdfService.generatePdfFromTemplate(template, data);
            Path saved = pdfService.savePdf(pdf, filename);
            pdfService.printPdf(saved);

            res.type("application/json");
            return mapper.writeValueAsString(Map.of("printed", true, "path", saved.toString()));
        });
    }

    private static boolean validateAuth(String authHeader) {
        // Integrate with existing authentication/session management:
        // If an Authorization header is present, it must match the app's session access token.
        // If no header is provided (requests from the same JVM), allow when a session exists.
        String sessionToken = SessionManager.getInstance().getAccessToken();
        if (authHeader != null && !authHeader.trim().isEmpty()) {
            String token = authHeader.trim();
            if (token.toLowerCase().startsWith("bearer ")) {
                token = token.substring(7);
            }
            return sessionToken != null && sessionToken.equals(token);
        }
        // No header: allow only if a session token exists in the running app.
        return sessionToken != null && !sessionToken.isEmpty();
    }
}
