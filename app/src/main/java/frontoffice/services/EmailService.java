package frontoffice.services;

import javax.mail.*;
import javax.mail.internet.*;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * Service for sending emails from the FrontOffice via Gmail SMTP.
 * Uses STARTTLS on port 587.
 */
public class EmailService {

    private static final String GMAIL_SMTP_HOST = "smtp.gmail.com";
    private static final int GMAIL_SMTP_PORT = 587;
    private static final String GMAIL_USERNAME = "hassanjebri99@gmail.com";
    private static final String GMAIL_PASSWORD = "tiekrpdfniuzvscz";

    /**
     * Sends a promo code email to the given recipient.
     *
     * @param toEmail    Connected user's email address
     * @param promoCode  The promo code string
     * @param offerTitle Title of the offer this code belongs to
     */
    public void sendPromoCodeEmail(String toEmail, String promoCode, String offerTitle)
            throws MessagingException, UnsupportedEncodingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", GMAIL_SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(GMAIL_SMTP_PORT));
        props.put("mail.smtp.ssl.trust", GMAIL_SMTP_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(GMAIL_USERNAME, GMAIL_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(GMAIL_USERNAME, "Travagir"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("🎁 Votre code promo Travagir — " + offerTitle);

        String html = buildEmailHtml(promoCode, offerTitle);
        message.setContent(html, "text/html; charset=utf-8");

        Transport.send(message);
    }

    private String buildEmailHtml(String promoCode, String offerTitle) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='utf-8'></head><body style='font-family:Arial,sans-serif;" +
                "background:#f4f4f4;padding:30px;'>" +
                "<div style='max-width:520px;margin:auto;background:#fff;border-radius:16px;" +
                "box-shadow:0 4px 20px rgba(0,0,0,0.1);overflow:hidden;'>" +
                "  <div style='background:linear-gradient(135deg,#667eea,#764ba2);padding:32px;text-align:center;'>" +
                "    <h1 style='color:#fff;margin:0;font-size:28px;'>🎁 Votre Code Promo</h1>" +
                "    <p style='color:rgba(255,255,255,0.85);margin-top:8px;font-size:15px;'>" + offerTitle + "</p>" +
                "  </div>" +
                "  <div style='padding:32px;text-align:center;'>" +
                "    <p style='color:#555;font-size:16px;margin-bottom:20px;'>Utilisez ce code pour profiter de votre réduction&nbsp;:</p>"
                +
                "    <div style='display:inline-block;background:#f0f0f0;border:2px dashed #764ba2;" +
                "border-radius:10px;padding:18px 36px;'>" +
                "      <span style='font-size:28px;font-weight:bold;letter-spacing:4px;color:#764ba2;'>" + promoCode
                + "</span>" +
                "    </div>" +
                "    <p style='color:#888;font-size:13px;margin-top:24px;'>Ce code est strictement personnel et à usage limité.</p>"
                +
                "  </div>" +
                "  <div style='background:#f9f9f9;padding:16px;text-align:center;'>" +
                "    <p style='color:#aaa;font-size:12px;margin:0;'>© 2025 Travagir — Bon voyage ✈️</p>" +
                "  </div>" +
                "</div></body></html>";
    }
}
