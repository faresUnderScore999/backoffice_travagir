package java_project.services;

import java_project.models.PromoCode;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.stage.FileChooser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

public class PromoCodePDFService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public CompletableFuture<Boolean> generatePromoCodePDF(PromoCode promoCode, String offerTitle, javafx.stage.Window parentWindow) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Show file chooser
                javafx.application.Platform.runLater(() -> {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Save Promo Code PDF");
                    fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                    fileChooser.setInitialFileName("promo_code_" + promoCode.getCode() + ".pdf");
                    
                    java.io.File file = fileChooser.showSaveDialog(parentWindow);
                    if (file != null) {
                        try {
                            createPDFDocument(promoCode, offerTitle, file.getAbsolutePath());
                            showSuccessNotification("PDF Generated", "PDF saved successfully: " + file.getName());
                        } catch (Exception e) {
                            showErrorNotification("PDF Error", "Failed to generate PDF: " + e.getMessage());
                        }
                    }
                });
                return true;
            } catch (Exception e) {
                showErrorNotification("PDF Error", "Error generating PDF: " + e.getMessage());
                return false;
            }
        });
    }
    
    private void createPDFDocument(PromoCode promoCode, String offerTitle, String filePath) throws Exception {
        // Create PDF writer
        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        // Load fonts
        PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        
        // Add title with background
        Paragraph title = new Paragraph("🎫 PROMO CODE DETAILS")
            .setFont(titleFont)
            .setFontSize(24)
            .setFontColor(ColorConstants.WHITE)
            .setMarginBottom(20);
        
        // Create header table
        Table headerTable = new Table(1);
        Cell headerCell = new Cell()
            .add(title)
            .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(249, 183, 41))
            .setPadding(20)
            .setBorder(Border.NO_BORDER);
        headerTable.addCell(headerCell);
        document.add(headerTable);
        
        // Add some spacing
        document.add(new Paragraph(" "));
        
        // Generate QR Code
        byte[] qrCodeBytes = generateQRCode(promoCode.getCode(), promoCode.getDescription());
        Image qrImage = new Image(com.itextpdf.io.image.ImageDataFactory.create(qrCodeBytes))
            .setWidth(100);
        
        // Main content table
        Table mainTable = new Table(2);
        
        // Left side - Promo code details
        Cell detailsCell = new Cell()
            .setPadding(15)
            .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1));
        
        detailsCell.add(new Paragraph("Promo Code Information")
            .setFont(boldFont)
            .setFontSize(16)
            .setFontColor(ColorConstants.DARK_GRAY)
            .setMarginBottom(10));
        
        // Add promo code details
        detailsCell.add(new Paragraph("Code: " + promoCode.getCode())
            .setFont(boldFont)
            .setFontSize(12)
            .setMarginBottom(5));
        
        detailsCell.add(new Paragraph("Description: " + (promoCode.getDescription() != null ? promoCode.getDescription() : "N/A"))
            .setFont(normalFont)
            .setFontSize(12)
            .setMarginBottom(5));
        
        detailsCell.add(new Paragraph("Offer: " + offerTitle)
            .setFont(normalFont)
            .setFontSize(12)
            .setMarginBottom(5));
        
        detailsCell.add(new Paragraph("Valid From: " + promoCode.getValidFrom().format(DATE_FORMATTER))
            .setFont(normalFont)
            .setFontSize(12)
            .setMarginBottom(5));
        
        detailsCell.add(new Paragraph("Valid To: " + promoCode.getValidTo().format(DATE_FORMATTER))
            .setFont(normalFont)
            .setFontSize(12)
            .setMarginBottom(5));
        
        detailsCell.add(new Paragraph("Usage Limit: " + promoCode.getUsageLimit())
            .setFont(normalFont)
            .setFontSize(12)
            .setMarginBottom(5));
        
        detailsCell.add(new Paragraph("Used Count: " + promoCode.getUsedCount())
            .setFont(normalFont)
            .setFontSize(12)
            .setMarginBottom(5));
        
        detailsCell.add(new Paragraph("Status: " + (promoCode.isActive() ? "Active" : "Inactive"))
            .setFont(normalFont)
            .setFontSize(12));
        
        // Right side - QR Code
        Cell qrCell = new Cell()
            .add(qrImage)
            .setPadding(15)
            .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1));
        
        qrCell.add(new Paragraph("Scan for Quick Access")
            .setFont(normalFont)
            .setFontSize(10)
            .setMarginTop(10));
        
        mainTable.addCell(detailsCell);
        mainTable.addCell(qrCell);
        document.add(mainTable);
        
        // Add some spacing
        document.add(new Paragraph(" "));
        
        // Add footer
        Table footerTable = new Table(1);
        Cell footerCell = new Cell()
            .add(new Paragraph("Generated on " + java.time.LocalDate.now().format(DATE_FORMATTER) + " | Travagir Promo System")
                .setFont(normalFont)
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY))
            .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(240, 240, 240))
            .setPadding(10)
            .setBorder(Border.NO_BORDER);
        
        footerTable.addCell(footerCell);
        document.add(footerTable);
        
        // Close document
        document.close();
    }
    
    private byte[] generateQRCode(String text, String description) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
            text + "|" + (description != null ? description : ""), 
            BarcodeFormat.QR_CODE, 
            200, 
            200
        );
        
        java.awt.image.BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        
        // Convert to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(bufferedImage, "PNG", baos);
        return baos.toByteArray();
    }
    
    private void showSuccessNotification(String title, String message) {
        javafx.application.Platform.runLater(() -> {
            try {
                System.out.println("✅ " + title + ": " + message);
            } catch (Exception e) {
                System.out.println("✅ " + title + ": " + message);
            }
        });
    }
    
    private void showErrorNotification(String title, String message) {
        javafx.application.Platform.runLater(() -> {
            try {
                System.err.println("❌ " + title + ": " + message);
            } catch (Exception e) {
                System.err.println("❌ " + title + ": " + message);
            }
        });
    }
}
