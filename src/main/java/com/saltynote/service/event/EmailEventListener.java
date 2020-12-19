package com.saltynote.service.event;

import java.io.IOException;

import javax.mail.MessagingException;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

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

  public EmailEventListener(EmailService emailService, VaultService vaultService) {
    this.emailService = emailService;
    this.vaultService = vaultService;
  }

  @EventListener
  public void handleEvent(EmailEvent event)
      throws MessagingException, IOException, TemplateException {
    log.info(event.getType() + " - " + event.getUser());
    Vault vault = vaultService.create(event.getUser().getId(), VaultType.NEW_ACCOUNT);
    emailService.sendAsHtml(
        event.getUser().getEmail(),
        event.getType().getSubject(),
        event
            .getType()
            .loadUser(event.getUser())
            .loadLinkInfo(vaultService.encode(vault))
            .getPayload());
  }
}
