package net.hzhou.demo.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.hzhou.demo.jwt.entity.RefreshToken;

public interface RefreshTokenRepository
    extends JpaRepository<RefreshToken, Integer>, JpaSpecificationExecutor<RefreshToken> {}
