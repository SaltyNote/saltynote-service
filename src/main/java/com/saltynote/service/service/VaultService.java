package com.saltynote.service.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.devskiller.friendly_id.FriendlyId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saltynote.service.domain.IdentifiableUser;
import com.saltynote.service.domain.VaultEntity;
import com.saltynote.service.domain.VaultType;
import com.saltynote.service.entity.Vault;
import com.saltynote.service.repository.VaultRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class VaultService implements RepositoryService<String, Vault> {

    private final VaultRepository repository;

    private final ObjectMapper objectMapper;

    private final JwtService jwtService;

    // TTL in milliseconds
    @Value("${jwt.refresh_token.ttl}")
    private long refreshTokenTTL;

    public Vault create(@NotNull String userId, VaultType type) {
        return create(userId, type, FriendlyId.createFriendlyId());
    }

    public Vault createForEmail(@NotNull String email, VaultType type) {
        return repository
            .save(new Vault().setEmail(email).setType(type.getValue()).setSecret(FriendlyId.createFriendlyId()));
    }

    public Vault create(@NotNull String userId, VaultType type, String secret) {
        return repository.save(new Vault().setUserId(userId).setType(type.getValue()).setSecret(secret));
    }

    public String encode(@NotNull VaultEntity entity) throws JsonProcessingException {
        return Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(entity));
    }

    public String encode(@NotNull Vault vault) throws JsonProcessingException {
        return encode(VaultEntity.from(vault));
    }

    public Optional<VaultEntity> decode(@NotNull String encodedValue) {
        try {
            return Optional.of(objectMapper.readValue(Base64.getDecoder().decode(encodedValue), VaultEntity.class));
        }
        catch (IOException e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    public String createRefreshToken(IdentifiableUser user) {
        String refreshToken = jwtService.createRefreshToken(user);
        Vault v = create(user.getId(), VaultType.REFRESH_TOKEN, refreshToken);
        return v.getSecret();
    }

    /**
     * This is try to find the latest refresh token for given user id. If the refresh
     * token ages below 20%, we will return this refresh token. Otherwise, a new refresh
     * token will be generated and returned.
     * @param user the target user
     * @return the refresh token value
     */
    public String fetchOrCreateRefreshToken(IdentifiableUser user) {
        Optional<Vault> vaultOp = repository.findFirstByUserIdAndTypeOrderByCreatedTimeDesc(user.getId(),
                VaultType.REFRESH_TOKEN.getValue());
        // If refresh token is young enough, then just return it.
        if (vaultOp.isPresent() && isRefreshTokenReusable(vaultOp.get().getSecret())) {
            return vaultOp.get().getSecret();
        }
        // Refresh token is not a kid anymore or no existing refresh token found, a new
        // one should be
        // created.
        return createRefreshToken(user);
    }

    /**
     * Validate given token and return the vault.
     * @param token token
     * @return vault for the token
     */
    public Optional<Vault> findByToken(String token) {
        Optional<VaultEntity> veo = decode(token);
        if (veo.isEmpty()) {
            return Optional.empty();
        }
        VaultEntity ve = veo.get();
        Optional<Vault> vault = repository.findBySecret(ve.getSecret());
        if (vault.isPresent() && !vault.get().getUserId().equals(ve.getUserId())) {
            log.error("User id are not match from decoded token {} and database {}", ve.getUserId(),
                    vault.get().getUserId());
            return Optional.empty();
        }

        return vault;
    }

    @Override
    public Vault create(Vault entity) {
        if (hasValidId(entity)) {
            log.warn("Note id must be empty: {}", entity);
            entity.setId(null);
        }
        return repository.save(entity);
    }

    @Override
    public Vault update(Vault entity) {
        checkIdExists(entity);
        return repository.save(entity);
    }

    @Override
    public Optional<Vault> getById(String id) {
        return repository.findById(id);
    }

    @Override
    public void delete(Vault entity) {
        repository.deleteById(entity.getId());
    }

    public void deleteById(String id) {
        repository.deleteById(id);
    }

    public Optional<Vault> findByUserIdAndTypeAndValue(String userId, VaultType type, String secret) {
        return repository.findByUserIdAndTypeAndSecret(userId, type.getValue(), secret);
    }

    public void cleanRefreshTokenByUserId(String userId) {
        repository.deleteByUserIdAndType(userId, VaultType.REFRESH_TOKEN.getValue());
    }

    private boolean isRefreshTokenReusable(String refreshToken) {
        try {
            DecodedJWT decodedJWT = jwtService.verifyRefreshToken(refreshToken);
            return decodedJWT.getExpiresAt().after(new Date(System.currentTimeMillis() + refreshTokenTTL * 8 / 10));
        }
        catch (JWTVerificationException e) {
            return false;
        }
    }

    public Optional<Vault> getByEmailAndSecretAndType(String email, String token, VaultType type) {
        return repository.findByEmailAndSecretAndType(email, token, type.getValue());
    }

    public List<Vault> getByEmail(String email) {
        return repository.findByEmail(email);
    }

    public List<Vault> getByUserIdAndType(String userId, VaultType vaultType) {
        return repository.findByUserIdAndType(userId, vaultType.getValue());
    }

    public List<Vault> getByUserId(String userId) {
        return repository.findByUserId(userId);
    }

}
