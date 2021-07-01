package com.saltynote.service.repository;

import java.util.List;
import java.util.Optional;

import javax.validation.constraints.NotBlank;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.saltynote.service.entity.Vault;

public interface VaultRepository
        extends JpaRepository<Vault, String>, JpaSpecificationExecutor<Vault> {
    Optional<Vault> findBySecret(String secret);

    void deleteByUserId(String userId);

    void deleteByUserIdAndType(String userId, String type);

    Optional<Vault> findByUserIdAndTypeAndSecret(String userId, String type, String secret);

    List<Vault> findByUserIdAndType(String userId, String type);

    Optional<Vault> findFirstByUserIdAndTypeOrderByCreatedTimeDesc(String userId, String type);

    List<Vault> findByUserId(String userId);

    List<Vault> findByEmail(@NotBlank String email);

    Optional<Vault> findByEmailAndSecretAndType(String email, String token, String value);
}
