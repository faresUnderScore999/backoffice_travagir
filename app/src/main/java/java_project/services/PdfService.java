package java_project.services;

import org.xhtmlrenderer.pdf.ITextRenderer;

import java.awt.Desktop;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class PdfService {

    public byte[] generatePdfFromHtml(String html) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(os);
            os.flush();
            return os.toByteArray();
        } catch (Exception e) {
            throw new IOException("Failed to generate PDF", e);
        } finally {
            os.close();
        }
    }

    public String renderTemplate(String templateName, Map<String, Object> data) throws IOException {
        String resourcePath = "/java_project/templates/" + templateName + ".html";
        InputStream is = getClass().getResourceAsStream(resourcePath);
        if (is == null) throw new FileNotFoundException("Template not found: " + resourcePath);
        String template = new String(is.readAllBytes());
        is.close();

        // Very small template replacement: replace {{key}} with value.toString()
        if (data != null) {
            for (Map.Entry<String, Object> e : data.entrySet()) {
                String placeholder = "{{" + e.getKey() + "}}";
                String value = e.getValue() == null ? "" : e.getValue().toString();
                template = template.replace(placeholder, value);
            }
        }
        return template;
    }

    public byte[] generatePdfFromTemplate(String templateName, Map<String, Object> data) throws IOException {
        String html = renderTemplate(templateName, data);
        return generatePdfFromHtml(html);
    }

    public Path savePdf(byte[] pdfBytes, String filename) throws IOException {
        Path dir = Paths.get("build/generated-pdfs");
        if (!Files.exists(dir)) Files.createDirectories(dir);
        Path out = dir.resolve(filename);
        Files.write(out, pdfBytes);
        return out;
    }

    public void printPdf(Path pdfFile) throws IOException {
        if (!Files.exists(pdfFile)) throw new FileNotFoundException(pdfFile.toString());
        if (!Desktop.isDesktopSupported()) throw new IOException("Desktop operations are not supported on this platform");
        Desktop.getDesktop().print(pdfFile.toFile());
    }
}
