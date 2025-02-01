package com.example.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service to handle email-related functionalities.
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender; // Spring MailSender

    /**
     * Sends a password reset email.
     * 
     * @param recipientEmail The email to send the reset link to.
     * @param resetLink The password reset link.
     */
    public void sendResetEmail(String recipientEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setSubject("Password Reset Request");
        message.setText("Click the link below to reset your password:\n" + resetLink);

        mailSender.send(message); // Send email
    }

    /**
     * Sends a confirmation email after a successful password reset.
     * 
     * @param recipientEmail The email to send confirmation to.
     */
    public void sendConfirmationEmail(String recipientEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setSubject("Password Reset Successful");
        message.setText("Your password has been successfully reset. If you did not perform this action, contact support.");

        mailSender.send(message); // Send email
    }
}
