package com.rtb.manageyourmoneybackend.user.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    public static final String APP_NAME = "manage-your-money";

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /* ==========================================
     * 1. ASYNC ACCOUNT CONFIRMATION
     * ========================================== */
    @Async
    public void sendAccountConfirmationEmail(String to, String confirmationLink) {
        String subject = "Welcome to %s - Confirm Your Account".formatted(APP_NAME);

        // Using Java Text Blocks for clean HTML
        String htmlBody = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                    <h2 style="color: #2c3e50; text-align: center;">Welcome Aboard!</h2>
                    <p style="font-size: 16px; color: #333;">Hello,</p>
                    <p style="font-size: 16px; color: #333;">Thank you for registering. To unlock your account and begin tracking you learnings, please verify your email address by clicking the button below:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #3498db; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">Verify Email Address</a>
                    </div>
                    <p style="font-size: 14px; color: #7f8c8d;">If the button doesn't work, copy and paste this link into your browser:</p>
                    <p style="font-size: 14px; color: #3498db; word-break: break-all;">%s</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="font-size: 12px; color: #95a5a6; text-align: center;">If you did not create this account, please safely ignore this email.</p>
                </div>
                """.formatted(confirmationLink, confirmationLink);

        sendHtmlEmail(to, subject, htmlBody);
    }

    /* ==========================================
     * 2. ASYNC PASSWORD RESET
     * ========================================== */
    @Async
    public void sendPasswordResetEmail(String to, String resetLink) {
        String subject = "Security Alert: Password Reset Request";

        String htmlBody = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                    <h2 style="color: #e74c3c; text-align: center;">Password Reset Request</h2>
                    <p style="font-size: 16px; color: #333;">Hello,</p>
                    <p style="font-size: 16px; color: #333;">We received a request to reset the password for your account. You can securely set a new password by clicking the button below:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #e74c3c; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">Reset Password</a>
                    </div>
                    <p style="font-size: 14px; color: #7f8c8d;">This link will expire in 15 minutes. If the button doesn't work, copy and paste this link into your browser:</p>
                    <p style="font-size: 14px; color: #3498db; word-break: break-all;">%s</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="font-size: 12px; color: #95a5a6; text-align: center;">If you did not request a password reset, no further action is required. Your account remains secure.</p>
                </div>
                """.formatted(resetLink, resetLink);

        sendHtmlEmail(to, subject, htmlBody);
    }

    /* ==========================================
     * 3. DATA EXPORT WITH ATTACHMENT
     * ========================================== */
    public void sendExportEmail(String to, File attachment) {
        String subject = "Your Data Export is Ready";

        String htmlBody = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                    <h2 style="color: #2c3e50; text-align: center;">Your Data Backup</h2>
                    <p style="font-size: 16px; color: #333;">Hello,</p>
                    <p style="font-size: 16px; color: #333;">Your data export has been completed. The compressed file containing your categories and expenses is attached to this email.</p>
                    <p style="font-size: 14px; color: #7f8c8d;">You can keep this file safe to restore or import your data back into the system later.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="font-size: 12px; color: #95a5a6; text-align: center;">If you did not request this export, please secure your account immediately.</p>
                </div>
                """;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            // Setting 'true' as 2nd parameter configures a multipart message required for attachments
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.addAttachment(attachment.getName(), attachment);

            mailSender.send(message);
            log.info("📧 Successfully dispatched data export email to: {}", to);

        } catch (MessagingException e) {
            log.error("❌ Failed to send export email to {}. Error: {}", to, e.getMessage(), e);
        }
    }

    /* ==========================================
     * HELPER: MIME MESSAGE DISPATCHER
     * ========================================== */
    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // True indicates this is a multipart message (allows HTML)
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            log.info("From Email: {}", fromEmail);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates the text is HTML

            mailSender.send(message);
            log.info("📧 Successfully dispatched async email to: {}", to);

        } catch (MessagingException e) {
            // Because this is on a background thread, exceptions cannot be thrown back to the HTTP controller.
            // We must log them carefully to monitor for SMTP failures.
            log.error("❌ Failed to send email to {}. Error: {}", to, e.getMessage(), e);
        }
    }

}