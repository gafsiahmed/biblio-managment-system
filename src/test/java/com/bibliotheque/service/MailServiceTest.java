package com.bibliotheque.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MailServiceTest {

    @Mock
    private JavaMailSender emailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    @Test
    void testSendReservationAvailable() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@biblio.com");
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Body</html>");

        emailService.sendReservationAvailable("test@test.com", "User", "Book Title", "2023-12-31");

        verify(emailSender).send(mimeMessage);
        verify(templateEngine).process(eq("emails/reservation-available"), any(Context.class));
    }

    @Test
    void testSendReturnConfirmation() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@biblio.com");
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Body</html>");

        emailService.sendReturnConfirmation("test@test.com", "User", "Book Title");

        verify(emailSender).send(mimeMessage);
        verify(templateEngine).process(eq("emails/loan-return"), any(Context.class));
    }
}
