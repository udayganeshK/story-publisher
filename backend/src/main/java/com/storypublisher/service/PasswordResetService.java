package com.storypublisher.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.storypublisher.dto.ForgotPasswordRequest;
import com.storypublisher.dto.ResetPasswordRequest;
import com.storypublisher.model.PasswordResetToken;
import com.storypublisher.model.User;
import com.storypublisher.repository.PasswordResetTokenRepository;
import com.storypublisher.repository.UserRepository;

@Service
@Transactional
public class PasswordResetService {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public boolean initiatePasswordReset(ForgotPasswordRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        
        if (userOptional.isEmpty()) {
            // For security reasons, we don't reveal if the email exists or not
            // But we still return true to prevent email enumeration attacks
            return true;
        }
        
        User user = userOptional.get();
        
        // Invalidate any existing tokens for this user
        passwordResetTokenRepository.markAllUserTokensAsUsed(user);
        
        // Generate new reset token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1); // Token expires in 1 hour
        
        PasswordResetToken resetToken = new PasswordResetToken(token, user, expiryDate);
        passwordResetTokenRepository.save(resetToken);
        
        // Send email - adding debug logging
        logger.info("🔗 About to send password reset email to: {}", user.getEmail());
        logger.info("🎫 Generated token: {}", token);
        
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), token);
            logger.info("✅ Email service call completed successfully");
        } catch (Exception e) {
            logger.error("❌ Failed to call email service", e);
            throw e;
        }
        
        return true;
    }
    
    public boolean resetPassword(ResetPasswordRequest request) {
        if (!request.isPasswordMatch()) {
            throw new RuntimeException("Passwords do not match");
        }
        
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(request.getToken());
        
        if (tokenOptional.isEmpty()) {
            throw new RuntimeException("Invalid or expired reset token");
        }
        
        PasswordResetToken resetToken = tokenOptional.get();
        
        if (!resetToken.isValid()) {
            throw new RuntimeException("Token has expired or already been used");
        }
        
        // Update user password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        
        return true;
    }
    
    public boolean validateResetToken(String token) {
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(token);
        return tokenOptional.isPresent() && tokenOptional.get().isValid();
    }
    
    @Transactional
    public void cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
