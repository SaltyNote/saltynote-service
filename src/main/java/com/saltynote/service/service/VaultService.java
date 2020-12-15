package com.saltynote.service.service;

import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Service;

import com.devskiller.friendly_id.FriendlyId;
import com.saltynote.service.domain.VaultType;
import com.saltynote.service.entity.Vault;
import com.saltynote.service.repository.VaultRepository;

@Service
public class VaultService {
  private final VaultRepository vaultRepository;

  public VaultService(VaultRepository vaultRepository) {
    this.vaultRepository = vaultRepository;
  }

  public Vault create(@NotNull String userId, VaultType type) {
    return vaultRepository.save(
        new Vault()
            .setUserId(userId)
            .setType(type.getValue())
            .setSecret(FriendlyId.createFriendlyId()));
  }
}
