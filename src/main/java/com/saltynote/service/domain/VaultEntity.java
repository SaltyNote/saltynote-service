package com.saltynote.service.domain;

import com.saltynote.service.entity.Vault;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class VaultEntity {

    private String userId;

    private String secret;

    public static VaultEntity from(Vault vault) {
        return new VaultEntity().setSecret(vault.getSecret()).setUserId(vault.getUserId());
    }

}
