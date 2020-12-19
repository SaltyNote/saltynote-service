package com.saltynote.service.service;

import java.io.IOException;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import com.devskiller.friendly_id.FriendlyId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saltynote.service.component.JwtInstance;
import com.saltynote.service.domain.IdentifiableUser;
import com.saltynote.service.domain.VaultEntity;
import com.saltynote.service.domain.VaultType;
import com.saltynote.service.entity.Vault;
import com.saltynote.service.repository.VaultRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VaultService implements RepositoryService<VaultRepository> {
  private final VaultRepository vaultRepository;
  private final ObjectMapper objectMapper;
  private final JwtInstance jwtInstance;

  public VaultService(
      VaultRepository vaultRepository, ObjectMapper objectMapper, JwtInstance jwtInstance) {
    this.vaultRepository = vaultRepository;
    this.objectMapper = objectMapper;
    this.jwtInstance = jwtInstance;
  }

  public Vault create(@NotNull String userId, VaultType type) {
    return create(userId, type, FriendlyId.createFriendlyId());
  }

  public Vault create(@NotNull String userId, VaultType type, String secret) {
    return vaultRepository.save(
        new Vault().setUserId(userId).setType(type.getValue()).setSecret(secret));
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

  public String createRefreshToken(IdentifiableUser user) {
    String refreshToken = jwtInstance.createRefreshToken(user);
    Vault v = create(user.getId(), VaultType.REFRESH_TOKEN, refreshToken);
    return v.getSecret();
  }

  @Override
  public VaultRepository getRepository() {
    return vaultRepository;
  }

  public Optional<Vault> findByUserIdAndTypeAndValue(String userId, VaultType type, String secret) {
    return vaultRepository.findByUserIdAndTypeAndSecret(userId, type.getValue(), secret);
  }

  public void cleanRefreshTokenByUserId(String userId) {
    vaultRepository.deleteByUserIdAndType(userId, VaultType.REFRESH_TOKEN.getValue());
  }
}
