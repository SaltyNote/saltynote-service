package com.saltynote.service.repository;

import com.saltynote.service.entity.Vault;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface VaultRepository extends JpaRepository<Vault, Long>, JpaSpecificationExecutor<Vault> {

    Optional<Vault> findBySecret(String secret);

    void deleteByUserId(Long userId);

    void deleteByUserIdAndType(Long userId, String type);

    Optional<Vault> findByUserIdAndTypeAndSecret(Long userId, String type, String secret);

    List<Vault> findByUserIdAndType(Long userId, String type);

    Optional<Vault> findFirstByUserIdAndTypeOrderByCreatedTimeDesc(Long userId, String type);

    List<Vault> findByUserId(Long userId);

    List<Vault> findByEmail(@NotBlank String email);

    Optional<Vault> findByEmailAndSecretAndType(String email, String token, String value);

}
