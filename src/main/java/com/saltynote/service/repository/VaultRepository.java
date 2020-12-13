package com.saltynote.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.saltynote.service.entity.Vault;

public interface VaultRepository
    extends JpaRepository<Vault, Integer>, JpaSpecificationExecutor<Vault> {}
