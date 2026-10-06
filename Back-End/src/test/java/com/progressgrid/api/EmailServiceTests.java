package com.progressgrid.api;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.progressgrid.api.service.EmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/** With no email provider, a reset code only reaches the log when that was switched on on purpose. */
class EmailServiceTests {

    private static final String CODE = "482913";

    private final Logger logger = (Logger) LoggerFactory.getLogger(EmailService.class);
    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();
    private final EmailService emailService = new EmailService();

    @BeforeEach
    void captureLogs() {
        logs.start();
        logger.addAppender(logs);
        // No Resend key and no SMTP account: nothing can deliver the email.
        ReflectionTestUtils.setField(emailService, "resendApiKey", "");
        ReflectionTestUtils.setField(emailService, "smtpMailFrom", "");
    }

    @AfterEach
    void stopCapturing() {
        logger.detachAppender(logs);
    }

    @Test
    void codesAreNotLoggedByDefault() {
        emailService.sendOtpEmail("someone@example.com", "someone", CODE);

        assertThat(logs.list).isNotEmpty();
        assertThat(logs.list).noneMatch(event -> event.getFormattedMessage().contains(CODE));
    }

    @Test
    void codesAreLoggedWhenExplicitlyEnabled() {
        ReflectionTestUtils.setField(emailService, "logCodes", true);
        emailService.sendOtpEmail("someone@example.com", "someone", CODE);

        assertThat(logs.list).anyMatch(event -> event.getFormattedMessage().contains(CODE));
    }
}
