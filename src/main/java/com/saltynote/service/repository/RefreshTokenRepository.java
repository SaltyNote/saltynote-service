package com.saltynote.service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.saltynote.service.entity.RefreshToken;

public interface RefreshTokenRepository
    extends JpaRepository<RefreshToken, String>, JpaSpecificationExecutor<RefreshToken> {
  Optional<RefreshToken> findByUserIdAndRefreshToken(String userId, String refreshToken);

  void deleteAllByUserId(String userId);
}
