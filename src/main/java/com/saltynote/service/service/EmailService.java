package com.saltynote.service.service;

import com.saltynote.service.domain.EmailPayload;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.util.Collections;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String emailSender;

    private final Configuration freemarkerConfig;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        this.freemarkerConfig = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        this.freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/templates/");
    }

    public void send(String receiver, String subject, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(receiver);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        mailMessage.setFrom(emailSender);
        mailSender.send(mailMessage);
    }

    public void sendAsHtml(String receiver, String subject, @NotNull EmailPayload emailPayload)
            throws MessagingException, IOException, TemplateException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(receiver);
        helper.setSubject(subject);
        helper.setFrom(emailSender);
        // Use the true flag to indicate the text included is HTML
        Template t = freemarkerConfig.getTemplate("email/general.ftlh");
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(t,
                Collections.singletonMap("payload", emailPayload));
        helper.setText(content, true);
        mailSender.send(message);
    }

}
