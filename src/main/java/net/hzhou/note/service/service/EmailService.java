package net.hzhou.note.service.service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
  private JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String emailSender;

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void send(String toEmail, String subject, String message) {
    SimpleMailMessage mailMessage = new SimpleMailMessage();
    mailMessage.setTo(toEmail);
    mailMessage.setSubject(subject);
    mailMessage.setText(message);
    mailMessage.setFrom(emailSender);
    mailSender.send(mailMessage);
  }

  public void send(@NotNull SimpleMailMessage mailMessage) {
    mailSender.send(mailMessage);
  }

  public void sendAsHtml(String toEmail, String subject, String content) throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    helper.setTo(toEmail);
    helper.setSubject(subject);
    helper.setFrom(emailSender);
    // use the true flag to indicate the text included is HTML
    helper.setText(content, true);
    mailSender.send(message);
  }
}
