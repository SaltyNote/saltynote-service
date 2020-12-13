package com.saltynote.service.event;

import org.springframework.context.ApplicationEvent;

import com.saltynote.service.domain.EmailPayload;
import com.saltynote.service.entity.SiteUser;
import com.saltynote.service.entity.Vault;
import com.saltynote.service.utils.BaseUtils;
import lombok.Getter;

@Getter
public class EmailEvent extends ApplicationEvent {

  public enum Type {
    NEW_USER(
        "Welcome to SaltyNote!",
        new EmailPayload()
            .setLink("")
            .setLinkText("Click to Verify Your Email")
            .setMessage("Below is the link for your email verification.")) {
      @Override
      public Type loadVault(Vault vault) {

        this.getPayload().setLink(BaseUtils.getConfirmationUrl(vault.getSecret()));
        return this;
      }
    };

    @Getter private final String subject;
    @Getter private final EmailPayload payload;

    Type(String subject, EmailPayload payload) {
      this.subject = subject;
      this.payload = payload;
    }

    public Type loadUser(SiteUser user) {
      this.payload.setUsername(user.getUsername());
      return this;
    }

    public abstract Type loadVault(Vault vault);
  }

  private SiteUser user;
  private Type type;

  public EmailEvent(Object source, SiteUser user, Type type) {
    super(source);
    this.user = user;
    this.type = type;
  }
}
