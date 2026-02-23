package java_project.services;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PdfServiceTest {

    @Test
    public void generatePdfFromTemplate_shouldReturnBytes_andSaveFile() throws Exception {
        PdfService service = new PdfService();

        Map<String, Object> data = Map.of("title", "Unit Test", "body", "This is a test PDF.");
        byte[] pdf = service.generatePdfFromTemplate("sample", data);

        assertNotNull(pdf);
        assertTrue(pdf.length > 100, "Expected non-empty PDF bytes");

        Path saved = service.savePdf(pdf, "unit-test.pdf");
        assertTrue(Files.exists(saved));

        // Clean up
        Files.deleteIfExists(saved);
    }
}
