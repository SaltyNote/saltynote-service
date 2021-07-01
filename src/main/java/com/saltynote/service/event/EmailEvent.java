package com.saltynote.service.event;

import org.springframework.context.ApplicationEvent;

import com.saltynote.service.domain.EmailPayload;
import com.saltynote.service.domain.VaultType;
import com.saltynote.service.entity.SiteUser;
import com.saltynote.service.entity.Vault;
import com.saltynote.service.utils.BaseUtils;
import lombok.Getter;

@Getter
public class EmailEvent extends ApplicationEvent {

    public enum Type {
        NEW_USER(
                "Signup Code to SaltyNote!",
                new EmailPayload().setMessage("Below is the code you will use for signup.")) {
            @Override
            public Type loadVault(Vault vault, String encodedVault) {
                this.getPayload().setLinkText(vault.getSecret());
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
            public Type loadVault(Vault vault, String encodedVault) {
                this.getPayload().setLink(BaseUtils.getPasswordResetUrl(encodedVault));
                return this;
            }

            @Override
            public VaultType getVaultType() {
                return VaultType.PASSWORD;
            }
        };

        @Getter
        private final String subject;
        @Getter
        private final EmailPayload payload;

        Type(String subject, EmailPayload payload) {
            this.subject = subject;
            this.payload = payload;
        }

        public Type loadUser(SiteUser user) {
            this.payload.setUsername(user.getUsername());
            return this;
        }

        public abstract Type loadVault(Vault vault, String encodedVault);

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
