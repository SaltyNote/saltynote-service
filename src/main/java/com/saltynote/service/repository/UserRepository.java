package com.saltynote.service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.saltynote.service.entity.SiteUser;

public interface UserRepository
    extends JpaRepository<SiteUser, String>, JpaSpecificationExecutor<SiteUser> {

  SiteUser findByUsername(String username);

  Optional<SiteUser> findByEmail(String email);
}
