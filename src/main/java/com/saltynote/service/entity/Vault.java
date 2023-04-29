package com.saltynote.service.entity;

import com.saltynote.service.domain.Identifiable;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Document
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Accessors(chain = true)
public class Vault implements Serializable, Identifiable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String userId;

    private String secret;

    private String type;

    @Email
    private String email;

    private Long createdTime = System.currentTimeMillis();

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Vault vault = (Vault) o;
        return Objects.equals(id, vault.id) && Objects.equals(userId, vault.userId)
                && Objects.equals(secret, vault.secret) && Objects.equals(type, vault.type)
                && Objects.equals(email, vault.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, secret, type, email);
    }

}
