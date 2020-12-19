package com.saltynote.service.service;

import java.io.IOException;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import com.devskiller.friendly_id.FriendlyId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saltynote.service.domain.VaultEntity;
import com.saltynote.service.domain.VaultType;
import com.saltynote.service.entity.Vault;
import com.saltynote.service.repository.VaultRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VaultService {
  private final VaultRepository vaultRepository;
  private final ObjectMapper objectMapper;

  public VaultService(VaultRepository vaultRepository, ObjectMapper objectMapper) {
    this.vaultRepository = vaultRepository;
    this.objectMapper = objectMapper;
  }

  public Vault create(@NotNull String userId, VaultType type) {
    return vaultRepository.save(
        new Vault()
            .setUserId(userId)
            .setType(type.getValue())
            .setSecret(FriendlyId.createFriendlyId()));
  }

  public String encode(@NotNull VaultEntity entity) throws JsonProcessingException {
    return Base64Utils.encodeToString(objectMapper.writeValueAsBytes(entity));
  }

  public String encode(@NotNull Vault vault) throws JsonProcessingException {
    return encode(VaultEntity.from(vault));
  }

  public Optional<VaultEntity> decode(@NotNull String encodedValue) {
    try {
      return Optional.of(
          objectMapper.readValue(Base64Utils.decodeFromString(encodedValue), VaultEntity.class));
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      return Optional.empty();
    }
  }
}
