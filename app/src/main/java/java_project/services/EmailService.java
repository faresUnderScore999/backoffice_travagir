package java_project.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import javax.mail.*;
import javax.mail.internet.*;
import java_project.models.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class EmailService {
    
    private final String GMAIL_SMTP_HOST = "smtp.gmail.com";
    private final int GMAIL_SMTP_PORT = 587;
    private final String GMAIL_USERNAME = "hiibarouissi098@gmail.com"; // Your Gmail
    private final String GMAIL_PASSWORD = "blwfulerpvakndfl"; // Your Gmail App Password
    
    private final ObjectMapper mapper = new ObjectMapper();
    private final UserService userService = new UserService();
    
    public EmailService() {
        mapper.registerModule(new JavaTimeModule());
    }
    
    public CompletableFuture<Boolean> sendOfferToAllUsers(String offerTitle, String offerDescription, double discount) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get all users from database
                List<User> users = getAllUsers();
                
                // FOR TESTING: Add static test email address
                User testUser = new User();
                testUser.setId(999);
                testUser.setName("Test User");
                testUser.setEmail("hassanjebri66@gmail.com");
                testUser.setTel("123456789");
                users.add(testUser);
                
                if (users.isEmpty()) {
                    System.err.println("❌ No users found in database");
                    return false;
                }
                
                // Create email content
                String subject = "🔥 Special Offer: " + offerTitle;
                String htmlContent = createOfferEmailHTML(offerTitle, offerDescription, discount);
                
                // Send email to all users
                int successCount = 0;
                int failureCount = 0;
                
                System.out.println("\n🚀 STARTING OFFER EMAIL CAMPAIGN");
                System.out.println("📋 Offer Details:");
                System.out.println("   📝 Title: " + offerTitle);
                System.out.println("   💰 Discount: " + discount + "%");
                System.out.println("   📊 Total Users: " + users.size());
                System.out.println("   🧪 Test Email: hassanjebri66@gmail.com");
                System.out.println("   📧 Sender: " + GMAIL_USERNAME);
                System.out.println("─".repeat(50));
                
                for (User user : users) {
                    if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                        try {
                            System.out.println("📤 Sending email to: " + user.getEmail() + " (ID: " + user.getId() + ")");
                            sendEmail(user.getEmail(), subject, htmlContent);
                            successCount++;
                            System.out.println("✅ SUCCESS: Email sent to " + user.getEmail());
                            
                            // Special message for test email
                            if (user.getEmail().equals("hassanjebri66@gmail.com")) {
                                System.out.println("🧪 TEST EMAIL: Check hassanjebri66@gmail.com inbox!");
                            }
                            
                        } catch (Exception e) {
                            failureCount++;
                            System.err.println("❌ FAILED: Could not send to " + user.getEmail());
                            System.err.println("   Error: " + e.getMessage());
                        }
                    } else {
                        System.out.println("⚠️  SKIPPED: User " + user.getId() + " has no email address");
                    }
                }
                
                System.out.println("─".repeat(50));
                System.out.println("📊 EMAIL CAMPAIGN RESULTS:");
                System.out.println("   ✅ Success: " + successCount + " emails sent");
                System.out.println("   ❌ Failed: " + failureCount + " emails failed");
                System.out.println("   📊 Total: " + users.size() + " users processed");
                System.out.println("   📈 Success Rate: " + (users.size() > 0 ? (successCount * 100 / users.size()) : 0) + "%");
                System.out.println("   🧪 Test Email Status: " + (successCount > 0 ? "SENT ✓" : "FAILED ✗"));
                System.out.println("🎯 CAMPAIGN COMPLETE\n");
                
                return successCount > 0;
                
            } catch (Exception e) {
                System.err.println("❌ Email campaign failed: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }
    
    public CompletableFuture<Boolean> sendPromoCodeToAllUsers(String promoCode, String description, String validUntil) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get all users from database
                List<User> users = getAllUsers();
                
                // FOR TESTING: Add static test email address
                User testUser = new User();
                testUser.setId(999);
                testUser.setName("Test User");
                testUser.setEmail("hassanjebri66@gmail.com");
                testUser.setTel("123456789");
                users.add(testUser);
                
                if (users.isEmpty()) {
                    System.err.println("❌ No users found in database");
                    return false;
                }
                
                // Create email content
                String subject = "🎫 Exclusive Promo Code: " + promoCode;
                String htmlContent = createPromoCodeEmailHTML(promoCode, description, validUntil);
                
                // Send email to all users
                int successCount = 0;
                int failureCount = 0;
                
                System.out.println("\n🚀 STARTING PROMO CODE EMAIL CAMPAIGN");
                System.out.println("🎫 Promo Code Details:");
                System.out.println("   🏷️  Code: " + promoCode);
                System.out.println("   📝 Description: " + description);
                System.out.println("   📅 Valid Until: " + validUntil);
                System.out.println("   📊 Total Users: " + users.size());
                System.out.println("   🧪 Test Email: hassanjebri66@gmail.com");
                System.out.println("   📧 Sender: " + GMAIL_USERNAME);
                System.out.println("─".repeat(50));
                
                for (User user : users) {
                    if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                        try {
                            System.out.println("📤 Sending promo email to: " + user.getEmail() + " (ID: " + user.getId() + ")");
                            sendEmail(user.getEmail(), subject, htmlContent);
                            successCount++;
                            System.out.println("✅ SUCCESS: Promo email sent to " + user.getEmail());
                            
                            // Special message for test email
                            if (user.getEmail().equals("jebrihassan66@gmail.com")) {
                                System.out.println("🧪 TEST EMAIL: Check hassanjebri66@gmail.com inbox for promo code!");
                            }
                            
                        } catch (Exception e) {
                            failureCount++;
                            System.err.println("❌ FAILED: Could not send promo to " + user.getEmail());
                            System.err.println("   Error: " + e.getMessage());
                        }
                    } else {
                        System.out.println("⚠️  SKIPPED: User " + user.getId() + " has no email address");
                    }
                }
                
                System.out.println("─".repeat(50));
                System.out.println("📊 PROMO CODE EMAIL RESULTS:");
                System.out.println("   ✅ Success: " + successCount + " emails sent");
                System.out.println("   ❌ Failed: " + failureCount + " emails failed");
                System.out.println("   📊 Total: " + users.size() + " users processed");
                System.out.println("   📈 Success Rate: " + (users.size() > 0 ? (successCount * 100 / users.size()) : 0) + "%");
                System.out.println("   🧪 Test Email Status: " + (successCount > 0 ? "SENT ✓" : "FAILED ✗"));
                System.out.println("🎯 PROMO CAMPAIGN COMPLETE\n");
                
                return successCount > 0;
                
            } catch (Exception e) {
                System.err.println("❌ Promo code email campaign failed: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }
    
    private List<User> getAllUsers() throws Exception {
        // This would typically call your backend API
        // For now, let's create a mock implementation
        // In production, you'd call: userService.getAllUsers() or similar
        
        // Mock users for demonstration - replace with actual API call
        List<User> users = new ArrayList<>();
        
        User user1 = new User();
        user1.setId(1);
        user1.setName("John Doe");
        user1.setEmail("john.doe@example.com");
        user1.setTel("123456789");
        users.add(user1);
        
        User user2 = new User();
        user2.setId(2);
        user2.setName("Jane Smith");
        user2.setEmail("jane.smith@example.com");
        user2.setTel("987654321");
        users.add(user2);
        
        User user3 = new User();
        user3.setId(3);
        user3.setName("Bob Johnson");
        user3.setEmail("bob.johnson@example.com");
        user3.setTel("555123456");
        users.add(user3);
        
        return users;
    }
    
    private void sendEmail(String toEmail, String subject, String htmlContent) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", GMAIL_SMTP_HOST);
        props.put("mail.smtp.port", GMAIL_SMTP_PORT);
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(GMAIL_USERNAME, GMAIL_PASSWORD);
            }
        });
        
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(GMAIL_USERNAME));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        
        // Create HTML content
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlContent, "text/html");
        
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(htmlPart);
        
        message.setContent(multipart);
        
        // Send email
        Transport.send(message);
       }
        
    
    private String createOfferEmailHTML(String title, String description, double discount) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>🔥 EXCLUSIVE OFFER - Travagir</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { 
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        padding: 20px;
                        line-height: 1.6;
                    }
                    .container { 
                        max-width: 650px; 
                        margin: 0 auto; 
                        background: white;
                        border-radius: 20px;
                        overflow: hidden;
                        box-shadow: 0 20px 40px rgba(0,0,0,0.1);
                    }
                    .header { 
                        background: linear-gradient(135deg, #FF6B6B 0%, #4ECDC4 100%);
                        color: white; 
                        padding: 40px 30px; 
                        text-align: center;
                        position: relative;
                    }
                    .header::before {
                        content: '';
                        position: absolute;
                        top: 0; left: 0; right: 0; bottom: 0;
                        background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="white" opacity="0.1"/><circle cx="75" cy="75" r="1" fill="white" opacity="0.1"/><circle cx="50" cy="10" r="0.5" fill="white" opacity="0.2"/><circle cx="20" cy="60" r="0.5" fill="white" opacity="0.2"/><circle cx="80" cy="40" r="0.5" fill="white" opacity="0.2"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
                        opacity: 0.3;
                    }
                    .header h1 { 
                        font-size: 36px; 
                        font-weight: 800;
                        margin-bottom: 10px;
                        text-shadow: 2px 2px 4px rgba(0,0,0,0.2);
                        position: relative;
                        z-index: 1;
                    }
                    .header p { 
                        font-size: 18px; 
                        opacity: 0.95;
                        position: relative;
                        z-index: 1;
                    }
                    .content { 
                        padding: 40px 30px; 
                        background: white;
                    }
                    .offer-box { 
                        background: linear-gradient(135deg, #FFE66D 0%, #FF6B6B 100%);
                        border-radius: 20px; 
                        padding: 40px; 
                        margin: 30px 0; 
                        text-align: center;
                        box-shadow: 0 10px 30px rgba(255,107,107,0.3);
                        position: relative;
                        overflow: hidden;
                    }
                    .offer-box::before {
                        content: '🎉';
                        position: absolute;
                        font-size: 120px;
                        opacity: 0.1;
                        top: -20px;
                        right: -20px;
                        transform: rotate(15deg);
                    }
                    .discount { 
                        font-size: 64px; 
                        font-weight: 900; 
                        color: white;
                        margin: 15px 0;
                        text-shadow: 3px 3px 6px rgba(0,0,0,0.3);
                        position: relative;
                        z-index: 1;
                    }
                    .discount-label {
                        font-size: 18px;
                        color: white;
                        font-weight: 600;
                        text-transform: uppercase;
                        letter-spacing: 2px;
                        position: relative;
                        z-index: 1;
                    }
                    .title { 
                        font-size: 28px; 
                        font-weight: 700; 
                        color: #2C3E50; 
                        margin-bottom: 15px;
                        text-align: center;
                    }
                    .description { 
                        color: #5A6C7D; 
                        font-size: 16px;
                        line-height: 1.8; 
                        margin-bottom: 30px;
                        text-align: center;
                        background: #F8F9FA;
                        padding: 20px;
                        border-radius: 15px;
                        border-left: 4px solid #4ECDC4;
                    }
                    .btn { 
                        display: inline-block; 
                        padding: 18px 40px; 
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white; 
                        text-decoration: none; 
                        border-radius: 50px; 
                        font-weight: 700;
                        font-size: 16px;
                        margin-top: 30px;
                        box-shadow: 0 10px 25px rgba(102,126,234,0.4);
                        transition: all 0.3s ease;
                        text-transform: uppercase;
                        letter-spacing: 1px;
                    }
                    .btn:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 15px 35px rgba(102,126,234,0.5);
                    }
                    .footer { 
                        background: linear-gradient(135deg, #2C3E50 0%, #34495E 100%);
                        padding: 30px; 
                        text-align: center; 
                        color: white;
                        font-size: 14px;
                    }
                    .footer-logo {
                        font-size: 24px;
                        font-weight: 800;
                        margin-bottom: 10px;
                        background: linear-gradient(135deg, #FF6B6B 0%, #4ECDC4 100%);
                        -webkit-background-clip: text;
                        -webkit-text-fill-color: transparent;
                        background-clip: text;
                    }
                    .social-links {
                        margin: 20px 0;
                    }
                    .social-links a {
                        display: inline-block;
                        width: 40px;
                        height: 40px;
                        background: rgba(255,255,255,0.1);
                        border-radius: 50%;
                        margin: 0 5px;
                        line-height: 40px;
                        text-align: center;
                        color: white;
                        text-decoration: none;
                        transition: all 0.3s ease;
                    }
                    .social-links a:hover {
                        background: rgba(255,255,255,0.2);
                        transform: scale(1.1);
                    }
                    .pulse {
                        animation: pulse 2s infinite;
                    }
                    @keyframes pulse {
                        0% { transform: scale(1); }
                        50% { transform: scale(1.05); }
                        100% { transform: scale(1); }
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔥 EXCLUSIVE OFFER</h1>
                        <p>Limited Time Deal - Don't Miss Out!</p>
                    </div>
                    <div class="content">
                        <div class="title">%s</div>
                        <div class="offer-box pulse">
                            <div class="discount-label">SPECIAL DISCOUNT</div>
                            <div class="discount">%.0f%% OFF</div>
                            <p style="color: white; font-weight: 600; margin-top: 10px;">Just for you! 🎯</p>
                        </div>
                        <div class="description">
                            <strong>📝 Offer Details:</strong><br>
                            %s
                        </div>
                        <div style="text-align: center;">
                            <a href="#" class="btn">🚀 Claim Your Offer Now</a>
                        </div>
                    </div>
                    <div class="footer">
                        <div class="footer-logo">✈️ Travagir</div>
                        <p>Your Gateway to Amazing Adventures</p>
                        <div class="social-links">
                            <a href="#">📘</a>
                            <a href="#">📷</a>
                            <a href="#">🐦</a>
                            <a href="#">📧</a>
                        </div>
                        <p style="margin-top: 20px; opacity: 0.8;">© 2024 Travagir | All rights reserved</p>
                        <p style="opacity: 0.6; font-size: 12px;">This email was sent to you because you're a valued customer.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(title, discount, description);
    }
    
    private String createPromoCodeEmailHTML(String promoCode, String description, String validUntil) {
        String template = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>🎫 EXCLUSIVE PROMO CODE - Travagir</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { 
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        padding: 20px;
                        line-height: 1.6;
                    }
                    .container { 
                        max-width: 650px; 
                        margin: 0 auto; 
                        background: white;
                        border-radius: 20px;
                        overflow: hidden;
                        box-shadow: 0 20px 40px rgba(0,0,0,0.1);
                    }
                    .header { 
                        background: linear-gradient(135deg, #4FACFE 0%, #00F2FE 100%);
                        color: white; 
                        padding: 40px 30px; 
                        text-align: center;
                        position: relative;
                    }
                    .header::before {
                        content: '';
                        position: absolute;
                        top: 0; left: 0; right: 0; bottom: 0;
                        background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="stars" width="50" height="50" patternUnits="userSpaceOnUse"><text x="10" y="20" font-size="20" fill="white" opacity="0.1">⭐</text><text x="30" y="40" font-size="15" fill="white" opacity="0.1">⭐</text><text x="5" y="45" font-size="10" fill="white" opacity="0.1">⭐</text></pattern></defs><rect width="100" height="100" fill="url(%23stars)"/></svg>');
                        opacity: 0.3;
                    }
                    .header h1 { 
                        font-size: 36px; 
                        font-weight: 800;
                        margin-bottom: 10px;
                        text-shadow: 2px 2px 4px rgba(0,0,0,0.2);
                        position: relative;
                        z-index: 1;
                    }
                    .header p { 
                        font-size: 18px; 
                        opacity: 0.95;
                        position: relative;
                        z-index: 1;
                    }
                    .content { 
                        padding: 40px 30px; 
                        background: white;
                    }
                    .title { 
                        font-size: 28px; 
                        font-weight: 700; 
                        color: #2C3E50; 
                        margin-bottom: 20px;
                        text-align: center;
                    }
                    .promo-box { 
                        background: linear-gradient(135deg, #4FACFE 0%, #00F2FE 100%);
                        color: white; 
                        border-radius: 20px; 
                        padding: 40px; 
                        margin: 30px 0; 
                        text-align: center;
                        box-shadow: 0 15px 35px rgba(79,172,254,0.4);
                        position: relative;
                        overflow: hidden;
                    }
                    .promo-box::before {
                        content: '🎫';
                        position: absolute;
                        font-size: 150px;
                        opacity: 0.1;
                        top: -30px;
                        left: -30px;
                        transform: rotate(-15deg);
                    }
                    .promo-box::after {
                        content: '💎';
                        position: absolute;
                        font-size: 100px;
                        opacity: 0.1;
                        bottom: -20px;
                        right: -20px;
                        transform: rotate(15deg);
                    }
                    .promo-label {
                        font-size: 18px;
                        color: white;
                        font-weight: 600;
                        text-transform: uppercase;
                        letter-spacing: 2px;
                        margin-bottom: 15px;
                        position: relative;
                        z-index: 1;
                    }
                    .promo-code { 
                        font-size: 42px; 
                        font-weight: 900; 
                        background: white; 
                        color: #4FACFE; 
                        padding: 20px 40px; 
                        border-radius: 15px; 
                        display: inline-block; 
                        margin: 20px 0; 
                        letter-spacing: 4px;
                        box-shadow: 0 10px 25px rgba(0,0,0,0.2);
                        position: relative;
                        z-index: 1;
                        border: 3px dashed rgba(255,255,255,0.3);
                        transition: all 0.3s ease;
                    }
                    .promo-code:hover {
                        transform: scale(1.05);
                        box-shadow: 0 15px 35px rgba(0,0,0,0.3);
                    }
                    .copy-hint {
                        font-size: 14px;
                        color: white;
                        opacity: 0.9;
                        margin-top: 10px;
                        font-style: italic;
                        position: relative;
                        z-index: 1;
                    }
                    .description { 
                        color: #5A6C7D; 
                        font-size: 16px;
                        line-height: 1.8; 
                        margin-bottom: 30px;
                        text-align: center;
                        background: linear-gradient(135deg, #F8F9FA 0%, #E9ECEF 100%);
                        padding: 25px;
                        border-radius: 15px;
                        border-left: 4px solid #4FACFE;
                        position: relative;
                    }
                    .description::before {
                        content: '📝';
                        position: absolute;
                        top: -10px;
                        left: 20px;
                        background: white;
                        padding: 5px 10px;
                        border-radius: 50%;
                        font-size: 20px;
                    }
                    .valid-until { 
                        background: linear-gradient(135deg, #FFE66D 0%, #FF6B6B 100%);
                        border-radius: 15px; 
                        padding: 20px; 
                        margin: 30px 0; 
                        text-align: center; 
                        color: white; 
                        font-weight: 700;
                        box-shadow: 0 10px 25px rgba(255,107,107,0.3);
                        position: relative;
                        overflow: hidden;
                    }
                    .valid-until::before {
                        content: '⏰';
                        position: absolute;
                        font-size: 60px;
                        opacity: 0.2;
                        top: 10px;
                        right: 20px;
                    }
                    .valid-until strong {
                        font-size: 18px;
                        display: block;
                        margin-bottom: 5px;
                    }
                    .btn { 
                        display: inline-block; 
                        padding: 18px 40px; 
                        background: linear-gradient(135deg, #FF6B6B 0%, #4ECDC4 100%);
                        color: white; 
                        text-decoration: none; 
                        border-radius: 50px; 
                        font-weight: 700;
                        font-size: 16px;
                        margin-top: 30px;
                        box-shadow: 0 10px 25px rgba(255,107,107,0.4);
                        transition: all 0.3s ease;
                        text-transform: uppercase;
                        letter-spacing: 1px;
                        position: relative;
                        overflow: hidden;
                    }
                    .btn::before {
                        content: '';
                        position: absolute;
                        top: 0; left: -100%;
                        width: 100%;
                        height: 100%;
                        background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
                        transition: left 0.5s ease;
                    }
                    .btn:hover::before {
                        left: 100%;
                    }
                    .btn:hover {
                        transform: translateY(-3px);
                        box-shadow: 0 15px 35px rgba(255,107,107,0.5);
                    }
                    .footer { 
                        background: linear-gradient(135deg, #2C3E50 0%, #34495E 100%);
                        padding: 30px; 
                        text-align: center; 
                        color: white;
                        font-size: 14px;
                    }
                    .footer-logo {
                        font-size: 24px;
                        font-weight: 800;
                        margin-bottom: 10px;
                        background: linear-gradient(135deg, #4FACFE 0%, #00F2FE 100%);
                        -webkit-background-clip: text;
                        -webkit-text-fill-color: transparent;
                        background-clip: text;
                    }
                    .social-links {
                        margin: 20px 0;
                    }
                    .social-links a {
                        display: inline-block;
                        width: 40px;
                        height: 40px;
                        background: rgba(255,255,255,0.1);
                        border-radius: 50%;
                        margin: 0 5px;
                        line-height: 40px;
                        text-align: center;
                        color: white;
                        text-decoration: none;
                        transition: all 0.3s ease;
                    }
                    .social-links a:hover {
                        background: rgba(255,255,255,0.2);
                        transform: scale(1.1) rotate(360deg);
                    }
                    .pulse {
                        animation: pulse 2s infinite;
                    }
                    .glow {
                        animation: glow 2s ease-in-out infinite alternate;
                    }
                    @keyframes pulse {
                        0% { transform: scale(1); }
                        50% { transform: scale(1.05); }
                        100% { transform: scale(1); }
                    }
                    @keyframes glow {
                        from { box-shadow: 0 0 10px rgba(79,172,254,0.4); }
                        to { box-shadow: 0 0 20px rgba(79,172,254,0.8); }
                    }
                    .features {
                        display: flex;
                        justify-content: space-around;
                        margin: 30px 0;
                        text-align: center;
                    }
                    .feature {
                        flex: 1;
                        padding: 20px;
                    }
                    .feature-icon {
                        font-size: 32px;
                        margin-bottom: 10px;
                    }
                    .feature-text {
                        font-size: 14px;
                        color: #5A6C7D;
                        font-weight: 600;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎫 EXCLUSIVE PROMO CODE</h1>
                        <p>Special Discount Just For You! ✨</p>
                    </div>
                    <div class="content">
                        <div class="title">Your Personal Promo Code</div>
                        
                        <div class="promo-box glow">
                            <div class="promo-label">🎯 YOUR EXCLUSIVE CODE</div>
                            <div class="promo-code pulse">PROMO_CODE_PLACEHOLDER</div>
                            <div class="copy-hint">💡 Click to copy and save this code!</div>
                        </div>
                        
                        <div class="features">
                            <div class="feature">
                                <div class="feature-icon">⚡</div>
                                <div class="feature-text">Instant Discount</div>
                            </div>
                            <div class="feature">
                                <div class="feature-icon">🔒</div>
                                <div class="feature-text">Secure & Valid</div>
                            </div>
                            <div class="feature">
                                <div class="feature-icon">🎁</div>
                                <div class="feature-text">Exclusive Offer</div>
                            </div>
                        </div>
                        
                        <div class="description">
                            <strong>🌟 About This Promo:</strong><br>
                            DESCRIPTION_PLACEHOLDER
                        </div>
                        
                        <div class="valid-until">
                            <strong>⏰ Valid Until:</strong> VALID_UNTIL_PLACEHOLDER
                            <div style="font-size: 14px; opacity: 0.9; margin-top: 5px;">Don't miss out on this amazing deal!</div>
                        </div>
                        
                        <div style="text-align: center;">
                            <a href="#" class="btn">🚀 Use Promo Code Now</a>
                        </div>
                    </div>
                    <div class="footer">
                        <div class="footer-logo">✈️ Travagir</div>
                        <p>Your Gateway to Amazing Adventures</p>
                        <div class="social-links">
                            <a href="#">📘</a>
                            <a href="#">📷</a>
                            <a href="#">🐦</a>
                            <a href="#">📧</a>
                        </div>
                        <p style="margin-top: 20px; opacity: 0.8;">© 2024 Travagir | All rights reserved</p>
                        <p style="opacity: 0.6; font-size: 12px;">This email was sent to you because you're a valued customer.</p>
                    </div>
                </div>
            </body>
            </html>
            """;
        
        // Simple string replacement to avoid all formatting issues
        return template
            .replace("PROMO_CODE_PLACEHOLDER", promoCode)
            .replace("DESCRIPTION_PLACEHOLDER", description)
            .replace("VALID_UNTIL_PLACEHOLDER", validUntil);
    }
}
