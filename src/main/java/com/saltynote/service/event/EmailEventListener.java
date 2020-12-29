package com.saltynote.service.event;

import java.io.IOException;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.saltynote.service.domain.EmailPayload;
import com.saltynote.service.domain.VaultType;
import com.saltynote.service.entity.Vault;
import com.saltynote.service.service.EmailService;
import com.saltynote.service.service.VaultService;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

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
    Vault vault = vaultService.create(event.getUser().getId(), VaultType.NEW_ACCOUNT);
    EmailPayload payload =
        event
            .getType()
            .loadUser(event.getUser())
            .loadLinkInfo(vaultService.encode(vault))
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
