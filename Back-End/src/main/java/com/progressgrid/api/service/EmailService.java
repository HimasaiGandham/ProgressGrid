package com.progressgrid.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String DEFAULT_FROM = "ProgressGrid <onboarding@resend.dev>";
    private static final String SUBJECT = "ProgressGrid - Password Reset Verification Code: ";

    @Value("${resend.api.key:}")
    private String resendApiKey;

    @Value("${resend.from.email:" + DEFAULT_FROM + "}")
    private String resendFromEmail;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String smtpMailFrom;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Sends through Resend if RESEND_API_KEY is set, otherwise SMTP if MAIL_USERNAME is set. If
     * neither delivers, the code is written to the log so password reset still works in local
     * development. With email configured and working, codes never reach the log.
     */
    public void sendOtpEmail(String toEmail, String username, String otp) {
        String html = buildEmailHtml(username, otp);
        if (!resendApiKey.isBlank() && sendViaResend(toEmail, otp, html)) {
            return;
        }
        if (mailSender != null && !smtpMailFrom.isBlank() && sendViaSmtp(toEmail, otp, html)) {
            return;
        }
        log.info("[OTP DISPATCH] Recipient: {} | User: {} | Code: {}", toEmail, username, otp);
        log.info("No email provider delivered this code. Set RESEND_API_KEY or MAIL_USERNAME and MAIL_PASSWORD to send real emails.");
    }

    private boolean sendViaResend(String toEmail, String otp, String html) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "from", resendFromEmail.isBlank() ? DEFAULT_FROM : resendFromEmail.trim(),
                    "to", List.of(toEmail),
                    "subject", SUBJECT + otp,
                    "html", html));

            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + resendApiKey.trim())
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() / 100 == 2) {
                log.info("[RESEND] Email sent to {}", toEmail);
                return true;
            }
            log.warn("[RESEND] Resend API returned {}: {}", response.statusCode(), response.body());
        } catch (Exception e) {
            log.warn("[RESEND] Failed to call Resend API: {}", e.getMessage());
        }
        return false;
    }

    private boolean sendViaSmtp(String toEmail, String otp, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(smtpMailFrom, "ProgressGrid Security");
            helper.setTo(toEmail);
            helper.setSubject(SUBJECT + otp);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("[SMTP] Email sent to {}", toEmail);
            return true;
        } catch (Exception e) {
            log.warn("[SMTP] Could not send email via SMTP: {}", e.getMessage());
            return false;
        }
    }

    private String buildEmailHtml(String username, String otp) {
        // The username is user input, so it's escaped before going into the email's HTML.
        String name = username != null ? HtmlUtils.htmlEscape(username) : "User";
        return "<div style='font-family: Arial, sans-serif; max-width: 520px; margin: 0 auto; padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px; background-color: #ffffff;'>"
                + "<div style='text-align: center; margin-bottom: 20px;'>"
                + "<h2 style='color: #4f46e5; margin: 0;'>ProgressGrid</h2>"
                + "<p style='color: #6b7280; font-size: 14px; margin-top: 4px;'>Password Reset Request</p>"
                + "</div>"
                + "<p style='color: #374151; font-size: 15px;'>Hello <strong>" + name + "</strong>,</p>"
                + "<p style='color: #4b5563; font-size: 14px; line-height: 1.5;'>We received a request to reset the password for your ProgressGrid account. Use the verification code below to verify your identity:</p>"
                + "<div style='text-align: center; margin: 28px 0;'>"
                + "<span style='display: inline-block; font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #111827; background-color: #f3f4f6; padding: 14px 28px; border-radius: 8px; border: 1px dashed #cbd5e1;'>" + otp + "</span>"
                + "</div>"
                + "<p style='color: #6b7280; font-size: 13px;'>This code is valid for <strong>10 minutes</strong>. If you did not make this request, you can safely ignore this email.</p>"
                + "<hr style='border: none; border-top: 1px solid #f3f4f6; margin: 24px 0;'/>"
                + "<p style='color: #9ca3af; font-size: 12px; text-align: center;'>ProgressGrid Habit Tracker</p>"
                + "</div>";
    }
}
