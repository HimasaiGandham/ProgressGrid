package com.progressgrid.api.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${resend.api.key:}")
    private String resendApiKey;

    @Value("${resend.from.email:ProgressGrid <onboarding@resend.dev>}")
    private String resendFromEmail;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String smtpMailFrom;

    public void sendOtpEmail(String toEmail, String username, String otp) {
        // Prominently log to console for instant visibility during live testing
        log.info("==========================================================");
        log.info("[OTP DISPATCH] Recipient: {} | User: {} | Code: {}", toEmail, username, otp);
        log.info("==========================================================");

        // 1. Prioritize Resend API if API Key is configured
        if (resendApiKey != null && !resendApiKey.trim().isEmpty() && !resendApiKey.contains("YOUR_RESEND_API_KEY")) {
            boolean sent = sendViaResend(toEmail, username, otp);
            if (sent) {
                return;
            }
        }

        // 2. Fallback to SMTP if configured
        if (mailSender != null && smtpMailFrom != null && !smtpMailFrom.trim().isEmpty()) {
            sendViaSmtp(toEmail, username, otp);
            return;
        }

        log.info("[EMAIL SERVICE] Neither RESEND_API_KEY nor SMTP username is configured.");
        log.info("[EMAIL SERVICE] -> To receive real emails in your inbox, set 'resend.api.key=re_...' in application.properties or set RESEND_API_KEY in environment variables.");
        log.info("[EMAIL SERVICE] -> For testing right now, use the OTP code above: {}", otp);
    }

    private boolean sendViaResend(String toEmail, String username, String otp) {
        try {
            log.info("[RESEND] Dispatching email to {} via Resend API...", toEmail);
            String html = buildEmailHtml(username, otp);

            String from = (resendFromEmail != null && !resendFromEmail.trim().isEmpty())
                    ? resendFromEmail.trim()
                    : "ProgressGrid <onboarding@resend.dev>";

            String jsonPayload = String.format(
                    "{\"from\":\"%s\",\"to\":[\"%s\"],\"subject\":\"ProgressGrid - Password Reset Verification Code: %s\",\"html\":%s}",
                    escapeJson(from),
                    escapeJson(toEmail),
                    escapeJson(otp),
                    toJsonString(html)
            );

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + resendApiKey.trim())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("[RESEND] Email successfully delivered to {}! Response: {}", toEmail, response.body());
                return true;
            } else {
                log.warn("[RESEND] Resend API returned error {}: {}", response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            log.error("[RESEND] Failed to call Resend API: {}", e.getMessage());
            return false;
        }
    }

    private void sendViaSmtp(String toEmail, String username, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(smtpMailFrom, "ProgressGrid Security");
            helper.setTo(toEmail);
            helper.setSubject("ProgressGrid - Password Reset Verification Code: " + otp);
            helper.setText(buildEmailHtml(username, otp), true);

            mailSender.send(message);
            log.info("[SMTP] Email successfully sent to {}", toEmail);
        } catch (Exception e) {
            log.warn("[SMTP] Could not send email via SMTP ({}). Falling back to console OTP.", e.getMessage());
        }
    }

    private String buildEmailHtml(String username, String otp) {
        return "<div style='font-family: Arial, sans-serif; max-width: 520px; margin: 0 auto; padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px; background-color: #ffffff;'>"
                + "<div style='text-align: center; margin-bottom: 20px;'>"
                + "<h2 style='color: #4f46e5; margin: 0;'>ProgressGrid</h2>"
                + "<p style='color: #6b7280; font-size: 14px; margin-top: 4px;'>Password Reset Request</p>"
                + "</div>"
                + "<p style='color: #374151; font-size: 15px;'>Hello <strong>" + (username != null ? username : "User") + "</strong>,</p>"
                + "<p style='color: #4b5563; font-size: 14px; line-height: 1.5;'>We received a request to reset the password for your ProgressGrid account. Use the verification code below to verify your identity:</p>"
                + "<div style='text-align: center; margin: 28px 0;'>"
                + "<span style='display: inline-block; font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #111827; background-color: #f3f4f6; padding: 14px 28px; border-radius: 8px; border: 1px dashed #cbd5e1;'>" + otp + "</span>"
                + "</div>"
                + "<p style='color: #6b7280; font-size: 13px;'>This code is valid for <strong>10 minutes</strong>. If you did not make this request, you can safely ignore this email.</p>"
                + "<hr style='border: none; border-top: 1px solid #f3f4f6; margin: 24px 0;'/>"
                + "<p style='color: #9ca3af; font-size: 12px; text-align: center;'>ProgressGrid Habit & Workflow Tracker</p>"
                + "</div>";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String toJsonString(String s) {
        if (s == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
