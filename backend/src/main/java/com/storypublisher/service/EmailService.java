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
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #ffffff; }
                    .header { background-color: #1e40af; color: #ffffff; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background-color: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { 
                        display: inline-block; 
                        padding: 14px 28px; 
                        background-color: #1e40af; 
                        color: #ffffff !important; 
                        text-decoration: none !important; 
                        border-radius: 6px; 
                        font-weight: bold; 
                        font-size: 16px; 
                        margin: 20px 0; 
                        border: 2px solid #1e40af;
                        text-align: center;
                        min-width: 200px;
                    }
                    .button:hover { background-color: #1d4ed8; border-color: #1d4ed8; }
                    .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #e5e7eb; font-size: 12px; color: #6b7280; }
                    .link-fallback { word-break: break-all; color: #1e40af; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1 style="color: #ffffff; margin: 0;">🔐 Password Reset Request</h1>
                </div>
                <div class="content">
                    <h2 style="color: #1f2937;">Hello!</h2>
                    <p>You have requested to reset your password for your Story Publisher account.</p>
                    <p>Click the button below to reset your password:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" class="button" style="color: #ffffff !important; text-decoration: none !important;">
                            Reset My Password
                        </a>
                    </div>
                    <p><strong style="color: #dc2626;">⏰ This link will expire in 1 hour</strong> for security reasons.</p>
                    <p>If you didn't request this password reset, please ignore this email. Your password will remain unchanged.</p>
                    <div class="footer">
                        <p><strong>If the button doesn't work, copy and paste this link into your browser:</strong></p>
                        <p class="link-fallback"><a href="%s" style="color: #1e40af;">%s</a></p>
                        <p style="margin-top: 20px;">© 2025 Story Publisher. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, resetUrl, resetUrl, resetUrl);
    }
}
