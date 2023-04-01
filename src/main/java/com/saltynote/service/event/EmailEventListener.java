package com.saltynote.service.event;

import com.saltynote.service.domain.EmailPayload;
import com.saltynote.service.entity.Vault;
import com.saltynote.service.service.EmailService;
import com.saltynote.service.service.VaultService;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
@Slf4j
public class EmailEventListener {

    private final EmailService emailService;
    private final VaultService vaultService;

    @Value("${spring.mail.username}")
    private String mailUser;

    public EmailEventListener(EmailService emailService, VaultService vaultService) {
        this.emailService = emailService;
        this.vaultService = vaultService;
    }

    @EventListener
    public void handleEvent(EmailEvent event)
            throws MessagingException, IOException, TemplateException {
        log.info("Event is received for {}", event.getType());
        Vault vault =
                event.getType() == EmailEvent.Type.NEW_USER
                        ? vaultService.createForEmail(
                        event.getUser().getEmail(), event.getType().getVaultType())
                        : vaultService.create(event.getUser().getId(), event.getType().getVaultType());
        EmailPayload payload =
                event
                        .getType()
                        .loadUser(event.getUser())
                        .loadVault(vault, vaultService.encode(vault))
                        .getPayload();

        if (StringUtils.hasText(mailUser)) {
            emailService.sendAsHtml(event.getUser().getEmail(), event.getType().getSubject(), payload);
        } else {
            log.info("============================================");
            log.info("Email Payload Info = {}", payload);
            log.info("============================================");
        }
    }
}
