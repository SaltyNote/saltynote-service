package com.saltynote.service.repository;

import com.saltynote.service.entity.Vault;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface VaultRepository extends MongoRepository<Vault, String> {

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
