package com.bibliotheque.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Async
    public void sendHtmlMessage(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);
            String htmlBody = templateEngine.process(templateName, context);

            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            emailSender.send(message);
            log.info("Email sent to {} with subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", to, e);
        }
    }

    public void sendReservationConfirmation(String to, String userName, String resourceTitle, String reservationNumber, int position) {
        sendHtmlMessage(to, "Confirmation de réservation", "emails/reservation-confirmation",
                Map.of("userName", userName,
                       "resourceTitle", resourceTitle,
                       "reservationNumber", reservationNumber,
                       "position", position));
    }

    public void sendReservationAvailable(String to, String userName, String resourceTitle, String expiryDate) {
        sendHtmlMessage(to, "Ressource disponible !", "emails/reservation-available",
                Map.of("userName", userName,
                       "resourceTitle", resourceTitle,
                       "expiryDate", expiryDate));
    }

    public void sendDueDateReminder(String to, String userName, String resourceTitle, String dueDate) {
        sendHtmlMessage(to, "Rappel : Date de retour proche", "emails/loan-reminder",
                Map.of("userName", userName,
                       "resourceTitle", resourceTitle,
                       "dueDate", dueDate));
    }

    public void sendOverdueAlert(String to, String userName, String resourceTitle, String dueDate, double fee) {
        sendHtmlMessage(to, "ALERTE : Retard de retour", "emails/loan-overdue",
                Map.of("userName", userName,
                       "resourceTitle", resourceTitle,
                       "dueDate", dueDate,
                       "fee", fee));
    }

    public void sendReturnConfirmation(String to, String userName, String resourceTitle) {
        sendHtmlMessage(to, "Confirmation de retour", "emails/loan-return",
                Map.of("userName", userName,
                       "resourceTitle", resourceTitle));
    }

    public void sendAccountCreationEmail(String to, String userName, String loginName, String password, String token) {
        String verificationLink = baseUrl + "/verify-email?token=" + token;
        sendHtmlMessage(to, "Bienvenue - Création de compte", "emails/account-creation",
                Map.of("userName", userName,
                       "loginName", loginName,
                       "password", password,
                       "verificationLink", verificationLink));
    }
}
