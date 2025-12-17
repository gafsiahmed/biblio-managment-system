package com.bibliotheque.service;

import com.bibliotheque.model.User;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

  private final JavaMailSender mailSender;

  @Value("${spring.application.name:bibliotheque-management}")
  private String appName;

  @Value("${app.base-url:http://localhost:8080}")
  private String baseUrl;

  public MailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
    this.mailSender = mailSenderProvider.getIfAvailable();
  }

  public void sendVerificationEmail(User user, String token) {
    String subject = "Vérification de votre email";
    String verifyUrl = baseUrl + "/verify-email?token=" + token;
    String text = "Bonjour " + (user.getFirstName() != null ? user.getFirstName() : user.getUsername()) +
      ",\n\nVeuillez vérifier votre email: " + verifyUrl + "\n\n" + appName;
    if (mailSender != null) {
      try {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
      } catch (Exception e) {
        System.out.println("Verification link: " + verifyUrl);
      }
    } else {
      System.out.println("Verification link: " + verifyUrl);
    }
  }
}
