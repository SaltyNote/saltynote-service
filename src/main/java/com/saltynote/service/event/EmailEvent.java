package com.saltynote.service.event;

import org.springframework.context.ApplicationEvent;

import com.saltynote.service.domain.EmailPayload;
import com.saltynote.service.domain.VaultType;
import com.saltynote.service.entity.SiteUser;
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
      public Type loadLinkInfo(String linkInfo) {
        this.getPayload().setLink(BaseUtils.getConfirmationUrl(linkInfo));
        return this;
      }

      @Override
      public VaultType getVaultType() {
        return VaultType.NEW_ACCOUNT;
      }
    },
    PASSWORD_FORGET(
        "Password Reset from SaltyNote!",
        new EmailPayload()
            .setLink("")
            .setLinkText("Reset Your Password")
            .setMessage("Below is the link for you to reset your password.")) {
      @Override
      public Type loadLinkInfo(String secret) {
        this.getPayload().setLink(BaseUtils.getPasswordResetUrl(secret));
        return this;
      }

      @Override
      public VaultType getVaultType() {
        return VaultType.PASSWORD;
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

    public abstract Type loadLinkInfo(String linkInfo);

    public abstract VaultType getVaultType();
  }

  private final SiteUser user;
  private final Type type;

  public EmailEvent(Object source, SiteUser user, Type type) {
    super(source);
    this.user = user;
    this.type = type;
  }
}
