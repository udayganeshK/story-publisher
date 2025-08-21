package com.storypublisher.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender javaMailSender;
    
    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;
    
    @Value("${app.mail.from:noreply@storypublisher.com}")
    private String fromEmail;
    
    @Value("${app.mail.from.name:Story Publisher}")
    private String fromName;
    
    @Value("${app.frontend.url:http://localhost:6001}")
    private String frontendUrl;
    
    /**
     * Send password reset email.
     * If mail is enabled and configured, sends real email.
     * Otherwise, logs the email content for development.
     */
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
        
        logger.info("📧 Email service called - Mail enabled: {}", mailEnabled);
        logger.info("📧 Sending to: {}, From: {}", toEmail, fromEmail);
        
        if (!mailEnabled) {
            // Mock mode for development
            logMockEmail(toEmail, resetUrl, resetToken);
            return;
        }
        
        try {
            logger.info("🔗 Attempting to send real email via Gmail SMTP...");
            
            // Send real email via Gmail SMTP
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            helper.setFrom(fromEmail, fromName);
            helper.setSubject("Reset Your Password - Story Publisher");
            helper.setText(createEmailTemplate(resetUrl), true);
            
            logger.info("📤 Sending email message...");
            javaMailSender.send(message);
            
            logger.info("✅ Password reset email sent successfully to: {}", toEmail);
            logger.info("🔗 Reset URL: {}", resetUrl);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send password reset email to: {}", toEmail, e);
            logger.error("❌ Error details: {}", e.getMessage());
            // Fallback to mock email due to send failure
            logger.info("📧 Falling back to mock email due to send failure:");
            logMockEmail(toEmail, resetUrl, resetToken);
        }
    }
    
    private void logMockEmail(String toEmail, String resetUrl, String resetToken) {
        logger.info("=== PASSWORD RESET EMAIL (MOCK) ===");
        logger.info("To: {}", toEmail);
        logger.info("From: {}", fromEmail);
        logger.info("Subject: Reset Your Password - Story Publisher");
        logger.info("Reset URL: {}", resetUrl);
        logger.info("Token: {}", resetToken);
        logger.info("=====================================");
        logger.info("📧 EMAIL CONTENT:");
        logger.info(createEmailTemplate(resetUrl));
        logger.info("=====================================");
    }
    
    private String createEmailTemplate(String resetUrl) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Reset Your Password</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4f46e5; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background-color: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #4f46e5; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; margin: 20px 0; }
                    .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #e5e7eb; font-size: 12px; color: #6b7280; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>🔐 Password Reset Request</h1>
                </div>
                <div class="content">
                    <h2>Hello!</h2>
                    <p>You have requested to reset your password for your Story Publisher account.</p>
                    <p>Click the button below to reset your password:</p>
                    <div style="text-align: center;">
                        <a href="%s" class="button">Reset My Password</a>
                    </div>
                    <p><strong>This link will expire in 1 hour</strong> for security reasons.</p>
                    <p>If you didn't request this password reset, please ignore this email. Your password will remain unchanged.</p>
                    <div class="footer">
                        <p>If the button doesn't work, copy and paste this link into your browser:<br>
                        <a href="%s">%s</a></p>
                        <p>© 2025 Story Publisher. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, resetUrl, resetUrl, resetUrl);
    }
}
